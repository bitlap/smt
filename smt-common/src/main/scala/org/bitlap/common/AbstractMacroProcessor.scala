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

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.reflect.macros.blackbox

/** This is a generic implementation of macro handling, and subclasses need to inherit it to reduce redundant code.
 *
 *  @author
 *    梦境迷离
 *  @since 2021/7/24
 *  @version 1.0
 */
abstract class AbstractMacroProcessor(val c: blackbox.Context) {

  import c.universe._

  final case class FieldZipInformation(fieldNames: List[String], fieldIndexTypeMapping: List[(Int, Type)])

  final case class FieldTreeInformation(
    index: Int,
    fieldTerm: Tree,
    fieldType: Type,
    zeroValue: Tree,
    isSeq: Boolean = false,
    isList: Boolean = false,
    isOption: Boolean = false,
    genericType: Option[Type] = None
  )

  final case class FieldInformation(
    fieldName: String,
    fieldType: Type,
    isSeq: Boolean = false,
    isList: Boolean = false,
    isOption: Boolean = false,
    genericType: Option[Type] = None
  )

  def tryGetOrElse(tree: Tree, default: Tree): Tree =
    q"_root_.scala.util.Try($tree).getOrElse($default)"

  def tryOptionGetOrElse(optionTree: Tree, default: Tree): Tree =
    q"_root_.scala.util.Try($optionTree.getOrElse($default)).getOrElse($default)"

  def tryOption(optionTree: Tree): Tree =
    q"_root_.scala.util.Try($optionTree).getOrElse(_root_.scala.None)"

  /** Get the list of case class constructor parameters and return the column index, column name, and parameter type
   *  that zip as a `List[FieldTreeInformation]`.
   *
   *  @param columnsFunc
   *    The function to get CSV row data temporary identifier, also known as a line.
   *  @tparam T
   *    Type of the case class.
   *  @return
   */
  def checkGetFieldTreeInformationList[T: WeakTypeTag](columnsFunc: TermName): List[FieldTreeInformation] = {
    val idxColumn    = (i: Int) => q"$columnsFunc()($i)"
    val params       = getCaseClassFieldInfo[T]()
    val paramsSize   = params.size
    val types        = params.map(_.fieldType)
    val indexColumns = (0 until paramsSize).toList.map(i => i -> idxColumn(i))
    if (indexColumns.size != types.size) {
      c.abort(c.enclosingPosition, "The column num of CSV file is different from that in case class constructor!")
    }

    indexColumns zip types map { kv =>
      val (isOption, isSeq, isList) = isWrapType(kv._2)
      val typed                     = c.typecheck(tq"${kv._2}", c.TYPEmode).tpe
      var genericType: Option[Type] = None
      if (isList || isSeq || isOption) {
        genericType = Option(typed.typeArgs.head)
      }
      FieldTreeInformation(
        kv._1._1,
        kv._1._2,
        kv._2,
        getDefaultValue(kv._2),
        isSeq,
        isList,
        isOption,
        genericType
      )
    }
  }

  /** Get only the symbol of the case class constructor parameters.
   *
   *  @tparam T
   *    Type of the case class.
   *  @return
   */
  def getCaseClassFieldInfo[T: WeakTypeTag](): List[FieldInformation] = {
    val parameters = resolveParameters[T]
    if (parameters.size > 1) {
      c.abort(c.enclosingPosition, "The constructor of case class has currying!")
    }
    parameters.flatten.map { p =>
      val typed                     = c.typecheck(tq"$p", c.TYPEmode).tpe
      var genericType: Option[Type] = None
      val (isOption, isSeq, isList) = isWrapType(typed)
      if (isList || isSeq || isOption) {
        genericType = Option(typed.typeArgs.head)
      }
      FieldInformation(
        p.name.decodedName.toString,
        typed,
        isSeq,
        isList,
        isOption,
        genericType
      )
    }
  }

  /** Print the expanded code of macro.
   *
   *  @param force
   *  @param resTree
   *  @tparam T
   *  @return
   */
  def exprPrintTree[T: WeakTypeTag](force: Boolean, resTree: Tree): Expr[T] = {
    c.info(
      c.enclosingPosition,
      s"\n###### Time: ${ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)} Expanded macro start ######\n" + resTree
        .toString() + "\n###### Expanded macro end ######\n",
      force = force
    )
    c.Expr[T](resTree)
  }

  /** Get the constructor symbol of the case class.
   *
   *  @tparam T
   *    Type of the case class.
   *  @return
   *    The parameters may be currying, so it's a two-level list.
   */
  def resolveParameters[T: WeakTypeTag]: List[List[Symbol]] =
    c.weakTypeOf[T].resultType.member(TermName("<init>")).typeSignature.paramLists

  /** Get the `TypeName` of the class.
   *
   *  @tparam T
   *    Type of the case class.
   *  @return
   */
  def resolveClassTypeName[T: WeakTypeTag]: TypeName =
    TypeName(c.weakTypeOf[T].typeSymbol.name.decodedName.toString)

  /** Get the list of case class constructor parameters and return the column index and parameter type that zip as a
   *  `FieldZipInformation`.
   *
   *  @tparam T
   *    Type of the case class.
   *  @return
   */
  def checkGetFieldZipInformation[T: WeakTypeTag]: FieldZipInformation = {
    val params     = getCaseClassFieldInfo[T]()
    val paramsSize = params.size
    val names      = params.map(_.fieldName)
    FieldZipInformation(
      names,
      params.zip(0 until paramsSize).map(f => f._2 -> f._1.fieldType)
    )
  }

  /** Get the builderId of the current class which generated by *Builder,apply macro.
   *
   *  @param annoBuilderPrefix
   *  @return
   */
  def getBuilderId(annoBuilderPrefix: String): Int =
    c.prefix.actualType.toString.replace(annoBuilderPrefix, "").toInt

  private def getDefaultValue(typ: Type): Tree =
    typ match {
      case t if t =:= typeOf[Int] =>
        q"0"
      case t if t =:= typeOf[String] =>
        val empty = ""
        q"$empty"
      case t if t =:= typeOf[Float] =>
        q"0.asInstanceOf[Float]"
      case t if t =:= typeOf[Double] =>
        q"0D"
      case t if t =:= typeOf[Char] =>
        q"'?'"
      case t if t =:= typeOf[Byte] =>
        q"0"
      case t if t =:= typeOf[Short] =>
        q"0"
      case t if t =:= typeOf[Boolean] =>
        q"false"
      case t if t =:= typeOf[Long] =>
        q"0L"
      case t if t <:< typeOf[List[_]]   => q"_root_.scala.Nil"
      case t if t <:< typeOf[Seq[_]]    => q"_root_.scala.Nil"
      case t if t <:< typeOf[Option[_]] => q"_root_.scala.None"
      case _ =>
        q"null"
    }

  private type OptionSeqList = (Boolean, Boolean, Boolean)

  private def isWrapType(typed: Type): OptionSeqList = {
    var isList: Boolean   = false
    var isSeq: Boolean    = false
    var isOption: Boolean = false
    typed match {
      case t if t <:< typeOf[List[_]] =>
        isList = true
      case t if t <:< typeOf[Option[_]] =>
        isOption = true
      case t if t <:< typeOf[Seq[_]] && !isList =>
        isSeq = true
      case _ =>
    }
    Tuple3(isOption, isSeq, isList)
  }

}
