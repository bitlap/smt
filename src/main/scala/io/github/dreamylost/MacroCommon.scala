package io.github.dreamylost

import scala.reflect.macros.whitebox

/**
 * Common methods
 *
 * @author 梦境迷离
 * @since 2021/6/28
 * @version 1.0
 */
trait MacroCommon {

  def printTree(c: whitebox.Context)(force: Boolean, resTree: c.Tree): Unit = {
    c.info(
      c.enclosingPosition,
      "\n###### Expanded macro ######\n" + resTree.toString() + "\n###### Expanded macro ######\n",
      force = force
    )
  }

  /**
   * Check the class and its accompanying objects, and return the class definition.
   *
   * @param c
   * @param annottees
   * @return Return ClassDef
   */
  def checkAndReturnClass(c: whitebox.Context)(annottees: c.Expr[Any]*): c.universe.ClassDef = {
    import c.universe._
    val annotateeClass: ClassDef = annottees.map(_.tree).toList match {
      case (classDecl: ClassDef) :: Nil => classDecl
      case (classDecl: ClassDef) :: (compDecl: ModuleDef) :: Nil => classDecl
      case _ => c.abort(c.enclosingPosition, "Unexpected annottee. Only applicable to class definitions.")
    }
    annotateeClass
  }

  /**
   * Modify the associated object itself according to whether there is an associated object.
   *
   * @param c
   * @param annottees
   * @param modifyAction The dependent type need aux-pattern in scala2. Now let's get around this.
   * @return Return the result of modifyAction
   */
  def handleWithImplType(c: whitebox.Context)(annottees: c.Expr[Any]*)
    (modifyAction: (c.universe.ClassDef, Option[c.universe.ModuleDef]) => Any): c.Expr[Nothing] = {
    import c.universe._
    annottees.map(_.tree) match {
      case (classDecl: ClassDef) :: Nil => modifyAction(classDecl, None).asInstanceOf[c.Expr[Nothing]]
      case (classDecl: ClassDef) :: (compDecl: ModuleDef) :: Nil => modifyAction(classDecl, Some(compDecl)).asInstanceOf[c.Expr[Nothing]]
      case _ => c.abort(c.enclosingPosition, "Invalid annottee")
    }
  }

  /**
   * Expand the class and check whether the class is a case class.
   *
   * @param c
   * @param annotateeClass classDef
   * @return Return true if it is a case class
   */
  def isCaseClass(c: whitebox.Context)(annotateeClass: c.universe.ClassDef): Boolean = {
    import c.universe._
    annotateeClass match {
      case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
        if (mods.asInstanceOf[Modifiers].hasFlag(Flag.CASE)) {
          c.info(c.enclosingPosition, "Annotation is used on 'case class'.")
          true
        } else false
      case _ => c.abort(c.enclosingPosition, s"Annotation is only supported on class. classDef: $annotateeClass")
    }
  }
}
