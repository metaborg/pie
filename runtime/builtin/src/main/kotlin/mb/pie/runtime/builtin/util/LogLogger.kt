package mb.pie.runtime.builtin.util

import com.google.inject.Inject
import mb.log.*
import mb.pie.runtime.core.impl.logger.StreamLogger


class LogLogger @Inject constructor(logger: Logger)
  : StreamLogger(LoggingOutputStream(logger.forContext("PIE log"), Level.Info), LoggingOutputStream(logger.forContext("PIE log"), Level.Trace))
