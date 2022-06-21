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
class Transformable[From, To] {

  /** @param selectFromField
   *    Select the name of the field to be mapped in the `From` class.
   *  @param map
   *    Specify the type mapping of the field, which must be provided when the type is incompatible, or else attempt to
   *    search for an implicit `Transformer[FromField, ToField]` (a failed search will result in a compile failure).
   *
   *  @tparam FromField
   *    field type
   *  @tparam ToField
   *    field type
   *  @return
   *    Transformable
   */
  @unchecked
  def setType[FromField, ToField](
    selectFromField: From => FromField,
    map: FromField => ToField
  ): Transformable[From, To] =
    macro TransformerMacro.mapTypeImpl[From, To, FromField, ToField]

  /** @param selectFromField
   *    Select the name of the field to be mapped in the `From` class.
   *  @param selectToField
   *    Select the name of the field to be mapped in the `To` class.
   *
   *  @tparam FromField
   *    field type
   *  @tparam ToField
   *    field type
   *  @return
   *    Transformable
   */
  @unchecked
  def setName[FromField, ToField](
    selectFromField: From => FromField,
    selectToField: To => ToField
  ): Transformable[From, To] =
    macro TransformerMacro.mapNameImpl[From, To, FromField, ToField]

  def instance: Transformer[From, To] = macro TransformerMacro.instanceImpl[From, To]

}

object Transformable {

  /** Automatically derive `Transformable[From, To]` for case classes only, for non-case classes you should use the
   *  `setType` method to configure the mapping relationship.
   *  @tparam From
   *  @tparam To
   *  @return
   */
  def apply[From <: Product, To <: Product]: Transformable[From, To] =
    macro TransformerMacro.applyImpl[From, To]
}
