package io.github.dreamylost

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import com.alipay.sofa.jraft.rpc.{ RpcContext, RpcRequestClosure, RpcRequestProcessor }
import io.github.dreamylost.sofa.{ CustomRpcProcessor, Processable, ProcessorCreator }
import io.github.dreamylost.test.proto.BOpenSession.{ BOpenSessionReq, BOpenSessionResp }
import java.util.concurrent.Executor

/**
 *
 * @author 梦境迷离
 * @version 1.0,2021/12/6
 */
class ProcessorCreatorTest extends AnyFlatSpec with Matchers {

  // origin
  "ProcessorCreator1" should "compile ok" in {
    val openSession = ProcessorCreator[RpcRequestClosure,
      RpcRequestProcessor, RpcContext, BOpenSessionReq, BOpenSessionResp, NetService, Executor](
      new NetService, BOpenSessionResp.getDefaultInstance, (command: Runnable) => ???
    )(
      (service, rpcRequestClosure, req) => {
        import scala.jdk.CollectionConverters.MapHasAsScala
        val username = req.getUsername
        val password = req.getPassword
        val configurationMap = req.getConfigurationMap
        val ret = service.openSession(username, password, configurationMap.asScala.toMap)
        BOpenSessionResp.newBuilder().setSessionHandle(ret).build()
      },
      (service, rpcContext, exception) => {
        BOpenSessionResp.newBuilder().setStatus(exception.getLocalizedMessage).build()
      }
    )

    println(openSession.defaultResp)

    println(openSession.getClass.getClass.getName)

    println(openSession.interest())
  }

}
