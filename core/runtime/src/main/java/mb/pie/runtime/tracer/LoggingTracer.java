package mb.pie.runtime.tracer;

import mb.log.api.Level;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.InconsistentResourceProvide;
import mb.pie.api.InconsistentResourceRequire;
import mb.pie.api.InconsistentTaskRequire;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.ResourceRequireDep;
import mb.pie.api.StringUtil;
import mb.pie.api.Task;
import mb.pie.api.TaskData;
import mb.pie.api.TaskKey;
import mb.pie.api.TaskRequireDep;
import mb.pie.api.Tracer;
import mb.pie.api.exec.ExecReason;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class LoggingTracer implements Tracer {
    private final Logger logger;
    private final Level bottomUpLoggingLevel;
    private final int strLimit;
    private final AtomicInteger indentation = new AtomicInteger(0);


    public LoggingTracer(LoggerFactory loggerFactory, Level bottomUpLoggingLevel, int strLimit) {
        this.logger = loggerFactory.create(LoggingTracer.class);
        this.bottomUpLoggingLevel = bottomUpLoggingLevel;
        this.strLimit = strLimit;
    }

    public LoggingTracer(LoggerFactory loggerFactory) {
        this(loggerFactory, Level.Debug, 4096);
    }


    private void log(Level level, String message) { logger.log(level, getIndent() + message); }

    @Override
    public void requireTopDownInitialStart(TaskKey key, Task<?> task) {
        if(!logger.isTraceEnabled()) return;
        logger.trace(getIndent() + "Top-down build start: " + task.desc(strLimit));
        indentation.incrementAndGet();
    }

    @Override
    public void requireTopDownInitialEnd(TaskKey key, Task<?> task, @Nullable Serializable output) {
        if(!logger.isTraceEnabled()) return;
        indentation.decrementAndGet();
        logger.trace(getIndent() + "Top-down build end: " + output);
    }

    @Override
    public void requireTopDownStart(TaskKey key, Task<?> task) {
        if(!logger.isTraceEnabled()) return;
        logger.trace(getIndent() + "↓ " + task.desc(strLimit));
        indentation.incrementAndGet();
    }

    @Override
    public void requireTopDownEnd(TaskKey key, Task<?> task, @Nullable Serializable output) {
        if(!logger.isTraceEnabled()) return;
        indentation.decrementAndGet();
        logger.trace(getIndent() + "✔ " + task.desc(strLimit) + " ≡ " + outputToString(output));
    }


    private boolean isBottomUpDisabled() { return !logger.isEnabled(bottomUpLoggingLevel); }

    private void logBottomUp(String message) { log(bottomUpLoggingLevel, getIndent() + message); }

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
    public void checkAffectedByProvidedResource(TaskKey provider, @Nullable ResourceProvideDep dep, @Nullable InconsistentResourceProvide reason) {
        if(isBottomUpDisabled()) return;
        if(reason != null && dep != null) {
            logBottomUp("☒ " + provider.toShortString(strLimit) + "(" + dep.stamp + " ≠ " + reason.newStamp + ")");
        } else if(dep != null) {
            logBottomUp("☑ " + provider.toShortString(strLimit) + "(" + dep.stamp + ")");
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
            logBottomUp("☑ " + requirer.toShortString(strLimit) + "(" + dep.stamp + ")");
        } else {
            logBottomUp("☐ " + requirer.toShortString(strLimit));
        }
    }

    @Override
    public void scheduleAffectedByResourceEnd(ResourceKey resource) {
        if(isBottomUpDisabled()) return;
        indentation.decrementAndGet();
        //logBottomUp("¿ " + resource);
    }

    @Override
    public void scheduleAffectedByTaskOutputStart(TaskKey requiree, @Nullable Serializable output) {
        if(isBottomUpDisabled()) return;
        logBottomUp("¿ " + requiree.toShortString(strLimit));
        indentation.incrementAndGet();
    }

    @Override
    public void checkAffectedByRequiredTask(TaskKey requirer, @Nullable TaskRequireDep dep, @Nullable InconsistentTaskRequire reason) {
        if(isBottomUpDisabled()) return;
        if(reason != null && dep != null) {
            logBottomUp("☒ " + requirer.toShortString(strLimit) + "(" + outputToString(dep.stamp) + " ≠ " + outputToString(reason.newStamp) + ")");
        } else if(dep != null) {
            logBottomUp("☑ " + requirer.toShortString(strLimit) + "(" + outputToString(dep.stamp) + ")");
        } else {
            logBottomUp("☐ " + requirer.toShortString(strLimit));
        }
    }

    @Override
    public void scheduleAffectedByTaskOutputEnd(TaskKey requiree, @Nullable Serializable output) {
        if(isBottomUpDisabled()) return;
        indentation.decrementAndGet();
        //logBottomUp("¿ " + requiree.toShortString(strLimit));
    }

    @Override
    public void scheduleTask(TaskKey key) {
        if(isBottomUpDisabled()) return;
        logBottomUp("† " + key);
    }

    @Override
    public void requireScheduledNowStart(TaskKey key) {}

    @Override
    public void requireScheduledNowEnd(TaskKey key, @Nullable TaskData data) {}


    @Override
    public void checkVisitedStart(TaskKey key) {}

    @Override
    public void checkVisitedEnd(TaskKey key, @Nullable Serializable output) {}

    @Override
    public void checkStoredStart(TaskKey key) {}

    @Override
    public void checkStoredEnd(TaskKey key, @Nullable Serializable output) {}

    @Override
    public void checkResourceProvideStart(TaskKey provider, Task<?> task, ResourceProvideDep dep) {}

    @Override
    public void checkResourceProvideEnd(TaskKey provider, Task<?> task, ResourceProvideDep dep, @Nullable InconsistentResourceProvide reason) {
        if(!logger.isTraceEnabled()) return;
        if(reason != null) {
            logger.trace(getIndent() + "☒ " + dep.key + " (" + dep.stamp + " ≠ " + reason.newStamp + ")");
        } else {
            logger.trace(getIndent() + "☑ " + dep.key);
        }
    }

    @Override
    public void checkResourceRequireStart(TaskKey requirer, Task<?> task, ResourceRequireDep dep) {}

    @Override
    public void checkResourceRequireEnd(TaskKey requirer, Task<?> task, ResourceRequireDep dep, @Nullable InconsistentResourceRequire reason) {
        if(!logger.isTraceEnabled()) return;
        if(reason != null) {
            logger.trace(getIndent() + "☒ " + dep.key + " (" + dep.stamp + " ≠ " + reason.newStamp + ")");
        } else {
            logger.trace(getIndent() + "☑ " + dep.key);
        }
    }

    @Override
    public void checkTaskRequireStart(TaskKey key, Task<?> task, TaskRequireDep dep) {}

    @Override
    public void checkTaskRequireEnd(TaskKey key, Task<?> task, TaskRequireDep dep, @Nullable InconsistentTaskRequire reason) {
        if(!logger.isTraceEnabled()) return;
        if(reason != null) {
            logger.trace(getIndent() + "☒ " + dep.callee.toShortString(strLimit) + " (" + outputToString(dep.stamp) + " ≠ " + outputToString(reason.newStamp) + ")");
        } else {
            logger.trace(getIndent() + "☑ " + dep.callee.toShortString(strLimit));
        }
    }


    @Override
    public void upToDate(TaskKey key, Task<?> task) {
        if(!logger.isTraceEnabled()) return;
        logger.trace(getIndent() + "⏎ " + key.toShortString(strLimit));
    }

    @Override
    public void executeStart(TaskKey key, Task<?> task, ExecReason reason) {
        if(!logger.isInfoEnabled()) return;
        logger.info(getIndent() + "→ " + task.desc(strLimit) + " (reason: " + reason + ")");
        indentation.incrementAndGet();
    }

    @Override
    public void executeEndSuccess(TaskKey key, Task<?> task, ExecReason reason, TaskData data) {
        if(!logger.isInfoEnabled()) return;
        indentation.decrementAndGet();
        logger.info(getIndent() + "← " + outputToString(data.output));
    }

    @Override
    public void executeEndFailed(TaskKey key, Task<?> task, ExecReason reason, Exception e) {
        if(!logger.isInfoEnabled()) return;
        indentation.decrementAndGet();
        logger.error(getIndent() + "← " + StringUtil.toShortString(e.toString(), strLimit), e);
    }

    @Override
    public void executeEndInterrupted(TaskKey key, Task<?> task, ExecReason reason, InterruptedException e) {
        if(!logger.isInfoEnabled()) return;
        indentation.decrementAndGet();
        logger.info(getIndent() + "← " + StringUtil.toShortString(e.toString(), strLimit));
    }


    @Override
    public void invokeCallbackStart(Consumer<@Nullable Serializable> observer, TaskKey key, @Nullable Serializable output) {
        if(!logger.isTraceEnabled()) return;
        logger.trace(getIndent() + "§ " + StringUtil.toShortString(observer.toString(), strLimit) + "(" + outputToString(output) + ")");
    }

    @Override
    public void invokeCallbackEnd(Consumer<@Nullable Serializable> observer, TaskKey key, @Nullable Serializable output) {}


    private String getIndent() {
        final int indentation = this.indentation.get();
        final StringBuilder sb = new StringBuilder(indentation);
        for(int i = 0; i < indentation; ++i) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private String outputToString(@Nullable Serializable output) {
        return output != null ? StringUtil.toShortString(output.toString(), strLimit) : "null";
    }
}
