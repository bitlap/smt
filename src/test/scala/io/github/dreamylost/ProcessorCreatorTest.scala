package io.github.dreamylost

import com.alipay.sofa.jraft.rpc.{ RpcContext, RpcRequestClosure, RpcRequestProcessor }
import io.github.dreamylost.test.proto.BOpenSession.{ BOpenSessionReq, BOpenSessionResp }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.Executor

/**
 *
 * @author 梦境迷离
 * @version 1.0,2021/12/6
 */
class ProcessorCreatorTest extends AnyFlatSpec with Matchers {

  // please exec `sbt compile` to generate java class fof the protobuf
  // origin
  "ProcessorCreator1" should "compile ok" in {
    implicit val service = new NetService
    implicit val executor: Executor = new Executor {
      override def execute(command: Runnable): Unit = ()
    }
    val openSession = ProcessorCreator[RpcRequestClosure, RpcRequestProcessor, RpcContext, BOpenSessionReq, BOpenSessionResp, NetService, Executor](
      BOpenSessionResp.getDefaultInstance,
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
    implicit val service = new NetService
    val openSession = ProcessorCreator[RpcRequestClosure, RpcRequestProcessor, RpcContext, BOpenSessionReq, BOpenSessionResp, NetService](
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
    val openSession = ProcessorCreator[NetService, RpcRequestClosure, RpcRequestProcessor, RpcContext, BOpenSessionReq, BOpenSessionResp](
      (service, rpc, req) => {
        import scala.jdk.CollectionConverters.MapHasAsScala
        val username = req.getUsername
        val password = req.getPassword
        val configurationMap = req.getConfigurationMap
        val ret = service.openSession(username, password, configurationMap.asScala.toMap)
        BOpenSessionResp.newBuilder().setSessionHandle(ret).build()
      },
      (service, rpc, exception) => {
        BOpenSessionResp.newBuilder().setStatus(exception.getLocalizedMessage).build()
      }
    )

    println(openSession.defaultResp)

    println(openSession.getClass.getClass.getName)

    println(openSession.interest())
  }
}
