package org.djnzx

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import tyrian.*
import tyrian.Html.cls
import tyrian.Html.div
import tyrian.Html.img
import tyrian.Html.src

object Header {

  // with scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) }
  // JSImport.Default causes failure in runtime (in browser)
  // due to the wrong binding
  /*

    @JSImport("some-lib", JSImport.Default)
      =>
    import Foo from "some-lib"


    @JSImport("some-lib", JSImport.Namespace)
      =>
    import * as SomeLib from "some-lib"
    SomeLib.bar()

   */
  @js.native
  @JSImport("url:../../static/img/fiery-lava-128x128.png", JSImport.Namespace)
  val imageUrl: String = js.native

  def view: Html[Nothing] =
    div(cls := "p-1")(
      img(
        src := imageUrl
      )
    )

}
