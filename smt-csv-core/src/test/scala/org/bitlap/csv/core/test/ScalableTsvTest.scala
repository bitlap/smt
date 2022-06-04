package org.bitlap.csv.core.test

import org.bitlap.csv.core.{ CsvableBuilder, ScalableBuilder, TsvFormat }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File

/** @author
 *    梦境迷离
 *  @version 1.0,6/4/22
 */
class ScalableTsvTest extends AnyFlatSpec with Matchers {

  "ScalableTsvTest1" should "ok when file is tsv" in {
    implicit val format = new TsvFormat {
      override val delimiter: Char             = ' '
      override val ignoreEmptyLines: Boolean   = true
      override val ignoreHeader: Boolean       = true
      override val prependHeader: List[String] = List("time", "entity", "dimensions", "metricName", "metricValue")
    }
    val metrics =
      ScalableBuilder[Metric3]
        .convertFrom(ClassLoader.getSystemResourceAsStream("simple_data_header.tsv"))
    println(metrics)
    assert(metrics.nonEmpty)
    assert(
      metrics.toString() ==
        """List(Some(Metric3(100,1,{"city":"北京","os":"Mac"},vv,1)), Some(Metric3(100,1,{"city":"北京","os":"Mac"},pv,2)), Some(Metric3(100,1,{"city":"北京","os":"Windows"},vv,1)), Some(Metric3(100,1,{"city":"北京","os":"Windows"},pv,3)))"""
    )

    val file = new File("./simple_data_header.tsv")
    CsvableBuilder[Metric3]
      // NOTE: not support pass anonymous object to convertTo method.
      .convertTo(metrics.filter(_.isDefined).map(_.get), file)
    file.delete()
  }
}
