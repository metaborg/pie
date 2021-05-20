package mb.pie.runtime.exec;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.ResourceRequireDep;
import mb.pie.api.STask;
import mb.pie.api.STaskDef;
import mb.pie.api.StoreWriteTxn;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import mb.pie.api.TaskKey;
import mb.pie.api.TaskRequireDep;
import mb.pie.api.Tracer;
import mb.pie.api.exec.CancelToken;
import mb.pie.api.stamp.OutputStamp;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamp;
import mb.pie.api.stamp.ResourceStamper;
import mb.pie.runtime.DefaultStampers;
import mb.resource.ReadableResource;
import mb.resource.Resource;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class ExecContextImpl implements ExecContext {
    private final StoreWriteTxn txn;
    private final RequireTask requireTask;
    private final boolean modifyObservability;
    private final CancelToken cancel;
    private final TaskDefs taskDefs;
    private final ResourceService resourceService;
    private final DefaultStampers defaultStampers;
    private final LoggerFactory loggerFactory;
    private final Tracer tracer;

    // LinkedHashSet to remove duplicates while preserving insertion order.
    private final LinkedHashSet<TaskRequireDep> taskRequires = new LinkedHashSet<>();
    private final LinkedHashSet<ResourceRequireDep> resourceRequires = new LinkedHashSet<>();
    private final LinkedHashSet<ResourceProvideDep> resourceProvides = new LinkedHashSet<>();


    public ExecContextImpl(
        StoreWriteTxn txn,
        RequireTask requireTask,
        boolean modifyObservability,
        CancelToken cancel,
        TaskDefs taskDefs,
        ResourceService resourceService,
        DefaultStampers defaultStampers,
        LoggerFactory loggerFactory,
        Tracer tracer
    ) {
        this.txn = txn;
        this.requireTask = requireTask;
        this.modifyObservability = modifyObservability;
        this.cancel = cancel;
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.defaultStampers = defaultStampers;
        this.loggerFactory = loggerFactory;
        this.tracer = tracer;
    }


    @Override
    public <I extends Serializable, O extends @Nullable Serializable> O require(TaskDef<I, O> taskDef, I input) {
        return require(new Task<>(taskDef, input), defaultStampers.output);
    }

    @Override
    public <I extends Serializable, O extends @Nullable Serializable> O require(TaskDef<I, O> taskDef, I input, OutputStamper stamper) {
        return require(new Task<>(taskDef, input), stamper);
    }

    @Override
    public <O extends @Nullable Serializable> O require(Task<O> task) {
        return require(task, defaultStampers.output);
    }

    @Override
    public <O extends @Nullable Serializable> O require(Task<O> task, OutputStamper stamper) {
        cancel.throwIfCanceled();
        final TaskKey key = task.key();
        final O output = requireTask.require(key, task, modifyObservability, txn, cancel);
        final OutputStamp stamp = stamper.stamp(output);
        taskRequires.add(new TaskRequireDep(key, stamp));
        tracer.requiredTask(task, stamper);
        return output;
    }

    @Override
    public <I extends Serializable, O extends @Nullable Serializable> O require(STaskDef<I, O> sTaskDef, I input) {
        return require(new Task<>(sTaskDef.toTaskDef(taskDefs), input), defaultStampers.output);
    }

    @Override
    public <I extends Serializable, O extends @Nullable Serializable> O require(STaskDef<I, O> sTaskDef, I input, OutputStamper stamper) {
        return require(new Task<>(sTaskDef.toTaskDef(taskDefs), input), stamper);
    }

    @Override
    public <O extends @Nullable Serializable> O require(STask<O> sTask) {
        return require(sTask.toTask(taskDefs), defaultStampers.output);
    }

    @Override
    public <O extends @Nullable Serializable> O require(STask<O> sTask, OutputStamper stamper) {
        return require(sTask.toTask(taskDefs), stamper);
    }

    @Override
    public <O extends @Nullable Serializable> O require(Supplier<O> supplier) {
        return supplier.get(this);
    }

    @Override
    public <I extends Serializable, O extends @Nullable Serializable> O require(Function<I, O> function, I input) {
        return function.apply(this, input);
    }

    @Override public OutputStamper getDefaultOutputStamper() {
        return defaultStampers.output;
    }


    @Override public ResourceService getResourceService() {
        return resourceService;
    }

    @Override
    public <R extends Resource> void require(R resource, ResourceStamper<R> stamper) throws IOException {
        @SuppressWarnings("unchecked") final ResourceStamp<Resource> stamp = (ResourceStamp<Resource>)stamper.stamp(resource);
        resourceRequires.add(new ResourceRequireDep(resource.getKey(), stamp));
        tracer.requiredResource(resource, stamper);
    }

    @Override
    public <R extends Resource> void provide(R resource, ResourceStamper<R> stamper) throws IOException {
        @SuppressWarnings("unchecked") final ResourceStamp<Resource> stamp = (ResourceStamp<Resource>)stamper.stamp(resource);
        resourceProvides.add(new ResourceProvideDep(resource.getKey(), stamp));
        tracer.providedResource(resource, stamper);
    }


    @Override public ResourceStamper<ReadableResource> getDefaultRequireReadableResourceStamper() {
        return defaultStampers.requireReadableResource;
    }

    @Override public ResourceStamper<ReadableResource> getDefaultProvideReadableResourceStamper() {
        return defaultStampers.provideReadableResource;
    }

    @Override public ResourceStamper<HierarchicalResource> getDefaultRequireHierarchicalResourceStamper() {
        return defaultStampers.requireHierarchicalResource;
    }

    @Override public ResourceStamper<HierarchicalResource> getDefaultProvideHierarchicalResourceStamper() {
        return defaultStampers.provideHierarchicalResource;
    }


    @Override public CancelToken cancelToken() {
        return cancel;
    }

    @Override public Logger logger() {
        return loggerFactory.create(ExecContextImpl.class);
    }


    static class Deps {
        final LinkedHashSet<TaskRequireDep> taskRequires;
        final LinkedHashSet<ResourceRequireDep> resourceRequires;
        final LinkedHashSet<ResourceProvideDep> resourceProvides;

        Deps(LinkedHashSet<TaskRequireDep> taskRequires, LinkedHashSet<ResourceRequireDep> resourceRequires, LinkedHashSet<ResourceProvideDep> resourceProvides) {
            this.taskRequires = taskRequires;
            this.resourceRequires = resourceRequires;
            this.resourceProvides = resourceProvides;
        }
    }

    Deps deps() {
        return new Deps(taskRequires, resourceRequires, resourceProvides);
    }
}
