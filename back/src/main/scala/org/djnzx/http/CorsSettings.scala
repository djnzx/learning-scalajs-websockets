package org.djnzx.http

import java.net.Inet4Address
import java.net.NetworkInterface
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.CORSPolicy
import scala.jdk.CollectionConverters.*

object CorsSettings {

  private val localPrefixes =
    NetworkInterface.getNetworkInterfaces.asScala
      .flatMap(_.getInetAddresses.asScala)
      .collect { case a: Inet4Address => a }
      .filterNot(_.isLoopbackAddress)
      .map { n => pprint.log(n); n }
      .map(a => a.getHostAddress.split('.').take(3).mkString(".") + ".")
      .toSet

  // TODO: https://github.com/http4s/http4s/security/advisories/GHSA-52cf-226f-rhr6
  val cors: CORSPolicy = CORS.policy
    .withAllowOriginHost { h =>
      val host = h.host.renderString
      host == "localhost" || localPrefixes.exists(host.startsWith)
    }
    .withExposeHeadersAll // ExposeHeaders.None - required for login (passing headers to JS)
//    .withAllowCredentials(true)         // AllowCredentials.Deny
//    .withAllowMethodsAll                // GET, POST, PUT, PATCH, HEAD, DELETE
//    .withAllowHeadersAll                // AllowHeaders.Reflect
//    .withMaxAge(10.seconds)             // None

}
