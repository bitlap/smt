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
