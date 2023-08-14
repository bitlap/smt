/*
 * Copyright (c) 2023 bitlap
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

package org.bitlap.common.internal

import org.bitlap.common.jdbc._

import scala.reflect.macros.whitebox

/** @author
 *    梦境迷离
 *  @version 1.0,6/15/22
 */
class ExtractorMacro(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

  import c.universe._

  private val valuesTermName      = TermName("values")
  private val typeMappingTermName = TermName("typeMapping")
  private val columnSizeTermName  = TermName("columnSize")
  protected val packageName       = q"_root_.org.bitlap.common.jdbc"

  def applyImpl[T <: GenericRow: WeakTypeTag]: Expr[Extractor[T]] = {
    val className       = classTypeName[T]
    val genericTypeList = genericTypes[T]
    val fieldValues = genericTypeList.zipWithIndex.map { case (tpe, i) =>
      q"$valuesTermName($i).asInstanceOf[$tpe]"
    }
    // scalafmt: { maxColumn = 400 }
    val tree = q"""
       new $packageName.Extractor[$className[..$genericTypes]] {
          override def from(resultSet: java.sql.ResultSet, $typeMappingTermName: (java.sql.ResultSet, Int) => _root_.scala.IndexedSeq[Any] = super.getColumnValues): _root_.scala.Seq[$className[..$genericTypes]] = {
              val $columnSizeTermName   = resultSet.getMetaData.getColumnCount
              val result = _root_.scala.collection.mutable.ListBuffer[$className[..$genericTypes]]()
              while (resultSet.next()) {
                val $valuesTermName = $typeMappingTermName(resultSet, $columnSizeTermName)
                assert($columnSizeTermName == ${fieldValues.size})
                result += ${className.toTermName}(..$fieldValues)
              }
              result.toSeq
          }
       }
     """
    c.Expr[Extractor[T]](tree)

  }
}
