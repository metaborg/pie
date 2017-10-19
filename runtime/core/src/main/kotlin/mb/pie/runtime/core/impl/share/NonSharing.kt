package mb.pie.runtime.core.impl.share

import mb.pie.runtime.core.*

class NonSharingBuildShare : BuildShare {
  override fun <I : In, O : Out> reuseOrCreate(app: BuildApp<I, O>, cacheFunc: (BuildApp<I, O>) -> BuildRes<I, O>?, buildFunc: (BuildApp<I, O>) -> BuildRes<I, O>): BuildRes<I, O> {
    return cacheFunc(app) ?: buildFunc(app)
  }

  override fun <I : In, O : Out> reuseOrCreate(app: BuildApp<I, O>, buildFunc: (BuildApp<I, O>) -> BuildRes<I, O>): BuildRes<I, O> {
    return buildFunc(app)
  }


  override fun toString() = "NonSharingBuildShare"
}