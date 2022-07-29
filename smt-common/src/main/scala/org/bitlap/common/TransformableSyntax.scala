package org.bitlap.common
import org.bitlap.common.internal.TransformerMacro

/** @author
 *    梦境迷离
 *  @version 1.0,2022/7/29
 */
trait TransformableSyntax[From <: Product, To <: Product] { self =>

  val transformer: Transformer[From, To]

  def Syntax: Transformable[From, To] = macro TransformerMacro.applyImpl[From, To]

  implicit val _self: TransformableSyntax[From, To] = self

}
