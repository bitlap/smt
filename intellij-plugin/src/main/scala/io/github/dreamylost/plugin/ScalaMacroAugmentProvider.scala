package io.github.dreamylost.plugin

import java.util

import com.intellij.psi.PsiElement
import com.intellij.psi.augment.PsiAugmentProvider

/**
 * Desc:
 *
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/7/1
 */
class ScalaMacroAugmentProvider extends PsiAugmentProvider {

  override def getAugments[Psi <: PsiElement](element: PsiElement, `type`: Class[Psi], nameHint: String): util.List[Psi] = {
    println(`type`)
    super.getAugments(element, `type`, nameHint)
  }

}
