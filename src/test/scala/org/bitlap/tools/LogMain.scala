/*
 * Copyright (c) 2021 org.bitlap
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

package org.bitlap.tools

/**
 *
 * @author 梦境迷离
 * @since 2021/6/29
 * @version 1.0
 */
object LogMain extends App {

  private final val log: java.util.logging.Logger = java.util.logging.Logger.getLogger(LogMain.getClass.getName)

  // object is not type
  private final val log2: org.apache.logging.log4j.Logger = org.apache.logging.log4j.LogManager.getLogger(LogMain.getClass.getName)

  private final val log3: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(LogMain.getClass)

  log.info("hello1")
  log2.info("hello2")
  log3.info("hello3")

}
