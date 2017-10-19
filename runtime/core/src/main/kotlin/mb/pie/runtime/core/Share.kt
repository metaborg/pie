package mb.pie.runtime.core


interface BuildShare {
  fun <I : In, O : Out> reuseOrCreate(app: FuncApp<I, O>, cacheFunc: (FuncApp<I, O>) -> ExecRes<I, O>?, execFunc: (FuncApp<I, O>) -> ExecRes<I, O>): ExecRes<I, O>
  fun <I : In, O : Out> reuseOrCreate(app: FuncApp<I, O>, execFunc: (FuncApp<I, O>) -> ExecRes<I, O>): ExecRes<I, O>
}
