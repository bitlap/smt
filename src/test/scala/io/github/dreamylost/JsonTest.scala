package io.github.dreamylost

import play.api.libs.json.Json
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 *
 * @author 梦境迷离
 * @since 2021/6/18
 * @version 1.0
 */
class JsonTest extends AnyFlatSpec with Matchers {

  // class must be wrote here

  @json
  case class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))

  object TestClass1

  @json
  case class TestClass2(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))

  @json
  @SerialVersionUID(1L)
  case class TestClass3(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))

  "json1" should "println case class, exists companion object" in {
    val ret = Json.prettyPrint(Json.toJson(TestClass1(1, 2, "")))
    println(ret)
    assert(ret == "{\n  \"i\" : 1,\n  \"j\" : 2,\n  \"x\" : \"\",\n  \"o\" : \"\"\n}")
  }

  "json2" should "println case class, no companion object" in {
    val json = Json.toJson(TestClass2(1, 2, ""))
    val ret = Json.prettyPrint(json)
    println(Json.fromJson[TestClass2](json))
    println(ret)
    assert(ret == "{\n  \"i\" : 1,\n  \"j\" : 2,\n  \"x\" : \"\",\n  \"o\" : \"\"\n}")
  }

  "json3" should "println case class, contains other annotation" in {
    val ret = Json.prettyPrint(Json.toJson(TestClass3(1, 2, "")))
    println(ret)
    assert(ret == "{\n  \"i\" : 1,\n  \"j\" : 2,\n  \"x\" : \"\",\n  \"o\" : \"\"\n}")
  }
}
