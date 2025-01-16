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

import org.bitlap.common.{ MacroCache, Options, Transformable, Transformer => BitlapTransformer }

import scala.collection.mutable
import scala.reflect.macros.whitebox

/** @author
 *    梦境迷离
 *  @version 1.0,6/15/22
 */
class TransformerMacro(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

  import c.universe._

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
    c.Expr[Transformable[From, To]](tree)
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
    c.Expr[Transformable[From, To]](tree)
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
    c.Expr[Transformable[From, To]](tree)
  }

  def enableOptionDefaultsToNoneImpl[From, To] =
    setOptions[From, To](
      MacroCache.transformerOptionsMapping
        .getOrElseUpdate(_, mutable.Set.empty)
        .add(Options.enableOptionDefaultsToNone),
      MacroCache.transformerOptionsMapping
        .getOrElseUpdate(_, mutable.Set.empty)
        .remove(Options.disableOptionDefaultsToNone)
    )

  def enableCollectionDefaultsToEmptyImpl[From, To] =
    setOptions[From, To](
      MacroCache.transformerOptionsMapping
        .getOrElseUpdate(_, mutable.Set.empty)
        .add(Options.enableCollectionDefaultsToEmpty),
      MacroCache.transformerOptionsMapping
        .getOrElseUpdate(_, mutable.Set.empty)
        .remove(Options.disableCollectionDefaultsToEmpty)
    )

  def disableOptionDefaultsToNoneImpl[From, To] =
    setOptions[From, To](
      MacroCache.transformerOptionsMapping
        .getOrElseUpdate(_, mutable.Set.empty)
        .add(Options.disableOptionDefaultsToNone),
      MacroCache.transformerOptionsMapping
        .getOrElseUpdate(_, mutable.Set.empty)
        .remove(Options.enableCollectionDefaultsToEmpty)
    )

  def disableCollectionDefaultsToEmptyImpl[From, To] =
    setOptions[From, To](
      MacroCache.transformerOptionsMapping
        .getOrElseUpdate(_, mutable.Set.empty)
        .add(Options.disableCollectionDefaultsToEmpty),
      MacroCache.transformerOptionsMapping
        .getOrElseUpdate(_, mutable.Set.empty)
        .remove(Options.enableCollectionDefaultsToEmpty)
    )

  private def setOptions[From, To](enable: Int => Unit, disable: Int => Unit): Expr[Transformable[From, To]] = {
    val builderId = getBuilderId(annoBuilderPrefix)
    enable(builderId)
    disable(builderId)
    val tree = q"new ${c.prefix.actualType}"
    c.Expr[Transformable[From, To]](tree)
  }

  def instanceImpl[From: WeakTypeTag, To: WeakTypeTag]: Expr[BitlapTransformer[From, To]] = {
    val fromClassName = classTypeName[From]
    val toClassName   = classTypeName[To]
    val tree =
      q"""
       ..$getPreTree  
       new $packageName.Transformer[$fromClassName, $toClassName] {
          override def transform($fromTermName: $fromClassName): $toClassName = {
            ${getTransformBody[From, To]}
          }
      }
     """
    c.Expr[BitlapTransformer[From, To]](tree)
  }

  def applyImpl[From: WeakTypeTag, To: WeakTypeTag]: Expr[Transformable[From, To]] =
    deriveTransformableApplyImpl[From, To]

  private def deriveTransformableApplyImpl[From: WeakTypeTag, To: WeakTypeTag]: Expr[Transformable[From, To]] = {
    val builderClassName = TypeName(annoBuilderPrefix + MacroCache.getBuilderId)
    val fromClassName    = classTypeName[From]
    val toClassName      = classTypeName[To]

    val tree =
      q"""
        class $builderClassName extends $packageName.Transformable[$fromClassName, $toClassName]
        new $builderClassName
       """
    c.Expr[Transformable[From, To]](tree)
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
    val toClassName   = classTypeName[To]
    val toClassInfo   = caseClassFieldInfos[To]()
    val fromClassInfo = caseClassFieldInfos[From]()
    val customFieldNameMapping =
      MacroCache.classFieldNameMapping.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)
    val customFieldTypeMapping =
      MacroCache.classFieldTypeMapping.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)

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
          checkForNoMappingField[From, To](
            realToFieldName,
            fromClassInfo.find(_.fieldName == realToFieldName),
            field,
            customFieldNameMapping
          )
      }
    }.filterNot(_ == EmptyTree)
    q"""
       ${toClassName.toTermName}.apply(
          ..$fields
       )
     """
  }

  private def checkForNoMappingField[From: WeakTypeTag, To: WeakTypeTag](
      realFromFieldName: String,
      fromFieldOpt: Option[FieldInformation],
      toField: FieldInformation,
      customFieldNameMapping: mutable.Map[String, String]
  ): Tree = {
    val toClassInfo   = caseClassFieldInfos[To]()
    val fromClassInfo = caseClassFieldInfos[From]()

    val customOptionsMapping =
      MacroCache.transformerOptionsMapping.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Set.empty)
    val customDefaultValueMapping =
      MacroCache.classFieldDefaultValueMapping.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)

    val fromFieldTerm = q"$fromTermName.${TermName(realFromFieldName)}"
    fromFieldOpt match {
      case Some(fromField) if !(fromField.fieldType weak_<:< toField.fieldType) =>
        tryForCollectionType(fromFieldTerm, fromField, toField)
      case Some(fromField) if fromField.fieldType weak_<:< toField.fieldType =>
        q"${TermName(toField.fieldName)} = $fromFieldTerm"
      case None if customDefaultValueMapping.keySet.contains(toField.fieldName) =>
        val value = q"""${TermName(builderDefaultValuePrefix$ + toField.fieldName)}"""
        q"${TermName(toField.fieldName)} = $value"
      case _ =>
        val isStrictCollection = toField.collectionFlags.isStrictCollection && customOptionsMapping.contains(Options.enableCollectionDefaultsToEmpty)
        val isOption           = toField.collectionFlags.isOption && customOptionsMapping.contains(Options.enableOptionDefaultsToNone)
        if (isStrictCollection || isOption) {
          q"${TermName(toField.fieldName)} = ${getZeroValue(toField.fieldType)}"
        } else {
          if (!toField.hasDefaultValue) {
            checkMissingFields[From, To](
              fromClassInfo,
              toClassInfo,
              customDefaultValueMapping,
              customFieldNameMapping,
              customOptionsMapping
            )
            EmptyTree
          } else {
            EmptyTree
          }
        }

    }
  }

  private def tryForCollectionType(fromFieldTerm: Tree, fromField: FieldInformation, toField: FieldInformation): Tree =
    (fromField, toField) match {
      case (
            FieldInformation(_, fromFieldType, collectionsFlags1, genericType1, _, _),
            FieldInformation(_, toFieldType, collectionsFlags2, genericType2, _, _)
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
        // scalafmt: { maxColumn = 400 }
        q"""${TermName(toField.fieldName)} = $packageName.Transformer[${information1.fieldType}, ${information2.fieldType}].transform($fromFieldTerm)"""
    }

  private def checkMissingFields[From: WeakTypeTag, To: WeakTypeTag](
      fromClassInfo: List[FieldInformation],
      toClassInfo: List[FieldInformation],
      customDefaultValueMapping: mutable.Map[String, Any],
      customFieldNameMapping: mutable.Map[String, String],
      customOptionsMapping: mutable.Set[Options]
  ) = {
    val toClassName               = classTypeName[To]
    val fromClassName             = classTypeName[From]
    val missingFields             = toClassInfo.filterNot(t => fromClassInfo.map(_.fieldName).contains(t.fieldName))
    val missingExcludeMappingName = missingFields.filterNot(m => customFieldNameMapping.contains(m.fieldName))
    if (missingExcludeMappingName.nonEmpty) {
      val noDefaultValueFields = missingExcludeMappingName
        .filterNot(m => customDefaultValueMapping.keySet.contains(m.fieldName))
        .filterNot(_.hasDefaultValue)
      if (noDefaultValueFields.nonEmpty) {
        // scalafmt: { maxColumn = 400 }
        val needHandleFields = (if (customOptionsMapping.contains(Options.enableOptionDefaultsToNone)) {
                                  noDefaultValueFields.filterNot(_.collectionFlags.isOption)
                                } else if (customOptionsMapping.contains(Options.enableCollectionDefaultsToEmpty)) {
                                  noDefaultValueFields.filterNot(_.collectionFlags.isStrictCollection)
                                } else {
                                  noDefaultValueFields
                                }).map(_.fieldName)
        c.abort(
          c.enclosingPosition,
          s"Missing field mapping: `$fromClassName`.? => `$toClassName`.[${needHandleFields.mkString(",")}]." +
            s"\nPlease consider using `setName`、`setDefaultValue` or `enable*` methods for `$toClassName`.[${needHandleFields.mkString(",")}]!"
        )
      }
    }
  }
}
