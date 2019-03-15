package mb.pie.runtime.logger;

import mb.pie.api.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

public class StreamLogger implements Logger {
    private final PrintWriter errorWriter;
    private final @Nullable PrintWriter warnWriter;
    private final @Nullable PrintWriter infoWriter;
    private final @Nullable PrintWriter debugWriter;
    private final @Nullable PrintWriter traceWriter;
    private AtomicInteger indentation = new AtomicInteger(0);


    public StreamLogger(
        PrintWriter errorWriter,
        @Nullable PrintWriter warnWriter,
        @Nullable PrintWriter infoWriter,
        @Nullable PrintWriter debugWriter,
        @Nullable PrintWriter traceWriter
    ) {
        this.errorWriter = errorWriter;
        this.warnWriter = warnWriter;
        this.infoWriter = infoWriter;
        this.debugWriter = debugWriter;
        this.traceWriter = traceWriter;
    }

    public static StreamLogger onlyErrors() {
        return new StreamLogger(newPrintWriter(), null, null, null, null);
    }

    public static StreamLogger nonVerbose() {
        return new StreamLogger(newPrintWriter(), newPrintWriter(), newPrintWriter(), null, null);
    }

    public static StreamLogger verbose() {
        return new StreamLogger(newPrintWriter(), newPrintWriter(), newPrintWriter(), newPrintWriter(), newPrintWriter());
    }


    private String getIndent() {
        final int indentation = this.indentation.get();
        final StringBuilder sb = new StringBuilder(indentation);
        for(int i = 0; i < indentation; ++i) {
            sb.append(' ');
        }
        return sb.toString();
    }

    @Override public void error(@Nullable String message, Throwable throwable) {
        errorWriter.println(getIndent() + message);
        if(throwable != null && throwable.getMessage() != null) {
            errorWriter.println(throwable.getMessage());
        }
    }

    @Override public void warn(@Nullable String message, Throwable throwable) {
        if(warnWriter == null) return;
        warnWriter.println(getIndent() + message);
        if(throwable != null && throwable.getMessage() != null) {
            warnWriter.println(throwable.getMessage());
        }
    }

    @Override public void info(String message) {
        if(infoWriter == null) return;
        infoWriter.println(getIndent() + message);
    }

    @Override public void debug(String message) {
        if(debugWriter == null) return;
        debugWriter.println(getIndent() + message);
    }

    @Override public void trace(String message) {
        if(traceWriter == null) return;
        traceWriter.println(getIndent() + message);
    }


    private static PrintWriter newPrintWriter() {
        return new PrintWriter(System.out, true);
    }
}
