package org.djnzx

import cats.effect.IO
import scala.scalajs.js.annotation.*
import tyrian.*
import tyrian.Html.*
import tyrian.websocket.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianIOApp[Msg, Model]:

  private val wsUrl = "ws://localhost:8081/ws"

  def router: Location => Msg = Routing.none(Msg.NoOp)

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model(Nil, None), Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =

    case Msg.Connect =>
      val cmd = WebSocket.connect[IO, Msg](wsUrl) {
        case WebSocketConnect.Socket(ws) => Msg.WsOpen(ws)
        case WebSocketConnect.Error(_)   => Msg.NoOp
      }
      (model, cmd)

    case Msg.WsOpen(ws) =>
      (model.copy(ws = Some(ws)), Cmd.None)

    case Msg.WsReceive(s) =>
      (model, Cmd.None)

    case Msg.Insert =>
      (model.copy(counters = Counter.init :: model.counters), Cmd.None)

    case Msg.Remove =>
      model.counters match
        case Nil    => (model, Cmd.None)
        case _ :: t => (model.copy(counters = t), Cmd.None)

    case Msg.Modify(id, m) =>
      val updated = model.counters.zipWithIndex.map { case (c, i) =>
        if i == id then Counter.update(m, c) else c
      }
      (model.copy(counters = updated), Cmd.None)

    case Msg.NoOp => (model, Cmd.None)

  def view(model: Model): Html[Msg] =
    val counters = model.counters.zipWithIndex.map { case (c, i) =>
      Counter.view(c).map(msg => Msg.Modify(i, msg))
    }

    val elems =
      Header.view ::
        List(
          div(cls := "flex space-x-4")(
            button(onClick(Msg.Remove), cls := "px-2 bg-sky-200 rounded")(text("remove")),
            button(onClick(Msg.Insert), cls := "px-2 bg-sky-200 rounded")(text("insert")),
            button(onClick(Msg.Connect), cls := "px-2 bg-green-200 rounded")(text("connect ws"))
          )
        ) ++ counters

    div(cls := "p-4")(div(cls := "space-y-4")(elems*))

  def subscriptions(model: Model): Sub[IO, Msg] =
    model.ws.fold(Sub.None) { ws =>
      ws.subscribe {
        case WebSocketEvent.Open        => Msg.NoOp
        case WebSocketEvent.Receive(s)  => Msg.WsReceive(s)
        case WebSocketEvent.Error(_)    => Msg.NoOp
        case WebSocketEvent.Close(_, _) => Msg.NoOp
        case WebSocketEvent.Heartbeat   => Msg.NoOp
      }
    }

case class Model(counters: List[Counter.Model], ws: Option[WebSocket[IO]])

enum Msg:
  case Insert, Remove
  case Modify(i: Int, msg: Counter.Msg)
  case Connect
  case WsOpen(ws: WebSocket[IO])
  case WsReceive(s: String)
  case NoOp

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
