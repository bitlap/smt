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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 *
 * @author 梦境迷离
 * @since 2021/6/24
 * @version 1.0
 */
class SynchronizedTest extends AnyFlatSpec with Matchers {

  "synchronized1" should "is ok at class" in {
    @synchronized
    def getStr(k: Int): String = {
      k + ""
    }
    """@synchronized
       def getStr(k: Int): String = {
          k + ""
        }
      """ should compile

    @synchronized
    def getStr2(k: Int): String = {
      k + ""
    }
    """@synchronized
       def getStr2(k: Int) = {
          k + ""
        }
      """ should compile
  }

  "synchronized2" should "is ok by custom obj" in {

    val obj = new Object

    @synchronized(verbose = true, lockedName = "obj")
    def getStr3(k: Int): String = {
      k + ""
    }
    """
     @synchronized(lockedName = "obj")
     def getStr3(k: Int) = {
          k + ""
        }
      """ should compile

    object TestObject {
      // def getStr(k: Int): String = this.synchronized(k.$plus(""))
      // def getStr(k: Int): String = this.synchronized(this.synchronized(k.$plus("")))
      @synchronized
      def getStr(k: Int): String = {
        this.synchronized(k + "")
      }
    }
  }

  "synchronized3" should "fail when obj not exists or it was used on fields" in {

    """
     @synchronized(lockedName = "obj")
     class A
      """ shouldNot compile

    """
     @synchronized(lockedName = "obj")
     val s = "1"
      """ shouldNot compile
  }

  "synchronized4" should "failed when input not in order" in {
    """
      |    val obj = new Object
      |    @synchronized(lockedName = "obj", verbose = true)
      |    def getStr3(k: Int): String = {
      |      k + ""
      |    }
      |""".stripMargin shouldNot compile
  }

}
