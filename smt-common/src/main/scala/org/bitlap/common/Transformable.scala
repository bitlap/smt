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
   *  @param selectToField
   *    Selects which field of `To` the `From` will eventually be mapped to.
   *  @param map
   *    Specify the type mapping of the field, which must be provided when the type is incompatible, or else attempt to
   *    search for an implicit `Transformer[FromField, ToField]` (a failed search will result in a compile failure).
   *
   *    1. If the field names are the same and the field types are compatible (`FromField` can be assigned to
   *       `ToField`), then you don't need to use the `mapField` method, and you don't need an implicit
   *       `Transformer[FromField, ToField]`. such as:
   *       {{{
   *    F => F
   *    Option[F] => Option[F]
   *    Seq[F] => Seq[F]
   *    List[F] => List[F]
   *    Int => Long
   *    List[F] => Seq[F]
   *    ...
   *       }}}
   *
   *  2. If field type is of the following types you can use in a more convenient way as long as you define an implicit
   *  `Transformer[F, T]`, If `F` or `T` is not a case class, then you still need to use the `mapField` method input
   *  argument `map`.
   *  {{{
   *    F => T
   *    Option[F] => Option[T]
   *    Seq[F] => Seq[T]
   *    List[F] => List[T]
   *    List[F] => Seq[T]
   *    ...
   *  }}}
   *
   *  3. If field type is of the following types you must use the `mapField` method with input argument `map`:
   *  {{{
   *   Seq[_] => List[_]
   *   List[List[F]] => List[List[T]]           and more nesting.
   *   List[Seq[F]] => List[Seq[T]]             and more nesting.
   *   Seq[Seq[F]] => Seq[Seq[T]]               and more nesting.
   *   Seq[List[F]] => Seq[List[T]]             and more nesting.
   *   Option[Option[F]] => Option[Option[T]]   and more nesting.
   *   List[Option[F]] => List[Option[T]]       and more nesting.
   *   More structures like this not supported at this time.
   *  }}}
   *
   *  Note: They need use the `mapField` method to mapping type:
   *  {{{
   *   Seq[String] => List[String]
   *   Seq[Long] => Seq[Int]
   *   ...
   *  }}}
   *
   *  @tparam FromField
   *    field type
   *  @tparam ToField
   *    field type
   *  @return
   *    Transformable
   */
  @unchecked
  def mapField[FromField, ToField](
    selectFromField: From => FromField,
    selectToField: To => ToField,
    map: FromField => ToField
  ): Transformable[From, To] =
    macro TransformerMacro.mapFieldWithValueImpl[From, To, FromField, ToField]

  /** Same method as above, but without the map parameter. That is, no type mapping needs to be configured.
   */
  @unchecked
  def mapField[FromField, ToField](
    selectFromField: From => FromField,
    selectToField: To => ToField
  ): Transformable[From, To] =
    macro TransformerMacro.mapFieldImpl[From, To, FromField, ToField]

  def instance: Transformer[From, To] = macro TransformerMacro.instanceImpl[From, To]

}

object Transformable {

  /** Automatically derive `Transformable[From, To]` for case classes only, for non-case classes you should use the
   *  `mapField` method to configure the mapping relationship.
   *  @tparam From
   *  @tparam To
   *  @return
   */
  def apply[From <: Product, To <: Product]: Transformable[From, To] =
    macro TransformerMacro.applyImpl[From, To]
}
