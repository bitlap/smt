package io.github.dreamylost.sofa

import org.bitlap.network.proto.driver.BOpenSession.{ BOpenSessionReq, BOpenSessionResp }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.Executor

/**
 *
 * @author 梦境迷离
 * @version 1.0,2021/12/3
 */
object ProcessableTest extends AnyFlatSpec with Matchers {

  "Processable" should "compile ok" in {
    val openSession = Processable[BOpenSessionReq, NetService, Executor](
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
      },
      new NetService, BOpenSessionResp.getDefaultInstance, null
    )

    println(openSession.defaultResp)

    println(openSession.getClass.getClass.getName)

    println(openSession.interest())
  }

}
