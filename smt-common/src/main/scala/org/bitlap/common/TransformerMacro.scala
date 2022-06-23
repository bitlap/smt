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

  import scala.collection.immutable

  protected val packageName              = q"_root_.org.bitlap.common"
  private val builderFunctionPrefix      = "_TransformableFunction$"
  private val builderDefaultValuePrefix$ = "_TransformableDefaultValue$"
  private val annoBuilderPrefix          = "_AnonObjectTransformable$"
  private val fromTermName               = TermName("from")

  def mapTypeImpl[From, To, FromField, ToField](
    selectFromField: Expr[From => FromField],
    map: Expr[FromField => ToField]
  ): Expr[Transformable[From, To]] = {
    val Function(_, Select(_, fromName)) = selectFromField.tree
    val builderId                        = getBuilderId(annoBuilderPrefix)
    MacroCache.classFieldTypeMapping
      .getOrElseUpdate(builderId, mutable.Map.empty)
      .update(fromName.decodedName.toString, map)
    val tree = q"new ${c.prefix.actualType}"
    exprPrintTree[Transformable[From, To]](force = false, tree)
  }

  def setDefaultValueImpl[From, To, ToField](
    selectToField: Expr[To => ToField],
    defaultValue: Expr[ToField]
  ): Expr[Transformable[From, To]] = {
    val Function(_, Select(_, toName)) = selectToField.tree
    val builderId                      = getBuilderId(annoBuilderPrefix)
    MacroCache.classFieldDefaultValueMapping
      .getOrElseUpdate(builderId, mutable.Map.empty)
      .update(toName.decodedName.toString, defaultValue)
    val tree = q"new ${c.prefix.actualType}"
    exprPrintTree[Transformable[From, To]](force = false, tree)
  }

  def mapNameImpl[From, To, FromField, ToField](
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
    val tree =
      q"""
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
    val customFunctionTrees = buildPreTrees(
      MacroCache.classFieldTypeMapping.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)
    )
    val customDefaultValueTrees = buildPreTrees(
      MacroCache.classFieldDefaultValueMapping.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)
    )

    customFunctionTrees ++ customDefaultValueTrees
  }

  private def buildPreTrees(mapping: mutable.Map[String, Any]): Iterable[Tree] = {
    val (_, preTrees) = mapping.collect { case (key, expr: Expr[Tree] @unchecked) =>
      val wrapName = (prefix: String) => TermName(prefix + key)
      expr.tree match {
        case function: Function =>
          key -> q"lazy val ${wrapName(builderFunctionPrefix)}: ${function.tpe} = $function"
        case tree: Tree =>
          key -> q"lazy val ${wrapName(builderDefaultValuePrefix$)} = $tree"
      }
    }.unzip

    preTrees
  }

  private def getTransformBody[From: WeakTypeTag, To: WeakTypeTag]: Tree = {
    val toClassName   = resolveClassTypeName[To]
    val fromClassName = resolveClassTypeName[From]
    val toClassInfo   = getCaseClassFieldInfo[To]()
    val fromClassInfo = getCaseClassFieldInfo[From]()
    val customDefaultValueMapping =
      MacroCache.classFieldDefaultValueMapping.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)
    val customFieldNameMapping =
      MacroCache.classFieldNameMapping.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)
    val customFieldTypeMapping =
      MacroCache.classFieldTypeMapping.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)

    c.info(c.enclosingPosition, s"Field default value mapping: $customDefaultValueMapping", force = true)
    c.info(c.enclosingPosition, s"Field name mapping: $customFieldNameMapping", force = true)
    c.info(c.enclosingPosition, s"Field type mapping: $customFieldTypeMapping", force = true)

    val missingFields             = toClassInfo.map(_.fieldName).filterNot(fromClassInfo.map(_.fieldName).contains)
    val missingExcludeMappingName = missingFields.filterNot(customFieldNameMapping.contains)
    if (missingExcludeMappingName.nonEmpty) {
      val noDefaultValueFields = missingExcludeMappingName.filterNot(customDefaultValueMapping.keySet.contains)
      if (noDefaultValueFields.nonEmpty) {
        c.abort(
          c.enclosingPosition,
          s"From type: `$fromClassName` has fewer fields than To type: `$toClassName` and cannot be transformed!" +
            s"\nMissing field mapping: `$fromClassName`.? => `$toClassName`.`${missingExcludeMappingName.mkString(",")}`." +
            s"\nPlease consider using `setName` or `setDefaultValue` method for `$toClassName`.${missingExcludeMappingName
                .mkString(",")}!"
        )
      }
    }

    val fields = toClassInfo.map { field =>
      val fromFieldName   = customFieldNameMapping.get(field.fieldName)
      val realToFieldName = fromFieldName.fold(field.fieldName)(x => x)
      // scalafmt: { maxColumn = 400 }
      fromFieldName match {
        case Some(fromName) if customFieldTypeMapping.contains(fromName) =>
          q"""${TermName(field.fieldName)} = ${TermName(builderFunctionPrefix + fromName)}.apply(${q"$fromTermName.${TermName(realToFieldName)}"})"""
        case None if customFieldTypeMapping.contains(field.fieldName) =>
          q"""${TermName(field.fieldName)} = ${TermName(builderFunctionPrefix + field.fieldName)}.apply(${q"$fromTermName.${TermName(realToFieldName)}"})"""
        case _ =>
          checkFieldGetFieldTerm[From](
            realToFieldName,
            fromClassInfo.find(_.fieldName == realToFieldName),
            field,
            customDefaultValueMapping
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
    toField: FieldInformation,
    customDefaultValueMapping: mutable.Map[String, Any]
  ): Tree = {
    val fromFieldTerm = q"$fromTermName.${TermName(realFromFieldName)}"
    val fromClassName = resolveClassTypeName[From]

    if (fromFieldOpt.isEmpty && !customDefaultValueMapping.keySet.contains(toField.fieldName)) {
      c.abort(
        c.enclosingPosition,
        s"The value `$realFromFieldName` is not a member of `$fromClassName`!" +
          s"\nPlease consider using `setDefaultValue` method!"
      )
      return fromFieldTerm
    }

    fromFieldOpt match {
      case Some(fromField) if !(fromField.fieldType weak_<:< toField.fieldType) =>
        tryForWrapType(fromFieldTerm, fromField, toField)
      case Some(fromField) if fromField.fieldType weak_<:< toField.fieldType =>
        q"${TermName(toField.fieldName)} = $fromFieldTerm"
      case _ =>
        val value = q"""${TermName(builderDefaultValuePrefix$ + toField.fieldName)}"""
        q"${TermName(toField.fieldName)} = $value"

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
        // scalafmt: { maxColumn = 400 }
        q"""
            ${TermName(toField.fieldName)} = $packageName.Transformer[$fromFieldType, $toFieldType].transform($fromFieldTerm)
         """
      case (information1, information2) =>
        c.warning(
          c.enclosingPosition,
          s"No implicit `Transformer` is defined for ${information1.fieldType} => ${information2.fieldType}, which may cause compilation errors!!!" +
            s"\nPlease consider using `setType` method, or define an `Transformer[${information1.fieldType}, ${information2.fieldType}]` implicit !"
        )
        // scalafmt: { maxColumn = 400 }
        q"""${TermName(toField.fieldName)} = $packageName.Transformer[${information1.fieldType}, ${information2.fieldType}].transform($fromFieldTerm)"""
    }

}
