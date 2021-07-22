package mb.pie.runtime.tracer;

import mb.log.api.Level;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.InconsistentResourceProvide;
import mb.pie.api.InconsistentResourceRequire;
import mb.pie.api.InconsistentTaskRequire;
import mb.pie.api.Observability;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.ResourceRequireDep;
import mb.pie.api.StringUtil;
import mb.pie.api.Task;
import mb.pie.api.TaskData;
import mb.pie.api.TaskKey;
import mb.pie.api.TaskRequireDep;
import mb.pie.api.exec.ExecReason;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Tracer implementation that prints a kind of stack trace to a logger. Indentation is used to indicate sub-processes or
 * stacks. The following sigils are used, along with their meaning:
 *
 * <ul>
 * <li>✓: indicates that a task was required, but is already up-to-date.</li>
 * <li>→: indicates that a task is being executed (increases indentation).</li>
 * <li>←: indicates that a task has been executed, with its output being printed (decreases indentation).</li>
 * <li>?: indicates that a task is being checked for execution (increases indentation until checking has completed).</li>
 * <li>☒: indicates that a dependency check has failed. The task is in an inconsistent state and needs to be scheduled/executed.</li>
 * <li>☑: indicates that a dependency check has succeeded. This dependency does not put the task in an inconsistent state.</li>
 * <li>¿: indicates that a resource or task's affected tasks are being checked for scheduling (increases indentation until checking has completed).</li>
 * <li>☐: indicates that a dependency check has been skipped, because the task is unobserved.</li>
 * <li>↑: indicates that a task has been scheduled for execution (if it has not already been scheduled).</li>
 * <li>↓: indicates that a scheduled task has been deferred.</li>
 * <li>⇒: indicates that the observability of a task has changed from one to another.</li>
 *   <ul>
 *       <li>‼: explicitly observed</li>
 *       <li>!: implicitly observed</li>
 *       <li>†: unobserved</li>
 *   </ul>
 * </ul>
 */
public class LoggingTracer extends EmptyTracer {
    private final Logger logger;
    private final Level execLoggingLevel;
    private final Level upToDateLoggingLevel;
    private final Level topDownLoggingLevel;
    private final Level bottomUpLoggingLevel;
    private final int strLimit;
    private final AtomicInteger indentation = new AtomicInteger(0);


    public LoggingTracer(
        LoggerFactory loggerFactory,
        Level execLoggingLevel,
        Level upToDateLoggingLevel,
        Level topDownLoggingLevel,
        Level bottomUpLoggingLevel,
        int strLimit
    ) {
        this.logger = loggerFactory.create(LoggingTracer.class);
        this.execLoggingLevel = execLoggingLevel;
        this.upToDateLoggingLevel = upToDateLoggingLevel;
        this.topDownLoggingLevel = topDownLoggingLevel;
        this.bottomUpLoggingLevel = bottomUpLoggingLevel;
        this.strLimit = strLimit;
    }

    public LoggingTracer(LoggerFactory loggerFactory) {
        this(loggerFactory, Level.Debug, Level.Trace, Level.Trace, Level.Trace, 1024);
    }


    private void log(Level level, String message) { logger.log(level, getIndent() + message); }

    private void log(Level level, String message, Exception e) { logger.log(level, getIndent() + message, e); }


    private boolean isExecDisabled() { return !logger.isEnabled(execLoggingLevel); }

    private void logExec(String message) { log(execLoggingLevel, message); }

    private void logExec(String message, Exception e) { log(execLoggingLevel, message, e); }

    @Override
    public void executeStart(TaskKey key, Task<?> task, ExecReason reason) {
        if(isExecDisabled()) return;
        logExec("→ " + task.desc(strLimit) + " (reason: " + reason + ")");
        indentation.incrementAndGet();
    }

    @Override
    public void executeEndSuccess(TaskKey key, Task<?> task, ExecReason reason, TaskData data) {
        if(isExecDisabled()) return;
        indentation.decrementAndGet();
        logExec("← " + outputToString(data.output));
    }

    @Override
    public void executeEndFailed(TaskKey key, Task<?> task, ExecReason reason, Exception e) {
        if(isExecDisabled()) return;
        indentation.decrementAndGet();
        logExec("← " + StringUtil.toShortString(e.toString(), strLimit), e);
    }

    @Override
    public void executeEndInterrupted(TaskKey key, Task<?> task, ExecReason reason, InterruptedException e) {
        if(isExecDisabled()) return;
        indentation.decrementAndGet();
        logExec("← " + StringUtil.toShortString(e.toString(), strLimit));
    }


    private boolean isUpToDateDisabled() { return !logger.isEnabled(upToDateLoggingLevel); }

    private void logUpToDate(String message) { log(upToDateLoggingLevel, message); }

    @Override
    public void upToDate(TaskKey key, Task<?> task) {
        if(isUpToDateDisabled()) return;
        logUpToDate("✓ " + key.toShortString(strLimit));
    }


    private boolean isTopDownDisabled() { return !logger.isEnabled(topDownLoggingLevel); }

    private void logTopDown(String message) { log(topDownLoggingLevel, message); }

    @Override
    public void requireTopDownInitialStart(TaskKey key, Task<?> task) {
        if(isTopDownDisabled()) return;
        logTopDown("Top-down build start: " + task.desc(strLimit));
        indentation.incrementAndGet();
    }

    @Override
    public void requireTopDownInitialEnd(TaskKey key, Task<?> task, @Nullable Serializable output) {
        if(isTopDownDisabled()) return;
        indentation.decrementAndGet();
        logTopDown("Top-down build end: " + output);
    }

    @Override
    public void checkTopDownStart(TaskKey key, Task<?> task) {
        if(isTopDownDisabled()) return;
        logTopDown("? " + task.desc(strLimit));
        indentation.incrementAndGet();
    }

    @Override
    public void checkTopDownEnd(TaskKey key, Task<?> task) {
        if(isTopDownDisabled()) return;
        indentation.decrementAndGet();
    }

    @Override
    public void checkResourceProvideEnd(TaskKey provider, Task<?> task, ResourceProvideDep dep, @Nullable InconsistentResourceProvide reason) {
        if(isTopDownDisabled()) return;
        if(reason != null) {
            logTopDown("☒ " + dep.key + " (" + dep.stamp + " ≠ " + reason.newStamp + ")");
        } else {
            logTopDown("☑ " + dep.key + " (" + dep.stamp + ")");
        }
    }

    @Override
    public void checkResourceRequireEnd(TaskKey requirer, Task<?> task, ResourceRequireDep dep, @Nullable InconsistentResourceRequire reason) {
        if(isTopDownDisabled()) return;
        if(reason != null) {
            logTopDown("☒ " + dep.key + " (" + dep.stamp + " ≠ " + reason.newStamp + ")");
        } else {
            logTopDown("☑ " + dep.key + " (" + dep.stamp + ")");
        }
    }

    @Override
    public void checkTaskRequireEnd(TaskKey key, Task<?> task, TaskRequireDep dep, @Nullable InconsistentTaskRequire reason) {
        if(isTopDownDisabled()) return;
        if(reason != null) {
            logTopDown("☒ " + dep.callee.toShortString(strLimit) + " (" + outputToString(dep.stamp) + " ≠ " + outputToString(reason.newStamp) + ")");
        } else {
            logTopDown("☑ " + dep.callee.toShortString(strLimit) + " (" + outputToString(dep.stamp) + ")");
        }
    }


    private boolean isBottomUpDisabled() { return !logger.isEnabled(bottomUpLoggingLevel); }

    private void logBottomUp(String message) { log(bottomUpLoggingLevel, message); }

    @Override
    public void requireBottomUpInitialStart(Set<? extends ResourceKey> changedResources) {
        if(isBottomUpDisabled()) return;
        logBottomUp("Bottom-up build start: " + changedResources);
        indentation.incrementAndGet();
    }

    @Override
    public void requireBottomUpInitialEnd() {
        if(isBottomUpDisabled()) return;
        indentation.decrementAndGet();
        logBottomUp("Bottom-up build end");
    }

    @Override
    public void scheduleAffectedByResourceStart(ResourceKey resource) {
        if(isBottomUpDisabled()) return;
        logBottomUp("¿ " + resource);
        indentation.incrementAndGet();
    }

    @Override
    public void scheduleAffectedByResourceEnd(ResourceKey resource) {
        if(isBottomUpDisabled()) return;
        indentation.decrementAndGet();
    }

    @Override
    public void checkAffectedByProvidedResource(TaskKey provider, @Nullable ResourceProvideDep dep, @Nullable InconsistentResourceProvide reason) {
        if(isBottomUpDisabled()) return;
        if(reason != null && dep != null) {
            logBottomUp("☒ " + provider.toShortString(strLimit) + "(" + dep.stamp + " ≠ " + reason.newStamp + ")");
        } else if(dep != null) {
            logBottomUp("☑ " + provider.toShortString(strLimit) + " (" + dep.stamp + ")");
        } else {
            logBottomUp("☐ " + provider.toShortString(strLimit));
        }
    }

    @Override
    public void checkAffectedByRequiredResource(TaskKey requirer, @Nullable ResourceRequireDep dep, @Nullable InconsistentResourceRequire reason) {
        if(isBottomUpDisabled()) return;
        if(reason != null && dep != null) {
            logBottomUp("☒ " + requirer.toShortString(strLimit) + "(" + dep.stamp + " ≠ " + reason.newStamp + ")");
        } else if(dep != null) {
            logBottomUp("☑ " + requirer.toShortString(strLimit) + " (" + dep.stamp + ")");
        } else {
            logBottomUp("☐ " + requirer.toShortString(strLimit));
        }
    }

    @Override
    public void scheduleAffectedByTaskOutputStart(TaskKey requiree, @Nullable Serializable output) {
        if(isBottomUpDisabled()) return;
        logBottomUp("¿ " + requiree.toShortString(strLimit));
        indentation.incrementAndGet();
    }

    @Override
    public void scheduleAffectedByTaskOutputEnd(TaskKey requiree, @Nullable Serializable output) {
        if(isBottomUpDisabled()) return;
        indentation.decrementAndGet();
    }

    @Override
    public void checkAffectedByRequiredTask(TaskKey requirer, @Nullable TaskRequireDep dep, @Nullable InconsistentTaskRequire reason) {
        if(isBottomUpDisabled()) return;
        if(reason != null && dep != null) {
            logBottomUp("☒ " + requirer.toShortString(strLimit) + " (" + outputToString(dep.stamp) + " ≠ " + outputToString(reason.newStamp) + ")");
        } else if(dep != null) {
            logBottomUp("☑ " + requirer.toShortString(strLimit) + " (" + outputToString(dep.stamp) + ")");
        } else {
            logBottomUp("☐ " + requirer.toShortString(strLimit));
        }
    }

    @Override
    public void scheduleTask(TaskKey key) {
        if(isBottomUpDisabled()) return;
        logBottomUp("↑ " + key);
    }

    @Override public void deferTask(TaskKey key) {
        if(isBottomUpDisabled()) return;
        logBottomUp("↓ " + key);
    }

    @Override
    public void invokeCallbackStart(Consumer<@Nullable Serializable> observer, TaskKey key, @Nullable Serializable output) {
        if(!logger.isTraceEnabled()) return;
        logger.trace(getIndent() + "☎ " + StringUtil.toShortString(observer.toString(), strLimit) + " (" + outputToString(output) + ")");
    }

    @Override
    public void invokeCallbackEnd(Consumer<@Nullable Serializable> observer, TaskKey key, @Nullable Serializable output) {}


    @Override
    public void setTaskObservability(TaskKey key, Observability previousObservability, Observability newObservability) {
        if(previousObservability == newObservability) return;
        final String previousSigil = observabilityToSigil(previousObservability);
        final String newSigil = observabilityToSigil(newObservability);
        logger.trace(getIndent() + previousSigil + "⇒" + newSigil + " " + key.toShortString(strLimit));
    }

    private String observabilityToSigil(Observability observability) {
        switch(observability) {
            case ExplicitObserved:
                return "‼";
            case ImplicitObserved:
                return "!";
            case Unobserved:
                return "†";
        }
        return "";
    }


    private String getIndent() {
        final int indentation = this.indentation.get();
        final StringBuilder sb = new StringBuilder(indentation);
        for(int i = 0; i < indentation; ++i) {
            sb.append("  ");
        }
        return sb.toString();
    }

    private String outputToString(@Nullable Serializable output) {
        return output != null ? StringUtil.toShortString(output.toString(), strLimit) : "null";
    }
}
