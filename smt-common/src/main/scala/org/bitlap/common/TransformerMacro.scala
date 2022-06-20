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

import org.bitlap.common.{ Transformer => BitlapTransformer }
import scala.collection.mutable
import scala.reflect.macros.whitebox

/** @author
 *    梦境迷离
 *  @version 1.0,6/15/22
 */
class TransformerMacro(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {
  import c.universe._

  protected val packageName         = q"_root_.org.bitlap.common"
  private val builderFunctionPrefix = "_TransformableFunction$"
  private val annoBuilderPrefix     = "_AnonObjectTransformable$"
  private val fromTermName          = TermName("from")

  def mapFieldWithValueImpl[From, To, FromField, ToField](
    selectFromField: Expr[From => FromField],
    selectToField: Expr[To => ToField],
    map: Expr[FromField => ToField]
  ): Expr[Transformable[From, To]] = {
    val Function(_, Select(_, fromName)) = selectFromField.tree
    val Function(_, Select(_, toName))   = selectToField.tree
    val builderId                        = getBuilderId(annoBuilderPrefix)
    MacroCache.classFieldNameMapping
      .getOrElseUpdate(builderId, mutable.Map.empty)
      .update(toName.decodedName.toString, fromName.decodedName.toString)
    MacroCache.classFieldValueMapping
      .getOrElseUpdate(builderId, mutable.Map.empty)
      .update(toName.decodedName.toString, map)
    val tree = q"new ${c.prefix.actualType}"
    exprPrintTree[Transformable[From, To]](force = false, tree)
  }

  def mapFieldImpl[From, To, FromField, ToField](
    selectFromField: Expr[From => FromField],
    selectToField: Expr[To => ToField]
  ): Expr[Transformable[From, To]] = {
    val Function(_, Select(_, fromName)) = selectFromField.tree
    val Function(_, Select(_, toName))   = selectToField.tree
    val builderId                        = getBuilderId(annoBuilderPrefix)
    MacroCache.classFieldNameMapping
      .getOrElseUpdate(builderId, mutable.Map.empty)
      .update(toName.decodedName.toString, fromName.decodedName.toString)

    val tree = q"new ${c.prefix.actualType}"
    exprPrintTree[Transformable[From, To]](force = false, tree)
  }

  def instanceImpl[From: WeakTypeTag, To: WeakTypeTag]: Expr[BitlapTransformer[From, To]] = {
    val fromClassName = resolveClassTypeName[From]
    val toClassName   = resolveClassTypeName[To]
    val tree = q"""
       ..$getPreTree  
       new $packageName.Transformer[$fromClassName, $toClassName] {
          override def transform($fromTermName: $fromClassName): $toClassName = {
            ${getTransformBody[From, To]}
          }
      }
     """
    exprPrintTree[BitlapTransformer[From, To]](force = false, tree)
  }

  def applyImpl[From: WeakTypeTag, To: WeakTypeTag]: Expr[Transformable[From, To]] =
    deriveTransformableApplyImpl[From, To]

  private def deriveTransformableApplyImpl[From: WeakTypeTag, To: WeakTypeTag]: Expr[Transformable[From, To]] = {
    val builderClassName = TypeName(annoBuilderPrefix + MacroCache.getBuilderId)
    val fromClassName    = resolveClassTypeName[From]
    val toClassName      = resolveClassTypeName[To]

    val tree =
      q"""
        class $builderClassName extends $packageName.Transformable[$fromClassName, $toClassName]
        new $builderClassName
       """
    exprPrintTree[Transformable[From, To]](force = false, tree)
  }

  private def getPreTree: Iterable[Tree] = {
    val customTrees = MacroCache.classFieldValueMapping.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)
    val (_, preTrees) = customTrees.collect { case (key, expr: Expr[Tree] @unchecked) =>
      expr.tree match {
        case buildFunction: Function =>
          val functionName = TermName(builderFunctionPrefix + key)
          key -> q"lazy val $functionName: ${buildFunction.tpe} = $buildFunction"
      }
    }.unzip
    preTrees
  }

  private def getTransformBody[From: WeakTypeTag, To: WeakTypeTag]: Tree = {
    val toClassName   = resolveClassTypeName[To]
    val fromClassName = resolveClassTypeName[From]
    val toClassInfo   = getCaseClassFieldInfo[To]()
    val fromClassInfo = getCaseClassFieldInfo[From]()
    if (fromClassInfo.size < toClassInfo.size) {
      c.abort(
        c.enclosingPosition,
        s"From type: `$fromClassName` has fewer fields than To type: `$toClassName` and cannot be transformed"
      )
    }

    val customFieldNameMapping =
      MacroCache.classFieldNameMapping.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)
    val customFieldValueMapping =
      MacroCache.classFieldValueMapping.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)
    c.info(c.enclosingPosition, s"Field Name Mapping:$customFieldNameMapping", force = true)
    c.info(c.enclosingPosition, s"Field Value Mapping:$customFieldValueMapping", force = true)
    val fields = toClassInfo.map { field =>
      val fromFieldName     = customFieldNameMapping.get(field.fieldName)
      val realFromFieldName = fromFieldName.fold(field.fieldName)(x => x)
      if (customFieldValueMapping.contains(field.fieldName)) {
        q"""${TermName(builderFunctionPrefix + field.fieldName)}.apply(${q"$fromTermName.${TermName(realFromFieldName)}"})"""
      } else {
        checkFieldGetFieldTerm[From](
          realFromFieldName,
          fromClassInfo.find(_.fieldName == realFromFieldName),
          field
        )
      }
    }
    q"""
       ${toClassName.toTermName}.apply(
          ..$fields
       )
     """
  }

  private def checkFieldGetFieldTerm[From: WeakTypeTag](
    realFromFieldName: String,
    fromFieldOpt: Option[FieldInformation],
    toField: FieldInformation
  ): Tree = {
    val fromFieldTerm = q"$fromTermName.${TermName(realFromFieldName)}"
    val fromClassName = resolveClassTypeName[From]

    if (fromFieldOpt.isEmpty) {
      c.abort(
        c.enclosingPosition,
        s"value `$realFromFieldName` is not a member of `$fromClassName`, Please consider using `mapField` method!"
      )
      return fromFieldTerm
    }

    val fromField = fromFieldOpt.get
    if (!(fromField.fieldType weak_<:< toField.fieldType)) {
      tryForWrapType(fromFieldTerm, fromField, toField)
    } else {
      fromFieldTerm
    }
  }

  private def tryForWrapType(fromFieldTerm: Tree, fromField: FieldInformation, toField: FieldInformation): Tree =
    (fromField, toField) match {
      case (
            FieldInformation(_, fromFieldType, collectionsFlags1, genericType1),
            FieldInformation(_, toFieldType, collectionsFlags2, genericType2)
          )
          if ((collectionsFlags1.isSeq && collectionsFlags2.isSeq) ||
            (collectionsFlags1.isList && collectionsFlags2.isList) ||
            (collectionsFlags1.isSet && collectionsFlags2.isSet) ||
            (collectionsFlags1.isVector && collectionsFlags2.isVector) ||
            (collectionsFlags1.isOption && collectionsFlags2.isOption))
            && genericType1.nonEmpty && genericType2.nonEmpty =>
        q"""
            $packageName.Transformer[$fromFieldType, $toFieldType].transform($fromFieldTerm)
         """
      case (information1, information2) =>
        c.warning(
          c.enclosingPosition,
          s"No implicit `Transformer` is defined for ${information1.fieldType} => ${information2.fieldType}, which may cause compilation errors!!!"
        )
        q"""$packageName.Transformer[${information1.fieldType}, ${information2.fieldType}].transform($fromFieldTerm)"""
    }

}
