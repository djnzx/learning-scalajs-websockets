package org.djnzx

import cats.effect.IO
import tyrian.*
import tyrian.websocket.*

object WsHandler:

  private val wsUrl = s"ws://${org.scalajs.dom.window.location.hostname}:8081/ws"

  val connectCmd: Cmd[IO, Msg] =
    WebSocket.connect[IO, Msg](wsUrl) {
      case WebSocketConnect.Socket(ws) => Msg.WsOpen(ws)
      case WebSocketConnect.Error(_)   => Msg.NoOp
    }

  def handle(model: Model): PartialFunction[Msg, (Model, Cmd[IO, Msg])] =

    case Msg.WsConnect =>
      model -> connectCmd

    case Msg.WsDisconnect =>
      model.ws match
        case None     => model            -> Cmd.None
        case Some(ws) => model.disconnect -> ws.disconnect

    case Msg.WsOpen(ws) =>
      (model.connect(ws), Cmd.None)

    case Msg.WsReady if model.pendingSend && model.input.nonEmpty =>
      val cmd = model.ws.fold(Cmd.None)(_.publish(model.input))
      (model.withMessageOut(model.input).clearInput.copy(pendingSend = false), cmd)

    case Msg.WsReady =>
      (model.copy(pendingSend = false), Cmd.None)

    case Msg.WsReceive(s @ "done!") if model.disconnectOnDone =>
      val cmd = model.ws.fold(Cmd.None)(_.disconnect)
      (model.withMessageIn(s).disconnect, cmd)

    case Msg.WsReceive(s) =>
      (model.withMessageIn(s), Cmd.None)

    case Msg.ToggleDisconnectOnDone =>
      (model.copy(disconnectOnDone = !model.disconnectOnDone), Cmd.None)

  def subscribeAndHandle(ws: WebSocket[IO]): Sub[IO, Msg] =
    ws.subscribe {
      case WebSocketEvent.Open        => Msg.WsReady
      case WebSocketEvent.Receive(s)  => Msg.WsReceive(s)
      case WebSocketEvent.Error(_)    => Msg.NoOp
      case WebSocketEvent.Close(_, _) => Msg.NoOp
      case WebSocketEvent.Heartbeat   => Msg.NoOp
    }
