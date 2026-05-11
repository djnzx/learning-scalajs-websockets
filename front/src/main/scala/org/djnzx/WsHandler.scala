package org.djnzx

import cats.effect.IO
import org.http4s.Uri
import org.http4s.client.websocket.*
import org.http4s.dom.WebSocketClient
import tyrian.*

final class WsConn(conn: WSConnectionHighLevel[IO], closeF: IO[Unit]):
  def send(text: String): Cmd[IO, Msg] = Cmd.SideEffect(conn.sendText(text))
  def disconnect: Cmd[IO, Msg] = Cmd.SideEffect(closeF)
  def subscribe: Sub[IO, Msg] =
    Sub.make("ws")(
      conn.receiveStream.collect { case WSFrame.Text(data, _) => Msg.WsReceive(data): Msg }
    )(closeF)

object WsHandler:

  private val wsUri = Uri.unsafeFromString(s"ws://${org.scalajs.dom.window.location.hostname}:8081/ws")

  val connectCmd: Cmd[IO, Msg] =
    Cmd.Run(
      WebSocketClient[IO].connectHighLevel(WSRequest(wsUri)).allocated
        .map { case (conn, closeF) => WsConn(conn, closeF) },
      Msg.WsOpen(_)
    )

  def handle(model: Model): PartialFunction[Msg, (Model, Cmd[IO, Msg])] =

    case Msg.WsConnect =>
      model -> connectCmd

    case Msg.WsDisconnect =>
      model.ws match
        case None     => model            -> Cmd.None
        case Some(ws) => model.disconnect -> ws.disconnect

    case Msg.WsOpen(ws) if model.pendingSend && model.input.nonEmpty =>
      val cmd = ws.send(model.input)
      (model.connect(ws).withMessageOut(model.input).clearInput.copy(pendingSend = false), cmd)

    case Msg.WsOpen(conn) =>
      (model.connect(conn).copy(pendingSend = false), Cmd.None)

    case Msg.WsReceive(s @ "done!") if model.disconnectOnDone =>
      val cmd = model.ws.fold(Cmd.None)(_.disconnect)
      (model.withMessageIn(s).disconnect, cmd)

    case Msg.WsReceive(s) =>
      (model.withMessageIn(s), Cmd.None)

    case Msg.ToggleDisconnectOnDone =>
      (model.copy(disconnectOnDone = !model.disconnectOnDone), Cmd.None)
