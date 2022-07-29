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

package org.bitlap

/** @author
 *    梦境迷离
 *  @version 1.0,6/20/22
 */
package object common {

  implicit final def transformerOption[F, T](implicit e: Transformer[F, T]): Transformer[Option[F], Option[T]] =
    new Transformer[Option[F], Option[T]] {
      override def transform(from: Option[F]): Option[T] = from.map(e.transform)
    }

  implicit final def transformerSeq[F, T](implicit e: Transformer[F, T]): Transformer[Seq[F], Seq[T]] =
    new Transformer[Seq[F], Seq[T]] {
      override def transform(from: Seq[F]): Seq[T] = from.map(e.transform)
    }

  implicit final def transformerSet[F, T](implicit e: Transformer[F, T]): Transformer[Set[F], Set[T]] =
    new Transformer[Set[F], Set[T]] {
      override def transform(from: Set[F]): Set[T] = from.map(e.transform)
    }

  implicit final def transformerList[F, T](implicit e: Transformer[F, T]): Transformer[List[F], List[T]] =
    new Transformer[List[F], List[T]] {
      override def transform(from: List[F]): List[T] = from.map(e.transform)
    }

  implicit final def transformerVector[F, T](implicit e: Transformer[F, T]): Transformer[Vector[F], Vector[T]] =
    new Transformer[Vector[F], Vector[T]] {
      override def transform(from: Vector[F]): Vector[T] = from.map(e.transform)
    }

  implicit final def transformerMap[K, F, T](implicit
    e: Transformer[F, T]
  ): Transformer[Map[K, F], Map[K, T]] =
    new Transformer[Map[K, F], Map[K, T]] {
      override def transform(from: Map[K, F]): Map[K, T] = from.map(kv => kv._1 -> e.transform(kv._2))
    }

  implicit final def transformerSeqList[F, T](implicit e: Transformer[F, T]): Transformer[Seq[F], List[T]] =
    new Transformer[Seq[F], List[T]] {
      override def transform(from: Seq[F]): List[T] = from.map(e.transform).toList
    }

  implicit final def transformerListSeq[F, T](implicit e: Transformer[F, T]): Transformer[List[F], Seq[T]] =
    new Transformer[List[F], Seq[T]] {
      override def transform(from: List[F]): Seq[T] = from.map(e.transform)
    }

  implicit final class TransformerOps[F](private val from: F) extends AnyVal {
    final def transform[T](implicit transformer: Transformer[F, T]): T = transformer.transform(from)
  }

  implicit final class TransformableSyntaxOps[F <: Product](private val from: F) extends AnyVal {
    final def transformCaseClass[T <: Product](implicit transformableSyntax: TransformableSyntax[F, T]): T =
      transformableSyntax.transformer.transform(from)
  }
}
