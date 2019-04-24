package mb.pie.runtime.logger;

import mb.pie.api.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

public class NoopLogger implements Logger {
    @Override public void error(@Nullable String message, Throwable throwable) {}

    @Override public void warn(@Nullable String message, Throwable throwable) {}

    @Override public void info(String message) {}

    @Override public void debug(String message) {}

    @Override public void trace(String message) {}
}
