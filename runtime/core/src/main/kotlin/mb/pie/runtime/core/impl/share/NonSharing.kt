package mb.pie.runtime.core.impl.share

import mb.pie.runtime.core.*

class NonSharingShare : Share {
  override fun <I : In, O : Out> reuseOrCreate(app: FuncApp<I, O>, cacheFunc: (FuncApp<I, O>) -> O?, execFunc: (FuncApp<I, O>) -> O): O {
    return cacheFunc(app) ?: execFunc(app)
  }

  override fun <I : In, O : Out> reuseOrCreate(app: FuncApp<I, O>, execFunc: (FuncApp<I, O>) -> O): O {
    return execFunc(app)
  }


  override fun toString() = "NonSharingShare"
}