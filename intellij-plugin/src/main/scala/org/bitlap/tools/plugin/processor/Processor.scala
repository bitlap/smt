package org.bitlap.tools.plugin.processor

import org.bitlap.tools.plugin.processor.ProcessType.ProcessType
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScTypeDefinition

/**
 * Desc: Scala annotation processor
 *
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/6/30
 */
trait Processor {

  /**
   * check this annotation processor should work with companion object
   */
  def needCompanion: Boolean

  /**
   * process generate codes with different `ProcessType`
   */
  def process(source: ScTypeDefinition, typ: ProcessType): Seq[String]
}
