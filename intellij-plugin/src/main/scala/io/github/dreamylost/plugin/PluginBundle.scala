package io.github.dreamylost.plugin

import com.intellij.DynamicBundle
import org.jetbrains.annotations.{ NotNull, PropertyKey }

/**
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/6/30
 */
class PluginBundle extends DynamicBundle("messages.PluginBundle")

object PluginBundle {
  private val INSTANCE = new PluginBundle()

  def message(@NotNull @PropertyKey(resourceBundle = "messages.PluginBundle") key: String, @NotNull params: Any*): String = {
    INSTANCE.getMessage(key, params)
  }
}
