package io.github.dreamylost

import com.alipay.sofa.jraft.rpc.{ RpcContext, RpcRequestClosure, RpcRequestProcessor }
import io.github.dreamylost.test.proto.BOpenSession.{ BOpenSessionReq, BOpenSessionResp }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.github.dreamylost.sofa.ProcessorCreator

import java.util.concurrent.Executor

/**
 *
 * @author 梦境迷离
 * @version 1.0,2021/12/6
 */
class ProcessorCreatorTest extends AnyFlatSpec with Matchers {

  // origin
  "ProcessorCreator1" should "compile ok" in {
    val openSession = ProcessorCreator[RpcRequestClosure, RpcRequestProcessor, RpcContext, BOpenSessionReq, BOpenSessionResp, NetService, Executor](
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

  // simple v1
  "ProcessorCreator2" should "compile ok" in {
    val openSession = ProcessorCreator[RpcRequestClosure, RpcRequestProcessor, RpcContext, BOpenSessionReq, BOpenSessionResp, NetService](new NetService)(
      (service, _, req) => {
        import scala.jdk.CollectionConverters.MapHasAsScala
        val username = req.getUsername
        val password = req.getPassword
        val configurationMap = req.getConfigurationMap
        val ret = service.openSession(username, password, configurationMap.asScala.toMap)
        BOpenSessionResp.newBuilder().setSessionHandle(ret).build()
      },
      (_, _, exception) => {
        BOpenSessionResp.newBuilder().setStatus(exception.getLocalizedMessage).build()
      }
    )

    println(openSession.defaultResp)

    println(openSession.getClass.getClass.getName)

    println(openSession.interest())
  }

  // simple v2
  "ProcessorCreator3" should "compile ok" in {
    // NetService must be a class and with an no parameter construction
    val openSession = ProcessorCreator[RpcRequestClosure, RpcRequestProcessor, RpcContext, BOpenSessionReq, BOpenSessionResp, NetService](
      (service: NetService, rpc: RpcRequestClosure, req: BOpenSessionReq) => {
        import scala.jdk.CollectionConverters.MapHasAsScala
        val username = req.getUsername
        val password = req.getPassword
        val configurationMap = req.getConfigurationMap
        val ret = service.openSession(username, password, configurationMap.asScala.toMap)
        BOpenSessionResp.newBuilder().setSessionHandle(ret).build()
      },
      (service: NetService, rpc: RpcContext, exception: Exception) => {
        BOpenSessionResp.newBuilder().setStatus(exception.getLocalizedMessage).build()
      }
    )

    println(openSession.defaultResp)

    println(openSession.getClass.getClass.getName)

    println(openSession.interest())
  }
}
