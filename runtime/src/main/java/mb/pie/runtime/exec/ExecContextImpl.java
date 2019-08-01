package mb.pie.runtime.exec;

import mb.pie.api.*;
import mb.pie.api.exec.Cancelled;
import mb.pie.api.stamp.OutputStamp;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamp;
import mb.pie.api.stamp.ResourceStamper;
import mb.pie.runtime.DefaultStampers;
import mb.resource.*;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class ExecContextImpl implements ExecContext {
    private final RequireTask requireTask;
    private final boolean modifyObservability;
    private final Cancelled cancel;
    private final TaskDefs taskDefs;
    private final ResourceService resourceService;
    private final Store store;
    private final DefaultStampers defaultStampers;
    private final Logger logger;

    private final ArrayList<TaskRequireDep> taskRequires = new ArrayList<>();
    private final ArrayList<ResourceRequireDep> resourceRequires = new ArrayList<>();
    private final ArrayList<ResourceProvideDep> resourceProvides = new ArrayList<>();


    public ExecContextImpl(
        RequireTask requireTask,
        boolean modifyObservability,
        Cancelled cancel,
        TaskDefs taskDefs,
        ResourceService resourceService,
        Store store,
        DefaultStampers defaultStampers,
        Logger logger
    ) {
        this.requireTask = requireTask;
        this.modifyObservability = modifyObservability;
        this.cancel = cancel;
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.store = store;
        this.defaultStampers = defaultStampers;
        this.logger = logger;
    }


    @Override
    public <O extends @Nullable Serializable> O require(Task<O> task) throws ExecException, InterruptedException {
        return require(task, defaultStampers.output);
    }

    @Override
    public <O extends @Nullable Serializable> O require(Task<O> task, OutputStamper stamper) throws ExecException, InterruptedException {
        cancel.throwIfCancelled();
        final TaskKey key = task.key();
        final O output = requireTask.require(key, task, modifyObservability, cancel);
        final OutputStamp stamp = stamper.stamp(output);
        taskRequires.add(new TaskRequireDep(key, stamp));
        Stats.addCallReq();
        return output;
    }

    @Override
    public <I extends Serializable, O extends @Nullable Serializable> O require(TaskDef<I, O> taskDef, I input) throws ExecException, InterruptedException {
        return require(new Task<>(taskDef, input), defaultStampers.output);
    }

    @Override
    public <I extends Serializable, O extends @Nullable Serializable> O require(TaskDef<I, O> taskDef, I input, OutputStamper stamper) throws ExecException, InterruptedException {
        return require(new Task<>(taskDef, input), stamper);
    }

    @Override
    public @Nullable Serializable require(STask sTask) throws ExecException, InterruptedException {
        return require(sTask.toTask(taskDefs), defaultStampers.output);
    }

    @Override
    public @Nullable Serializable require(STask sTask, OutputStamper stamper) throws ExecException, InterruptedException {
        return require(sTask.toTask(taskDefs), stamper);
    }

    @Override
    public @Nullable Serializable require(String taskDefId, Serializable input) throws ExecException, InterruptedException {
        final TaskDef<?, ?> taskDef = getTaskDef(taskDefId);
        return require(new Task<>(taskDef, input), defaultStampers.output);
    }

    @Override
    public @Nullable Serializable require(String taskDefId, Serializable input, OutputStamper stamper) throws ExecException, InterruptedException {
        final TaskDef<?, ?> taskDef = getTaskDef(taskDefId);
        return require(new Task<>(taskDef, input), stamper);
    }

    @Override public OutputStamper getDefaultOutputStamper() {
        return defaultStampers.output;
    }

    private TaskDef<?, ?> getTaskDef(String id) {
        final @Nullable TaskDef<?, ?> taskDef = taskDefs.getTaskDef(id);
        if(taskDef != null) {
            return taskDef;
        } else {
            throw new RuntimeException("Cannot retrieve task with identifier '" + id + "', it cannot be found");
        }
    }


    @Override
    public <R extends Resource> void require(R resource, ResourceStamper<R> stamper) throws IOException {
        @SuppressWarnings("unchecked") final ResourceStamp<Resource> stamp =
            (ResourceStamp<Resource>) stamper.stamp(resource);
        resourceRequires.add(new ResourceRequireDep(resource.getKey(), stamp));
        Stats.addFileReq();
    }

    @Override
    public <R extends Resource> void provide(R resource, ResourceStamper<R> stamper) throws IOException {
        @SuppressWarnings("unchecked") final ResourceStamp<Resource> stamp =
            (ResourceStamp<Resource>) stamper.stamp(resource);
        resourceProvides.add(new ResourceProvideDep(resource.getKey(), stamp));
        Stats.addFileGen();
    }

    @Override public Resource getResource(ResourceKey key) {
        return resourceService.getResource(key);
    }

    @Override public HierarchicalResource getResource(ResourcePath path) {
        final Resource resource = resourceService.getResource(path);
        if(!(resource instanceof HierarchicalResource)) {
            throw new ResourceRuntimeException("Cannot get hierarchical resource for path '" + path + "', a resource was found, but it does not implement HierarchicalResource");
        }
        return (HierarchicalResource) resource;
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


    @Override public Logger logger() {
        return logger;
    }


    class Deps {
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
