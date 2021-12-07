package org.bitlap

import io.github.dreamylost.toString

/**
 *
 * @author 梦境迷离
 * @since 2021/6/16
 * @version 1.0
 */
object Main extends App {

  @toString(includeInternalFields = true, includeFieldNames = true)
  class TestClass(val i: Int = 0, var j: Int) {
    val y: Int = 0
    var z: String = "hello"
    var x: String = "world"
  }

  val s = new TestClass(1, 2).toString
  println(s)

}
