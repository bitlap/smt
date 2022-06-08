package org.bitlap.common

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.reflect.macros.whitebox
import scala.reflect.ClassTag
import scala.reflect.runtime.{ universe => ru }
import scala.reflect.runtime.universe._

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
object CaseClassExtractor {

  def getFieldValueUnSafely[T: ru.TypeTag](obj: T, field: CaseClassField)(implicit
    classTag: ClassTag[T]
  ): Option[field.Field] = {
    val mirror = ru.runtimeMirror(getClass.getClassLoader)
    getMethods[T]
      .filter(_.name.toTermName.decodedName.toString == field.stringify)
      .map(m => mirror.reflect(obj).reflectField(m).get)
      .headOption
      .map(_.asInstanceOf[field.Field])
  }

  def getMethods[T: ru.TypeTag]: List[ru.MethodSymbol] = typeOf[T].members.collect {
    case m: MethodSymbol if m.isCaseAccessor => m
  }.toList

  def getFieldValueSafely[T <: Product, Field](t: T, name: String): Option[Field] = macro macroImpl[T, Field]

  def macroImpl[T: c.WeakTypeTag, Field: c.WeakTypeTag](
    c: whitebox.Context
  )(t: c.Expr[T], name: c.Expr[String]): c.Expr[Option[Field]] = {
    import c.universe._
    val typ = TypeName(c.weakTypeOf[Field].typeSymbol.name.decodedName.toString)
    val tree =
      q"""
       if ($t == null) None else {
          val idx = $t.productElementNames.indexOf($name)
          Option($t.productElement(idx).asInstanceOf[$typ])       
       }
     """
    exprPrintTree[Option[Field]](c)(tree)

  }

  private def exprPrintTree[Field: c.WeakTypeTag](c: whitebox.Context)(resTree: c.Tree): c.Expr[Field] = {
    c.info(
      c.enclosingPosition,
      s"\n###### Time: ${ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)} Expanded macro start ######\n" + resTree
        .toString() + "\n###### Expanded macro end ######\n",
      force = false
    )
    c.Expr[Field](resTree)
  }
}
