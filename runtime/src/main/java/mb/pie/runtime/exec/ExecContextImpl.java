package mb.pie.runtime.exec;

import mb.pie.api.*;
import mb.pie.api.exec.Cancelled;
import mb.pie.api.stamp.OutputStamp;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamp;
import mb.pie.api.stamp.ResourceStamper;
import mb.pie.runtime.DefaultStampers;
import mb.resource.ReadableResource;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.ResourceRegistry;
import mb.resource.fs.FSResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class ExecContextImpl implements ExecContext {
    private final RequireTask requireTask;
    private final Cancelled cancel;
    private final TaskDefs taskDefs;
    private final ResourceRegistry resourceRegistry;
    private final Store store;
    private final DefaultStampers defaultStampers;
    private final Logger logger;

    private final ArrayList<TaskRequireDep> taskRequires = new ArrayList<>();
    private final ArrayList<ResourceRequireDep> resourceRequires = new ArrayList<>();
    private final ArrayList<ResourceProvideDep> resourceProvides = new ArrayList<>();


    public ExecContextImpl(
        RequireTask requireTask,
        Cancelled cancel,
        TaskDefs taskDefs,
        ResourceRegistry resourceRegistry,
        Store store,
        DefaultStampers defaultStampers,
        Logger logger
    ) {
        this.requireTask = requireTask;
        this.cancel = cancel;
        this.taskDefs = taskDefs;
        this.resourceRegistry = resourceRegistry;
        this.store = store;
        this.defaultStampers = defaultStampers;
        this.logger = logger;
    }


    @Override
    public <I extends Serializable, O extends @Nullable Serializable> O require(Task<I, O> task) throws ExecException, InterruptedException {
        return require(task, defaultStampers.output);
    }

    @Override
    public <I extends Serializable, O extends @Nullable Serializable> O require(Task<I, O> task, OutputStamper stamper) throws ExecException, InterruptedException {
        cancel.throwIfCancelled();
        final TaskKey key = task.key();
        final O output = requireTask.require(key, task, cancel);
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
    public <I extends Serializable> @Nullable Serializable require(STask<I> task) throws ExecException, InterruptedException {
        return require(task.toTask(taskDefs), defaultStampers.output);
    }

    @Override
    public <I extends Serializable> @Nullable Serializable require(STask<I> task, OutputStamper stamper) throws ExecException, InterruptedException {
        return require(task.toTask(taskDefs), stamper);
    }

    @Override
    public <I extends Serializable> @Nullable Serializable require(String taskDefId, I input) throws ExecException, InterruptedException {
        final TaskDef<I, @Nullable Serializable> taskDef = getTaskDef(taskDefId);
        return require(new Task<>(taskDef, input), defaultStampers.output);
    }

    @Override
    public <I extends Serializable> @Nullable Serializable require(String taskDefId, I input, OutputStamper stamper) throws ExecException, InterruptedException {
        final TaskDef<I, @Nullable Serializable> taskDef = getTaskDef(taskDefId);
        return require(new Task<>(taskDef, input), stamper);
    }

    private <I extends Serializable> TaskDef<I, @Nullable Serializable> getTaskDef(String id) {
        final @Nullable TaskDef<I, @Nullable Serializable> taskDef = taskDefs.getTaskDef(id);
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
        return resourceRegistry.getResource(key);
    }


    @Override public ResourceStamper<ReadableResource> defaultRequireReadableResourceStamper() {
        return defaultStampers.requireReadableResource;
    }

    @Override public ResourceStamper<ReadableResource> defaultProvideReadableResourceStamper() {
        return defaultStampers.provideReadableResource;
    }

    @Override public ResourceStamper<FSResource> defaultRequireFSResourceStamper() {
        return defaultStampers.requireFSResource;
    }

    @Override public ResourceStamper<FSResource> defaultProvideFSResourceStamper() {
        return defaultStampers.provideFSResource;
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
