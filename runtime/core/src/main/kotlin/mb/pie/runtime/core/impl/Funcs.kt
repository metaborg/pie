package mb.pie.runtime.core.impl

import mb.pie.runtime.core.*


interface Funcs {
  fun <I : In, O : Out> getFunc(id: String): Func<I, O>
  fun getUFunc(id: String): UFunc
  fun getAnyFunc(id: String): AnyFunc
}

class FuncsImpl(private val funcs: Map<String, UFunc>) : Funcs {
  override fun getUFunc(id: String): UFunc {
    return (funcs[id] ?: error("Function with identifier '$id' does not exist"))
  }

  override fun getAnyFunc(id: String): AnyFunc {
    @Suppress("UNCHECKED_CAST")
    return getUFunc(id) as AnyFunc
  }

  override fun <I : In, O : Out> getFunc(id: String): Func<I, O> {
    @Suppress("UNCHECKED_CAST")
    return getUFunc(id) as Func<I, O>
  }
}
