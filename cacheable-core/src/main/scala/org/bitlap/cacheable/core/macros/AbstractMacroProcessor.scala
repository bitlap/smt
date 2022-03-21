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

package org.bitlap.cacheable.core.macros

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.reflect.macros.whitebox

/**
 *
 * @author 梦境迷离
 * @since 2022/3/19
 * @version 1.0
 */
abstract class AbstractMacroProcessor(val c: whitebox.Context) {

  import c.universe._

  protected val verbose: Boolean = false

  /**
   * Output ast result.
   *
   * @param force
   * @param resTree
   */
  def printTree(force: Boolean, resTree: Tree): Unit = {
    c.info(
      c.enclosingPosition,
      s"\n###### Time: ${ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)} " +
        s"Expanded macro start ######\n" + resTree.toString() + "\n###### Expanded macro end ######\n",
      force = force
    )
  }

  /**
   * Find the specified method Name in the enclosing class definition.
   *
   * @param t
   * @return Return a optional [[scala.reflect.api.Names#TermName]]
   */
  def findDefDefInEnclosingClass(t: Name): Option[TermName] = {
    getDefDefInEnclosingClass.find(_.decodedName.toString == t.decodedName.toString)
  }

  /**
   * Find all method Name in the enclosing class definition.
   *
   * @return Return a sequence of [[scala.reflect.api.Names#TermName]]
   */
  def getDefDefInEnclosingClass: Set[TermName] = {
    val buffer = ListBuffer[TermName]()

    @tailrec
    def doFind(trees: List[Tree]): Option[TermName] = trees match {
      case Nil => None
      case tree :: tail =>
        tree match {
          case DefDef(_, name, _, _, _, _) =>
            c.info(c.enclosingPosition, s"Method: `${name.decodedName.toString}` in enclosing class: `$getEnclosingClassName`.", force = verbose)
            buffer.append(name)
            doFind(tail)
          case _ =>
            doFind(tail)
        }
    }

    c.enclosingClass match {
      case ClassDef(_, _, _, Template(_, _, body)) => doFind(body)
      case ModuleDef(_, _, Template(_, _, body))   => doFind(body)
    }
    buffer.result().toSet
  }

  /**
   * Get enclosing class name
   *
   * @return
   */
  def getEnclosingClassName: String = {
    c.enclosingClass match {
      case ClassDef(_, name, _, Template(_, _, _)) => name.decodedName.toString
      case ModuleDef(_, name, Template(_, _, _))   => name.decodedName.toString
    }
  }
}
