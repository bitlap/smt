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
   * Check the class and its companion object, and return the class definition.
   *
   * @param c
   * @param annottees
   * @return Return ClassDef
   */
  def checkAndGetClassDef(c: whitebox.Context)(annottees: c.Expr[Any]*): c.universe.ClassDef = {
    import c.universe._
    annottees.map(_.tree).toList match {
      case (classDecl: ClassDef) :: Nil => classDecl
      case (classDecl: ClassDef) :: (compDecl: ModuleDef) :: Nil => classDecl
      case _ => c.abort(c.enclosingPosition, "Unexpected annottee. Only applicable to class definitions.")
    }
  }

  /**
   * Get class if it exists.
   *
   * @param c
   * @param annottees
   * @return Return ClassDef without verify.
   */
  def tryGetClassDef(c: whitebox.Context)(annottees: c.Expr[Any]*): Option[c.universe.ClassDef] = {
    import c.universe._
    annottees.map(_.tree).toList match {
      case (classDecl: ClassDef) :: Nil => Some(classDecl)
      case (classDecl: ClassDef) :: (compDecl: ModuleDef) :: Nil => Some(classDecl)
      case _ => None
    }
  }

  /**
   * Get companion object if it exists.
   *
   * @param c
   * @param annottees
   * @return
   */
  def tryGetCompanionObject(c: whitebox.Context)(annottees: c.Expr[Any]*): Option[c.universe.ModuleDef] = {
    import c.universe._
    annottees.map(_.tree).toList match {
      case (classDecl: ClassDef) :: Nil => None
      case (classDecl: ClassDef) :: (compDecl: ModuleDef) :: Nil => Some(compDecl)
      case (compDecl: ModuleDef) :: Nil => Some(compDecl)
      case _ => None
    }
  }

  /**
   * Wrap tree result with companion object.
   * @param c
   * @param resTree class
   * @param annottees
   * @return
   */
  def treeResultWithCompanionObject(c: whitebox.Context)(resTree: c.Tree, annottees: c.Expr[Any]*): c.universe.Tree = {
    import c.universe._
    val companionOpt = tryGetCompanionObject(c)(annottees: _*)
    if (companionOpt.isEmpty) {
      resTree
    } else {
      val q"$mods object $obj extends ..$bases { ..$body }" = companionOpt.get
      val companion = q"$mods object $obj extends ..$bases { ..$body }"
      q"""
         $resTree
         $companion
         """
    }
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
          c.info(c.enclosingPosition, "Annotation is used on 'case class'.", force = true)
          true
        } else false
      case _ => c.abort(c.enclosingPosition, s"Annotation is only supported on class. classDef: $annotateeClass")
    }
  }

  /**
   * Expand the constructor and get the field TermName.
   *
   * @param c
   * @param field
   * @return
   */
  def fieldTermName(c: whitebox.Context)(field: c.universe.Tree): c.universe.TermName = {
    import c.universe._
    field match {
      case q"$mods val $tname: $tpt = $expr" => tname.asInstanceOf[TermName]
      case q"$mods var $tname: $tpt = $expr" => tname.asInstanceOf[TermName]
    }
  }

  /**
   *  Expand the constructor and get the field with assign.
   * @param c
   * @param annotteeClassParams
   * @return
   */
  def fieldAssignExpr(c: whitebox.Context)(annotteeClassParams: Seq[c.Tree]): Seq[c.Tree] = {
    import c.universe._
    annotteeClassParams.map {
      case q"$mods var $tname: $tpt = $expr" => q"$tname: $tpt" //Ignore expr
      case q"$mods val $tname: $tpt = $expr" => q"$tname: $tpt"
    }
  }

  /**
   * Modify companion objects.
   *
   * @param c
   * @param compDeclOpt
   * @param codeBlock
   * @param className
   * @return
   */
  def modifiedCompanion(c: whitebox.Context)(
    compDeclOpt: Option[c.universe.ModuleDef],
    codeBlock:   c.Tree, className: c.TypeName): c.universe.Tree = {
    import c.universe._
    compDeclOpt map { compDecl =>
      val q"$mods object $obj extends ..$bases { ..$body }" = compDecl
      val o =
        q"""
          $mods object $obj extends ..$bases {
            ..$body
            ..$codeBlock
          }
        """
      c.info(c.enclosingPosition, s"modifiedCompanion className: $className, exists obj: $o", force = true)
      o
    } getOrElse {
      // Create a companion object with the builder
      val o = q"object ${className.toTermName} { ..$codeBlock }"
      c.info(c.enclosingPosition, s"modifiedCompanion className: $className, new obj: $o", force = true)
      o
    }
  }

  /**
   * Extract the internal fields of members belonging to the class， but not in primary constructor.
   *
   * @param c
   */
  def getClassMemberValDef(c: whitebox.Context)(annotteeClassDefinitions: Seq[c.Tree]): Seq[c.Tree] = {
    import c.universe._
    annotteeClassDefinitions.filter(p => p match {
      case _: ValDef => true
      case _         => false
    })
  }
}
