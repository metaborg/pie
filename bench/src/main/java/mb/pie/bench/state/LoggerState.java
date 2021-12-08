package mb.pie.bench.state;

import mb.log.api.LoggerFactory;
import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.log.noop.NoopLoggerFactory;
import mb.log.slf4j.SLF4JLoggerFactory;
import mb.log.stream.StreamLoggerFactory;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class LoggerState {
    // Parameters

    @Param({"noop"}) public LoggerFactoryKind loggerFactory;


    // Trial
    private @Nullable LoggerComponent loggerComponent;

    public LoggerComponent setupTrial() {
        if(loggerComponent != null) {
            throw new IllegalStateException("setupTrial was called before tearDownTrial");
        }
        loggerComponent = DaggerLoggerComponent.builder()
            .loggerModule(new LoggerModule(loggerFactory.get()))
            .build();
        return loggerComponent;
    }

    public void tearDownTrial() {
        if(loggerComponent == null) {
            throw new IllegalStateException("tearDownTrial was called before setupTrial");
        }
        loggerComponent = null;
    }


    // Parameter enums

    public enum LoggerFactoryKind {
        stdout_errors {
            @Override public LoggerFactory get() { return StreamLoggerFactory.stdOutErrors(); }
        },
        stdout_errors_and_warnings {
            @Override public LoggerFactory get() { return StreamLoggerFactory.stdOutErrorsAndWarnings(); }
        },
        stdout_non_verbose {
            @Override public LoggerFactory get() { return StreamLoggerFactory.stdOutNonVerbose(); }
        },
        stdout_verbose {
            @Override public LoggerFactory get() { return StreamLoggerFactory.stdOutVerbose(); }
        },
        stdout_very_verbose {
            @Override public LoggerFactory get() { return StreamLoggerFactory.stdOutVeryVerbose(); }
        },
        slf4j {
            @Override public LoggerFactory get() { return new SLF4JLoggerFactory(); }
        },
        noop {
            @Override public LoggerFactory get() { return NoopLoggerFactory.instance; }
        };

        public abstract LoggerFactory get();
    }
}
