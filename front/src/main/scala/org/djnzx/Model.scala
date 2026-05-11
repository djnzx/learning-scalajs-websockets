package org.djnzx

case class Model(
  counters: List[Counter.Model],
  ws: Option[WsConn],
  input: String = "",
  messages: List[String] = Nil,
  pendingSend: Boolean = false,
  disconnectOnDone: Boolean = true
):
  def connect(conn: WsConn): Model = copy(ws = Some(conn))
  def disconnect: Model = copy(ws = None)
  def withInput(s: String): Model = copy(input = s)
  def clearInput: Model = withInput("")
  def withCounter: Model = copy(counters = Counter.init :: counters)
  def withoutCounter: Model = copy(counters = counters.drop(1))
  def modifyCounter(id: Int, m: Counter.Msg): Model =
    copy(counters = counters.zipWithIndex.map {
      case (c, i) if i == id => Counter.update(m, c)
      case (c, _)            => c
    })
  def withMessage(msg: String): Model = copy(messages = msg :: messages)
  def withMessageIn(msg: String): Model = withMessage(s"< $msg")
  def withMessageOut(msg: String): Model = withMessage(s"> $msg")
