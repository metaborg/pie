package mb.pie.runtime.exec;

import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.Logger;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.ResourceRequireDep;
import mb.pie.api.STask;
import mb.pie.api.STaskDef;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import mb.pie.api.TaskKey;
import mb.pie.api.TaskRequireDep;
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

public class ExecContextImpl implements ExecContext {
    private final RequireTask requireTask;
    private final boolean modifyObservability;
    private final CancelToken cancel;
    private final TaskDefs taskDefs;
    private final ResourceService resourceService;
    private final DefaultStampers defaultStampers;
    private final Logger logger;

    private final ArrayList<TaskRequireDep> taskRequires = new ArrayList<>();
    private final ArrayList<ResourceRequireDep> resourceRequires = new ArrayList<>();
    private final ArrayList<ResourceProvideDep> resourceProvides = new ArrayList<>();


    public ExecContextImpl(
        RequireTask requireTask,
        boolean modifyObservability,
        CancelToken cancel,
        TaskDefs taskDefs,
        ResourceService resourceService,
        DefaultStampers defaultStampers,
        Logger logger
    ) {
        this.requireTask = requireTask;
        this.modifyObservability = modifyObservability;
        this.cancel = cancel;
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.defaultStampers = defaultStampers;
        this.logger = logger;
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
        final O output = requireTask.require(key, task, modifyObservability, cancel);
        final OutputStamp stamp = stamper.stamp(output);
        taskRequires.add(new TaskRequireDep(key, stamp));
        Stats.addCallReq();
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
        @SuppressWarnings("unchecked") final ResourceStamp<Resource> stamp =
            (ResourceStamp<Resource>)stamper.stamp(resource);
        resourceRequires.add(new ResourceRequireDep(resource.getKey(), stamp));
        Stats.addFileReq();
    }

    @Override
    public <R extends Resource> void provide(R resource, ResourceStamper<R> stamper) throws IOException {
        @SuppressWarnings("unchecked") final ResourceStamp<Resource> stamp =
            (ResourceStamp<Resource>)stamper.stamp(resource);
        resourceProvides.add(new ResourceProvideDep(resource.getKey(), stamp));
        Stats.addFileGen();
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
        return logger;
    }


    static class Deps {
        final ArrayList<TaskRequireDep> taskRequires;
        final ArrayList<ResourceRequireDep> resourceRequires;
        final ArrayList<ResourceProvideDep> resourceProvides;

        Deps(ArrayList<TaskRequireDep> taskRequires, ArrayList<ResourceRequireDep> resourceRequires, ArrayList<ResourceProvideDep> resourceProvides) {
            this.taskRequires = taskRequires;
            this.resourceRequires = resourceRequires;
            this.resourceProvides = resourceProvides;
        }
    }

    Deps deps() {
        return new Deps(taskRequires, resourceRequires, resourceProvides);
    }
}
