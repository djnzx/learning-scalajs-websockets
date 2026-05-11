package org.djnzx

enum Msg:
  // counter
  case InsertCounter
  case RemoveCounter
  case ModifyCounter(i: Int, msg: Counter.Msg)
  // web socket
  case WsConnect
  case WsDisconnect
  case WsOpen(conn: WsConn)
  case WsReceive(s: String)
  // form
  case InputChange(ch: String)
  case SendMessage
  case ToggleDisconnectOnDone
  // stub
  case NoOp
