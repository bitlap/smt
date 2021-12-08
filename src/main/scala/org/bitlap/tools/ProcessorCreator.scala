/*
 * Copyright (c) 2021 org.bitlap
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

package org.bitlap.tools

import org.bitlap.tools.macros.ProcessorCreatorMacro

import java.util.concurrent.Executor

/**
 * The macro util to generate processor for alipay sofa jraft rpc.
 *
 * @author 梦境迷离
 * @version 1.0,2021/12/6
 */
object ProcessorCreator {

  /**
   *
   * @param service          Instance of the [[Service]]
   * @param defaultResp      Default instance of the Response Message
   * @param executor         Instance of the Executor
   * @param processRequest   Function to handle request
   * @param processException Function to handle exception
   * @tparam RRC     RpcRequestClosure
   * @tparam RRP     RpcRequestProcessor
   * @tparam RC      RpcContext
   * @tparam Req     Request Message of the protobuf
   * @tparam Resp    Response Message of the protobuf
   * @tparam Service Should be custom class/interface/trait which handle the business logic of Processors
   * @tparam E       Should be subclass of the Executor
   * @return [[ RRP ]] Instance of the RpcRequestProcessor subclass
   */
  def apply[RRC, RRP[_ <: Req], RC, Req, Resp, Service, E <: Executor]
    (
    defaultResp:      Resp,
    processRequest:   (Service, RRC, Req) ⇒ Resp,
    processException: (Service, RC, Exception) ⇒ Resp
  )(implicit service: Service, executor: E): RRP[Req] = macro ProcessorCreatorMacro.SimpleImpl[RRC, RRP[_ <: Req], RC, Req, Resp, Service, E]

  def apply[RRC, RRP[_ <: Req], RC, Req, Resp, Service]
    (
    processRequest:   (Service, RRC, Req) ⇒ Resp,
    processException: (Service, RC, Exception) ⇒ Resp
  )(implicit service: Service): RRP[Req] = macro ProcessorCreatorMacro.WithoutExecutorAndDefaultResp[RRC, RRP[_ <: Req], RC, Req, Resp, Service]

  /**
   * Having two identical type parameters will cause the compiler to recognize error and change the order of generics to avoid.
   */
  def apply[Service, RRC, RRP[_ <: Req], RC, Req, Resp]
    (
    processRequest:   (Service, RRC, Req) ⇒ Resp,
    processException: (Service, RC, Exception) ⇒ Resp
  ): RRP[Req] = macro ProcessorCreatorMacro.OnlyWithFunctionalParameters[Service, RRC, RRP[_ <: Req], RC, Req, Resp]
}
