package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Logger interface.
 */
public interface Logger {
    void error(String message, @Nullable Throwable throwable);

    void warn(String message, @Nullable Throwable throwable);

    void info(String message);

    void debug(String message);

    void trace(String message);
}

