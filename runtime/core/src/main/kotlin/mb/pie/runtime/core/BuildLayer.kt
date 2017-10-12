package mb.pie.runtime.core

import mb.pie.runtime.core.impl.Build

interface BuildLayer {
  fun <I : In, O : Out> requireStart(app: BuildApp<I, O>)

  fun <I : In, O : Out> requireEnd(app: BuildApp<I, O>)

  fun <I : In, O : Out> validate(app: BuildApp<I, O>, result: BuildRes<I, O>, build: Build)
}

class ValidationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
