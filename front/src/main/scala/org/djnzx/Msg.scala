package org.djnzx

import cats.effect.IO
import tyrian.websocket.WebSocket

enum Msg:
  // counter
  case InsertCounter
  case RemoveCounter
  case ModifyCounter(i: Int, msg: Counter.Msg)
  // web socket
  case WsConnect
  case WsDisconnect
  case WsOpen(ws: WebSocket[IO])
  case WsReady
  case WsReceive(s: String)
  // form
  case InputChange(ch: String)
  case SendMessage
  case ToggleDisconnectOnDone
  // stub
  case NoOp
