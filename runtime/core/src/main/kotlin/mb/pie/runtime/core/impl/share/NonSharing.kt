package mb.pie.runtime.core.impl.share

import mb.pie.runtime.core.*

class NonSharingBuildShare : BuildShare {
  override fun <I : In, O : Out> reuseOrCreate(app: FuncApp<I, O>, cacheFunc: (FuncApp<I, O>) -> ExecRes<I, O>?, execFunc: (FuncApp<I, O>) -> ExecRes<I, O>): ExecRes<I, O> {
    return cacheFunc(app) ?: execFunc(app)
  }

  override fun <I : In, O : Out> reuseOrCreate(app: FuncApp<I, O>, execFunc: (FuncApp<I, O>) -> ExecRes<I, O>): ExecRes<I, O> {
    return execFunc(app)
  }


  override fun toString() = "NonSharingBuildShare"
}