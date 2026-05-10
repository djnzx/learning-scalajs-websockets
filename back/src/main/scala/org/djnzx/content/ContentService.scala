package org.djnzx.content

import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Random
import cats.syntax.all.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import org.djnzx.Thing
import scala.concurrent.duration.*

class ContentService[F[_]: Async] private () {

  private def elem(rng: Random[F]): F[Thing] = for {
    ms <- rng.betweenInt(1000, 3001)
    _  <- Async[F].sleep(ms.millis)
    d  <- Async[F].realTime
    now = LocalDateTime.ofInstant(Instant.ofEpochMilli(d.toMillis), ZoneId.systemDefault()).toLocalTime
  } yield Thing(now.toString)

  def emit: fs2.Stream[F, Thing] =
    fs2.Stream.eval(Random.scalaUtilRandom[F])
      .map(elem)
      .flatMap(x => fs2.Stream.eval(x).repeat)

}

object ContentService {
  def apply[F[_]: Async](): Resource[F, ContentService[F]] =
    Resource.pure(new ContentService[F]())
}
