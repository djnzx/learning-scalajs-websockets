package org.djnzx.content

import cats.effect.Async
import cats.effect.Fiber
import cats.effect.Ref
import cats.effect.implicits.genSpawnOps
import cats.implicits.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger

class ContentRoutes[F[_]: {Async, Logger}] private (
  content: ContentService[F],
  handle: Ref[F, Option[Fiber[F, Throwable, Unit]]]
) extends Http4sDsl[F] {

  private val log = Logger[F]

  private val contents =
    content.emit
      .evalTap(x => log.info(x.toString))
      .compile
      .drain

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root / "start" =>
      handle
        .modify {
          case s @ Some(_) => (s, Conflict("Stream already running"))
          case None        => (None, contents.start.flatMap(f => handle.set(Some(f))) >> Ok("Stream started"))
        }.flatten

    case POST -> Root / "stop" =>
      handle
        .modify {
          case Some(f) => (None, f.cancel >> Ok("Stream stopped"))
          case None    => (None, Ok("Stream is not running"))
        }.flatten
  }

}

object ContentRoutes {
  def apply[F[_]: {Async, Logger}](content: ContentService[F]): F[ContentRoutes[F]] =
    Ref.of[F, Option[Fiber[F, Throwable, Unit]]](None)
      .map(of => new ContentRoutes[F](content, of))
}
