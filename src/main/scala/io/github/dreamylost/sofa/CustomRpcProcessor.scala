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

import com.alipay.sofa.jraft.rpc.{ RpcContext, RpcRequestClosure, RpcRequestProcessor }
import com.google.protobuf.Message
import com.typesafe.scalalogging.LazyLogging

import java.util.concurrent.Executor
import scala.reflect.ClassTag

/**
 * @param executor    The executor used to execute the specified sofa RPC request
 * @param defaultResp Default message instance for sofa
 * @tparam Req The Request proto message for sofa
 * @author 梦境迷离
 * @version 1.0,2021/12/3
 */
abstract class CustomRpcProcessor[Req <: Message](executor: Executor, override val defaultResp: Message)(implicit reqClassTag: ClassTag[Req])
  extends RpcRequestProcessor[Req](executor, defaultResp) with LazyLogging {

  override def handleRequest(rpcCtx: RpcContext, request: Req) {
    try {
      val msg = processRequest(request, new RpcRequestClosure(rpcCtx, this.defaultResp))
      if (msg != null) {
        rpcCtx.sendResponse(msg)
      }
    } catch {
      case e: Exception =>
        logger.error(s"handleRequest $request failed", e)
        rpcCtx.sendResponse(processError(rpcCtx, e))
    }
  }

  def processError(rpcCtx: RpcContext, exception: Exception): Message

  override def interest(): String = {
    reqClassTag.runtimeClass.getName
  }
}
