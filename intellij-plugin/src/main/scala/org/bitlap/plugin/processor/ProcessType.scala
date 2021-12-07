package org.bitlap.plugin.processor

/**
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/7/5
 */
object ProcessType extends Enumeration {
  type ProcessType = Value
  val Method, Field, Inner, Super = Value
}
