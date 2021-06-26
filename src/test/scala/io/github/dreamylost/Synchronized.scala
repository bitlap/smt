package io.github.dreamylost

import org.scalatest.{ FlatSpec, Matchers }

/**
 *
 * @author 梦境迷离
 * @since 2021/6/24
 * @version 1.0
 */
class Synchronized extends FlatSpec with Matchers {

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

    @synchronized(lockedName = "obj")
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

    """
     @synchronized(lockedName = "obj")
     class A
      """ shouldNot compile

    """
     @synchronized(lockedName = "obj")
     val s = "1"
      """ shouldNot compile
  }

}
