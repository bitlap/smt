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

package org.bitlap.csv.core.test

import org.bitlap.csv.core.FileUtils

import org.bitlap.csv.core.StringUtils
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.io.{ BufferedReader, InputStreamReader }
import org.bitlap.csv.core.defaultCsvFormat

/** @author
 *    梦境迷离
 *  @version 1.0,2022/4/30
 */
class StringUtilsTest extends AnyFlatSpec with Matchers {

  "StringUtilsTest1" should "ok" in {
    val line = """abc,"{""a"":""b"",""c"":""d""}",d,12,2,false,0.1,0.23333"""
    val csv  = StringUtils.splitColumns(line, defaultCsvFormat)
    println(csv)
    assert(csv.size == 8)
  }

  "StringUtilsTest2" should "ok" in {
    // only extract json `"{""a"":""b"",""c"":""d""}"`
    val line = """abc,"{""a"":""b"",""c"":""d""}",d,12,2,false,0.1,0.23333"""
    val csv  = StringUtils.extractJsonValues[Dimension3](line)((k, v) => Dimension3(k, v))
    println(csv)
    assert(csv.toString() == "List(Dimension3(\"a\",\"b\"), Dimension3(\"c\",\"d\"))")
  }

  "StringUtilsTest3" should "ok for file" in {
    val reader         = new InputStreamReader(ClassLoader.getSystemResourceAsStream("simple_data.csv"))
    val bufferedReader = new BufferedReader(reader)
    FileUtils.using(bufferedReader) { input =>
      var line: String = null
      while ({
        line = input.readLine()
        line != null
      }) {
        // List(Dimension3("city","北京"), Dimension3("os","Mac"))
        val dims = StringUtils.extractJsonValues(line)((k, v) => Dimension3(k, v))
        println(dims.size == 2)
      }
    }
  }
}
