package mb.ceres.impl

import mb.ceres.*
import java.io.PrintStream
import java.util.concurrent.atomic.AtomicInteger


class StreamBuildReporter(val stream: PrintStream = System.out) : BuildReporter {
  var indentation = AtomicInteger(0)
  val indent get() = " ".repeat(indentation.get())

  override fun <I : In, O : Out> require(app: BuildApp<I, O>) {

  }

  override fun <I : In, O : Out> build(app: BuildApp<I, O>, reason: BuildReason) {
    stream.println("$indent> ${app.toShortString(100)} (reason: $reason)")
    indentation.incrementAndGet()
  }

  override fun <I : In, O : Out> buildSuccess(app: BuildApp<I, O>, reason: BuildReason, result: BuildRes<I, O>) {
    indentation.decrementAndGet()
    stream.println("$indent< ${result.toShortString(100)}")
  }

  override fun <I : In, O : Out> buildFailed(app: BuildApp<I, O>, reason: BuildReason, exception: BuildException) {
    indentation.decrementAndGet()
    stream.println("$indent< $exception")
  }
}