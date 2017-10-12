package mb.pie.runtime.core.impl.layer

import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.Build

class NoopValidationLayer : BuildLayer {
  override fun <I : In, O : Out> requireStart(app: BuildApp<I, O>) {}

  override fun <I : In, O : Out> requireEnd(app: BuildApp<I, O>) {}

  override fun <I : In, O : Out> validate(app: BuildApp<I, O>, result: BuildRes<I, O>, build: Build) {}
}