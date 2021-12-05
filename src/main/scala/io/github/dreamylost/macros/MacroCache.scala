package io.github.dreamylost.macros

object MacroCache {

  private var identityCount = 0

  def getIdentityId: Int = identityCount.synchronized { identityCount += 1; identityCount }

}
