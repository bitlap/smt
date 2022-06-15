package org.bitlap.common

/** @author
 *    梦境迷离
 *  @version 1.0,6/15/22
 */
class CTransformerBuilder[In, Out] {

  def mapValue[InF, OutF](outField: Out => OutF, value: InF => OutF): CTransformerBuilder[In, Out] =
    macro CTransformerMacro.mapFieldTypeImpl[In, Out, InF, OutF]

  def mapName[InF, OutF](inField: In => InF, outField: Out => OutF): CTransformerBuilder[In, Out] =
    macro CTransformerMacro.mapFieldNameImpl[In, Out, InF, OutF]

  def build: CTransformer[In, Out] = macro CTransformerMacro.buildImpl[In, Out]

}
object CTransformerBuilder {
  def apply[In <: Product, Out <: Product]: CTransformerBuilder[In, Out] =
    macro CTransformerMacro.applyImpl[In, Out]
}
