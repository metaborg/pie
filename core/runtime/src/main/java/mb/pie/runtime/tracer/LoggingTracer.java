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
import mb.pie.api.Tracer;
import mb.pie.api.exec.ExecReason;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.Resource;
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
public class LoggingTracer implements Tracer {
    private final Logger logger;
    private final Level execLoggingLevel;
    private final Level upToDateLoggingLevel;
    private final Level topDownLoggingLevel;
    private final Level bottomUpLoggingLevel;
    private final int strLimit;
    private final @Nullable MetricsTracer metricsTracer;
    private final AtomicInteger indentation = new AtomicInteger(0);


    public LoggingTracer(
        LoggerFactory loggerFactory,
        Level execLoggingLevel,
        Level upToDateLoggingLevel,
        Level topDownLoggingLevel,
        Level bottomUpLoggingLevel,
        int strLimit,
        @Nullable MetricsTracer metricsTracer
    ) {
        this.logger = loggerFactory.create(LoggingTracer.class);
        this.execLoggingLevel = execLoggingLevel;
        this.upToDateLoggingLevel = upToDateLoggingLevel;
        this.topDownLoggingLevel = topDownLoggingLevel;
        this.bottomUpLoggingLevel = bottomUpLoggingLevel;
        this.strLimit = strLimit;
        this.metricsTracer = metricsTracer;
    }

    public LoggingTracer(LoggerFactory loggerFactory, @Nullable MetricsTracer metricsTracer) {
        this(loggerFactory, Level.Debug, Level.Trace, Level.Trace, Level.Trace, 1024, metricsTracer);
    }

    public LoggingTracer(LoggerFactory loggerFactory) {
        this(loggerFactory, null);
    }


    private void log(Level level, String message) {
        logger.log(level, getIndent() + message);
    }

    private void log(Level level, String message, Exception e) {
        logger.log(level, getIndent() + message, e);
    }


    @Override public void providedResource(Resource resource, ResourceStamper<?> stamper) {
        if(metricsTracer != null) metricsTracer.providedResource(resource, stamper);
    }

    @Override public void requiredResource(Resource resource, ResourceStamper<?> stamper) {
        if(metricsTracer != null) metricsTracer.requiredResource(resource, stamper);
    }

    @Override public void requiredTask(Task<?> task, OutputStamper stamper) {
        if(metricsTracer != null) metricsTracer.requiredTask(task, stamper);
    }


    private boolean isExecDisabled() {
        return !logger.isEnabled(execLoggingLevel);
    }

    private void logExec(String message) {
        log(execLoggingLevel, message);
    }

    private void logExec(String message, Exception e) {
        log(execLoggingLevel, message, e);
    }

    @Override
    public void executeStart(TaskKey key, Task<?> task, ExecReason reason) {
        if(metricsTracer != null) metricsTracer.executeStart(key, task, reason);
        if(isExecDisabled()) return;
        logExec("→ " + task.desc(strLimit) + " (reason: " + reason + ")");
        indentation.incrementAndGet();
    }

    @Override
    public void executeEndSuccess(TaskKey key, Task<?> task, ExecReason reason, TaskData data) {
        if(metricsTracer != null) metricsTracer.executeEndSuccess(key, task, reason, data);
        if(isExecDisabled()) return;
        indentation.decrementAndGet();
        logExec("← " + getDurationString(key) + outputToString(data.getOutput()));
    }

    @Override
    public void executeEndFailed(TaskKey key, Task<?> task, ExecReason reason, Exception e) {
        if(metricsTracer != null) metricsTracer.executeEndFailed(key, task, reason, e);
        if(isExecDisabled()) return;
        indentation.decrementAndGet();
        logExec("← " + getDurationString(key) + "exception: " + StringUtil.toShortString(e.toString(), strLimit), e);
    }

    @Override
    public void executeEndInterrupted(TaskKey key, Task<?> task, ExecReason reason, InterruptedException e) {
        if(metricsTracer != null) metricsTracer.executeEndInterrupted(key, task, reason, e);
        if(isExecDisabled()) return;
        indentation.decrementAndGet();
        logExec("← " + getDurationString(key) + "interrupted: " + StringUtil.toShortString(e.toString(), strLimit));
    }

    private String getDurationString(TaskKey key) {
        if(metricsTracer == null) return "";
        final @Nullable Long duration = metricsTracer.getReport().executionDurationPerTask.get(key);
        if(duration != null) {
            return "[" + (duration / 1000000.0) + "] ";
        }
        return "";
    }


    @Override public void requireStart(TaskKey key, Task<?> task) {
        if(metricsTracer != null) metricsTracer.requireStart(key, task);
    }

    @Override public void requireEnd(TaskKey key, Task<?> task) {
        if(metricsTracer != null) metricsTracer.requireEnd(key, task);
    }


    private boolean isUpToDateDisabled() {
        return !logger.isEnabled(upToDateLoggingLevel);
    }

    private void logUpToDate(String message) {
        log(upToDateLoggingLevel, message);
    }

    @Override
    public void upToDate(TaskKey key, Task<?> task) {
        if(metricsTracer != null) metricsTracer.upToDate(key, task);
        if(isUpToDateDisabled()) return;
        logUpToDate("✓ " + key.toShortString(strLimit));
    }


    private boolean isTopDownDisabled() {return !logger.isEnabled(topDownLoggingLevel);}

    private void logTopDown(String message) {log(topDownLoggingLevel, message);}

    @Override
    public void requireTopDownInitialStart(TaskKey key, Task<?> task) {
        if(metricsTracer != null) metricsTracer.requireTopDownInitialStart(key, task);
        if(isTopDownDisabled()) return;
        logTopDown("Top-down build start: " + task.desc(strLimit));
        indentation.incrementAndGet();
    }

    @Override
    public void requireTopDownInitialEnd(TaskKey key, Task<?> task, @Nullable Serializable output) {
        if(metricsTracer != null) metricsTracer.requireTopDownInitialEnd(key, task, output);
        if(isTopDownDisabled()) return;
        indentation.decrementAndGet();
        logTopDown("Top-down build end: " + output);
    }

    @Override
    public void checkTopDownStart(TaskKey key, Task<?> task) {
        if(metricsTracer != null) metricsTracer.checkTopDownStart(key, task);
        if(isTopDownDisabled()) return;
        logTopDown("? " + task.desc(strLimit));
        indentation.incrementAndGet();
    }

    @Override
    public void checkTopDownEnd(TaskKey key, Task<?> task) {
        if(metricsTracer != null) metricsTracer.checkTopDownEnd(key, task);
        if(isTopDownDisabled()) return;
        indentation.decrementAndGet();
    }

    @Override public void checkResourceProvideStart(TaskKey provider, Task<?> task, ResourceProvideDep dep) {
        if(metricsTracer != null) metricsTracer.checkResourceProvideStart(provider, task, dep);
    }

    @Override
    public void checkResourceProvideEnd(TaskKey provider, Task<?> task, ResourceProvideDep dep, @Nullable InconsistentResourceProvide reason) {
        if(metricsTracer != null) metricsTracer.checkResourceProvideEnd(provider, task, dep, reason);
        if(isTopDownDisabled()) return;
        if(reason != null) {
            logTopDown("☒ " + dep.key + " (" + dep.stamp + " ≠ " + reason.newStamp + ")");
        } else {
            logTopDown("☑ " + dep.key + " (" + dep.stamp + ")");
        }
    }

    @Override public void checkResourceRequireStart(TaskKey requirer, Task<?> task, ResourceRequireDep dep) {
        if(metricsTracer != null) metricsTracer.checkResourceRequireStart(requirer, task, dep);
    }

    @Override
    public void checkResourceRequireEnd(TaskKey requirer, Task<?> task, ResourceRequireDep dep, @Nullable InconsistentResourceRequire reason) {
        if(metricsTracer != null) metricsTracer.checkResourceRequireEnd(requirer, task, dep, reason);
        if(isTopDownDisabled()) return;
        if(reason != null) {
            logTopDown("☒ " + dep.key + " (" + dep.stamp + " ≠ " + reason.newStamp + ")");
        } else {
            logTopDown("☑ " + dep.key + " (" + dep.stamp + ")");
        }
    }

    @Override public void checkTaskRequireStart(TaskKey key, Task<?> task, TaskRequireDep dep) {
        if(metricsTracer != null) metricsTracer.checkTaskRequireStart(key, task, dep);
    }

    @Override
    public void checkTaskRequireEnd(TaskKey key, Task<?> task, TaskRequireDep dep, @Nullable InconsistentTaskRequire reason) {
        if(metricsTracer != null) metricsTracer.checkTaskRequireEnd(key, task, dep, reason);
        if(isTopDownDisabled()) return;
        if(reason != null) {
            logTopDown("☒ " + dep.callee.toShortString(strLimit) + " (" + outputToString(dep.stamp) + " ≠ " + outputToString(reason.newStamp) + ")");
        } else {
            logTopDown("☑ " + dep.callee.toShortString(strLimit) + " (" + outputToString(dep.stamp) + ")");
        }
    }


    private boolean isBottomUpDisabled() {return !logger.isEnabled(bottomUpLoggingLevel);}

    private void logBottomUp(String message) {log(bottomUpLoggingLevel, message);}

    @Override
    public void requireBottomUpInitialStart(Set<? extends ResourceKey> changedResources) {
        if(metricsTracer != null) metricsTracer.requireBottomUpInitialStart(changedResources);
        if(isBottomUpDisabled()) return;
        logBottomUp("Bottom-up build start: " + changedResources);
        indentation.incrementAndGet();
    }

    @Override
    public void requireBottomUpInitialEnd() {
        if(metricsTracer != null) metricsTracer.requireBottomUpInitialEnd();
        if(isBottomUpDisabled()) return;
        indentation.decrementAndGet();
        logBottomUp("Bottom-up build end");
    }

    @Override
    public void scheduleAffectedByResourceStart(ResourceKey resource) {
        if(metricsTracer != null) metricsTracer.scheduleAffectedByResourceStart(resource);
        if(isBottomUpDisabled()) return;
        logBottomUp("¿ " + resource);
        indentation.incrementAndGet();
    }

    @Override
    public void scheduleAffectedByResourceEnd(ResourceKey resource) {
        if(metricsTracer != null) metricsTracer.scheduleAffectedByResourceEnd(resource);
        if(isBottomUpDisabled()) return;
        indentation.decrementAndGet();
    }

    @Override
    public void checkAffectedByProvidedResource(TaskKey provider, @Nullable ResourceProvideDep dep, @Nullable InconsistentResourceProvide reason) {
        if(metricsTracer != null) metricsTracer.checkAffectedByProvidedResource(provider, dep, reason);
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
        if(metricsTracer != null) metricsTracer.checkAffectedByRequiredResource(requirer, dep, reason);
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
        if(metricsTracer != null) metricsTracer.scheduleAffectedByTaskOutputStart(requiree, output);
        if(isBottomUpDisabled()) return;
        logBottomUp("¿ " + requiree.toShortString(strLimit));
        indentation.incrementAndGet();
    }

    @Override
    public void scheduleAffectedByTaskOutputEnd(TaskKey requiree, @Nullable Serializable output) {
        if(metricsTracer != null) metricsTracer.scheduleAffectedByTaskOutputEnd(requiree, output);
        if(isBottomUpDisabled()) return;
        indentation.decrementAndGet();
    }

    @Override
    public void checkAffectedByRequiredTask(TaskKey requirer, @Nullable TaskRequireDep dep, @Nullable InconsistentTaskRequire reason) {
        if(metricsTracer != null) metricsTracer.checkAffectedByRequiredTask(requirer, dep, reason);
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
        if(metricsTracer != null) metricsTracer.scheduleTask(key);
        if(isBottomUpDisabled()) return;
        logBottomUp("↑ " + key);
    }

    @Override public void deferTask(TaskKey key) {
        if(metricsTracer != null) metricsTracer.deferTask(key);
        if(isBottomUpDisabled()) return;
        logBottomUp("↓ " + key);
    }

    @Override public void requireScheduledNowStart(TaskKey key) {
        if(metricsTracer != null) metricsTracer.requireScheduledNowStart(key);
    }

    @Override public void requireScheduledNowEnd(TaskKey key, @Nullable TaskData data) {
        if(metricsTracer != null) metricsTracer.requireScheduledNowEnd(key, data);
    }

    @Override public void checkVisitedStart(TaskKey key) {
        if(metricsTracer != null) metricsTracer.checkVisitedStart(key);
    }

    @Override public void checkVisitedEnd(TaskKey key, @Nullable Serializable output) {
        if(metricsTracer != null) metricsTracer.checkVisitedEnd(key, output);
    }

    @Override public void checkStoredStart(TaskKey key) {
        if(metricsTracer != null) metricsTracer.checkStoredStart(key);
    }

    @Override public void checkStoredEnd(TaskKey key, @Nullable Serializable output) {
        if(metricsTracer != null) metricsTracer.checkStoredEnd(key, output);
    }

    @Override
    public void invokeCallbackStart(Consumer<@Nullable Serializable> observer, TaskKey key, @Nullable Serializable output) {
        if(metricsTracer != null) metricsTracer.invokeCallbackStart(observer, key, output);
        if(!logger.isTraceEnabled()) return;
        logger.trace(getIndent() + "☎ " + StringUtil.toShortString(observer.toString(), strLimit) + " (" + outputToString(output) + ")");
    }

    @Override
    public void invokeCallbackEnd(Consumer<@Nullable Serializable> observer, TaskKey key, @Nullable Serializable output) {
        if(metricsTracer != null) metricsTracer.invokeCallbackEnd(observer, key, output);
    }


    @Override
    public void setTaskObservability(TaskKey key, Observability previousObservability, Observability newObservability) {
        if(metricsTracer != null) metricsTracer.setTaskObservability(key, previousObservability, newObservability);
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
