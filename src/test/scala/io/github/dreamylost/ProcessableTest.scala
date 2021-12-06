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

import com.alipay.sofa.jraft.rpc.{ RpcContext, RpcRequestClosure }
import io.github.dreamylost.test.proto.BOpenSession.{ BOpenSessionReq, BOpenSessionResp }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.Executor
import io.github.dreamylost.sofa.Processable
import scala.jdk.CollectionConverters.MapHasAsScala
import io.github.dreamylost.sofa.CustomRpcProcessor

/**
 *
 * @author 梦境迷离
 * @version 1.0,2021/12/3
 */
class ProcessableTest extends AnyFlatSpec with Matchers {

  // origin
  "Processable1" should "compile ok" in {
    val openSession: CustomRpcProcessor[BOpenSessionReq] = Processable[BOpenSessionReq, NetService, Executor](
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
  "Processable2" should "compile ok" in {
    val openSession: CustomRpcProcessor[BOpenSessionReq] = Processable[NetService, BOpenSessionReq, BOpenSessionResp](new NetService)(
      (service, _, req) => {
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
  "Processable3" should "compile ok" in {
    // NetService must be a class and with an no parameter construction
    val openSession: CustomRpcProcessor[BOpenSessionReq] = Processable[BOpenSessionReq, BOpenSessionResp, NetService](
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

  // simple v2
  "Processable4" should "compile ok" in {
    // NetService must be a class and with an no parameter construction
    val openSession: CustomRpcProcessor[BOpenSessionReq] = Processable[BOpenSessionReq, BOpenSessionResp, NetService](
      (service, rpc, req) => {
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
