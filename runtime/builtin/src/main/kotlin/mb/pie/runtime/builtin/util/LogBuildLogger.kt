package mb.pie.runtime.builtin.util

import com.google.inject.Inject
import mb.log.*
import mb.pie.runtime.core.impl.logger.StreamBuildLogger

class LogBuildLogger @Inject constructor(logger: Logger)
  : StreamBuildLogger(LoggingOutputStream(logger.forContext("Build log"), Level.Info), LoggingOutputStream(logger.forContext("Build log"), Level.Trace))