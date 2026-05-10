package org.djnzx.config

import cats.*
import cats.implicits.*
import pureconfig.ConfigReader
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderException
import scala.reflect.ClassTag

object syntax {

  extension (source: ConfigSource) {

    def loadF[F[_]: MonadThrow, A: {ConfigReader, ClassTag}]: F[A] =
      source.load[A].pure[F].flatMap {
        case Left(errors)  => ConfigReaderException(errors).raiseError
        case Right(config) => config.pure
      }

  }

}
