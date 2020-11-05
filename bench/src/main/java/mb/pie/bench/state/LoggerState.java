package mb.pie.bench.state;

import mb.log.api.LoggerFactory;
import mb.log.slf4j.SLF4JLoggerFactory;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class LoggerState {
    // Trial
    private @Nullable LoggerFactory loggerFactory;

    public LoggerFactory setupTrial() {
        if(loggerFactory != null) {
            throw new IllegalStateException("setupTrial was called before tearDownTrial");
        }
        loggerFactory = new SLF4JLoggerFactory();
        return loggerFactory;
    }

    public void tearDownTrial() {
        if(loggerFactory == null) {
            throw new IllegalStateException("tearDownTrial was called before setupTrial");
        }
        loggerFactory = null;
    }
}
