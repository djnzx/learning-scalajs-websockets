package org.djnzx.http

import cats.*
import cats.effect.Clock
import cats.implicits.*
import java.time.LocalDateTime
import java.time.ZoneId
import org.http4s.*
import org.http4s.dsl.*

class HealthRoutes[F[_]: {Monad, Clock}] extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "ping" =>
      Clock[F].realTimeInstant
        .map(t => LocalDateTime.ofInstant(t, ZoneId.systemDefault()))
        .flatMap(t => Ok(s"Ok @ $t"))
  }

}

object HealthRoutes {
  def apply[F[_]: {Monad, Clock}] = new HealthRoutes[F]
}
