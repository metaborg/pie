package mb.pie.runtime.core.impl.layer

import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.Build

/**
 * A build layer that does nothing. For debugging or benchmarking purposes only.
 * DO NOT USE in production, as it disables checking for inconsistencies in the build.
 */
class NoopBuildLayer : BuildLayer {
  override fun <I : In, O : Out> requireStart(app: BuildApp<I, O>) {}

  override fun <I : In, O : Out> requireEnd(app: BuildApp<I, O>) {}

  override fun <I : In, O : Out> validate(app: BuildApp<I, O>, result: BuildRes<I, O>, build: Build) {}
}