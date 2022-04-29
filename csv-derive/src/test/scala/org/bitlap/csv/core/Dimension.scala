package org.bitlap.csv.core

import org.bitlap.csv.derive.DeriveCsvConverter

/**
 *
 * @author 梦境迷离
 * @version 1.0,2022/4/29
 */
case class Dimension(key: String, value: Option[String], d: Char, c: Long, e: Short, f: Boolean, g: Float, h: Double)

object Dimension {

  implicit val dimensionCsvConverter: CsvConverter[Dimension] = DeriveCsvConverter.gen[Dimension]

}
