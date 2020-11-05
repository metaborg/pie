package mb.pie.runtime.tracer;

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
    private final int strLimit;
    private final AtomicInteger indentation = new AtomicInteger(0);


    public LoggingTracer(LoggerFactory loggerFactory) {
        this(loggerFactory, 2048);
    }

    public LoggingTracer(LoggerFactory loggerFactory, int strLimit) {
        this.logger = loggerFactory.create(LoggingTracer.class);
        this.strLimit = strLimit;
    }


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


    @Override
    public void requireBottomUpInitialStart(Set<? extends ResourceKey> changedResources) {
        if(!logger.isTraceEnabled()) return;
        logger.trace(getIndent() + "Bottom-up build start: " + changedResources);
        indentation.incrementAndGet();
    }

    @Override
    public void requireBottomUpInitialEnd() {
        if(!logger.isTraceEnabled()) return;
        indentation.decrementAndGet();
        logger.trace(getIndent() + "Bottom-up build end");
    }

    @Override
    public void scheduleAffectedByProvidedResource(ResourceKey changedResource, TaskKey provider, boolean isObserved, @Nullable InconsistentResourceProvide reason) {
        if(!logger.isTraceEnabled()) return;
        if(reason != null) {
            logger.trace(getIndent() + "☒ " + provider.toShortString(strLimit) + " ↠ " + changedResource + " (" + reason.dep.stamp + " ≠ " + reason.newStamp + ")");
        } else if(isObserved) {
            logger.trace(getIndent() + "☑ " + provider.toShortString(strLimit) + " ↠ " + changedResource);
        } else {
            logger.trace(getIndent() + "☐ " + provider.toShortString(strLimit) + " ↠ " + changedResource);
        }
    }

    @Override
    public void scheduleAffectedByRequiredResource(ResourceKey changedResource, TaskKey requiree, boolean isObserved, @Nullable InconsistentResourceRequire reason) {
        if(!logger.isTraceEnabled()) return;
        if(reason != null) {
            logger.trace(getIndent() + "☒ " + requiree.toShortString(strLimit) + " → " + changedResource + " (" + reason.dep.stamp + " ≠ " + reason.newStamp + ")");
        } else if(isObserved) {
            logger.trace(getIndent() + "☑ " + requiree.toShortString(strLimit) + " → " + changedResource);
        } else {
            logger.trace(getIndent() + "☐ " + requiree.toShortString(strLimit) + " → " + changedResource);
        }
    }

    @Override
    public void scheduleAffectedByRequiredTask(TaskKey requiree, TaskKey requirer, boolean isObserved, @Nullable InconsistentTaskRequire reason) {
        if(!logger.isTraceEnabled()) return;
        if(reason != null) {
            logger.trace(getIndent() + "☒ " + requirer.toShortString(strLimit) + " ⇢ " + requiree.toShortString(strLimit) + " (" + outputToString(reason.dep.stamp) + " ≠ " + outputToString(reason.newStamp) + ")");
        } else if(isObserved) {
            logger.trace(getIndent() + "☑ " + requirer.toShortString(strLimit) + " ⇢ " + requiree.toShortString(strLimit));
        } else {
            logger.trace(getIndent() + "☐ " + requirer.toShortString(strLimit) + " ⇢ " + requiree.toShortString(strLimit));
        }
    }

    @Override
    public void scheduleTask(TaskKey key) {
        if(!logger.isTraceEnabled()) return;
        logger.trace(getIndent() + "† " + key);
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
    public void checkResourceProvideStart(TaskKey key, Task<?> task, ResourceProvideDep dep) {}

    @Override
    public void checkResourceProvideEnd(TaskKey key, Task<?> task, ResourceProvideDep dep, @Nullable InconsistentResourceProvide reason) {
        if(!logger.isTraceEnabled()) return;
        if(reason != null) {
            logger.trace(getIndent() + "☒ " + dep.key + " (" + dep.stamp + " ≠ " + reason.newStamp + ")");
        } else {
            logger.trace(getIndent() + "☑ " + dep.key);
        }
    }

    @Override
    public void checkResourceRequireStart(TaskKey key, Task<?> task, ResourceRequireDep dep) {}

    @Override
    public void checkResourceRequireEnd(TaskKey key, Task<?> task, ResourceRequireDep dep, @Nullable InconsistentResourceRequire reason) {
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
