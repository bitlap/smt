/*
 * Copyright (c) 2022 bitlap
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.bitlap.csv.core

import java.util.regex.Pattern
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

/** split csv column value by columnSeparator.
 *
 *  @author
 *    梦境迷离
 *  @version 1.0,2022/4/30
 */
object StringUtils {

  private val regex: Regex     = "\\{(.*?)\\}".r
  private val kvr: Regex       = "(.*):(.*)".r
  private val pattern: Pattern = Pattern.compile(regex.toString())

  def extraJsonPairs(input: String): String = {
    val matcher = pattern.matcher(input)
    while (matcher.find) {
      val tail = matcher.group().tail.init
      if (tail != null && tail.nonEmpty) {
        return tail
      } else return null
    }

    null
  }

  def extractJsonValues[T <: Product](jsonString: String)(func: (String, String) => T): List[T] = {
    val pairs = extraJsonPairs(jsonString)
    if (pairs == null) return Nil
    val jsonElements = pairs.split(",")
    val kvs = jsonElements.collect {
      case kvr(k, v) if k.length > 2 && v.length > 2 => k.init.tail -> v.init.tail
    }
    kvs.toList.map(f => func(f._1, f._2))
  }

  def splitColumns(line: => String, format: CsvFormat): List[String] = {
    val listBuffer   = ListBuffer[String]()
    val columnBuffer = ListBuffer[Char]()
    val chars        = line.toCharArray

    var idx = 0
    while (idx < chars.length)
      chars(idx) match {
        case c if c == format.delimiter =>
          listBuffer.append(columnBuffer.mkString)
          columnBuffer.clear()
          idx += 1
        case c if c == format.escapeChar =>
          idx += 1
          var isTail = false
          while (idx < chars.length && !isTail)
            if (chars(idx) == format.escapeChar && idx + 1 < chars.length && chars(idx + 1) == format.escapeChar) {
              columnBuffer.append(format.escapeChar)
              idx += 2
            } else if (chars(idx) == format.escapeChar) {
              isTail = true
              idx += 1
            } else {
              columnBuffer.append(chars(idx))
              idx += 1
            }
        case c =>
          columnBuffer.append(c)
          idx += 1
      }
    if (columnBuffer.nonEmpty) {
      listBuffer.append(columnBuffer.mkString)
      columnBuffer.clear()
    }
    listBuffer.result()
  }

  def splitColumns(line: => String, columnSeparator: Char): List[String] = {
    val listBuffer   = ListBuffer[String]()
    val columnBuffer = ListBuffer[Char]()
    val chars        = line.toCharArray

    var idx = 0
    while (idx < chars.length)
      chars(idx) match {
        case c if c == columnSeparator =>
          listBuffer.append(columnBuffer.mkString)
          columnBuffer.clear()
          idx += 1
        case '\"' =>
          idx += 1
          var isTail = false
          while (idx < chars.length && !isTail)
            if (chars(idx) == '\"' && idx + 1 < chars.length && chars(idx + 1) == '\"') {
              columnBuffer.append('\"')
              idx += 2
            } else if (chars(idx) == '\"') {
              isTail = true
              idx += 1
            } else {
              columnBuffer.append(chars(idx))
              idx += 1
            }
        case c =>
          columnBuffer.append(c)
          idx += 1
      }
    if (columnBuffer.nonEmpty) {
      listBuffer.append(columnBuffer.mkString)
      columnBuffer.clear()
    }
    listBuffer.result()
  }
}
