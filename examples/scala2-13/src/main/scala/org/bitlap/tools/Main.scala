package org.bitlap.tools

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

  @toString(includeInternalFields = false, includeFieldNames = true)
  @apply
  @builder class A2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L)) {
    private val a: Int = 1
    var b: Int = 1
    protected var c: Int = _

    def helloWorld: String = "hello world"
  }

  println(A2(1, 2, None, None)) //use apply and toString
  println(A2.builder().int(1).j(2).k(Option("hello")).t(None).build()) //use builder and toString

}
