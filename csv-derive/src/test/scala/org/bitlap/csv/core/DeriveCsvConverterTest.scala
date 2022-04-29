package org.bitlap.csv.core

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 *
 * @author 梦境迷离
 * @version 1.0,2022/4/29
 */
class DeriveCsvConverterTest extends AnyFlatSpec with Matchers {

  "DeriveCsvConverter1" should "ok" in {
    val line = "abc,cdf,d,12,2,false,0.1,0.23333"
    val dimension = CsvConverter[Dimension].from(line)
    assert(dimension.toString == "Some(Dimension(abc,Some(cdf),d,12,2,false,0.1,0.23333))")

  }

  "DeriveCsvConverter2" should "ok when csv column empty" in {
    val line =
      "abc,,d,12,2,false,0.1,0.23333"
    val dimension = CsvConverter[Dimension].from(line)
    println(dimension.toString)
    assert(dimension.toString == "Some(Dimension(abc,None,d,12,2,false,0.1,0.23333))")
  }

  "DeriveCsvConverter3" should "ok when using list" in {
    val line =
      """1,cdf,d,12,2,false,0.1,0.2
        |2,cdf,d,12,2,false,0.1,0.1""".stripMargin
    val dimension = CsvConverter[List[Dimension]].from(line)
    assert(dimension.toString == "Some(List(Dimension(1,Some(cdf),d,12,2,false,0.1,0.2), Dimension(2,Some(cdf),d,12,2,false,0.1,0.1)))")

  }

}
