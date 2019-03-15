package mb.pie.runtime.logger.exec;

import mb.pie.api.*;
import mb.pie.api.exec.ExecReason;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class LoggerExecutorLogger implements ExecutorLogger {
    private final Logger logger;
    private final int descLimit;
    private AtomicInteger indentation = new AtomicInteger(0);


    public LoggerExecutorLogger(Logger logger) {
        this(logger, 200);
    }

    public LoggerExecutorLogger(Logger logger, int descLimit) {
        this.logger = logger;
        this.descLimit = descLimit;
    }


    private String getIndent() {
        final int indentation = this.indentation.get();
        final StringBuilder sb = new StringBuilder(indentation);
        for(int i = 0; i < indentation; ++i) {
            sb.append(' ');
        }
        return sb.toString();
    }

    @Override public void requireTopDownInitialStart(TaskKey key, Task<?, ?> task) {

    }

    @Override public void requireTopDownInitialEnd(TaskKey key, Task<?, ?> task, @Nullable Serializable output) {

    }

    @Override public void requireTopDownStart(TaskKey key, Task<?, ?> task) {
        logger.trace(getIndent() + "v " + task.desc(descLimit));
        indentation.incrementAndGet();
    }

    @Override public void requireTopDownEnd(TaskKey key, Task<?, ?> task, @Nullable Serializable output) {
        indentation.decrementAndGet();
        final String outputString = output != null ? StringUtil.toShortString(output.toString(), descLimit) : "null";
        logger.trace(getIndent() + "✔ " + task.desc(descLimit) + " -> " + outputString);
    }


    @Override public void requireBottomUpInitialStart(Set<ResourceKey> changedResources) {

    }

    @Override public void requireBottomUpInitialEnd() {

    }


    @Override public void checkVisitedStart(TaskKey key) {

    }

    @Override public void checkVisitedEnd(TaskKey key, @Nullable Serializable output) {

    }

    @Override public void checkStoredStart(TaskKey key) {

    }

    @Override public void checkStoredEnd(TaskKey key, @Nullable Serializable output) {

    }

    @Override public void checkResourceProvideStart(TaskKey key, Task<?, ?> task, ResourceProvideDep dep) {

    }

    @Override
    public void checkResourceProvideEnd(TaskKey key, Task<?, ?> task, ResourceProvideDep dep, @Nullable ExecReason reason) {
        if(reason != null) {
            if(reason instanceof InconsistentResourceProvide) {
                logger.trace(getIndent() + "␦ " + dep.key + " (inconsistent: " + dep.stamp + " vs " + ((InconsistentResourceProvide) reason).newStamp + ")");
            } else {
                logger.trace(getIndent() + "␦ " + dep.key + " (inconsistent)");
            }
        } else {
            logger.trace(getIndent() + "␦ " + dep.key + " (consistent: " + dep.stamp + ")");
        }
    }

    @Override public void checkResourceRequireStart(TaskKey key, Task<?, ?> task, ResourceRequireDep dep) {
    }

    @Override
    public void checkResourceRequireEnd(TaskKey key, Task<?, ?> task, ResourceRequireDep dep, @Nullable ExecReason reason) {
        if(reason != null) {
            if(reason instanceof InconsistentResourceProvide) {
                logger.trace(getIndent() + "␦ " + dep.key + " (inconsistent: " + dep.stamp + " vs " + ((InconsistentResourceProvide) reason).newStamp + ")");
            } else {
                logger.trace(getIndent() + "␦ " + dep.key + " (inconsistent)");
            }
        } else {
            logger.trace(getIndent() + "␦ " + dep.key + " (consistent: " + dep.stamp + ")");
        }
    }

    @Override public void checkTaskRequireStart(TaskKey key, Task<?, ?> task, TaskRequireDep dep) {
    }

    @Override
    public void checkTaskRequireEnd(TaskKey key, Task<?, ?> task, TaskRequireDep dep, @Nullable ExecReason reason) {
        if(reason instanceof InconsistentTaskReq) {
            logger.trace(getIndent() + "␦ " + dep.callee.toShortString(descLimit) + " (inconsistent: " + dep.stamp + " vs " + ((InconsistentTaskReq) reason).newStamp + ")");
        } else if(reason == null) {
            logger.trace(getIndent() + "␦ " + dep.callee.toShortString(descLimit) + " (consistent: " + dep.stamp + ")");
        }
    }


    @Override public void executeStart(TaskKey key, Task<?, ?> task, ExecReason reason) {
        logger.info(getIndent() + "> " + task.desc(descLimit) + " (reason: " + reason + ")");
    }

    @Override public void executeEnd(TaskKey key, Task<?, ?> task, ExecReason reason, TaskData<?, ?> data) {
        final String outputString = data.output != null ? StringUtil.toShortString(data.output.toString(), descLimit) : "null";
        logger.info(getIndent() + "< " + StringUtil.toShortString(outputString, descLimit));
    }


    @Override
    public void invokeObserverStart(Consumer<@Nullable Serializable> observer, TaskKey key, @Nullable Serializable output) {
        final String outputString = output != null ? StringUtil.toShortString(output.toString(), descLimit) : "null";
        logger.trace(getIndent() + "@ " + StringUtil.toShortString(observer.toString(), descLimit) + "(" + outputString + ")");
    }

    @Override
    public void invokeObserverEnd(Consumer<@Nullable Serializable> observer, TaskKey key, @Nullable Serializable output) {

    }
}