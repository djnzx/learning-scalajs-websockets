package org.djnzx

import cats.effect.IO
import scala.scalajs.js.annotation.*
import tyrian.*
import tyrian.Html.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianIOApp[Msg, Model]:

  def router: Location => Msg = Routing.none(Msg.NoOp)

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model(Nil, None), Cmd.None)

  private def handleForm(model: Model): PartialFunction[Msg, (Model, Cmd[IO, Msg])] =
    case Msg.InputChange(s) => (model.withInput(s), Cmd.None)
    case Msg.SendMessage    =>
      model.ws match
        case Some(ws) => (model.withMessageOut(model.input).clearInput, ws.publish(model.input))
        case None     => (model.copy(pendingSend = true), WsHandler.connectCmd)

  private def handleCounter(model: Model): PartialFunction[Msg, (Model, Cmd[IO, Msg])] =
    case Msg.InsertCounter        => (model.withCounter, Cmd.None)
    case Msg.RemoveCounter        => (model.withoutCounter, Cmd.None)
    case Msg.ModifyCounter(id, m) => (model.modifyCounter(id, m), Cmd.None)
    case Msg.NoOp                 => (model, Cmd.None)

  def update(m: Model): Msg => (Model, Cmd[IO, Msg]) =
    WsHandler.handle(m) orElse handleForm(m) orElse handleCounter(m)

  def view(model: Model): Html[Msg] =

    // insert, remove counter
    val counterBtnV = div(cls := "flex space-x-4")(
      button(onClick(Msg.InsertCounter), cls := "px-2 bg-sky-200 rounded")("insert"),
      button(onClick(Msg.RemoveCounter), cls := "px-2 bg-sky-200 rounded")("remove"),
    )

    // ws connect, disconnect, checkbox
    val wsConnected = model.ws.isDefined
    def wsBtnCls(active: Boolean, color: String) = active match
      case true  => s"px-2 rounded $color"
      case false => "px-2 rounded bg-gray-200 text-gray-400 cursor-not-allowed"

    val connectStyle = wsBtnCls(!wsConnected, "bg-green-200")
    val disconnectStyle = wsBtnCls(wsConnected, "bg-red-200")

    val connectDisconnectV = div(cls := "flex space-x-4")(
      button(List(onClick(Msg.WsConnect), cls := connectStyle) ++ Option.when(model.ws.isDefined)(disabled))("ws connect"),
      button(List(onClick(Msg.WsDisconnect), cls := disconnectStyle) ++ Option.when(model.ws.isEmpty)(disabled))("ws disconnect"),
      div(cls := "flex items-center space-x-1 text-sm")(
        input(
          `type` := "checkbox",
          checked(model.disconnectOnDone),
          onClick(Msg.ToggleDisconnectOnDone),
          cls := "cursor-pointer"
        ),
        span(
          cls := "cursor-pointer",
          onClick(Msg.ToggleDisconnectOnDone)
        )("disconnect on 'done!'")
      )
    )

    // input, ws send
    val inputAndButtonV = div(cls := "flex space-x-2")(
      input(
        `type` := "text",
        value := model.input,
        onInput(s => Msg.InputChange(s)),
        onKeyUp {
          case k if k.key == "Enter" => Msg.SendMessage
          case _                     => Msg.NoOp
        },
        cls := "border rounded px-2"
      ),
      button(onClick(Msg.SendMessage), cls := "px-2 bg-amber-200 rounded")("ws send")
    )

    // messages
    val messagesV = div(cls := "space-y-1")(model.messages.map(s => p(cls := "font-mono text-sm")(s))*)

    // raw counters
    val countersV = model.counters.zipWithIndex
      .map { case (c, i) => Counter.view(c).map(cm => Msg.ModifyCounter(i, cm)) }

    val elems = Header.view :: List(counterBtnV, connectDisconnectV, inputAndButtonV, messagesV) ++ countersV

    div(cls := "p-4")(div(cls := "space-y-4")(elems*))

  def subscriptions(model: Model): Sub[IO, Msg] =
    model.ws.fold(Sub.None)(WsHandler.subscribeAndHandle)
