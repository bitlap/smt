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

import java.io._
import scala.io.Source
import scala.language.reflectiveCalls
import scala.util.control.Exception.ignoring

import scala.collection.mutable.ListBuffer

/** @author
 *    梦境迷离
 *  @version 1.0,5/13/22
 */
object FileUtils {

  type Closable = {
    def close(): Unit
  }

  def using[R <: Closable, T](resource: => R)(f: R => T): T =
    try f(resource)
    finally
      ignoring(classOf[Throwable]) apply {
        resource.close()
      }

  def writer(file: File, lines: List[String]): Boolean = {
    checkFile(file)
    val bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file))
    try
      using(new PrintWriter(bufferedOutputStream, true)) { r =>
        lines.foreach(r.println)
      }
    finally bufferedOutputStream.close()
    true
  }

  def reader(file: InputStream, charset: String = "UTF-8"): List[String] =
    try
      using(Source.fromInputStream(new BufferedInputStream(file), charset)) { lines =>
        lines.getLines().toList
      }
    finally file.close()

  def checkFile(file: File): Unit = {
    if (file.isDirectory) {
      throw new Exception(s"File path: $file is a directory.")
    }
    if (!file.exists()) {
      file.createNewFile()
    }
  }

  def readFileFunc[T](reader: BufferedReader, func: String => Option[T]): List[Option[T]] = {
    val ts           = ListBuffer[Option[T]]()
    var line: String = null
    FileUtils.using(new BufferedReader(reader)) { input =>
      while ({
        line = input.readLine()
        line != null
      })
        ts.append(func(line))
    }
    ts.result()
  }
}
