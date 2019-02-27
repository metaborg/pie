package mb.pie.runtime.logger

import mb.pie.api.Logger
import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicInteger

public open class StreamLogger : Logger {
  private val errorWriter: PrintWriter;
  private val warnWriter: PrintWriter?;
  private val infoWriter: PrintWriter?;
  private val debugWriter: PrintWriter?;
  private val traceWriter: PrintWriter?;
  private var indentation: AtomicInteger = AtomicInteger(0);

  public constructor(
    errorWriter: PrintWriter = PrintWriter(System.out, true),
    warnWriter: PrintWriter? = PrintWriter(System.out, true),
    infoWriter: PrintWriter? = PrintWriter(System.out, true),
    debugWriter: PrintWriter? = PrintWriter(System.out, true),
    traceWriter: PrintWriter? = PrintWriter(System.out, true)
  ) {
    this.errorWriter = errorWriter;
    this.warnWriter = warnWriter;
    this.infoWriter = infoWriter;
    this.debugWriter = debugWriter;
    this.traceWriter = traceWriter;
  }

  companion object {
    @JvmStatic
    fun only_errors(): StreamLogger {
      return StreamLogger(warnWriter = null, infoWriter = null, debugWriter = null, traceWriter = null);
    }

    @JvmStatic
    fun non_verbose(): StreamLogger {
      return StreamLogger(debugWriter = null, traceWriter = null);
    }

    @JvmStatic
    fun verbose(): StreamLogger {
      return StreamLogger();
    }
  }

  private fun getIndent(): String {
    return " ".repeat(indentation.get());
  }

  override fun error(message: String, throwable: Throwable?) {
    errorWriter.println(getIndent() + message);
    if(throwable != null && throwable.message != null) {
      errorWriter.println(throwable.message);
    }
  }

  override fun warn(message: String, throwable: Throwable?) {
    if(warnWriter == null) return;
    warnWriter.println(getIndent() + message);
    if(throwable != null && throwable.message != null) {
      warnWriter.println(throwable.message);
    }
  }

  override fun info(message: String) {
    if(infoWriter == null) return;
    infoWriter.println(getIndent() + message);
  }

  override fun debug(message: String) {
    if(debugWriter == null) return;
    debugWriter.println(getIndent() + message);
  }

  override fun trace(message: String) {
    if(traceWriter == null) return;
    traceWriter.println(getIndent() + message);
  }
}
