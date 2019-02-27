package mb.pie.runtime.logger

import mb.pie.api.Logger

public class NoopLogger : Logger {
  override fun error(message: String, throwable: Throwable?) {}
  override fun warn(message: String, throwable: Throwable?) {}
  override fun info(message: String) {}
  override fun debug(message: String) {}
  override fun trace(message: String) {}
}
