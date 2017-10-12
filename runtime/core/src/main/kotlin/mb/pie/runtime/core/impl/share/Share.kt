package mb.pie.runtime.core.impl.share

import mb.pie.runtime.core.*

interface BuildShare {
  fun <I : In, O : Out> reuseOrCreate(app: BuildApp<I, O>, cacheFunc: (BuildApp<I, O>) -> BuildRes<I, O>?, buildFunc: (BuildApp<I, O>) -> BuildRes<I, O>): BuildRes<I, O>
  fun <I : In, O : Out> reuseOrCreate(app: BuildApp<I, O>, buildFunc: (BuildApp<I, O>) -> BuildRes<I, O>): BuildRes<I, O>
}
