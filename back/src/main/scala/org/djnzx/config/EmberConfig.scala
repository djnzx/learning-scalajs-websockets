package org.djnzx.config

import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import pureconfig.ConfigReader
import pureconfig.error.*

case class EmberConfig(host: Host, port: Port) derives ConfigReader

object EmberConfig {

  given hostReader: ConfigReader[Host] = ConfigReader[String]
    .emap(host => Host.fromString(host).toRight(CannotConvert(host, Host.getClass.toString, s"Invalid host string $host")))

  given portReader: ConfigReader[Port] = ConfigReader[Int]
    .emap(port => Port.fromInt(port).toRight(CannotConvert(port.toString, Port.getClass.toString, s"Invalid port string $port")))

}
