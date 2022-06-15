package org.bitlap.common

/** @author
 *    梦境迷离
 *  @version 1.0,6/15/22
 */
class CTransformerBuilder[In, Out] {

  def mapField[InF, OutF](inField: In => InF, outField: Out => OutF, value: InF => OutF): CTransformerBuilder[In, Out] =
    macro CTransformerMacro.mapNameWithValueImpl[In, Out, InF, OutF]

  def mapField[InF, OutF](inField: In => InF, outField: Out => OutF): CTransformerBuilder[In, Out] =
    macro CTransformerMacro.mapNameOnlyImpl[In, Out, InF, OutF]

  def build: CTransformer[In, Out] = macro CTransformerMacro.buildImpl[In, Out]

}
object CTransformerBuilder {
  def apply[In <: Product, Out <: Product]: CTransformerBuilder[In, Out] =
    macro CTransformerMacro.applyImpl[In, Out]
}
