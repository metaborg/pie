package mb.pie.runtime.exec;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.Layer;
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
import java.util.LinkedHashSet;

public class ExecContextImpl implements ExecContext {
    private final TaskDefs taskDefs;
    private final ResourceService resourceService;
    private final DefaultStampers defaultStampers;
    private final Layer layer;
    private final LoggerFactory loggerFactory;
    private final Tracer tracer;

    private final TaskKey currentTaskKey;

    private final StoreWriteTxn txn;
    private final RequireTask requireTask;
    private final boolean modifyObservability;
    private final CancelToken cancel;

    // LinkedHashSet to remove duplicates and preserve insertion order.
    private final LinkedHashSet<TaskRequireDep> taskRequires = new LinkedHashSet<>();
    private final LinkedHashSet<ResourceRequireDep> resourceRequires = new LinkedHashSet<>();
    private final LinkedHashSet<ResourceProvideDep> resourceProvides = new LinkedHashSet<>();


    public ExecContextImpl(
        TaskDefs taskDefs,
        ResourceService resourceService,
        DefaultStampers defaultStampers,
        Layer layer,
        LoggerFactory loggerFactory,
        Tracer tracer,

        TaskKey currentTaskKey,

        StoreWriteTxn txn,
        RequireTask requireTask,
        boolean modifyObservability,
        CancelToken cancel
    ) {
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.defaultStampers = defaultStampers;
        this.layer = layer;
        this.loggerFactory = loggerFactory;
        this.tracer = tracer;

        this.currentTaskKey = currentTaskKey;

        this.txn = txn;
        this.requireTask = requireTask;
        this.modifyObservability = modifyObservability;
        this.cancel = cancel;
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
        final TaskKey callee = task.key();
        layer.validateTaskRequire(currentTaskKey, callee, txn);
        txn.addTaskRequire(currentTaskKey, callee);

        final O output = requireTask.require(callee, task, modifyObservability, txn, cancel);
        final OutputStamp stamp = stamper.stamp(output);
        final TaskRequireDep dep = new TaskRequireDep(callee, stamp);
        if(taskRequires.contains(dep)) return output;

        taskRequires.add(dep);
        tracer.requiredTask(task, stamper);
        txn.addTaskRequireDep(currentTaskKey, dep);
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
        final ResourceRequireDep dep = new ResourceRequireDep(resource.getKey(), stamp);
        if(resourceRequires.contains(dep)) return;

        resourceRequires.add(dep);
        tracer.requiredResource(resource, stamper);
        layer.validateResourceRequireDep(currentTaskKey, dep, txn);
        txn.addResourceRequireDep(currentTaskKey, dep);
    }

    @Override
    public <R extends Resource> void provide(R resource, ResourceStamper<R> stamper) throws IOException {
        @SuppressWarnings("unchecked") final ResourceStamp<Resource> stamp = (ResourceStamp<Resource>)stamper.stamp(resource);
        final ResourceProvideDep dep = new ResourceProvideDep(resource.getKey(), stamp);
        if(resourceProvides.contains(dep)) return;

        resourceProvides.add(dep);
        tracer.providedResource(resource, stamper);
        layer.validateResourceProvideDep(currentTaskKey, dep, txn);
        txn.addResourceProvideDep(currentTaskKey, dep);
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
