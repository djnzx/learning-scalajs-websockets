package org.djnzx

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import org.djnzx.config.AppConfig
import org.djnzx.config.syntax.loadF
import org.djnzx.content.ContentService
import org.djnzx.http.Api
import org.djnzx.http.CorsSettings
import org.djnzx.ws.WsHandler
import org.djnzx.ws.WsRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource

object BackendLauncher extends IOApp.Simple {

  given logger: Logger[IO] = Slf4jLogger.getLogger

  val app: Resource[IO, Server] = for {
    config    <- Resource.eval(ConfigSource.default.loadF[IO, AppConfig])
    content   <- ContentService[IO]()
    wsHandler <- WsHandler[IO](content)
    wsRoutes  <- WsRoutes[IO](wsHandler)
    api       <- Api[IO](content)

    server <- EmberServerBuilder
                .default[IO]
                .withHost(config.ember.host)
                .withPort(config.ember.port)
                .withHttpApp(CorsSettings.cors(api.endpoints.orNotFound))
                .withHttpWebSocketApp(wsb => CorsSettings.cors(wsRoutes.routes(wsb).orNotFound))
                .build
  } yield server

  override def run: IO[Unit] =
    app.use(_ => IO.never)

}
