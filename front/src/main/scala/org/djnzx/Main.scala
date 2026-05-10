package org.djnzx

import cats.effect.IO
import scala.scalajs.js.annotation.*
import tyrian.*
import tyrian.Html.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianIOApp[Msg, Model]:

  def router: Location => Msg =
    Routing.none(Msg.NoOp)

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Nil, Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.Insert =>
      (Counter.init :: model, Cmd.None)

    case Msg.Remove =>
      model match
        case Nil    => (Nil, Cmd.None)
        case _ :: t => (t, Cmd.None)

    case Msg.Modify(id, m) =>
      val updated = model.zipWithIndex.map { case (c, i) =>
        if i == id then Counter.update(m, c) else c
      }

      (updated, Cmd.None)

    case Msg.NoOp =>
      (model, Cmd.None)

  /** page view */
  def view(model: Model): Html[Msg] =
    val counters = model.zipWithIndex.map { case (c, i) =>
      Counter.view(c).map(msg => Msg.Modify(i, msg))
    }

    val elems =
      Header.view ::
        List(
          div(cls := "flex space-x-4")(
            button(
              onClick(Msg.Remove),
              cls := "px-2 bg-sky-200 rounded"
            )(
              text("remove")
            ),
            button(
              onClick(Msg.Insert),
              cls := "px-2 bg-sky-200 rounded"
            )(
              text("insert")
            )
          )
        ) ++ counters

    div(cls := "p-4")(
      div(cls := "space-y-4")(elems*)
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.None

type Model = List[Counter.Model]

enum Msg:
  case Insert
  case Remove
  case Modify(i: Int, msg: Counter.Msg)
  case NoOp

object Counter:

  opaque type Model = Int

  def init: Model = 0

  enum Msg:
    case Increment, Decrement

  /** counter view */
  def view(model: Model): Html[Msg] =
    div(cls := "flex space-x-4")(
      button(
        onClick(Msg.Decrement),
        cls := "px-2 bg-red-300 rounded"
      )(
        text("-")
      ),
      p(cls := "text-lg font-medium")(model.toString),
      button(
        onClick(Msg.Increment),
        cls := "px-2 bg-lime-300 rounded"
      )(
        text("+")
      )
    )

  def update(msg: Msg, model: Model): Model =
    msg match
      case Msg.Increment => model + 1
      case Msg.Decrement => model - 1
