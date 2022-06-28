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

import org.bitlap.common.internal.CaseClassExtractorMacro

import scala.reflect.ClassTag
import scala.reflect.runtime.{ universe => ru }
import scala.reflect.runtime.universe._
import scala.util.{ Failure, Success }

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
object CaseClassExtractor {

  /** Using the characteristics of the product type to get the field value should force the conversion externally
   *  (safely).
   */
  def ofValue[T <: Product](t: T, field: CaseClassField): Option[Any] = macro CaseClassExtractorMacro.macroImpl[T]

  /** Using scala reflect to get the field value (safely).
   */
  @deprecated
  def reflectValue[T: ru.TypeTag](obj: T, field: CaseClassField)(implicit
    classTag: ClassTag[T]
  ): Option[field.Field] = {
    val mirror = ru.runtimeMirror(getClass.getClassLoader)
    val fieldOption = scala.util.Try(
      getMethods[T]
        .filter(_.name.toTermName.decodedName.toString == field.stringify)
        .map(m => mirror.reflect(obj).reflectField(m).get)
        .headOption
        .map(_.asInstanceOf[field.Field])
    )
    fieldOption match {
      case Success(value)     => value
      case Failure(exception) => exception.printStackTrace(); None
    }
  }

  def getMethods[T: ru.TypeTag]: List[ru.MethodSymbol] = typeOf[T].members.collect {
    case m: MethodSymbol if m.isCaseAccessor => m
  }.toList
}
