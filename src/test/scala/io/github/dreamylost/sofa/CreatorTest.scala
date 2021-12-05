package io.github.dreamylost.sofa

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 *
 * @author 梦境迷离
 * @since 2021/12/5
 * @version 1.0
 */
class CreatorTest extends AnyFlatSpec with Matchers {
  "Creator" should "ok" in {
    val service = new io.github.dreamylost.macros.Creator[io.github.dreamylost.sofa.NetService]().createInstance(null)(0)
    println(service.openSession("1", "2"))
  }
}
