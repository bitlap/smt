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

package org.bitlap.common
import org.bitlap.common.internal.TransformerMacro

/** @author
 *    梦境迷离
 *  @version 1.0,6/15/22
 */
class Transformable[From, To] {

  /** Sets the `From` to `To` mapping relationship of the field type.
   *
   *  When the map function returns a known constant value, it means that the type mapping becomes a set value.
   *
   *  @param selectFromField
   *    Select the name of the field to be mapped in the `From` class.
   *  @param map
   *    Specify the type mapping of the field, which must be provided when the type is incompatible, or else attempt to
   *    search for an implicit `Transformer[FromField, ToField]` (a failed search will result in a compile failure).
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

  /** Sets the `From` to `To` mapping relationship of the field name.
   *
   *  @param selectFromField
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

  /** Defines a default value for missing field to successfully create `To` object. This method has a higher priority
   *  than `enableOptionDefaultsToNone` or `enableCollectionDefaultsToEmpty`.
   *
   *  So, even if `enableCollectionDefaultsToEmpty` or `enableCollectionDefaultsToEmpty`, you can also use
   *  `setDefaultValue` method to set the initial value for a single field.
   */
  def setDefaultValue[ToField](selectToField: To => ToField, defaultValue: ToField): Transformable[From, To] =
    macro TransformerMacro.setDefaultValueImpl[From, To, ToField]

  /** Sets target value of optional fields to `None` if field is missing from source type `From`.
   */
  def enableOptionDefaultsToNone: Transformable[From, To] =
    macro TransformerMacro.enableOptionDefaultsToNoneImpl[From, To]

  /** Sets target value of collection fields to `empty` if field is missing from source type `From`.
   */
  def enableCollectionDefaultsToEmpty: Transformable[From, To] =
    macro TransformerMacro.enableCollectionDefaultsToEmptyImpl[From, To]

  /** Disable `None` fallback value for optional fields in `To`. This is the default configuration option.
   */
  def disableOptionDefaultsToNone: Transformable[From, To] =
    macro TransformerMacro.disableOptionDefaultsToNoneImpl[From, To]

  /** Disable `empty` fallback value for collection fields in `To`. Support List, Seq, Vector, Set. This is the default
   *  configuration option.
   */
  def disableCollectionDefaultsToEmpty: Transformable[From, To] =
    macro TransformerMacro.disableCollectionDefaultsToEmptyImpl[From, To]

  def instance: Transformer[From, To] = macro TransformerMacro.instanceImpl[From, To]

}

object Transformable {

  /** Automatically derive `Transformable[From, To]` for case classes only, for non-case classes you should use the
   *  `setType` method to configure the mapping relationship.
   *
   *  @tparam From
   *  @tparam To
   *  @return
   */
  def apply[From <: Product, To <: Product]: Transformable[From, To] =
    macro TransformerMacro.applyImpl[From, To]
}
