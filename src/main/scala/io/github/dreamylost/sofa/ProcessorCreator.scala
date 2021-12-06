package io.github.dreamylost.sofa

import java.util.concurrent.Executor

/**
 *
 * @author 梦境迷离
 * @version 1.0,2021/12/6
 */
object ProcessorCreator {

  def apply[RRC, RRP[_ <: Req], RC, Req, Resp, Service,E <: Executor]
  (service: Service, defaultResp: Resp, executor: E)
    (
      processRequest: (Service, RRC, Req) ⇒ Resp,
      processException: (Service, RC, Exception) ⇒ Resp
    ): RRP[Req] = macro ProcessorCreatorMacro.simpleImpl[RRC, RRP[_ <: Req], RC, Req, Resp, Service, E]

  def apply[RRC, RRP[_ <: Req], RC, Req, Resp, Service]
  (service: Service)
    (
      processRequest: (Service, RRC, Req) ⇒ Resp,
      processException: (Service, RC, Exception) ⇒ Resp
    ): RRP[Req] = macro ProcessorCreatorMacro.implWithoutExecutorAndDefaultResp[RRC, RRP[_ <: Req], RC, Req, Resp, Service]

  def apply[RRC, RRP[_ <: Req], RC, Req, Resp, Service]
  (
    processRequest: (Service, RRC, Req) ⇒ Resp,
    processException: (Service, RC, Exception) ⇒ Resp
  ): RRP[Req] = macro ProcessorCreatorMacro.implOnlyWithFunctionalParams[RRC, RRP[_ <: Req], RC, Req, Resp, Service]
}
