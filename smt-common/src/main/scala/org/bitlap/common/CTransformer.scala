package org.bitlap.common

/** @author
 *    梦境迷离
 *  @version 1.0,6/14/22
 */
trait CTransformer[-From, +To] {
  def transform(from: From): To
}

object CTransformer {
  def apply[From <: Product, To <: Product](implicit st: CTransformer[From, To]): CTransformer[From, To] = st
}
