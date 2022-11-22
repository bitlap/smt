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

package org.bitlap.common.jdbc

/** @author
 *    梦境迷离
 *  @version 1.0,2022/10/21
 */
trait GenericRow                                                                     extends Product with Serializable
final case class GenericRow1[T1](col1: T1)                                           extends GenericRow
final case class GenericRow2[T1, T2](col1: T1, col2: T2)                             extends GenericRow
final case class GenericRow3[T1, T2, T3](col1: T1, col2: T2, col3: T3)               extends GenericRow
final case class GenericRow4[T1, T2, T3, T4](col1: T1, col2: T2, col3: T3, col4: T4) extends GenericRow
final case class GenericRow5[T1, T2, T3, T4, T5](col1: T1, col2: T2, col3: T3, col4: T4, col5: T5) extends GenericRow
final case class GenericRow6[T1, T2, T3, T4, T5, T6](
  col1: T1,
  col2: T2,
  col3: T3,
  col4: T4,
  col5: T5,
  col6: T6
) extends GenericRow
final case class GenericRow7[T1, T2, T3, T4, T5, T6, T7](
  col1: T1,
  col2: T2,
  col3: T3,
  col4: T4,
  col5: T5,
  col6: T6,
  col7: T7
) extends GenericRow
final case class GenericRow8[T1, T2, T3, T4, T5, T6, T7, T8](
  col1: T1,
  col2: T2,
  col3: T3,
  col4: T4,
  col5: T5,
  col6: T6,
  col7: T7,
  col8: T8
) extends GenericRow
final case class GenericRow9[T1, T2, T3, T4, T5, T6, T7, T8, T9](
  col1: T1,
  col2: T2,
  col3: T3,
  col4: T4,
  col5: T5,
  col6: T6,
  col7: T7,
  col8: T8,
  col9: T9
) extends GenericRow
final case class GenericRow10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](
  col1: T1,
  col2: T2,
  col3: T3,
  col4: T4,
  col5: T5,
  col6: T6,
  col7: T7,
  col8: T8,
  col9: T9,
  col10: T10
) extends GenericRow
final case class GenericRow11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](
  col1: T1,
  col2: T2,
  col3: T3,
  col4: T4,
  col5: T5,
  col6: T6,
  col7: T7,
  col8: T8,
  col9: T9,
  col10: T10,
  col11: T11
) extends GenericRow
final case class GenericRow12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12](
  col1: T1,
  col2: T2,
  col3: T3,
  col4: T4,
  col5: T5,
  col6: T6,
  col7: T7,
  col8: T8,
  col9: T9,
  col10: T10,
  col11: T11,
  col12: T12
) extends GenericRow
final case class GenericRow13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13](
  col1: T1,
  col2: T2,
  col3: T3,
  col4: T4,
  col5: T5,
  col6: T6,
  col7: T7,
  col8: T8,
  col9: T9,
  col10: T10,
  col11: T11,
  col12: T12,
  col13: T13
) extends GenericRow
final case class GenericRow14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14](
  col1: T1,
  col2: T2,
  col3: T3,
  col4: T4,
  col5: T5,
  col6: T6,
  col7: T7,
  col8: T8,
  col9: T9,
  col10: T10,
  col11: T11,
  col12: T12,
  col13: T13,
  col14: T14
) extends GenericRow
final case class GenericRow15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15](
  col1: T1,
  col2: T2,
  col3: T3,
  col4: T4,
  col5: T5,
  col6: T6,
  col7: T7,
  col8: T8,
  col9: T9,
  col10: T10,
  col11: T11,
  col12: T12,
  col13: T13,
  col14: T14,
  col15: T15
) extends GenericRow

final case class GenericRow16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16](
  col1: T1,
  col2: T2,
  col3: T3,
  col4: T4,
  col5: T5,
  col6: T6,
  col7: T7,
  col8: T8,
  col9: T9,
  col10: T10,
  col11: T11,
  col12: T12,
  col13: T13,
  col14: T14,
  col15: T15,
  col16: T16
) extends GenericRow

final case class GenericRow17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17](
  col1: T1,
  col2: T2,
  col3: T3,
  col4: T4,
  col5: T5,
  col6: T6,
  col7: T7,
  col8: T8,
  col9: T9,
  col10: T10,
  col11: T11,
  col12: T12,
  col13: T13,
  col14: T14,
  col15: T15,
  col16: T16,
  col17: T17
) extends GenericRow

final case class GenericRow18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18](
  col1: T1,
  col2: T2,
  col3: T3,
  col4: T4,
  col5: T5,
  col6: T6,
  col7: T7,
  col8: T8,
  col9: T9,
  col10: T10,
  col11: T11,
  col12: T12,
  col13: T13,
  col14: T14,
  col15: T15,
  col16: T16,
  col17: T17,
  col18: T18
) extends GenericRow

final case class GenericRow19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19](
  col1: T1,
  col2: T2,
  col3: T3,
  col4: T4,
  col5: T5,
  col6: T6,
  col7: T7,
  col8: T8,
  col9: T9,
  col10: T10,
  col11: T11,
  col12: T12,
  col13: T13,
  col14: T14,
  col15: T15,
  col16: T16,
  col17: T17,
  col18: T18,
  col19: T19
) extends GenericRow

final case class GenericRow20[
  T1,
  T2,
  T3,
  T4,
  T5,
  T6,
  T7,
  T8,
  T9,
  T10,
  T11,
  T12,
  T13,
  T14,
  T15,
  T16,
  T17,
  T18,
  T19,
  T20
](
  col1: T1,
  col2: T2,
  col3: T3,
  col4: T4,
  col5: T5,
  col6: T6,
  col7: T7,
  col8: T8,
  col9: T9,
  col10: T10,
  col11: T11,
  col12: T12,
  col13: T13,
  col14: T14,
  col15: T15,
  col16: T16,
  col17: T17,
  col18: T18,
  col19: T19,
  col20: T20
) extends GenericRow

final case class GenericRow21[
  T1,
  T2,
  T3,
  T4,
  T5,
  T6,
  T7,
  T8,
  T9,
  T10,
  T11,
  T12,
  T13,
  T14,
  T15,
  T16,
  T17,
  T18,
  T19,
  T20,
  T21
](
  col1: T1,
  col2: T2,
  col3: T3,
  col4: T4,
  col5: T5,
  col6: T6,
  col7: T7,
  col8: T8,
  col9: T9,
  col10: T10,
  col11: T11,
  col12: T12,
  col13: T13,
  col14: T14,
  col15: T15,
  col16: T16,
  col17: T17,
  col18: T18,
  col19: T19,
  col20: T20,
  col21: T21
) extends GenericRow

final case class GenericRow22[
  T1,
  T2,
  T3,
  T4,
  T5,
  T6,
  T7,
  T8,
  T9,
  T10,
  T11,
  T12,
  T13,
  T14,
  T15,
  T16,
  T17,
  T18,
  T19,
  T20,
  T21,
  T22
](
  col1: T1,
  col2: T2,
  col3: T3,
  col4: T4,
  col5: T5,
  col6: T6,
  col7: T7,
  col8: T8,
  col9: T9,
  col10: T10,
  col11: T11,
  col12: T12,
  col13: T13,
  col14: T14,
  col15: T15,
  col16: T16,
  col17: T17,
  col18: T18,
  col19: T19,
  col20: T20,
  col21: T21,
  col22: T22
) extends GenericRow
