package io.github.dreamylost.plugin.utils

/**
 *
 * @author 梦境迷离
 * @since 2021/7/5
 * @version 1.0
 */
object Utils {

  def convert2ScalaPrimitive(tp: String): String = {
    val types = Map(
      "int" -> "Int",
      "long" -> "Long",
      "double" -> "Double",
      "float" -> "Float",
      "short" -> "Short",
      "byte" -> "Byte",
      "boolean" -> "Boolean",
      "char" -> "Char",
    )
    types.getOrElse(tp, tp)
  }

  def convert2ScalaType(tp: String): String = {
    convert2ScalaPrimitive(tp.replace("<", "[").replace(">", "]"))
  }
}

class Utils
