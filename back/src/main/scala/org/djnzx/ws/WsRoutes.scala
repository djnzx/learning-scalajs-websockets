package org.djnzx.ws

import cats.effect.Async
import cats.effect.Resource
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder2

class WsRoutes[F[_]: Async] private (handler: WsHandler[F]) extends Http4sDsl[F] {

  def routes(wsb: WebSocketBuilder2[F]): HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "ws" => handler.make(wsb)
  }

}

object WsRoutes {
  def apply[F[_]: Async](handler: WsHandler[F]): Resource[F, WsRoutes[F]] =
    Resource.pure(new WsRoutes[F](handler))
}
