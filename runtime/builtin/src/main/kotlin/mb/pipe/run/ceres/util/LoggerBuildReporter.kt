package mb.pipe.run.ceres.util

import com.google.inject.Inject
import mb.log.Level
import mb.log.Logger
import mb.log.LoggingOutputStream
import mb.pie.runtime.core.impl.StreamBuildReporter

class LoggerBuildReporter @Inject constructor(logger: Logger)
  : StreamBuildReporter(LoggingOutputStream(logger.forContext("Build log"), Level.Info), LoggingOutputStream(logger.forContext("Build log"), Level.Trace))