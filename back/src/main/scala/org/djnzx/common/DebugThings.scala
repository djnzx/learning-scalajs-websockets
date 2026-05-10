package org.djnzx.common

import cats.effect.kernel.Sync
import fs2.Stream

trait DebugThings[F[_]] {

  def logF[A](a: A)(line: sourcecode.Line, fileName: sourcecode.FileName)(using SF: Sync[F]): F[Unit] =
    SF.delay(pprint.log(a)(using line, fileName))

  def logS[A](a: A)(line: sourcecode.Line, fileName: sourcecode.FileName)(using SF: Sync[F]): Stream[F, Nothing] =
    Stream.eval(logF(a)(line, fileName)).drain

}
