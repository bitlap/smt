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

package io.github.dreamylost.sofa

import com.alipay.sofa.jraft.rpc.{ RpcContext, RpcRequestClosure }
import com.google.protobuf.Message

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import scala.reflect.macros.blackbox

/**
 * Processable macro
 *
 * @author 梦境迷离
 * @since 2021/12/4
 * @version 1.0
 */
object ProcessableMacro {

  private val classNamePrefix: String = "anonymous_"

  def processorWithDefaultRespServiceImpl[Req <: Message: c.WeakTypeTag, Resp <: Message: c.WeakTypeTag, Service: c.WeakTypeTag]
    (c: blackbox.Context)
    (
    processRequest:   c.Expr[(Service, RpcRequestClosure, Req) ⇒ Req],
    processException: c.Expr[(Service, RpcContext, Exception) ⇒ Req]
  ): c.Expr[CustomRpcProcessor[Req]] = {
    import c.universe._
    val serviceType = weakTypeOf[Service]
    if (serviceType.typeSymbol.isAbstract || !serviceType.typeSymbol.isClass) {
      c.abort(c.enclosingPosition, "Not support for abstract classes")
    }
    if (serviceType.typeSymbol.isModuleClass) {
      c.abort(c.enclosingPosition, "Not support for module classes")
    }
    val className = TypeName(classNamePrefix + UUID.randomUUID().toString.replace("-", ""))
    val reqProtoType = weakTypeOf[Req]
    val respProtoType = weakTypeOf[Resp].companion //getDefaultInstance is static method, it's in companion
    val processor =
      q"""
       class $className(private val service: $serviceType, executor: java.util.concurrent.Executor = null)
         extends io.github.dreamylost.sofa.CustomRpcProcessor[$reqProtoType](executor, $respProtoType.getDefaultInstance) {

         override def processRequest(request: $reqProtoType, done: com.alipay.sofa.jraft.rpc.RpcRequestClosure): com.google.protobuf.Message = {
            $processRequest(service, done, request)
         }

         override def processError(rpcCtx: com.alipay.sofa.jraft.rpc.RpcContext, exception: Exception): com.google.protobuf.Message = {
            $processException(service, rpcCtx, exception)
         }
       }
       val service = new io.github.dreamylost.macros.Creator[$serviceType].createInstance(null)(0)
       new $className(service, null)
     """
    printTree[Req](c)(processor)
  }

  def processorWithDefaultRespImpl[Service: c.WeakTypeTag, Req <: Message: c.WeakTypeTag, Resp <: Message: c.WeakTypeTag]
    (c: blackbox.Context)
    (service: c.Expr[Service])
    (
    processRequest:   c.Expr[(Service, RpcRequestClosure, Req) ⇒ Req],
    processException: c.Expr[(Service, RpcContext, Exception) ⇒ Req]
  ): c.Expr[CustomRpcProcessor[Req]] = {
    import c.universe._
    val className = TypeName(classNamePrefix + UUID.randomUUID().toString.replace("-", ""))
    val serviceType = weakTypeOf[Service]
    val reqProtoType = weakTypeOf[Req]
    val respProtoType = weakTypeOf[Resp].companion //getDefaultInstance is static method, it's in companion
    val processor =
      q"""
       class $className(private val service: $serviceType, executor: java.util.concurrent.Executor = null)
         extends io.github.dreamylost.sofa.CustomRpcProcessor[$reqProtoType](executor, $respProtoType.getDefaultInstance) {

         override def processRequest(request: $reqProtoType, done: com.alipay.sofa.jraft.rpc.RpcRequestClosure): com.google.protobuf.Message = {
            $processRequest(service, done, request)
         }

         override def processError(rpcCtx: com.alipay.sofa.jraft.rpc.RpcContext, exception: Exception): com.google.protobuf.Message = {
            $processException(service, rpcCtx, exception)
         }
       }
       new $className($service, null)
     """
    printTree[Req](c)(processor)
  }

  def processorImpl[Req <: Message: c.WeakTypeTag, Service: c.WeakTypeTag, Executor: c.WeakTypeTag]
    (c: blackbox.Context)
    (
    service:     c.Expr[Service],
    defaultResp: c.Expr[Req],
    executor:    c.Expr[Executor]
  )
    (
    processRequest:   c.Expr[(Service, RpcRequestClosure, Req) ⇒ Req],
    processException: c.Expr[(Service, RpcContext, Exception) ⇒ Req]
  ): c.Expr[CustomRpcProcessor[Req]] = {
    import c.universe._
    val className = TypeName(classNamePrefix + UUID.randomUUID().toString.replace("-", ""))
    val serviceType = weakTypeOf[Service]
    val requestProtoType = weakTypeOf[Req]
    val processor =
      q"""
       class $className(private val service: $serviceType, executor: java.util.concurrent.Executor = null)
         extends io.github.dreamylost.sofa.CustomRpcProcessor[$requestProtoType](executor, $defaultResp) {

         override def processRequest(request: $requestProtoType, done: com.alipay.sofa.jraft.rpc.RpcRequestClosure): com.google.protobuf.Message = {
            $processRequest(service, done, request)
         }

         override def processError(rpcCtx: com.alipay.sofa.jraft.rpc.RpcContext, exception: Exception): com.google.protobuf.Message = {
            $processException(service, rpcCtx, exception)
         }
       }
       new $className($service, $executor)
     """
    printTree[Req](c)(processor)
  }

  private def printTree[Req <: Message](c: blackbox.Context)(processor: c.Tree): c.Expr[CustomRpcProcessor[Req]] = {
    val ret = c.Expr[CustomRpcProcessor[Req]](processor)
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
