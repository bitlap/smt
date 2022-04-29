package org.bitlap.csv.derive

import org.bitlap.csv.core.CsvConverter
import scala.reflect.macros.blackbox
import org.bitlap.csv.core.AbstractMacroProcessor

/**
 *
 * @author 梦境迷离
 * @version 1.0,2022/4/29
 */
object DeriveCsvConverter {

  def gen[CC]: CsvConverter[CC] = macro Macro.macroImpl[CC]

  class Macro(override val c: blackbox.Context) extends AbstractMacroProcessor(c) {
    def macroImpl[CC: c.WeakTypeTag]: c.Expr[CC] = {
      import c.universe._
      val clazzName = c.weakTypeOf[CC].typeSymbol.name
      val typeName = TypeName(clazzName.decodedName.toString)
      val tree =
        q"""
        new CsvConverter[$typeName] {
            override def from(line: String): Option[$typeName] = _root_.org.bitlap.csv.core.DeriveToCaseClass[$typeName](line, ",")
            override def to(t: $typeName): String = _root_.org.bitlap.csv.core.DeriveToString[$typeName](t)
        }
       """
      printTree[CC](c)(force = true, tree).asInstanceOf[c.Expr[CC]]
    }
  }

}
