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

  protected val packageName = q"_root_.org.bitlap.common"

  private val annoBuilderPrefix = "_AnonObjectCTransformerBuilder$"
  private val inTermName        = TermName("in")

  def mapFieldTypeImpl[In: WeakTypeTag, Out: WeakTypeTag, InF: WeakTypeTag, OutF: WeakTypeTag](
    outField: Expr[Out => OutF],
    value: Expr[InF => OutF]
  ): Expr[CTransformerBuilder[In, Out]] = {
    val Function(_, Select(_, outName)) = outField.tree
    val builderId                       = getBuilderId(annoBuilderPrefix)
    MacroCache.classFieldTypeMapping
      .getOrElseUpdate(builderId, mutable.Map.empty)
      .update(outName.decodedName.toString, value)
    val tree = q"new ${c.prefix.actualType}"
    exprPrintTree[CTransformerBuilder[In, Out]](force = false, tree)
  }

  def mapFieldNameImpl[In: WeakTypeTag, Out: WeakTypeTag, InF: WeakTypeTag, OutF: WeakTypeTag](
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
       new $packageName.CTransformer[$inClassName, $outClassName] {
          override def transform($inTermName: $inClassName): $outClassName = {
            ${getCCTransformBody[In, Out]}
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

  private def getCCTransformBody[In: WeakTypeTag, Out: WeakTypeTag]: Tree = {
    val outClassName = resolveClassTypeName[Out]
    val outClassInfo = getCaseClassFieldInfo[Out]()
    val customFieldMapping =
      MacroCache.classFieldNameMapping.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)
    c.info(c.enclosingPosition, s"Fields Mapping:$customFieldMapping", force = true)
    val fields = outClassInfo.map { field =>
      val inFieldName   = customFieldMapping.get(field.fieldName)
      val realFieldName = inFieldName.fold(field.fieldName)(x => x)
      q"$inTermName.${TermName(realFieldName)}"
    }
    q"""
       ${outClassName.toTermName}.apply(
          ..$fields
       )
     """
  }
}
