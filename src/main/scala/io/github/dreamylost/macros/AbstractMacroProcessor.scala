/*
 * Copyright (c) 2021 jxnu-liguobin && contributors
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

package io.github.dreamylost.macros

import scala.reflect.macros.whitebox

/**
 *
 * @author 梦境迷离
 * @since 2021/7/24
 * @version 1.0
 */
abstract class AbstractMacroProcessor(val c: whitebox.Context) {

  import c.universe._

  protected lazy val SDKClasses = Set("java.lang.Object", "scala.AnyRef")

  /**
   * Subclasses should override the method and return the final result abstract syntax tree, or an abstract syntax tree close to the final result.
   * When the macro implementation is very simple, we don't need to use this method, so we don't need to implement it.
   * When there are many macro input parameters, we will not use this method temporarily because we need to pass parameters.
   *
   * @param classDecl
   * @param compDeclOpt
   * @return c.Expr[Any], Why use Any? The dependent type need aux-pattern in scala2. Now let's get around this.
   *
   */
  def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): Any = ???

  /**
   * Subclasses must override the method.
   *
   * @param annottees
   * @return Macro expanded final syntax tree.
   */
  def impl(annottees: Expr[Any]*): Expr[Any]

  /**
   * Eval tree.
   *
   * @param tree
   * @tparam T
   * @return
   */
  def evalTree[T: WeakTypeTag](tree: Tree): T = c.eval(c.Expr[T](c.untypecheck(tree.duplicate)))

  def extractArgumentsTuple1[T: WeakTypeTag](partialFunction: PartialFunction[Tree, Tuple1[T]]): Tuple1[T] = {
    partialFunction.apply(c.prefix.tree)
  }

  def extractArgumentsTuple2[T1: WeakTypeTag, T2: WeakTypeTag](partialFunction: PartialFunction[Tree, (T1, T2)]): (T1, T2) = {
    partialFunction.apply(c.prefix.tree)
  }

  def extractArgumentsTuple4[T1: WeakTypeTag, T2: WeakTypeTag, T3: WeakTypeTag, T4: WeakTypeTag](partialFunction: PartialFunction[Tree, (T1, T2, T3, T4)]): (T1, T2, T3, T4) = {
    partialFunction.apply(c.prefix.tree)
  }

  /**
   * Output ast result.
   *
   * @param force
   * @param resTree
   */
  def printTree(force: Boolean, resTree: Tree): Unit = {
    c.info(
      c.enclosingPosition,
      "\n###### Expanded macro ######\n" + resTree.toString() + "\n###### Expanded macro ######\n",
      force = force
    )
  }

  /**
   * Check the class and its companion object, and return the class definition.
   *
   * @param annottees
   * @return Return ClassDef
   */
  def checkAndGetClassDef(annottees: Expr[Any]*): ClassDef = {
    annottees.map(_.tree).toList match {
      case (classDecl: ClassDef) :: Nil => classDecl
      case (classDecl: ClassDef) :: (compDecl: ModuleDef) :: Nil => classDecl
      case _ => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
    }
  }

  /**
   * Get companion object if it exists.
   *
   * @param annottees
   * @return
   */
  def tryGetCompanionObject(annottees: Expr[Any]*): Option[ModuleDef] = {
    annottees.map(_.tree).toList match {
      case (classDecl: ClassDef) :: Nil => None
      case (classDecl: ClassDef) :: (compDecl: ModuleDef) :: Nil => Some(compDecl)
      case (compDecl: ModuleDef) :: Nil => Some(compDecl)
      case _ => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
    }
  }

  /**
   * Wrap tree result with companion object.
   *
   * @param resTree class
   * @param annottees
   * @return
   */
  def treeResultWithCompanionObject(resTree: Tree, annottees: Expr[Any]*): Tree = {
    val companionOpt = tryGetCompanionObject(annottees: _*)
    companionOpt.fold(resTree) { t =>
      q"""
         $resTree
         $t
         """
    }
  }

  /**
   * Modify the associated object itself according to whether there is an associated object.
   *
   * @param annottees
   * @param modifyAction The actual processing function
   * @return Return the result of modifyAction
   */
  def handleWithImplType(annottees: Expr[Any]*)
    (modifyAction: (ClassDef, Option[ModuleDef]) => Any): Expr[Nothing] = {
    annottees.map(_.tree) match {
      case (classDecl: ClassDef) :: Nil => modifyAction(classDecl, None).asInstanceOf[Expr[Nothing]]
      case (classDecl: ClassDef) :: (compDecl: ModuleDef) :: Nil => modifyAction(classDecl, Some(compDecl)).asInstanceOf[Expr[Nothing]]
      case _ => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
    }
  }

  /**
   * Expand the class and check whether the class is a case class.
   *
   * @param annotateeClass classDef
   * @return Return true if it is a case class
   */
  def isCaseClass(annotateeClass: ClassDef): Boolean = {
    annotateeClass match {
      case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
        mods.asInstanceOf[Modifiers].hasFlag(Flag.CASE)
      case _ => c.abort(c.enclosingPosition, ErrorMessage.ONLY_CLASS)
    }
  }

  /**
   * Expand the constructor and get the field TermName.
   *
   * @param field
   * @return
   */
  def getFieldTermName(field: Tree): TermName = {
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
   * @param field
   * @return
   */
  def getMethodParamName(field: Tree): Name = {
    val q"$mods val $tname: $tpt = $expr" = field
    tpt.asInstanceOf[Ident].name.decodedName
  }

  /**
   * Check whether the mods of the fields has a `private[this]`, because it cannot be used in equals method.
   *
   * @param field
   * @return
   */
  def classParamsIsNotLocal(field: Tree): Boolean = {
    lazy val modifierNotLocal = (mods: Modifiers) => {
      !(
        mods.hasFlag(Flag.PRIVATE | Flag.LOCAL) | mods.hasFlag(Flag.PROTECTED | Flag.LOCAL)
      )
    }
    field match {
      case q"$mods val $tname: $tpt = $expr" => modifierNotLocal(mods.asInstanceOf[Modifiers])
      case q"$mods var $tname: $tpt = $expr" => modifierNotLocal(mods.asInstanceOf[Modifiers])
      case q"$mods val $pat = $expr"         => modifierNotLocal(mods.asInstanceOf[Modifiers])
      case q"$mods var $pat = $expr"         => modifierNotLocal(mods.asInstanceOf[Modifiers])
    }
  }

  /**
   * Expand the constructor and get the field with assign.
   *
   * @param annotteeClassParams
   * @return
   */
  def getConstructorFieldAssignExprs(annotteeClassParams: Seq[Tree]): Seq[Tree] = {
    annotteeClassParams.map {
      case q"$mods var $tname: $tpt = $expr" => q"$tname: $tpt" //Ignore expr
      case q"$mods val $tname: $tpt = $expr" => q"$tname: $tpt"
    }
  }

  /**
   * Modify companion objects.
   *
   * @param compDeclOpt
   * @param codeBlock
   * @param className
   * @return
   */
  def modifiedCompanion(
    compDeclOpt: Option[ModuleDef],
    codeBlock:   Tree, className: TypeName): Tree = {
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
   * @param annotteeClassDefinitions
   */
  def getClassMemberValDefs(annotteeClassDefinitions: Seq[Tree]): Seq[Tree] = {
    annotteeClassDefinitions.filter(p => p match {
      case _: ValDef => true
      case _         => false
    })
  }

  /**
   * Extract the methods belonging to the class， contains Secondary Constructor.
   *
   * @param annotteeClassDefinitions
   */
  def getClassMemberDefDefs(annotteeClassDefinitions: Seq[Tree]): Seq[Tree] = {
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
   * @example {{ new TestClass12(i)(j)(k)(t) }}
   */
  def getConstructorWithCurrying(typeName: TypeName, fieldss: List[List[Tree]], isCase: Boolean): Tree = {
    val allFieldsTermName = fieldss.map(f => f.map(ff => getFieldTermName(ff)))
    // not currying
    val constructor = if (fieldss.isEmpty || fieldss.size == 1) {
      q"${if (isCase) q"${typeName.toTermName}(..${allFieldsTermName.flatten})" else q"new $typeName(..${allFieldsTermName.flatten})"}"
    } else {
      // currying
      val first = allFieldsTermName.head
      if (isCase) q"${typeName.toTermName}(...$first)(...${allFieldsTermName.tail})"
      else q"new $typeName(..$first)(...${allFieldsTermName.tail})"
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
   * @example {{ def apply(int: Int)(j: Int)(k: Option[String])(t: Option[Long]): B3 = new B3(int)(j)(k)(t) }}
   */
  def getApplyMethodWithCurrying(typeName: TypeName, fieldss: List[List[Tree]], classTypeParams: List[Tree]): Tree = {
    val allFieldsTermName = fieldss.map(f => getConstructorFieldAssignExprs(f))
    val returnTypeParams = extractClassTypeParamsTypeName(classTypeParams)
    // not currying
    val applyMethod = if (fieldss.isEmpty || fieldss.size == 1) {
      q"def apply[..$classTypeParams](..${allFieldsTermName.flatten}): $typeName[..$returnTypeParams] = ${getConstructorWithCurrying(typeName, fieldss, isCase = false)}"
    } else {
      // currying
      val first = allFieldsTermName.head
      q"def apply[..$classTypeParams](..$first)(...${allFieldsTermName.tail}): $typeName[..$returnTypeParams] = ${getConstructorWithCurrying(typeName, fieldss, isCase = false)}"
    }
    c.info(c.enclosingPosition, s"getApplyMethodWithCurrying constructor: $applyMethod, paramss: $fieldss", force = true)
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
   * @param tpParams
   * @return
   */
  def extractClassTypeParamsTypeName(tpParams: List[Tree]): List[TypeName] = {
    tpParams.map(_.asInstanceOf[TypeDef].name)
  }

  /**
   * Is there a parent class? Does not contains sdk class, such as AnyRef Object
   *
   * @param superClasses
   * @return
   */
  def existsSuperClassExcludeSdkClass(superClasses: Seq[Tree]): Boolean = {
    superClasses.nonEmpty && !superClasses.forall(sc => SDKClasses.contains(sc.toString()))
  }

}
