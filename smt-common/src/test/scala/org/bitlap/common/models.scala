package org.bitlap.common

/** @author
 *    梦境迷离
 *  @since 2021/11/20
 *  @version 1.0
 */
object models {

  object from {

    import org.bitlap.common.models.to._

    sealed trait Model

    final case class FQueryResult(tableSchema: FTableSchema, rows: FRowSet) extends Model

    object FQueryResult {
      //  mapping name
      implicit val queryResultTransform: Transformer[FQueryResult, TQueryResult] =
        Transformable[FQueryResult, TQueryResult]
          .mapField(_.rows, _.trows)
          .mapField(_.tableSchema, _.ttableSchema)
          .instance
    }

    final case class FRowSet(rows: List[FRow] = Nil, startOffset: Long = 0) extends Model

    object FRowSet {
      // not need mapping
      implicit val rowSetTransform: Transformer[FRowSet, TRowSet] = Transformable[FRowSet, TRowSet].instance
    }

    final case class FRow(values: List[String] = Nil) extends Model

    object FRow {
      implicit val rowTransform: Transformer[FRow, TRow] = Transformable[FRow, TRow].instance // not need mapping
    }

    final case class FTableSchema(columns: List[FColumnDesc] = Nil) extends Model

    object FTableSchema {
      implicit val tableSchemaTransform: Transformer[FTableSchema, TTableSchema] =
        Transformable[FTableSchema, TTableSchema].instance
    }

    final case class FColumnDesc(columnName: String) extends Model

    object FColumnDesc {
      implicit val columnDescTransform: Transformer[FColumnDesc, TColumnDesc] = Transformable[FColumnDesc, TColumnDesc]
        .mapField(_.columnName, _.tcolumnName) // mapping name
        .instance
    }
  }

  object to {

    sealed trait TModel

    final case class TQueryResult(trows: TRowSet, ttableSchema: TTableSchema) extends TModel

    final case class TRowSet(startOffset: Long = 0, rows: List[TRow]) extends TModel

    final case class TRow(values: List[String] = Nil) extends TModel

    final case class TTableSchema(columns: List[TColumnDesc] = Nil) extends TModel

    final case class TColumnDesc(tcolumnName: String) extends TModel
  }
}
