package io.github.dreamylost.sofa

/**
 *
 * @author 梦境迷离
 * @since 2021/12/4
 * @version 1.0
 */
class NetService {

  def openSession(username: String, password: String, configuration: Map[String, String] = Map.empty): String = {
    username + password
  }

}
