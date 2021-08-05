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

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
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
  def createCustomExpr(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): Any = ???

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
      s"\n###### Time: ${ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)} Expanded macro start ######\n" + resTree.toString() + "\n###### Expanded macro end ######\n",
      force = force
    )
  }

  /**
   * Check the class and its companion object, and return the class definition.
   *
   * @param annottees
   * @return Return ClassDef
   */
  def checkGetClassDef(annottees: Seq[Expr[Any]]): ClassDef = {
    annottees.map(_.tree).toList match {
      case (classDecl: ClassDef) :: Nil => classDecl
      case (classDecl: ClassDef) :: (_: ModuleDef) :: Nil => classDecl
      case _ => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
    }
  }

  /**
   * Get object if it exists.
   *
   * @param annottees
   * @return
   */
  def getModuleDefOption(annottees: Seq[Expr[Any]]): Option[ModuleDef] = {
    annottees.map(_.tree).toList match {
      case (moduleDef: ModuleDef) :: Nil => Some(moduleDef)
      case (_: ClassDef) :: Nil => None
      case (_: ClassDef) :: (compDecl: ModuleDef) :: Nil => Some(compDecl)
      case (moduleDef: ModuleDef) :: (_: ClassDef) :: Nil => Some(moduleDef)
      case _ => None
    }
  }

  /**
   * Modify the associated object itself according to whether there is an associated object.
   *
   * @param annottees
   * @param modifyAction The actual processing function
   * @return Return the result of modifyAction
   */
  def collectCustomExpr(annottees: Seq[Expr[Any]])
    (modifyAction: (ClassDef, Option[ModuleDef]) => Any): Expr[Nothing] = {
    val classDef = checkGetClassDef(annottees)
    val compDecl = getModuleDefOption(annottees)
    modifyAction(classDef, compDecl).asInstanceOf[Expr[Nothing]]
  }

  /**
   * Check whether the class is a case class.
   *
   * @param annotateeClass classDef
   * @return Return true if it is a case class
   */
  def isCaseClass(annotateeClass: ClassDef): Boolean = {
    annotateeClass.mods.hasFlag(Flag.CASE)
  }

  /**
   * Check whether the mods of the fields has a `private[this]` or `protected[this]`, because it cannot be used out of class.
   *
   * @param tree Tree is a field or method?
   * @return false if mods exists private[this] or protected[this]
   */
  def isNotLocalClassMember(tree: Tree): Boolean = {
    lazy val modifierNotLocal = (mods: Modifiers) => {
      !(
        mods.hasFlag(Flag.PRIVATE | Flag.LOCAL) | mods.hasFlag(Flag.PROTECTED | Flag.LOCAL)
      )
    }
    tree match {
      case v: ValDef => modifierNotLocal(v.mods)
      case d: DefDef => modifierNotLocal(d.mods)
      case _         => true
    }
  }

  /**
   * Get the field TermName with type.
   *
   * @param annotteeClassParams
   * @return {{ i: Int}}
   */
  def getConstructorParamsNameWithType(annotteeClassParams: Seq[Tree]): Seq[Tree] = {
    annotteeClassParams.map(_.asInstanceOf[ValDef]).map(v => q"${v.name}: ${v.tpt}")
  }

  /**
   * Modify companion object or object.
   *
   * @param compDeclOpt
   * @param codeBlocks
   * @param className
   * @return
   */
  def appendModuleBody(
    compDeclOpt: Option[ModuleDef],
    codeBlocks:  List[Tree], className: TypeName): Tree = {
    compDeclOpt.fold(q"object ${className.toTermName} { ..$codeBlocks }") {
      compDecl =>
        c.info(c.enclosingPosition, s"appendModuleBody className: $className, exists obj: $compDecl", force = true)
        val ModuleDef(mods, name, impl) = compDecl
        val Template(parents, self, body) = impl
        val newImpl = Template(parents, self, body ++ codeBlocks)
        ModuleDef(mods, name, newImpl)
    }
  }

  /**
   * Extract the internal fields of members belonging to the class， but not in primary constructor.
   *
   * @param annotteeClassDefinitions
   */
  def getClassMemberValDefs(annotteeClassDefinitions: Seq[Tree]): Seq[ValDef] = {
    annotteeClassDefinitions.filter(_ match {
      case _: ValDef => true
      case _         => false
    }).map(_.asInstanceOf[ValDef])
  }

  /**
   * Extract the constructor params ValDef and flatten for currying.
   *
   * @param annotteeClassParams
   * @return {{ Seq(ValDef) }}
   */
  def getClassConstructorValDefsFlatten(annotteeClassParams: List[List[Tree]]): Seq[ValDef] = {
    annotteeClassParams.flatten.map(_.asInstanceOf[ValDef])
  }

  /**
   * Extract the constructor params ValDef not flatten.
   *
   * @param annotteeClassParams
   * @return {{ Seq(Seq(ValDef)) }}
   */
  def getClassConstructorValDefsNotFlatten(annotteeClassParams: List[List[Tree]]): Seq[Seq[ValDef]] = {
    annotteeClassParams.map(_.map(_.asInstanceOf[ValDef]))
  }

  /**
   * Extract the methods belonging to the class， contains Secondary Constructor.
   *
   * @param annotteeClassDefinitions
   */
  def getClassMemberDefDefs(annotteeClassDefinitions: Seq[Tree]): Seq[DefDef] = {
    annotteeClassDefinitions.filter(_ match {
      case _: DefDef => true
      case _         => false
    }).map(_.asInstanceOf[DefDef])
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
    val fieldssValDefNotFlatten = getClassConstructorValDefsNotFlatten(fieldss)
    val allFieldsTermName = fieldssValDefNotFlatten.map(_.map(_.name.toTermName))
    // not currying
    val constructor = if (fieldss.isEmpty || fieldss.size == 1) {
      q"${if (isCase) q"${typeName.toTermName}(..${allFieldsTermName.flatten})" else q"new $typeName(..${allFieldsTermName.flatten})"}"
    } else {
      // currying
      val first = allFieldsTermName.head
      if (isCase) q"${typeName.toTermName}(...$first)(...${allFieldsTermName.tail})"
      else q"new $typeName(..$first)(...${allFieldsTermName.tail})"
    }
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
    val allFieldsTermName = fieldss.map(f => getConstructorParamsNameWithType(f))
    val returnTypeParams = extractClassTypeParamsTypeName(classTypeParams)
    // not currying
    val applyMethod = if (fieldss.isEmpty || fieldss.size == 1) {
      q"def apply[..$classTypeParams](..${allFieldsTermName.flatten}): $typeName[..$returnTypeParams] = ${getConstructorWithCurrying(typeName, fieldss, isCase = false)}"
    } else {
      // currying
      val first = allFieldsTermName.head
      q"def apply[..$classTypeParams](..$first)(...${allFieldsTermName.tail}): $typeName[..$returnTypeParams] = ${getConstructorWithCurrying(typeName, fieldss, isCase = false)}"
    }
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

  private[macros] case class ValDefAccessor(
      mods: Modifiers,
      name: TermName,
      tpt:  Tree,
      rhs:  Tree
  ) {

    def typeName: TypeName = symbol.name.toTypeName

    def symbol: c.universe.Symbol = paramType.typeSymbol

    def paramType = c.typecheck(tq"$tpt", c.TYPEmode).tpe
  }

  /**
   * Retrieves the accessor fields on a class and returns a Seq of ValDefAccessor.
   *
   * @param params The list of params retrieved from the class
   * @return An Sequence of tuples where each tuple encodes the string name and string type of a field
   */
  def valDefAccessors(params: Seq[Tree]): Seq[ValDefAccessor] = {
    params.map {
      case ValDef(mods, name: TermName, tpt: Tree, rhs) =>
        ValDefAccessor(mods, name, tpt, rhs)
    }
  }

  /**
   * Extract the necessary structure information of the class for macro programming.
   *
   * @param classDecl
   */
  def mapToClassDeclInfo(classDecl: ClassDef): ClassDefinition = {
    val q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" = classDecl
    val (className, classParamss, classTypeParams) = (tpname.asInstanceOf[TypeName], paramss.asInstanceOf[List[List[Tree]]], tparams.asInstanceOf[List[Tree]])
    ClassDefinition(self.asInstanceOf[ValDef], mods.asInstanceOf[Modifiers], className, classParamss, classTypeParams, stats.asInstanceOf[List[Tree]], parents.asInstanceOf[List[Tree]])
  }

  /**
   * Extract the necessary structure information of the moduleDef for macro programming.
   *
   * @param moduleDef
   */
  def mapToModuleDeclInfo(moduleDef: ModuleDef): ClassDefinition = {
    val q"$mods object $tpname extends { ..$earlydefns } with ..$parents { $self => ..$stats }" = moduleDef
    ClassDefinition(self.asInstanceOf[ValDef], mods.asInstanceOf[Modifiers], tpname.asInstanceOf[TermName].toTypeName, Nil, Nil, stats.asInstanceOf[List[Tree]], parents.asInstanceOf[List[Tree]])
  }

  /**
   * Generate the specified syntax tree and assign it to the tree definition itself.
   * Used only when you modify the definition of the class itself. Such as add method/add field.
   *
   * @param classDecl
   * @param classInfoAction Content body added in class definition
   * @return
   */
  def appendClassBody(classDecl: ClassDef, classInfoAction: ClassDefinition => List[Tree]): c.universe.ClassDef = {
    val classInfo = mapToClassDeclInfo(classDecl)
    val ClassDef(mods, name, tparams, impl) = classDecl
    val Template(parents, self, body) = impl
    ClassDef(mods, name, tparams, Template(parents, self, body ++ classInfoAction(classInfo)))
  }

  // TODO fix, why cannot use ClassDef apply
  def prependImplDefBody(implDef: ImplDef, classInfoAction: ClassDefinition => List[Tree]): c.universe.Tree = {
    implDef match {
      case classDecl: ClassDef =>
        val classInfo = mapToClassDeclInfo(classDecl)
        val q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" = classDecl
        q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..${classInfoAction(classInfo) ++ stats} }"
      case moduleDef: ModuleDef =>
        val classInfo = mapToModuleDeclInfo(moduleDef)
        val q"$mods object $tpname extends { ..$earlydefns } with ..$parents { $self => ..$stats }" = moduleDef
        q"$mods object $tpname extends { ..$earlydefns } with ..$parents { $self => ..${classInfoAction(classInfo) ++ stats.toList} }"
    }
  }

  def appendImplDefSuper(implDef: ImplDef, classInfoAction: ClassDefinition => List[Tree]): c.universe.Tree = {
    implDef match {
      case classDecl: ClassDef =>
        val classInfo = mapToClassDeclInfo(classDecl)
        val q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" = classDecl
        q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..${parents ++ classInfoAction(classInfo)} { $self => ..$stats }"
      case moduleDef: ModuleDef =>
        val classInfo = mapToModuleDeclInfo(moduleDef)
        val q"$mods object $tpname extends { ..$earlydefns } with ..$parents { $self => ..$stats }" = moduleDef
        q"$mods object $tpname extends { ..$earlydefns } with ..${parents.toList ++ classInfoAction(classInfo)} { $self => ..$stats }"
    }
  }

  /**
   * Modify the method body of the method tree.
   *
   * @param defDef
   * @param defBodyAction Method body of final result
   * @return
   */
  def mapToMethodDef(defDef: DefDef, defBodyAction: => Tree): c.universe.DefDef = {
    val DefDef(mods, name, tparams, vparamss, tpt, rhs) = defDef
    DefDef(mods, name, tparams, vparamss, tpt, defBodyAction)
  }

  private[macros] case class ClassDefinition(
      self:            ValDef,
      mods:            Modifiers,
      className:       TypeName,
      classParamss:    List[List[Tree]],
      classTypeParams: List[Tree],
      body:            List[Tree],
      superClasses:    List[Tree],
      earlydefns:      List[Tree]       = Nil
  )

}
