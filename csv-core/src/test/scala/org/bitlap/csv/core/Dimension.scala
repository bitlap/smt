package org.bitlap.csv.core

/**
 *
 * @author 梦境迷离
 * @version 1.0,2022/4/29
 */
case class Dimension(key: String, value: Option[String], d: Char, c: Long, e: Short, f: Boolean, g: Float, h: Double)

object Dimension {

  implicit def dimensionCsvConverter: CsvConverter[Dimension] = new CsvConverter[Dimension] {
    override def from(line: String): Option[Dimension] = DeriveToCaseClass[Dimension](line, ",")
    override def to(t: Dimension): String = DeriveToString[Dimension](t)
  }

}
