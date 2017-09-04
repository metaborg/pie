package mb.ceres.impl

import mb.ceres.*
import mb.vfs.path.PPath
import java.io.OutputStream
import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicInteger


open class StreamBuildReporter(infoStream: OutputStream = System.out, traceStream: OutputStream? = System.out) : BuildReporter {
  val infoWriter: PrintWriter = PrintWriter(infoStream, true)
  val traceWriter: PrintWriter? = if (traceStream == null) null else PrintWriter(traceStream, true)
  var indentation = AtomicInteger(0)
  val indent get() = " ".repeat(indentation.get())

  override fun <I : In, O : Out> require(app: BuildApp<I, O>) {
    traceWriter?.println("$indent? ${app.toShortString(200)}")
    indentation.incrementAndGet()
  }

  override fun <I : In, O : Out> checkGenPath(app: BuildApp<I, O>, path: PPath, oldStamp: PathStamp, newStamp: PathStamp) {
    traceWriter?.println("$indent␦ $path ($oldStamp vs $newStamp)")
  }

  override fun <I : In, O : Out> checkReqPath(app: BuildApp<I, O>, path: PPath, oldStamp: PathStamp, newStamp: PathStamp) {
    traceWriter?.println("$indent␦ $path ($oldStamp vs $newStamp)")
  }

  override fun <I : In, O : Out> build(app: BuildApp<I, O>, reason: BuildReason) {
    infoWriter.println("$indent> ${app.toShortString(200)} (reason: $reason)")
  }

  override fun <I : In, O : Out> buildSuccess(app: BuildApp<I, O>, reason: BuildReason, result: BuildRes<I, O>) {
    infoWriter.println("$indent< ${result.toShortString(200)}")
  }

  override fun <I : In, O : Out> buildFailed(app: BuildApp<I, O>, reason: BuildReason, exception: BuildException) {
    indentation.decrementAndGet()
    infoWriter.println("$indent✕ $exception")
  }

  override fun <I : In, O : Out> consistent(app: BuildApp<I, O>,  result: BuildRes<I, O>) {
    indentation.decrementAndGet()
    traceWriter?.println("$indent✔ ${app.toShortString(200)}")
  }
}