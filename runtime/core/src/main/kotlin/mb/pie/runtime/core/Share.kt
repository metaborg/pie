package mb.pie.runtime.core


interface Share {
  fun <I : In, O : Out> reuseOrCreate(app: FuncApp<I, O>, cacheFunc: (FuncApp<I, O>) -> O?, execFunc: (FuncApp<I, O>) -> O): O
  fun <I : In, O : Out> reuseOrCreate(app: FuncApp<I, O>, execFunc: (FuncApp<I, O>) -> O): O
}
