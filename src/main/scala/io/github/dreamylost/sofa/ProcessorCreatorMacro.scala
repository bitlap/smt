package io.github.dreamylost.sofa

import io.github.dreamylost.macros.MacroCache

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.reflect.macros.blackbox

/**
 *
 * @author 梦境迷离
 * @version 1.0,2021/12/6
 */
object ProcessorCreatorMacro {


  private val classNamePrefix: String = "AnonProcessor$"
  private val cacheKey:String = "customRpcProcessor"

  def simpleImpl[RRC: c.WeakTypeTag, RRP: c.WeakTypeTag,
    RC: c.WeakTypeTag, Req: c.WeakTypeTag, Resp: c.WeakTypeTag,
    Service: c.WeakTypeTag, E: c.WeakTypeTag]
  (c: blackbox.Context)
    (
      service: c.Expr[Service],
      defaultResp: c.Expr[Req],
      executor: c.Expr[E]
    )
    (
      processRequest: c.Expr[(Service, RRC, Req) ⇒ Req],
      processException: c.Expr[(Service, RC, Exception) ⇒ Req]
    ): c.Expr[RRP] = {
    import c.universe._
    val serviceType = weakTypeOf[Service]
    if (serviceType.typeSymbol.isAbstract || !serviceType.typeSymbol.isClass) {
      c.abort(c.enclosingPosition, "Not support for abstract classes")
    }
    if (serviceType.typeSymbol.isModuleClass) {
      c.abort(c.enclosingPosition, "Not support for module classes")
    }
    val className = TypeName(classNamePrefix + MacroCache.getIdentityId)
    val reqProtoType = weakTypeOf[Req]
    val rpcRequestClosureType = weakTypeOf[RRC]
    if (!rpcRequestClosureType.resultType.toString.equals("com.alipay.sofa.jraft.rpc.RpcRequestClosure")) {
      c.abort(c.enclosingPosition, s"`RRC` only support for `com.alipay.sofa.jraft.rpc.RpcRequestClosure`, not ${rpcRequestClosureType.resultType.toString}")
    }
    val rpcContextType = weakTypeOf[RC]
    if (!rpcContextType.resultType.toString.equals("com.alipay.sofa.jraft.rpc.RpcContext")) {
      c.abort(c.enclosingPosition, s"`RRC` only support for `com.alipay.sofa.jraft.rpc.RpcContext`, not ${rpcContextType.resultType.toString}")
    }
    val respProtoType = weakTypeOf[Resp]
    val processor =
      q"""
       class $className(private val service: $serviceType, executor: java.util.concurrent.Executor = null)
         extends CustomAbstractRpcProcessor[$reqProtoType](executor, $defaultResp) {

         override def processRequest(request: $reqProtoType, done: $rpcRequestClosureType): $respProtoType = {
            $processRequest(service, done, request)
         }

         override def processError(rpcCtx: $rpcContextType, exception: Exception): $respProtoType = {
            $processException(service, rpcCtx, exception)
         }
       }
       new $className($service, $executor)
     """
    printTree[RRP](c)(processor)
  }


  def implWithoutExecutorAndDefaultResp[RRC: c.WeakTypeTag, RRP: c.WeakTypeTag,
    RC: c.WeakTypeTag, Req: c.WeakTypeTag, Resp: c.WeakTypeTag, Service: c.WeakTypeTag]
  (c: blackbox.Context)(service: c.Expr[Service])(
    processRequest: c.Expr[(Service, RRC, Req) ⇒ Req],
    processException: c.Expr[(Service, RC, Exception) ⇒ Req]
  ): c.Expr[RRP] = {
    import c.universe._
    val serviceType = weakTypeOf[Service]
    if (serviceType.typeSymbol.isAbstract || !serviceType.typeSymbol.isClass) {
      c.abort(c.enclosingPosition, "Not support for abstract classes")
    }
    if (serviceType.typeSymbol.isModuleClass) {
      c.abort(c.enclosingPosition, "Not support for module classes")
    }
    val className = TypeName(classNamePrefix + MacroCache.getIdentityId)
    val reqProtoType = weakTypeOf[Req]
    val rpcRequestClosureType = weakTypeOf[RRC]
    if (!rpcRequestClosureType.resultType.toString.equals("com.alipay.sofa.jraft.rpc.RpcRequestClosure")) {
      c.abort(c.enclosingPosition, s"`RRC` only support for `com.alipay.sofa.jraft.rpc.RpcRequestClosure`, not ${rpcRequestClosureType.resultType.toString}")
    }
    val rpcContextType = weakTypeOf[RC]
    if (!rpcContextType.resultType.toString.equals("com.alipay.sofa.jraft.rpc.RpcContext")) {
      c.abort(c.enclosingPosition, s"`RRC` only support for `com.alipay.sofa.jraft.rpc.RpcContext`, not ${rpcContextType.resultType.toString}")
    }
    val respProtoType = weakTypeOf[Resp]
    val respProtoCompanionType = weakTypeOf[Resp].companion //getDefaultInstance is static method, it's in companion
    val processor =
      q"""
       class $className(private val service: $serviceType, executor: java.util.concurrent.Executor = null)
         extends CustomAbstractRpcProcessor[$reqProtoType](executor, $respProtoCompanionType.getDefaultInstance) {

         override def processRequest(request: $reqProtoType, done: $rpcRequestClosureType): $respProtoType = {
            $processRequest(service, done, request)
         }

         override def processError(rpcCtx: $rpcContextType, exception: Exception): $respProtoType = {
            $processException(service, rpcCtx, exception)
         }
       }
       new $className($service)
     """
    printTree[RRP](c)(processor)
  }


  def implOnlyWithFunctionalParams[RRC: c.WeakTypeTag, RRP: c.WeakTypeTag,
    RC: c.WeakTypeTag, Req: c.WeakTypeTag, Resp: c.WeakTypeTag, Service: c.WeakTypeTag]
  (c: blackbox.Context)(
    processRequest: c.Expr[(Service, RRC, Req) ⇒ Req],
    processException: c.Expr[(Service, RC, Exception) ⇒ Req]
  ): c.Expr[RRP] = {
    import c.universe._
    val customType= c.typecheck(tq"${MacroCache.customRpcProcessorCache(cacheKey).asInstanceOf[Tree]}", c.TYPEmode).tpe
    val serviceType = weakTypeOf[Service]
    if (serviceType.typeSymbol.isAbstract || !serviceType.typeSymbol.isClass) {
      c.abort(c.enclosingPosition, "Not support for abstract classes")
    }
    if (serviceType.typeSymbol.isModuleClass) {
      c.abort(c.enclosingPosition, "Not support for module classes")
    }
    val className = TypeName(classNamePrefix + MacroCache.getIdentityId)
    val reqProtoType = weakTypeOf[Req]
    val rpcRequestClosureType = weakTypeOf[RRC]
    if (!rpcRequestClosureType.resultType.toString.equals("com.alipay.sofa.jraft.rpc.RpcRequestClosure")) {
      c.abort(c.enclosingPosition, s"`RRC` only support for `com.alipay.sofa.jraft.rpc.RpcRequestClosure`, not ${rpcRequestClosureType.resultType.toString}")
    }
    val rpcContextType = weakTypeOf[RC]
    if (!rpcContextType.resultType.toString.equals("com.alipay.sofa.jraft.rpc.RpcContext")) {
      c.abort(c.enclosingPosition, s"`RRC` only support for `com.alipay.sofa.jraft.rpc.RpcContext`, not ${rpcContextType.resultType.toString}")
    }
    val respProtoType = weakTypeOf[Resp]
    val respProtoCompanionType = weakTypeOf[Resp].companion //getDefaultInstance is static method, it's in companion
    val processor =
      q"""
       class $className(private val service: $serviceType, executor: java.util.concurrent.Executor = null)
         extends $customType[$reqProtoType](executor, $respProtoCompanionType.getDefaultInstance) {

         override def processRequest(request: $reqProtoType, done: $rpcRequestClosureType): $respProtoType = {
            $processRequest(service, done, request)
         }

         override def processError(rpcCtx: $rpcContextType, exception: Exception): $respProtoType = {
            $processException(service, rpcCtx, exception)
         }
       }
       val service = new io.github.dreamylost.macros.Creator[$serviceType].createInstance(null)(0)
       new $className(service)
     """
    printTree[RRP](c)(processor)
  }

  private def printTree[RRP](c: blackbox.Context)(processor: c.Tree): c.Expr[RRP] = {
    val ret = c.Expr[RRP](processor)
    c.info(
      c.enclosingPosition,
      s"\n###### Time: ${
        ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
      } " +
        s"Expanded macro start ######\n" + ret.toString() + "\n###### Expanded macro end ######\n",
      force = true
    )
    ret
  }

}
