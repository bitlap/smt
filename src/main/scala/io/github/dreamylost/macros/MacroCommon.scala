package io.github.dreamylost.macros

import scala.reflect.macros.whitebox

/**
 * Common methods
 *
 * @author 梦境迷离
 * @since 2021/6/28
 * @version 1.0
 */
trait MacroCommon {

  /**
   * Eval tree.
   *
   * @param c
   * @param tree
   * @tparam T
   * @return
   */
  def evalTree[T: c.WeakTypeTag](c: whitebox.Context)(tree: c.Tree): T = c.eval(c.Expr[T](c.untypecheck(tree.duplicate)))

  def extractArgumentsTuple1[T: c.WeakTypeTag](c: whitebox.Context)(partialFunction: PartialFunction[c.Tree, Tuple1[T]]): Tuple1[T] = {
    partialFunction.apply(c.prefix.tree)
  }

  def extractArgumentsTuple2[T1: c.WeakTypeTag, T2: c.WeakTypeTag](c: whitebox.Context)(partialFunction: PartialFunction[c.Tree, (T1, T2)]): (T1, T2) = {
    partialFunction.apply(c.prefix.tree)
  }

  def extractArgumentsTuple3[T1: c.WeakTypeTag, T2: c.WeakTypeTag, T3: c.WeakTypeTag](c: whitebox.Context)(partialFunction: PartialFunction[c.Tree, (T1, T2, T3)]): (T1, T2, T3) = {
    partialFunction.apply(c.prefix.tree)
  }

  def extractArgumentsTuple4[T1: c.WeakTypeTag, T2: c.WeakTypeTag, T3: c.WeakTypeTag, T4: c.WeakTypeTag](c: whitebox.Context)(partialFunction: PartialFunction[c.Tree, (T1, T2, T3, T4)]): (T1, T2, T3, T4) = {
    partialFunction.apply(c.prefix.tree)
  }

  /**
   * Output ast result.
   *
   * @param c
   * @param force
   * @param resTree
   */
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
   *
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
      q"""
         $resTree
         ${companionOpt.get}
         """
    }
  }

  /**
   * Modify the associated object itself according to whether there is an associated object.
   *
   * @param c
   * @param annottees
   * @param modifyAction The actual processing function
   * @return Return the result of modifyAction
   */
  def handleWithImplType(c: whitebox.Context)(annottees: c.Expr[Any]*)
    (modifyAction: (c.universe.ClassDef, Option[c.universe.ModuleDef]) => Any): c.Expr[Nothing] = {
    import c.universe._
    annottees.map(_.tree) match {
      case (classDecl: ClassDef) :: Nil => modifyAction(classDecl, None).asInstanceOf[c.Expr[Nothing]]
      case (classDecl: ClassDef) :: (compDecl: ModuleDef) :: Nil => modifyAction(classDecl, Some(compDecl)).asInstanceOf[c.Expr[Nothing]]
      case _ => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
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
      case _ => c.abort(c.enclosingPosition, s"${ErrorMessage.ONLY_CLASS} classDef: $annotateeClass")
    }
  }

  /**
   * Expand the constructor and get the field TermName.
   *
   * @param c
   * @param field
   * @return
   */
  def getFieldTermName(c: whitebox.Context)(field: c.universe.Tree): c.universe.TermName = {
    import c.universe._
    field match {
      case q"$mods val $tname: $tpt = $expr" => tname.asInstanceOf[TermName]
      case q"$mods var $tname: $tpt = $expr" => tname.asInstanceOf[TermName]
      case q"$mods val $pat = $expr"         => pat.asInstanceOf[TermName] //for equalsAndHashcode, need contains all fields.
      case q"$mods var $pat = $expr"         => pat.asInstanceOf[TermName]
    }
  }

  /**
   * Expand the method params and get the param Name.
   *
   * @param c
   * @param field
   * @return
   */
  def getMethodParamName(c: whitebox.Context)(field: c.universe.Tree): c.universe.Name = {
    import c.universe._
    field match {
      case q"$mods val $tname: $tpt = $expr" => tpt.asInstanceOf[Ident].name.decodedName
    }
  }

  /**
   * Check whether the mods of the fields has a `private[this]`, because it cannot be used in equals method.
   *
   * @param c
   * @param field
   * @return
   */
  def classParamsIsPrivate(c: whitebox.Context)(field: c.universe.Tree): Boolean = {
    import c.universe._
    field match {
      case q"$mods val $tname: $tpt = $expr" => if (mods.asInstanceOf[Modifiers].hasFlag(Flag.PRIVATE)) false else true
      case q"$mods var $tname: $tpt = $expr" => true
    }
  }

  /**
   * Expand the constructor and get the field with assign.
   *
   * @param c
   * @param annotteeClassParams
   * @return
   */
  def getFieldAssignExprs(c: whitebox.Context)(annotteeClassParams: Seq[c.Tree]): Seq[c.Tree] = {
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
   * @param annotteeClassDefinitions
   */
  def getClassMemberValDefs(c: whitebox.Context)(annotteeClassDefinitions: Seq[c.Tree]): Seq[c.Tree] = {
    import c.universe._
    annotteeClassDefinitions.filter(p => p match {
      case _: ValDef => true
      case _         => false
    })
  }

  /**
   * Extract the methods belonging to the class， contains Secondary Constructor.
   *
   * @param c
   * @param annotteeClassDefinitions
   */
  def getClassMemberDefDefs(c: whitebox.Context)(annotteeClassDefinitions: Seq[c.Tree]): Seq[c.Tree] = {
    import c.universe._
    annotteeClassDefinitions.filter(p => p match {
      case _: DefDef => true
      case _         => false
    })
  }

  /**
   * We generate constructor with currying, and we have to deal with the first layer of currying alone.
   *
   * @param typeName
   * @param fieldss
   * @param isCase
   * @return A constructor with currying, it not contains tpt, provide for calling method.
   * @example [[new TestClass12(i)(j)(k)(t)]]
   */
  def getConstructorWithCurrying(c: whitebox.Context)(typeName: c.TypeName, fieldss: List[List[c.Tree]], isCase: Boolean): c.Tree = {
    import c.universe._
    val allFieldsTermName = fieldss.map(f => f.map(ff => getFieldTermName(c)(ff)))
    // not currying
    val constructor = if (fieldss.isEmpty || fieldss.size == 1) {
      q"${if (isCase) q"${typeName.toTermName}(..${allFieldsTermName.flatten})" else q"new $typeName(..${allFieldsTermName.flatten})"}"
    } else {
      // currying
      val first = allFieldsTermName.head
      if (isCase) q"${typeName.toTermName}(...$first)(...${allFieldsTermName.tail})"
      else q"new $typeName(...$first)(...${allFieldsTermName.tail})"
    }
    c.info(c.enclosingPosition, s"getConstructorWithCurrying constructor: $constructor, paramss: $fieldss", force = true)
    constructor
  }

  /**
   * We generate apply method with currying, and we have to deal with the first layer of currying alone.
   *
   * @param typeName
   * @param fieldss
   * @return A apply method with currying.
   * @example [[def apply(int: Int)(j: Int)(k: Option[String])(t: Option[Long]): B3 = new B3(int)(j)(k)(t)]]
   */
  def getApplyMethodWithCurrying(c: whitebox.Context)(typeName: c.TypeName, fieldss: List[List[c.Tree]], classTypeParams: List[c.Tree]): c.Tree = {
    import c.universe._
    val allFieldsTermName = fieldss.map(f => getFieldAssignExprs(c)(f))
    val returnTypeParams = extractClassTypeParamsTypeName(c)(classTypeParams)
    // not currying
    val applyMethod = if (fieldss.isEmpty || fieldss.size == 1) {
      q"def apply[..$classTypeParams](..${allFieldsTermName.flatten}): $typeName[..$returnTypeParams] = ${getConstructorWithCurrying(c)(typeName, fieldss, isCase = false)}"
    } else {
      // currying
      val first = allFieldsTermName.head
      q"def apply[..$classTypeParams](..$first)(...${allFieldsTermName.tail}): $typeName[..$returnTypeParams] = ${getConstructorWithCurrying(c)(typeName, fieldss, isCase = false)}"
    }
    c.info(c.enclosingPosition, s"getApplyWithCurrying constructor: $applyMethod, paramss: $fieldss", force = true)
    applyMethod
  }

  /**
   * Only for primitive types, we can get type and map to scala type.
   *
   * @param jType java type name
   * @return Scala type name
   */
  def toScalaType(jType: String): String = {
    val types = Map(
      "java.lang.Integer" -> "Int",
      "java.lang.Long" -> "Long",
      "java.lang.Double" -> "Double",
      "java.lang.Float" -> "Float",
      "java.lang.Short" -> "Short",
      "java.lang.Byte" -> "Byte",
      "java.lang.Boolean" -> "Boolean",
      "java.lang.Character" -> "Char",
      "java.lang.String" -> "String"
    )
    types.getOrElse(jType, jType)
  }

  /**
   * Gets a list of generic parameters.
   * This is because the generic parameters of a class cannot be used directly in the return type, and need to be converted.
   *
   * @param c
   * @param tpParams
   * @return
   */
  def extractClassTypeParamsTypeName(c: whitebox.Context)(tpParams: List[c.Tree]): List[c.TypeName] = {
    import c.universe._
    tpParams.map {
      case t: TypeDef => t.name
    }
  }
}
