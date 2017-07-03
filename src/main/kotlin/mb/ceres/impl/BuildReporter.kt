package mb.ceres.impl

import mb.ceres.BuildApp
import mb.ceres.BuildException
import mb.ceres.BuildReason
import mb.ceres.BuildReporter
import mb.ceres.BuildRes
import mb.ceres.In
import mb.ceres.Out
import java.io.OutputStream
import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicInteger


open class StreamBuildReporter(stream: OutputStream = System.out) : BuildReporter {
  val writer: PrintWriter = PrintWriter(stream, true)
  var indentation = AtomicInteger(0)
  val indent get() = " ".repeat(indentation.get())

  override fun <I : In, O : Out> require(app: BuildApp<I, O>) {

  }

  override fun <I : In, O : Out> build(app: BuildApp<I, O>, reason: BuildReason) {
    writer.println("$indent> ${app.toShortString(100)} (reason: $reason)")
    indentation.incrementAndGet()
  }

  override fun <I : In, O : Out> buildSuccess(app: BuildApp<I, O>, reason: BuildReason, result: BuildRes<I, O>) {
    indentation.decrementAndGet()
    writer.println("$indent< ${result.toShortString(100)}")
  }

  override fun <I : In, O : Out> buildFailed(app: BuildApp<I, O>, reason: BuildReason, exception: BuildException) {
    indentation.decrementAndGet()
    writer.println("$indent< $exception")
  }
}