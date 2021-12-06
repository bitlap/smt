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

import java.util.concurrent.Executor

/**
 * Create Processor by macro
 *
 * @author 梦境迷离
 * @version 1.0,2021/12/6
 */
object ProcessorCreator {

  def apply[RRC, RRP[_ <: Req], RC, Req, Resp, Service, E <: Executor]
    (service: Service, defaultResp: Resp, executor: E)
    (
    processRequest:   (Service, RRC, Req) ⇒ Resp,
    processException: (Service, RC, Exception) ⇒ Resp
  ): RRP[Req] = macro ProcessorCreatorMacro.SimpleImpl[RRC, RRP[_ <: Req], RC, Req, Resp, Service, E]

  def apply[RRC, RRP[_ <: Req], RC, Req, Resp, Service]
    (service: Service)
    (
    processRequest:   (Service, RRC, Req) ⇒ Resp,
    processException: (Service, RC, Exception) ⇒ Resp
  ): RRP[Req] = macro ProcessorCreatorMacro.WithoutExecutorAndDefaultResp[RRC, RRP[_ <: Req], RC, Req, Resp, Service]

  def apply[RRC, RRP[_ <: Req], RC, Req, Resp, Service]
    (
    processRequest:   (Service, RRC, Req) ⇒ Resp,
    processException: (Service, RC, Exception) ⇒ Resp
  ): RRP[Req] = macro ProcessorCreatorMacro.OnlyWithFunctionalParams[RRC, RRP[_ <: Req], RC, Req, Resp, Service]
}
