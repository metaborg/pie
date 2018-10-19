package mb.pie.runtime.logger

import mb.pie.api.Logger
import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicInteger

open class StreamLogger(
  private val errorWriter: PrintWriter = PrintWriter(System.out, true),
  private val warnWriter: PrintWriter? = PrintWriter(System.out, true),
  private val infoWriter: PrintWriter? = PrintWriter(System.out, true),
  private val debugWriter: PrintWriter? = PrintWriter(System.out, true),
  private val traceWriter: PrintWriter? = PrintWriter(System.out, true)
) : Logger {
  private var indentation = AtomicInteger(0)
  private val indent get() = " ".repeat(indentation.get())

  companion object {
    @JvmStatic
    fun only_errors(): StreamLogger = StreamLogger(warnWriter = null, infoWriter = null, debugWriter = null, traceWriter = null)

    @JvmStatic
    fun non_verbose(): StreamLogger = StreamLogger(debugWriter = null, traceWriter = null)

    @JvmStatic
    fun verbose(): StreamLogger = StreamLogger()
  }

  override fun error(message: String, throwable: Throwable?) {
    errorWriter.println("$indent$message")
    if(throwable?.message != null) {
      errorWriter.println(throwable.message)
    }
  }

  override fun warn(message: String, throwable: Throwable?) {
    if(warnWriter == null) return
    warnWriter.println("$indent$message")
    if(throwable?.message != null) {
      warnWriter.println(throwable.message)
    }
  }

  override fun info(message: String) {
    infoWriter?.println("$indent$message")
  }

  override fun debug(message: String) {
    debugWriter?.println("$indent$message")
  }

  override fun trace(message: String) {
    traceWriter?.println("$indent$message")
  }
}
