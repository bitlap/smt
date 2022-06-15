package org.bitlap.common

/** @author
 *    梦境迷离
 *  @version 1.0,6/14/22
 */
trait CTransformer[-In, +Out] {
  def transform(in: In): Out
}

object CTransformer {
  def apply[In <: Product, Out <: Product](implicit st: CTransformer[In, Out]): CTransformer[In, Out] = st
}
