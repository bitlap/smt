package io.github.dreamylost.sofa

import com.alipay.sofa.jraft.rpc.{ RpcRequestProcessor, RpcRequestClosure, RpcContext }
import com.typesafe.scalalogging.LazyLogging
import com.google.protobuf.Message
import java.util.concurrent.Executor
import scala.reflect.ClassTag

/**
 * Common processor, use should write it in classpath
 *
 * @param executor  The executor used to execute the specified sofa RPC request
 * @param defaultResp Default message instance for sofa
 * @tparam Req The Request proto message for sofa
 * @author 梦境迷离
 * @version 1.0,2021/12/3
 */
abstract class CustomAbstractRpcProcessor[Req <: Message](executor: Executor, override val defaultResp: Message)(implicit reqClassTag: ClassTag[Req])
  extends RpcRequestProcessor[Req](executor, defaultResp) with LazyLogging {

  override def handleRequest(rpcCtx: RpcContext, request: Req) {
    try {
      val msg = processRequest(request, new RpcRequestClosure(rpcCtx, this.defaultResp))
      if (msg != null) {
        rpcCtx.sendResponse(msg)
      }
    } catch {
      case e: Exception =>
        logger.error("handleRequest " + request + "failed", e) //StringContext
        rpcCtx.sendResponse(processError(rpcCtx, e))
    }
  }

  def processError(rpcCtx: RpcContext, exception: Exception): Message

  override def interest(): String = {
    reqClassTag.runtimeClass.getName
  }
}