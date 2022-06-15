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
  private val inTermName            = TermName("in")

  def mapNameWithValueImpl[In: WeakTypeTag, Out: WeakTypeTag, InF: WeakTypeTag, OutF: WeakTypeTag](
    inField: Expr[In => InF],
    outField: Expr[Out => OutF],
    value: Expr[InF => OutF]
  ): Expr[CTransformerBuilder[In, Out]] = {
    val Function(_, Select(_, inName))  = inField.tree
    val Function(_, Select(_, outName)) = outField.tree
    val builderId                       = getBuilderId(annoBuilderPrefix)
    MacroCache.classFieldNameMapping
      .getOrElseUpdate(builderId, mutable.Map.empty)
      .update(outName.decodedName.toString, inName.decodedName.toString)

    MacroCache.classFieldValueMapping
      .getOrElseUpdate(builderId, mutable.Map.empty)
      .update(outName.decodedName.toString, value)
    val tree = q"new ${c.prefix.actualType}"
    exprPrintTree[CTransformerBuilder[In, Out]](force = false, tree)
  }

  def mapNameOnlyImpl[In: WeakTypeTag, Out: WeakTypeTag, InF: WeakTypeTag, OutF: WeakTypeTag](
    inField: Expr[In => InF],
    outField: Expr[Out => OutF]
  ): Expr[CTransformerBuilder[In, Out]] = {
    val Function(_, Select(_, inName))  = inField.tree
    val Function(_, Select(_, outName)) = outField.tree
    val builderId                       = getBuilderId(annoBuilderPrefix)
    MacroCache.classFieldNameMapping
      .getOrElseUpdate(builderId, mutable.Map.empty)
      .update(outName.decodedName.toString, inName.decodedName.toString)

    val tree = q"new ${c.prefix.actualType}"
    exprPrintTree[CTransformerBuilder[In, Out]](force = false, tree)
  }

  def buildImpl[In: WeakTypeTag, Out: WeakTypeTag]: Expr[CTransformer[In, Out]] = {
    val inClassName  = resolveClassTypeName[In]
    val outClassName = resolveClassTypeName[Out]
    val tree = q"""
       ..$getPreTree  
       new $packageName.CTransformer[$inClassName, $outClassName] {
          override def transform($inTermName: $inClassName): $outClassName = {
            ${getCTransformBody[In, Out]}
          }
      }
     """
    exprPrintTree[CTransformer[In, Out]](force = false, tree)
  }

  def applyImpl[In: WeakTypeTag, Out: WeakTypeTag]: Expr[CTransformerBuilder[In, Out]] =
    deriveBuilderApplyImpl[In, Out]

  private def deriveBuilderApplyImpl[In: WeakTypeTag, Out: WeakTypeTag]: Expr[CTransformerBuilder[In, Out]] = {
    val builderClassName = TypeName(annoBuilderPrefix + MacroCache.getBuilderId)
    val inClassName      = resolveClassTypeName[In]
    val outClassName     = resolveClassTypeName[Out]

    val tree =
      q"""
        class $builderClassName extends $packageName.CTransformerBuilder[$inClassName, $outClassName]
        new $builderClassName
       """
    exprPrintTree[CTransformerBuilder[In, Out]](force = false, tree)
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

  private def getCTransformBody[In: WeakTypeTag, Out: WeakTypeTag]: Tree = {
    val outClassName = resolveClassTypeName[Out]
    val outClassInfo = getCaseClassFieldInfo[Out]()
    val customFieldNameMapping =
      MacroCache.classFieldNameMapping.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)
    val customFieldValueMapping =
      MacroCache.classFieldValueMapping.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)
    c.info(c.enclosingPosition, s"Field Name Mapping:$customFieldNameMapping", force = true)
    c.info(c.enclosingPosition, s"Field Value Mapping:$customFieldValueMapping", force = true)
    val fields = outClassInfo.map { field =>
      val inFieldName   = customFieldNameMapping.get(field.fieldName)
      val realFieldName = inFieldName.fold(field.fieldName)(x => x)
      if (customFieldValueMapping.contains(field.fieldName)) {
        val customFunction = () =>
          q"""${TermName(builderFunctionPrefix + field.fieldName)}.apply(${q"$inTermName.${TermName(realFieldName)}"})"""
        q"${customFunction()}"
      } else
        q"$inTermName.${TermName(realFieldName)}"
    }
    q"""
       ${outClassName.toTermName}.apply(
          ..$fields
       )
     """
  }
}
