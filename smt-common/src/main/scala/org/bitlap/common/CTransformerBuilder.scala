package org.bitlap.common

/** @author
 *    梦境迷离
 *  @version 1.0,6/15/22
 */
class CTransformerBuilder[From, To] {

  @unchecked
  def mapField[FromField, ToField](
    selectFromField: From => FromField,
    selectToField: To => ToField,
    map: FromField => ToField
  ): CTransformerBuilder[From, To] =
    macro CTransformerMacro.mapFieldWithValueImpl[From, To, FromField, ToField]

  @unchecked
  def mapField[FromField, ToField](
    selectFromField: From => FromField,
    selectToField: To => ToField
  ): CTransformerBuilder[From, To] =
    macro CTransformerMacro.mapNameImpl[From, To, FromField, ToField]

  def build: CTransformer[From, To] = macro CTransformerMacro.buildImpl[From, To]

}
object CTransformerBuilder {
  def apply[From <: Product, To <: Product]: CTransformerBuilder[From, To] =
    macro CTransformerMacro.applyImpl[From, To]
}
