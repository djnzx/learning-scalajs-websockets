package org.djnzx.http

import org.http4s.Uri
import org.http4s.headers.Origin
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.CORSPolicy

object CorsSettings {

  val fronendUrl: Origin.Host = Origin.Host(
    Uri.Scheme.http,
    Uri.RegName("localhost"),
    Some(1234)
  )

  // TODO: https://github.com/http4s/http4s/security/advisories/GHSA-52cf-226f-rhr6
  val cors: CORSPolicy = CORS.policy      // --- default policy ---
    .withAllowOriginHost(Set(fronendUrl)) // AllowOrigin.All
    .withExposeHeadersAll                 // ExposeHeaders.None - required for login (passing headers to JS)
//    .withAllowCredentials(true)         // AllowCredentials.Deny
//    .withAllowMethodsAll                // GET, POST, PUT, PATCH, HEAD, DELETE
//    .withAllowHeadersAll                // AllowHeaders.Reflect
//    .withMaxAge(10.seconds)             // None

}
