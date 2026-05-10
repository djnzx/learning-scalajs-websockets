package org.djnzx

sealed trait MsgServerToClient

object MsgServerToClient {

  case object WsWelcome extends MsgServerToClient
  case class WsMsg(value: String) extends MsgServerToClient
  case object WsBye extends MsgServerToClient

}
