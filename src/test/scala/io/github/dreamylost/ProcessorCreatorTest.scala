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

  // error
  "ProcessorCreator4" should "compile error" in {
    trait Service
    """
      | val openSession = ProcessorCreator[Service, RpcRequestClosure, RpcRequestProcessor, RpcContext, BOpenSessionReq, BOpenSessionResp](
      |      (service, rpc, req) => {
      |        BOpenSessionResp.newBuilder().setSessionHandle(null).build()
      |      },
      |      (service, rpc, exception) => {
      |        BOpenSessionResp.newBuilder().setStatus(exception.getLocalizedMessage).build()
      |      }
      |    )
      |
      |    println(openSession.defaultResp)
      |
      |    println(openSession.getClass.getClass.getName)
      |
      |    println(openSession.interest())
      |""".stripMargin shouldNot compile
  }

  // error
  "ProcessorCreator5" should "compile error" in {
    trait Service
    """
      | val openSession = ProcessorCreator[Service, RpcRequestProcessor, RpcRequestClosure, RpcContext, BOpenSessionReq, BOpenSessionResp](
      |      (service, rpc, req) => {
      |        BOpenSessionResp.newBuilder().setSessionHandle(null).build()
      |      },
      |      (service, rpc, exception) => {
      |        BOpenSessionResp.newBuilder().setStatus(exception.getLocalizedMessage).build()
      |      }
      |    )
      |
      |    println(openSession.defaultResp)
      |
      |    println(openSession.getClass.getClass.getName)
      |
      |    println(openSession.interest())
      |""".stripMargin shouldNot compile
  }

  // error
  "ProcessorCreator6" should "compile error" in {
    object Service
    """
      | val openSession = ProcessorCreator[Service, RpcRequestClosure, RpcRequestProcessor, RpcContext, BOpenSessionReq, BOpenSessionResp](
      |      (service, rpc, req) => {
      |        BOpenSessionResp.newBuilder().setSessionHandle(null).build()
      |      },
      |      (service, rpc, exception) => {
      |        BOpenSessionResp.newBuilder().setStatus(exception.getLocalizedMessage).build()
      |      }
      |    )
      |
      |    println(openSession.defaultResp)
      |
      |    println(openSession.getClass.getClass.getName)
      |
      |    println(openSession.interest())
      |""".stripMargin shouldNot compile
  }
}
