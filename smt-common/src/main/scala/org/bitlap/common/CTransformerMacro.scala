package org.bitlap.common

import org.bitlap.common.{ CTransformer => _ }
import scala.collection.mutable
import scala.reflect.macros.whitebox

/** @author
 *    梦境迷离
 *  @version 1.0,6/15/22
 */
class CTransformerMacro(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {
  import c.universe._

  protected val packageName         = q"_root_.org.bitlap.common"
  private val builderFunctionPrefix = "_CTransformerBuilderFunction$"
  private val annoBuilderPrefix     = "_AnonObjectCTransformerBuilder$"
  private val fromTermName          = TermName("from")

  def mapValueImpl[From: WeakTypeTag, To: WeakTypeTag, FromField: WeakTypeTag, ToField: WeakTypeTag](
    selectFromField: Expr[From => FromField],
    selectToField: Expr[To => ToField],
    map: Expr[FromField => ToField]
  ): Expr[CTransformerBuilder[From, To]] = {
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
    exprPrintTree[CTransformerBuilder[From, To]](force = false, tree)
  }

  def mapNameImpl[From: WeakTypeTag, To: WeakTypeTag, FromField: WeakTypeTag, ToField: WeakTypeTag](
    selectFromField: Expr[From => FromField],
    selectToField: Expr[To => ToField]
  ): Expr[CTransformerBuilder[From, To]] = {
    val Function(_, Select(_, fromName)) = selectFromField.tree
    val Function(_, Select(_, toName))   = selectToField.tree
    val builderId                        = getBuilderId(annoBuilderPrefix)
    MacroCache.classFieldNameMapping
      .getOrElseUpdate(builderId, mutable.Map.empty)
      .update(toName.decodedName.toString, fromName.decodedName.toString)

    val tree = q"new ${c.prefix.actualType}"
    exprPrintTree[CTransformerBuilder[From, To]](force = false, tree)
  }

  def buildImpl[From: WeakTypeTag, To: WeakTypeTag]: Expr[CTransformer[From, To]] = {
    val fromClassName = resolveClassTypeName[From]
    val toClassName   = resolveClassTypeName[To]
    val tree = q"""
       ..$getPreTree  
       new $packageName.CTransformer[$fromClassName, $toClassName] {
          override def transform($fromTermName: $fromClassName): $toClassName = {
            ${getCTransformBody[From, To]}
          }
      }
     """
    exprPrintTree[CTransformer[From, To]](force = false, tree)
  }

  def applyImpl[From: WeakTypeTag, To: WeakTypeTag]: Expr[CTransformerBuilder[From, To]] =
    deriveBuilderApplyImpl[From, To]

  private def deriveBuilderApplyImpl[From: WeakTypeTag, To: WeakTypeTag]: Expr[CTransformerBuilder[From, To]] = {
    val builderClassName = TypeName(annoBuilderPrefix + MacroCache.getBuilderId)
    val fromClassName    = resolveClassTypeName[From]
    val toClassName      = resolveClassTypeName[To]

    val tree =
      q"""
        class $builderClassName extends $packageName.CTransformerBuilder[$fromClassName, $toClassName]
        new $builderClassName
       """
    exprPrintTree[CTransformerBuilder[From, To]](force = false, tree)
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

  private def getCTransformBody[From: WeakTypeTag, To: WeakTypeTag]: Tree = {
    val toClassName   = resolveClassTypeName[To]
    val toClassInfo   = getCaseClassFieldInfo[To]()
    val fromClassInfo = getCaseClassFieldInfo[From]()
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
        checkField[From, To](fromClassInfo.find(_.fieldName == realFromFieldName), field)
        q"$fromTermName.${TermName(realFromFieldName)}"
      }

    }
    q"""
       ${toClassName.toTermName}.apply(
          ..$fields
       )
     """
  }

  private def checkField[From: WeakTypeTag, To: WeakTypeTag](
    fromFieldOpt: Option[FieldInformation],
    toField: FieldInformation
  ): Unit =
    if (fromFieldOpt.nonEmpty) {
      val fromField = fromFieldOpt.get
      if (!(fromField.fieldType <:< toField.fieldType)) {
        val fromClassName = resolveClassTypeName[From]
        val toClassName   = resolveClassTypeName[To]
        c.abort(
          c.enclosingPosition,
          s"The field `${fromField.fieldName}` type of class `$fromClassName` should the same as field `${toField.fieldName}` type of class `$toClassName`," +
            s" or Assign `${fromField.fieldName}` field to `${toField.fieldName}` field should be compatible, Please consider using three parameters of the `mapField` method"
        )
      }
    }
}
