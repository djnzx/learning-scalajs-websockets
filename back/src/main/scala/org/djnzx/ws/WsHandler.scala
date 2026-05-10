package org.djnzx.ws

import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Queue
import cats.implicits.*
import fs2.Pipe
import fs2.Stream
import org.djnzx
import org.djnzx.MsgServerToClient
import org.djnzx.MsgServerToClient.*
import org.djnzx.common.DebugThings
import org.djnzx.content.ContentService
import org.http4s.Response
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import sourcecode.FileName.generate as fn
import sourcecode.Line.generate as ln

class WsHandler[F[_]: Async] private (content: ContentService[F]) extends DebugThings[F] {

  def make(wsb: WebSocketBuilder2[F]): F[Response[F]] =
    for {
      queue <- Queue.unbounded[F, MsgServerToClient]
      ws    <- wsb
                 .withOnClose(logF(s"conn Closed")(ln, fn))
                 .build(
                   send(queue),
                   receive(queue)
                 )
      _     <- queue.offer(WsWelcome)
    } yield ws

  // sending events from back to front (client)
  def send(queue: Queue[F, MsgServerToClient]): Stream[F, WebSocketFrame] =
    Stream.fromQueueUnterminated(queue)
      .flatMap {
        case WsWelcome    => Stream(WebSocketFrame.Text("Welcome"))
        case WsMsg(value) => Stream(WebSocketFrame.Text(value))
        case WsBye        => Stream(WebSocketFrame.Text("Bye!"), WebSocketFrame.Close())
      }
      .takeWhile(!_.isInstanceOf[WebSocketFrame.Close], takeFailure = true)

  // receiving events from front (client) to back
  def receive(queue: Queue[F, MsgServerToClient]): Pipe[F, WebSocketFrame, Unit] =
    _.flatMap {
      case WebSocketFrame.Text(s, _) =>
        s.trim.toIntOption match {

          case Some(n) =>
            content.emit
              .take(n)
              .evalMap(thing => queue.offer(WsMsg(thing.timestamp)))
              .drain ++ Stream.eval(queue.offer(WsMsg("done!")))

          case None if s.trim == "bye" =>
            Stream.eval(queue.offer(WsBye))

          case None =>
            Stream.eval(queue.offer(WsMsg(s"RE: $s")))

        }

      case frame => Stream.eval(logF(frame)(ln, fn)).drain
    }

}

object WsHandler {
  def apply[F[_]: Async](content: ContentService[F]): Resource[F, WsHandler[F]] =
    Resource.pure(new WsHandler[F](content))
}
