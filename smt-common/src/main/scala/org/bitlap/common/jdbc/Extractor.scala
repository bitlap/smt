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

package org.bitlap.common.jdbc
import org.bitlap.common.internal.ExtractorMacro

import java.sql._

trait Extractor[T <: GenericRow] {

  protected def getColumnValues(resultSet: ResultSet, size: Int): IndexedSeq[Any] = {
    val metadata = resultSet.getMetaData
    1 to size map { idx =>
      val tpe  = metadata.getColumnType(idx)
      val name = metadata.getColumnName(idx)
      tpe match {
        case Types.VARCHAR   => resultSet.getString(name)
        case Types.BIGINT    => resultSet.getLong(name)
        case Types.TINYINT   => resultSet.getByte(name)
        case Types.SMALLINT  => resultSet.getShort(name)
        case Types.BOOLEAN   => resultSet.getBoolean(name)
        case Types.INTEGER   => resultSet.getInt(name)
        case Types.DOUBLE    => resultSet.getDouble(name)
        case Types.TIMESTAMP => resultSet.getTimestamp(name)
        case Types.TIME      => resultSet.getTime(name)
        case Types.FLOAT     => resultSet.getFloat(name)
        case Types.DATE      => resultSet.getDate(name)
        case _               => resultSet.getObject(name)
      }
    }
  }

  def from(resultSet: ResultSet, typeMapping: (ResultSet, Int) => IndexedSeq[Any] = getColumnValues): Seq[T]
}
object Extractor {

  def apply[T <: GenericRow]: Extractor[T] = macro ExtractorMacro.applyImpl[T]
}
