package org.djnzx

sealed trait MsgServerToClient

case object Welcome extends MsgServerToClient
case class Response(value: String) extends MsgServerToClient
