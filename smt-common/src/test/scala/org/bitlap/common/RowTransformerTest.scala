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

package org.bitlap.common

import org.bitlap.common.jdbc._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.sql.ResultSet

import java.sql.DriverManager
import java.sql.Types

/** @author
 *    梦境迷离
 *  @version 1.0,2022/10/21
 */
class RowTransformerTest extends AnyFlatSpec with Matchers {

  Class.forName("org.h2.Driver")

  // TODO  need bitlap server
  "RowTransformerTest simple case" should "ok for GenericRow2" in {
    val statement = DriverManager
      .getConnection(
        "jdbc:h2:mem:zim?caseSensitive=false;MODE=MYSQL;TRACE_LEVEL_FILE=2;INIT=RUNSCRIPT FROM 'classpath:test.sql'"
      )
      .createStatement()
    statement.execute(s"""select * from T_USER""".stripMargin)

    val rowSet: ResultSet = statement.getResultSet

    // default type mapping
    val ret1 = ResultSetTransformer[GenericRow4[Int, String, String, String]].toResults(rowSet)
    assert(ret1.size == 2)
    println(ret1)

    statement.execute(s"""select * from T_USER""".stripMargin)

    val rowSet2: ResultSet = statement.getResultSet

    // custom type mapping
    val ret2 = ResultSetTransformer[GenericRow4[Int, String, String, String]].toResults(
      rowSet2,
      (resultSet, columnSize) => {
        val metadata = resultSet.getMetaData
        1 to columnSize map { idx =>
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
            case _               => resultSet.getObject(name)
          }
        }
      }
    )
    println(ret2)
    assert(ret2.size == 2)
  }
}
