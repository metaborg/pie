package mb.pie.api;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.log.noop.NoopLogger;
import mb.pie.api.exec.CancelToken;
import mb.pie.api.exec.NullCancelableToken;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.DefaultResourceService;
import mb.resource.ReadableResource;
import mb.resource.Resource;
import mb.resource.ResourceService;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;

/**
 * A mock execution context that just executes tasks and ignores dependencies.
 */
public class MockExecContext implements ExecContext {
    private final TaskDefs taskDefs;
    private final ResourceService resourceService;
    private final Logger logger;
    private @Nullable Serializable internalObject = null;

    public MockExecContext(TaskDefs taskDefs, ResourceService resourceService, Logger logger) {
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.logger = logger;
    }

    public MockExecContext(TaskDefs taskDefs, ResourceService resourceService, LoggerFactory loggerFactory) {
        this(taskDefs, resourceService, loggerFactory.create(MockExecContext.class));
    }

    public MockExecContext(TaskDefs taskDefs, ResourceService resourceService) {
        this(taskDefs, resourceService, NoopLogger.instance);
    }

    public MockExecContext(TaskDefs taskDefs) {
        this(taskDefs, new DefaultResourceService(new FSResourceRegistry()));
    }

    public MockExecContext() {
        this(new MapTaskDefs());
    }

    @Override public <I extends Serializable, O extends Serializable> O require(TaskDef<I, O> taskDef, I input) {
        try {
            return taskDef.exec(this, input);
        } catch(Exception e) {
            throw new UncheckedExecException("Executing task '" + taskDef.desc(input, 100) + "' failed unexpectedly", e);
        }
    }

    @Override
    public <I extends Serializable, O extends Serializable> O require(TaskDef<I, O> taskDef, I input, OutputStamper stamper) {
        return require(taskDef, input);
    }

    @Override public <O extends Serializable> O require(Task<O> task) {
        return require(task.taskDef, task.input);
    }

    @Override public <O extends Serializable> O require(Task<O> task, OutputStamper stamper) {
        return require(task.taskDef, task.input);
    }

    @Override public <I extends Serializable, O extends Serializable> O require(STaskDef<I, O> sTaskDef, I input) {
        return require(sTaskDef.toTaskDef(taskDefs), input);
    }

    @Override
    public <I extends Serializable, O extends Serializable> O require(STaskDef<I, O> sTaskDef, I input, OutputStamper stamper) {
        return require(sTaskDef.toTaskDef(taskDefs), input);
    }

    @Override public <O extends Serializable> O require(STask<O> sTask) {
        return sTask.get(this);
    }

    @Override public <O extends Serializable> O require(STask<O> sTask, OutputStamper stamper) {
        return sTask.get(this);
    }

    @Override public <O extends Serializable> O require(Supplier<O> supplier) {
        return supplier.get(this);
    }

    @Override public <I extends Serializable, O extends Serializable> O require(Function<I, O> function, I input) {
        return function.apply(this, input);
    }

    @Override public OutputStamper getDefaultOutputStamper() {
        return OutputStampers.equals();
    }

    @Override public ResourceService getResourceService() {
        return resourceService;
    }

    @Override public <R extends Resource> boolean require(R resource, ResourceStamper<R> stamper) throws IOException {
        return true;
    }

    @Override public <R extends Resource> boolean provide(R resource, ResourceStamper<R> stamper) throws IOException {
        return true;
    }

    @Override public ResourceStamper<ReadableResource> getDefaultRequireReadableResourceStamper() {
        return ResourceStampers.modifiedFile();
    }

    @Override public ResourceStamper<ReadableResource> getDefaultProvideReadableResourceStamper() {
        return ResourceStampers.modifiedFile();
    }

    @Override public ResourceStamper<HierarchicalResource> getDefaultRequireHierarchicalResourceStamper() {
        return ResourceStampers.modifiedFile();
    }

    @Override public ResourceStamper<HierarchicalResource> getDefaultProvideHierarchicalResourceStamper() {
        return ResourceStampers.modifiedFile();
    }


    @Override public @Nullable Serializable getInternalObject() {
        return internalObject;
    }

    @Override public void setInternalObject(@Nullable Serializable obj) {
        internalObject = obj;
    }

    @Override public void clearInternalObject() {
        internalObject = null;
    }


    @Override public @Nullable Serializable getPreviousInput() {
        return null;
    }

    @Override public @Nullable Serializable getPreviousOutput() {
        return null;
    }

    @Override public @Nullable Observability getPreviousObservability() {
        return null;
    }

    @Override public Iterable<TaskRequireDep> getPreviousTaskRequireDeps() {
        return Collections.emptySet();
    }

    @Override public Iterable<ResourceRequireDep> getPreviousResourceRequireDeps() {
        return Collections.emptySet();
    }

    @Override public Iterable<ResourceProvideDep> getPreviousResourceProvideDeps() {
        return Collections.emptySet();
    }


    @Override public CancelToken cancelToken() {
        return NullCancelableToken.instance;
    }

    @Override public Logger logger() {
        return logger;
    }
}
