package io.github.dreamylost

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 *
 * @author 梦境迷离
 * @since 2021/6/19
 * @version 1.0
 */
class BuilderTest extends AnyFlatSpec with Matchers {

  "builder1" should "case class, non companion object" in {
    @builder
    case class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))
    // field : <caseaccessor> <paramaccessor> val i: Int = 0， so default value is "_"
    val ret = TestClass1.builder().i(1).j(0).x("x").build()
    println(ret)
    assert(ret.toString == "TestClass1(1,0,x,Some())")

  }
  "builder2" should "case class with companion object" in {
    @builder
    case class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))
    object TestClass1
    val ret = TestClass1.builder().i(1).j(0).x("x").build()
    println(ret)
    assert(ret.toString == "TestClass1(1,0,x,Some())")
  }

  "builder3" should "class with toString, non companion object" in {
    @toString //"toString" must be before "builder"
    @builder
    class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))
    val ret = TestClass1.builder().i(1).j(0).x("x").build()
    println(ret)
    assert(ret.toString == "TestClass1(i=1, j=0, x=x, o=Some())")
  }

  "builder4" should "class toString and companion object" in {
    @toString
    @builder
    class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))
    object TestClass1
    val ret = TestClass1.builder().i(1).j(0).x("x").build()
    assert(ret.toString == "TestClass1(i=1, j=0, x=x, o=Some())")
  }

  "builder5" should "case class with toString and companion object" in {
    @toString
    @builder
    case class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))
    object TestClass1
    val ret = TestClass1.builder().i(1).j(0).x("x").build()
    println(ret)
    assert(ret.toString == "TestClass1(i=1, j=0, x=x, o=Some())")
  }

  "builder6" should "case class with toString, non companion object" in {
    @toString
    @builder
    case class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))
    val ret = TestClass1.builder().i(1).j(0).x("x").build()
    println(ret)
    assert(ret.toString == "TestClass1(i=1, j=0, x=x, o=Some())")
  }

  "builder7" should "case class with toString and companion object not in order" in {
    @builder
    @toString //failed when companion object exists, fix in 0.0.6
    case class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))
    object TestClass1
    val ret = TestClass1.builder().i(1).j(0).x("x").build()
    println(ret)
    assert(ret.toString == "TestClass1(i=1, j=0, x=x, o=Some())")
  }

  "builder8" should "ok on currying" in {

    @builder
    case class TestClass11(val i: Int = 0)(var j: Int)(val k: Int)
      (val t: Option[String])

    @builder
    class TestClass12(val i: Int = 0)(var j: Int)(val k: Int)
      (val t: Option[String])
  }

  "builder9" should "ok with generic" in {

    @builder
    case class TestClass11[T](i: T)(var j: Int)(val k: Int)
      (val t: Option[String])

    val a = TestClass11.builder().i("hello generic").j(1).k(22).t(None).build()
    println(a)

    @builder
    class TestClass12[T, U](val i: T, var j: U)
    val b = TestClass12.builder().i(new AnyRef).j(List("helloworld", "generic is ok")).build()
    println(b)
  }
}
