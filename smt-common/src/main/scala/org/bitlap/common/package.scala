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

}
