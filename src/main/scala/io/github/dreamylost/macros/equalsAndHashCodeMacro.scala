package io.github.dreamylost.macros

import scala.reflect.macros.whitebox

/**
 *
 * @author 梦境迷离
 * @since 2021/7/18
 * @version 1.0
 */
object equalsAndHashCodeMacro extends MacroCommon {

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    val args: (Boolean, Seq[String]) = extractArgumentsTuple2(c) {
      case q"new equalsAndHashCode(verbose=$verbose)" => (evalTree(c)(verbose.asInstanceOf[Tree]), Nil)
      case q"new equalsAndHashCode(excludeFields=$excludeFields)" => (false, evalTree(c)(excludeFields.asInstanceOf[Tree]))
      case q"new equalsAndHashCode(verbose=$verbose, excludeFields=$excludeFields)" => (evalTree(c)(verbose.asInstanceOf[Tree]), evalTree(c)(excludeFields.asInstanceOf[Tree]))
      case q"new equalsAndHashCode()" => (false, Nil)
      case _ => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
    }

    val annotateeClass: ClassDef = checkAndGetClassDef(c)(annottees: _*)
    val isCase: Boolean = isCaseClass(c)(annotateeClass)
    if (isCase) {
      c.abort(c.enclosingPosition, s"${ErrorMessage.ONLY_CLASS} classDef: $annotateeClass")
    }
    val excludeFields = args._2

    def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): Any = {
      val (className, annotteeClassParams, annotteeClassDefinitions, superClasses) = classDecl match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
          c.info(c.enclosingPosition, s"modifiedDeclaration className: $tpname, paramss: $paramss", force = args._1)
          (tpname, paramss, stats.asInstanceOf[Seq[Tree]], parents)
        case _ => c.abort(c.enclosingPosition, s"${ErrorMessage.ONLY_CLASS} classDef: $classDecl")
      }
      val ctorFieldNames = annotteeClassParams.asInstanceOf[List[List[Tree]]].flatten.filter(cf => classParamsIsPrivate(c)(cf))
      val allFieldsTermName = ctorFieldNames.map(f => getFieldTermName(c)(f))

      c.info(c.enclosingPosition, s"modifiedDeclaration compDeclOpt: $compDeclOpt, ctorFieldNames: $ctorFieldNames, " +
        s"annotteeClassParams: $superClasses", force = args._1)

      /**
       * Extract the internal fields of members belonging to the class.
       */
      def getClassMemberAllTermName: Seq[c.TermName] = {
        getClassMemberValDefs(c)(annotteeClassDefinitions).filter(_ match {
          case q"$mods var $tname: $tpt = $expr" if !excludeFields.contains(tname.asInstanceOf[TermName].decodedName.toString) => true
          case q"$mods val $tname: $tpt = $expr" if !excludeFields.contains(tname.asInstanceOf[TermName].decodedName.toString) => true
          case q"$mods val $pat = $expr" if !excludeFields.contains(pat.asInstanceOf[TermName].decodedName.toString) => true
          case q"$mods var $pat = $expr" if !excludeFields.contains(pat.asInstanceOf[TermName].decodedName.toString) => true
          case _ => false
        }).map(f => getFieldTermName(c)(f))
      }

      val existsCanEqual = getClassMemberDefDefs(c)(annotteeClassDefinitions) exists {
        case q"$mods def $tname[..$tparams](...$paramss): $tpt = $expr" if tname.toString() == "canEqual" && paramss.nonEmpty =>
          val params = paramss.asInstanceOf[List[List[Tree]]].flatten.map(pp => getMethodParamName(c)(pp))
          params.exists(p => p.decodedName.toString == "Any")
        case _ => false
      }

      // + super.hashCode
      val SDKClasses = Set("java.lang.Object", "scala.AnyRef")
      val canEqualsExistsInSuper = if (superClasses.nonEmpty && !superClasses.forall(sc => SDKClasses.contains(sc.toString()))) { // TODO better way
        true
      } else false

      // equals template
      def ==(termNames: Seq[TermName]): c.universe.Tree = {
        val getEqualsExpr = (termName: TermName) => {
          q"this.$termName.equals(t.$termName)"
        }
        val equalsExprs = termNames.map(getEqualsExpr)
        val modifiers = if (canEqualsExistsInSuper) Modifiers(Flag.OVERRIDE, typeNames.EMPTY, List()) else Modifiers(NoFlags, typeNames.EMPTY, List())
        val canEqual = if (existsCanEqual) q"" else q"$modifiers def canEqual(that: Any) = that.isInstanceOf[$className]"
        q"""
        $canEqual

        override def equals(that: Any): Boolean =
          that match {
            case t: $className => t.canEqual(this) && Seq(..$equalsExprs).forall(f => f) && ${if (canEqualsExistsInSuper) q"super.equals(that)" else q"true"}
            case _ => false
        }
       """
      }

      // hashcode template
      def ##(termNames: Seq[TermName]): c.universe.Tree = {
        // the algorithm see https://alvinalexander.com/scala/how-to-define-equals-hashcode-methods-in-scala-object-equality/
        // We use default 1.
        if (!canEqualsExistsInSuper) {
          q"""
         override def hashCode(): Int = {
            val state = Seq(..$termNames)
            state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
          }
          """
        } else {
          q"""
         override def hashCode(): Int = {
            val state = Seq(..$termNames)
            state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b) + super.hashCode
          }
          """
        }
      }

      val allTernNames = allFieldsTermName ++ getClassMemberAllTermName
      val hashcode = ##(allTernNames)
      val equals = ==(allTernNames)
      val equalsAndHashcode =
        q"""
          ..$equals
          $hashcode
         """
      // return with object if it exists
      val resTree = annotateeClass match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
          val originalStatus = q"{ ..$stats }"
          val append =
            q"""
              ..$originalStatus
              ..$equalsAndHashcode
             """
          q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..${append} }"
      }
      c.Expr[Any](treeResultWithCompanionObject(c)(resTree, annottees: _*))
    }

    val resTree = handleWithImplType(c)(annottees: _*)(modifiedDeclaration)
    printTree(c)(force = args._1, resTree.tree)

    resTree
  }
}
