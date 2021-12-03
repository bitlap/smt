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
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import java.util.UUID
import scala.reflect.macros.blackbox

/**
 *
 * @author 梦境迷离
 * @version 1.0,2021/12/3
 */
object Processable {

  def apply[Resp <: Message, P <: CustomRpcProcessor[Resp], S]
    (
    processRequest:   (Resp, RpcRequestClosure, S) ⇒ Message,
    processException: (RpcContext, Exception, S) ⇒ Message,
    service:          S,
    defaultResp:      Resp
  ): P = macro processorImpl[Resp, P, S]

  def processorImpl[Resp: c.WeakTypeTag, P: c.WeakTypeTag, S: c.WeakTypeTag](c: blackbox.Context)(
    processRequest:   c.Expr[(Resp, RpcRequestClosure, S) ⇒ Resp],
    processException: c.Expr[(RpcContext, Exception, S) ⇒ Resp],
    service:          c.Expr[S],
    defaultResp:      c.Expr[Resp]
  ): c.Expr[P] = {
    import c.universe._
    val className = TypeName(UUID.randomUUID().toString.replace("-", ""))
    val serviceType = weakTypeOf[S]
    val requestProtoType = weakTypeOf[Resp]
    val processor =
      q"""
       class $className(private val service: $serviceType, executor: java.util.concurrent.Executor = null)
         extends io.github.dreamylost.sofa.CustomRpcProcessor[$requestProtoType](executor, $defaultResp) {

         override def processRequest(request: $requestProtoType, done: com.alipay.sofa.jraft.rpc.RpcRequestClosure): Message = {
            $processRequest(request, done, service)
         }

         override def processError(rpcCtx: com.alipay.sofa.jraft.rpc.RpcContext, exception: Exception): com.google.protobuf.Message 
            = $processException(rpcCtx, exception, $service)

         override def interest(): String = classOf[$requestProtoType].getName

       }
       new $className($service)
     """
    val ret = c.Expr[P](processor)
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

