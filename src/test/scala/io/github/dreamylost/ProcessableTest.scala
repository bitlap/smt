package io.github.dreamylost

import com.google.protobuf.Message
import io.github.dreamylost.sofa.{ CustomRpcProcessor, Processable }

/**
 *
 * @author li.guobin@immomo.com
 * @version 1.0,2021/12/3
 */
object ProcessableTest extends App {

  val s = Processable[Message, CustomRpcProcessor[Message], Service](
    (t, r, s) => t,
    (r, t, s) => null,
    new Service,
    null
  )

  println(s.defaultResp)

}

class Service {

}
