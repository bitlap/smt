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

import scala.collection.mutable.ListBuffer

/**
 * split csv column value by columnSeparator.
 *
 * @author 梦境迷离
 * @version 1.0,2022/4/30
 */
object StringUtils {

  // cache
  def splitColumns(line: String, columnSeparator: Char): List[String] = {
    val listBuffer = ListBuffer[String]()
    val columnBuffer = ListBuffer[Char]()
    val chars = line.toCharArray
    for (cidx <- 0 until chars.length) {
      if (chars(cidx) != columnSeparator) {
        columnBuffer.append(chars(cidx))
      } else {
        // todo 向前搜索和向后搜索
        if (chars(cidx - 1) == '\"' && chars(cidx + 1) == '\"') {
          columnBuffer.append(chars(cidx))
        } else {
          listBuffer.append(columnBuffer.mkString)
          columnBuffer.clear()
        }
      }
    }
    if (columnBuffer.nonEmpty) {
      listBuffer.append(columnBuffer.mkString)
      columnBuffer.clear()
    }
    listBuffer.result()
  }

}
