package org.djnzx.ws

import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Queue
import cats.implicits.*
import fs2.Pipe
import fs2.Stream
import org.djnzx
import org.djnzx.MsgServerToClient
import org.djnzx.Welcome
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
      _     <- queue.offer(Welcome)
    } yield ws

  // sending events from back to front (client)
  def send(queue: Queue[F, MsgServerToClient]): Stream[F, WebSocketFrame] =
    Stream.fromQueueUnterminated(queue)
      .map {
        case Welcome               => WebSocketFrame.Text("welcome")
        case djnzx.Response(value) => WebSocketFrame.Text(value)
      }

  // receiving events from front (client) to back
  def receive(queue: Queue[F, MsgServerToClient]): Pipe[F, WebSocketFrame, Unit] =
    _.flatMap {
      case WebSocketFrame.Text(s, _) =>
        s.trim.toIntOption match {

          case Some(n) =>
            content.emit
              .take(n)
              .evalMap(thing => queue.offer(djnzx.Response(thing.timestamp)))
              .drain ++ Stream.eval(queue.offer(djnzx.Response("done!")))

          case None =>
            Stream.eval(queue.offer(djnzx.Response(s"RE: $s")))

        }

      case frame => Stream.eval(logF(frame)(ln, fn)).drain
    }

}

object WsHandler {
  def apply[F[_]: Async](content: ContentService[F]): Resource[F, WsHandler[F]] =
    Resource.pure(new WsHandler[F](content))
}
