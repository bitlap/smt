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

package org.bitlap.common.internal

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

  final case class CollectionFlags(
    isSeq: Boolean = false,
    isList: Boolean = false,
    isOption: Boolean = false,
    isVector: Boolean = false,
    isSet: Boolean = false
  ) {
    def isCollection: Boolean = isSeq || isList || isOption || isVector || isSet
  }

  final case class FieldTreeInformation(
    index: Int,
    fieldTerm: Tree,
    fieldType: Type,
    zeroValue: Tree,
    collectionsFlags: CollectionFlags,
    genericType: List[Type] = Nil
  )

  final case class FieldInformation(
    fieldName: String,
    fieldType: Type,
    collectionFlags: CollectionFlags,
    genericType: List[Type] = Nil,
    hasDefaultValue: Boolean,
    zeroValue: Tree
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
    val params       = getCaseClassFieldInfoList[T]()
    val paramsSize   = params.size
    val types        = params.map(_.fieldType)
    val indexColumns = (0 until paramsSize).toList.map(i => i -> idxColumn(i))
    if (indexColumns.size != types.size) {
      c.abort(c.enclosingPosition, "The column num of CSV file is different from that in case class constructor!")
    }

    indexColumns zip types map { kv =>
      val FieldTypeFlag(isOption, isSeq, isList, isVector, isSet) = isWrapType(kv._2)
      val typed                                                   = c.typecheck(tq"${kv._2}", c.TYPEmode).tpe
      var genericType: List[Type]                                 = Nil
      if (isList || isSeq || isOption || isVector || isSet) {
        genericType = typed.dealias.typeArgs ::: genericType
      }
      FieldTreeInformation(
        kv._1._1,
        kv._1._2,
        kv._2,
        getZeroValue(kv._2),
        CollectionFlags(isSeq, isList, isOption, isVector, isSet),
        genericType
      )
    }
  }

  def getFieldDefaultValueMap[T: WeakTypeTag](init: MethodSymbol): Map[String, Tree] = {
    val classSym = weakTypeOf[T].typeSymbol
    init.paramLists.head
      .map(_.asTerm)
      .zipWithIndex
      .flatMap { case (p, i) =>
        if (!p.isParamWithDefault) None
        else {
          val getterName = TermName("apply$default$" + (i + 1))
          Some(p.name.decodedName.toString -> q"${classSym.name.toTermName}.$getterName") // moduleSym is none
        }
      }
      .toMap
  }

  /** Get only the symbol of the case class constructor parameters.
   *
   *  @tparam T
   *    Type of the case class.
   *  @return
   */
  def getCaseClassFieldInfoList[T: WeakTypeTag](): List[FieldInformation] = {
    val init              = c.weakTypeOf[T].resultType.member(TermName("<init>")).asMethod
    val defaultValuesTerm = getFieldDefaultValueMap[T](init)
    val parameters        = init.typeSignature.paramLists
    if (parameters.size > 1) {
      c.abort(c.enclosingPosition, "The constructor of case class has currying!")
    }
    parameters.flatten.map { p =>
      val typed                                                   = c.typecheck(tq"$p", c.TYPEmode).tpe
      var genericType: List[Type]                                 = Nil
      val FieldTypeFlag(isOption, isSeq, isList, isVector, isSet) = isWrapType(typed)
      if (isList || isSeq || isOption || isVector || isSet) {
        genericType = typed.dealias.typeArgs ::: genericType
      }
      FieldInformation(
        p.name.decodedName.toString,
        typed,
        CollectionFlags(isSeq, isList, isOption, isVector, isSet),
        genericType,
        defaultValuesTerm.contains(p.name.decodedName.toString),
        getZeroValue(typed)
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

  /** Get the `TypeName` of the class.
   *
   *  @tparam T
   *    Type of the case class.
   *  @return
   */
  def resolveClassTypeName[T: WeakTypeTag]: TypeName =
    c.weakTypeOf[T].typeSymbol.name.toTypeName

  /** Get the list of case class constructor parameters and return the column index and parameter type that zip as a
   *  `FieldZipInformation`.
   *
   *  @tparam T
   *    Type of the case class.
   *  @return
   */
  def checkGetFieldZipInformation[T: WeakTypeTag]: FieldZipInformation = {
    val params     = getCaseClassFieldInfoList[T]()
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

  def getZeroValue(typ: Type): Tree =
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
      case t if t weak_<:< typeOf[List[_]]   => q"_root_.scala.Nil"
      case t if t weak_<:< typeOf[Option[_]] => q"_root_.scala.None"
      case t if t weak_<:< typeOf[Set[_]]    => q"_root_.scala.Predef.Set.empty"
      case t if t weak_<:< typeOf[Vector[_]] => q"_root_.scala.Vector.empty"
      case t if t weak_<:< typeOf[Seq[_]]    => q"_root_.scala.Nil"
      case _ =>
        q"null"
    }

  final case class FieldTypeFlag(
    isOption: Boolean = false,
    isSeq: Boolean = false,
    isList: Boolean = false,
    isVector: Boolean = false,
    isSet: Boolean = false
  )

  private def isWrapType(typed: Type): FieldTypeFlag = {
    var isList: Boolean   = false
    var isSeq: Boolean    = false
    var isOption: Boolean = false
    var isVector: Boolean = false
    var isSet: Boolean    = false
    typed match {
      case t if t weak_<:< weakTypeOf[List[_]] =>
        isList = true
      case t if t weak_<:< weakTypeOf[Option[_]] =>
        isOption = true
      case t if t weak_<:< weakTypeOf[Vector[_]] =>
        isVector = true
      case t if t weak_<:< weakTypeOf[Set[_]] =>
        isSet = true
      case t if !isList && (t weak_<:< weakTypeOf[Seq[_]]) =>
        isSeq = true
      case _ =>
    }
    FieldTypeFlag(isOption, isSeq, isList, isVector, isSet)
  }

}
