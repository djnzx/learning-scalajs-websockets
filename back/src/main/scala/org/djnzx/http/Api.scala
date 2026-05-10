package org.djnzx.http

import cats.effect.Clock
import cats.effect.Resource
import cats.effect.kernel.Async
import cats.implicits.toSemigroupKOps
import org.djnzx.content.ContentRoutes
import org.djnzx.content.ContentService
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.typelevel.log4cats.Logger

class Api[F[_]: {Async, Clock, Logger}] private (contentR: ContentRoutes[F]) extends Http4sDsl[F] {

  private val healthR = HealthRoutes[F].routes

  val endpoints: HttpRoutes[F] = Router(
    "/api" -> (healthR <+> contentR.routes),
  )
}

object Api {
  def apply[F[_]: {Async, Clock, Logger}](content: ContentService[F]): Resource[F, Api[F]] =
    Resource.eval(ContentRoutes[F](content))
      .map(new Api[F](_))
}
