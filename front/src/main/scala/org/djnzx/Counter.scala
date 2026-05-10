package org.djnzx

import tyrian.Html
import tyrian.Html.*

object Counter:

  opaque type Model = Int

  def init: Model = 0

  enum Msg:
    case Increment, Decrement

  def view(model: Model): Html[Msg] =
    div(cls := "flex space-x-4")(
      button(onClick(Msg.Decrement), cls := "px-2 bg-red-300 rounded")("-"),
      p(cls := "text-lg font-medium")(model.toString),
      button(onClick(Msg.Increment), cls := "px-2 bg-lime-300 rounded")("+")
    )

  def update(msg: Msg, model: Model): Model =
    msg match
      case Msg.Increment => model + 1
      case Msg.Decrement => model - 1
