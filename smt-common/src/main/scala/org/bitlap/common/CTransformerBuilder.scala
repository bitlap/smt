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
