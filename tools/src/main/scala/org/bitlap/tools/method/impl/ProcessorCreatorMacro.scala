/*
 * Copyright (c) 2022 bitlap
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

package org.bitlap.tools.method.impl

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

  def SimpleImpl[RRC: c.WeakTypeTag, RRP: c.WeakTypeTag, RC: c.WeakTypeTag, Req: c.WeakTypeTag, Resp: c.WeakTypeTag, Service: c.WeakTypeTag, E: c.WeakTypeTag]
    (c: blackbox.Context)
    (
    defaultResp:      c.Expr[Req],
    processRequest:   c.Expr[(Service, RRC, Req) ⇒ Req],
    processException: c.Expr[(Service, RC, Exception) ⇒ Req]
  )(service: c.Expr[Service], executor: c.Expr[E]): c.Expr[RRP] = { // parameters in order, parameter names differ will compile error
    import c.universe._
    checkTree[RRC, RRP, RC, Service](c)(needCheckService = false)
    val serviceType = weakTypeOf[Service]
    val className = TypeName(classNamePrefix + MacroCache.getIdentityId)
    val reqProtoType = weakTypeOf[Req]
    val rpcRequestClosureType = weakTypeOf[RRC]
    val rpcContextType = weakTypeOf[RC]
    val respProtoType = weakTypeOf[Resp]
    val processor =
      q"""
       class $className(private val service: $serviceType, executor: java.util.concurrent.Executor = null)
         extends com.alipay.sofa.jraft.rpc.RpcRequestProcessor[$reqProtoType](executor, $defaultResp) with com.typesafe.scalalogging.LazyLogging {
            override def handleRequest(rpcCtx: com.alipay.sofa.jraft.rpc.RpcContext, request: $reqProtoType) {
              try {
                val msg = processRequest(request, new com.alipay.sofa.jraft.rpc.RpcRequestClosure(rpcCtx, this.defaultResp))
                if (msg != null) {
                  rpcCtx.sendResponse(msg)
                }
              } catch {
                case e: Exception =>
                  logger.error("handleRequest" + request + "failed", e)
                  rpcCtx.sendResponse(processError(rpcCtx, e))
              }
            }
           override def interest(): String = classOf[$reqProtoType].getName

           def processRequest(request: $reqProtoType, done: $rpcRequestClosureType): $respProtoType = {
              $processRequest(service, done, request)
           }

           def processError(rpcCtx: $rpcContextType, exception: Exception): $respProtoType = {
              $processException(service, rpcCtx, exception)
           }
       }
       new $className($service, $executor)
     """
    printTree[RRP](c)(processor)
  }

  def WithoutExecutorAndDefaultResp[RRC: c.WeakTypeTag, RRP: c.WeakTypeTag, RC: c.WeakTypeTag, Req: c.WeakTypeTag, Resp: c.WeakTypeTag, Service: c.WeakTypeTag]
    (c: blackbox.Context)(
    processRequest:   c.Expr[(Service, RRC, Req) ⇒ Req],
    processException: c.Expr[(Service, RC, Exception) ⇒ Req]
  )(service: c.Expr[Service]): c.Expr[RRP] = {
    import c.universe._
    checkTree[RRC, RRP, RC, Service](c)(needCheckService = false)
    val serviceType = weakTypeOf[Service]
    val className = TypeName(classNamePrefix + MacroCache.getIdentityId)
    val reqProtoType = weakTypeOf[Req]
    val rpcRequestClosureType = weakTypeOf[RRC]
    val rpcContextType = weakTypeOf[RC]
    val respProtoType = weakTypeOf[Resp]
    val respProtoCompanionType = weakTypeOf[Resp].companion //getDefaultInstance is static method, it's in companion
    val processor =
      q"""
       class $className(private val service: $serviceType, executor: java.util.concurrent.Executor = null)
         extends com.alipay.sofa.jraft.rpc.RpcRequestProcessor[$reqProtoType](executor, $respProtoCompanionType.getDefaultInstance)
           with com.typesafe.scalalogging.LazyLogging {
            override def handleRequest(rpcCtx: com.alipay.sofa.jraft.rpc.RpcContext, request: $reqProtoType) {
              try {
                val msg = processRequest(request, new com.alipay.sofa.jraft.rpc.RpcRequestClosure(rpcCtx, this.defaultResp))
                if (msg != null) {
                  rpcCtx.sendResponse(msg)
                }
              } catch {
                case e: Exception =>
                  logger.error("handleRequest" + request + "failed", e)
                  rpcCtx.sendResponse(processError(rpcCtx, e))
              }
            }
           override def interest(): String = classOf[$reqProtoType].getName

           def processRequest(request: $reqProtoType, done: $rpcRequestClosureType): $respProtoType = {
              $processRequest(service, done, request)
           }

           def processError(rpcCtx: $rpcContextType, exception: Exception): $respProtoType = {
              $processException(service, rpcCtx, exception)
           }
       }
       new $className($service)
     """
    printTree[RRP](c)(processor)
  }

  def OnlyWithFunctionalParameters[Service: c.WeakTypeTag, RRC: c.WeakTypeTag, RRP: c.WeakTypeTag, RC: c.WeakTypeTag, Req: c.WeakTypeTag, Resp: c.WeakTypeTag]
    (c: blackbox.Context)(
    processRequest: c.Expr[(Service, RRC, Req) ⇒ Req])
    (
    processException: c.Expr[(Service, RC, Exception) ⇒ Req]
  ): c.Expr[RRP] = {
    import c.universe._
    checkTree[RRC, RRP, RC, Service](c)(needCheckService = true)
    val serviceType = weakTypeOf[Service]
    val className = TypeName(classNamePrefix + MacroCache.getIdentityId)
    val reqProtoType = weakTypeOf[Req]
    val rpcRequestClosureType = weakTypeOf[RRC]
    val rpcContextType = weakTypeOf[RC]
    val respProtoType = weakTypeOf[Resp]
    val respProtoCompanionType = weakTypeOf[Resp].companion //getDefaultInstance is static method, it's in companion
    val processor =
      q"""
       class $className(private val service: $serviceType, executor: java.util.concurrent.Executor = null)
         extends com.alipay.sofa.jraft.rpc.RpcRequestProcessor[$reqProtoType](executor, $respProtoCompanionType.getDefaultInstance)
         with com.typesafe.scalalogging.LazyLogging {
            override def handleRequest(rpcCtx: com.alipay.sofa.jraft.rpc.RpcContext, request: $reqProtoType) {
              try {
                val msg = processRequest(request, new com.alipay.sofa.jraft.rpc.RpcRequestClosure(rpcCtx, this.defaultResp))
                if (msg != null) {
                  rpcCtx.sendResponse(msg)
                }
              } catch {
                case e: Exception =>
                  logger.error("handleRequest" + request + "failed", e)
                  rpcCtx.sendResponse(processError(rpcCtx, e))
              }
            }
           override def interest(): String = classOf[$reqProtoType].getName

           def processRequest(request: $reqProtoType, done: $rpcRequestClosureType): $respProtoType = {
              $processRequest(service, done, request)
           }

           def processError(rpcCtx: $rpcContextType, exception: Exception): $respProtoType = {
              $processException(service, rpcCtx, exception)
           }
       }
       val service = new org.bitlap.tools.method.impl.Creator[$serviceType].createInstance(null)(0)
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

  private def checkTree[RRC: c.WeakTypeTag, RRP: c.WeakTypeTag, RC: c.WeakTypeTag, Service: c.WeakTypeTag](c: blackbox.Context)
    (needCheckService: Boolean = true): Unit = {
    import c.universe._
    val serviceType = weakTypeOf[Service]
    if (needCheckService && (serviceType.typeSymbol.isAbstract || !serviceType.typeSymbol.isClass)) {
      c.abort(c.enclosingPosition, "Not support for abstract classes")
    }
    if (serviceType.typeSymbol.isModuleClass) {
      c.abort(c.enclosingPosition, "Not support for module classes")
    }
    val rpcRequestClosureType = weakTypeOf[RRC]
    if (!rpcRequestClosureType.resultType.toString.equals("com.alipay.sofa.jraft.rpc.RpcRequestClosure")) {
      c.abort(c.enclosingPosition, s"`RRC` only support for `com.alipay.sofa.jraft.rpc.RpcRequestClosure`, not `${rpcRequestClosureType.resultType.toString}`")
    }
    val rpcContextType = weakTypeOf[RC]
    if (!rpcContextType.resultType.toString.equals("com.alipay.sofa.jraft.rpc.RpcContext")) {
      c.abort(c.enclosingPosition, s"`RC` only support for `com.alipay.sofa.jraft.rpc.RpcContext`, not `${rpcContextType.resultType.toString}`")
    }
  }
}
