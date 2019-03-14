package mb.pie.runtime.exec;

import mb.fs.api.node.FSNode;
import mb.fs.api.path.FSPath;
import mb.pie.api.*;
import mb.pie.api.exec.Cancelled;
import mb.pie.api.fs.FileSystemResource;
import mb.pie.api.fs.ResourceUtil;
import mb.pie.api.stamp.OutputStamp;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamp;
import mb.pie.api.stamp.ResourceStamper;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class ExecContextImpl implements ExecContext {
    private final RequireTask requireTask;
    private final Cancelled cancel;
    private final TaskDefs taskDefs;
    private final ResourceSystems resourceSystems;
    private final Store store;
    private final OutputStamper defaultOutputStamper;
    private final ResourceStamper<FileSystemResource> defaultRequireFileSystemStamper;
    private final ResourceStamper<FileSystemResource> defaultProvideFileSystemStamper;
    private final Logger logger;

    private final ArrayList<TaskRequireDep> taskRequires = new ArrayList<>();
    private final ArrayList<ResourceRequireDep> resourceRequires = new ArrayList<>();
    private final ArrayList<ResourceProvideDep> resourceProvides = new ArrayList<>();


    public ExecContextImpl(
        RequireTask requireTask,
        Cancelled cancel,
        TaskDefs taskDefs,
        ResourceSystems resourceSystems,
        Store store,
        OutputStamper defaultOutputStamper,
        ResourceStamper<FileSystemResource> defaultRequireFileSystemStamper,
        ResourceStamper<FileSystemResource> defaultProvideFileSystemStamper,
        Logger logger
    ) {
        this.requireTask = requireTask;
        this.cancel = cancel;
        this.taskDefs = taskDefs;
        this.resourceSystems = resourceSystems;
        this.store = store;
        this.defaultOutputStamper = defaultOutputStamper;
        this.defaultRequireFileSystemStamper = defaultRequireFileSystemStamper;
        this.defaultProvideFileSystemStamper = defaultProvideFileSystemStamper;
        this.logger = logger;
    }


    @Override
    public <I extends Serializable, O extends @Nullable Serializable> O require(Task<I, O> task) throws ExecException, InterruptedException {
        return require(task, defaultOutputStamper);
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
        return require(new Task<>(taskDef, input), defaultOutputStamper);
    }

    @Override
    public <I extends Serializable, O extends @Nullable Serializable> O require(TaskDef<I, O> taskDef, I input, OutputStamper stamper) throws ExecException, InterruptedException {
        return require(new Task<>(taskDef, input), stamper);
    }

    @Override
    public <I extends Serializable> @Nullable Serializable require(STask<I> task) throws ExecException, InterruptedException {
        return require(task.toTask(taskDefs), defaultOutputStamper);
    }

    @Override
    public <I extends Serializable> @Nullable Serializable require(STask<I> task, OutputStamper stamper) throws ExecException, InterruptedException {
        return require(task.toTask(taskDefs), stamper);
    }

    @Override
    public <I extends Serializable> @Nullable Serializable require(String taskDefId, I input) throws ExecException, InterruptedException {
        final TaskDef<I, @Nullable Serializable> taskDef = getTaskDef(taskDefId);
        return require(new Task<>(taskDef, input), defaultOutputStamper);
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


    @Override public <R extends Resource> void require(R resource, ResourceStamper<R> stamper) throws IOException {
        @SuppressWarnings("unchecked") final ResourceStamp<Resource> stamp = (ResourceStamp<Resource>) stamper.stamp(resource);
        resourceRequires.add(new ResourceRequireDep(resource.getKey(), stamp));
        Stats.addFileReq();
    }

    @Override public <R extends Resource> void provide(R resource, ResourceStamper<R> stamper) throws IOException {
        @SuppressWarnings("unchecked") final ResourceStamp<Resource> stamp = (ResourceStamp<Resource>) stamper.stamp(resource);
        resourceProvides.add(new ResourceProvideDep(resource.getKey(), stamp));
        Stats.addFileGen();
    }


    @Override public FSNode require(FSPath path) throws IOException {
        return require(path, defaultRequireFileSystemStamper);
    }

    @Override public FSNode require(FSPath path, ResourceStamper<FileSystemResource> stamper) throws IOException {
        final String fileSystemId = path.getFileSystemId();
        final @Nullable ResourceSystem resourceSystem = resourceSystems.getResourceSystem(fileSystemId);
        if(resourceSystem == null) {
            throw new RuntimeException("Cannot get resource system for path " + path + "; resource system with id '" + fileSystemId + "' does not exist");
        }
        final FSNode node = ResourceUtil.toNode(path, resourceSystem);
        require(node, stamper);
        return node;
    }

    @Override public void provide(FSPath path) throws IOException {
        provide(path, defaultProvideFileSystemStamper);
    }

    @Override public void provide(FSPath path, ResourceStamper<FileSystemResource> stamper) throws IOException {
        final String fileSystemId = path.getFileSystemId();
        final @Nullable ResourceSystem resourceSystem = resourceSystems.getResourceSystem(fileSystemId);
        if(resourceSystem == null) {
            throw new RuntimeException("Cannot get resource system for path " + path + "; resource system with id '" + fileSystemId + "' does not exist");
        }
        final FSNode node = ResourceUtil.toNode(path, resourceSystem);
        provide(node, stamper);
    }


    @Override public FSNode toNode(FSPath path) {
        final String fileSystemId = path.getFileSystemId();
        final @Nullable ResourceSystem resourceSystem = resourceSystems.getResourceSystem(fileSystemId);
        if(resourceSystem == null) {
            throw new RuntimeException("Cannot get resource system for path " + path + "; resource system with id '" + fileSystemId + "' does not exist");
        }
        return ResourceUtil.toNode(path, resourceSystem);
    }


    @Override public ResourceStamper<FileSystemResource> defaultRequireFileSystemStamper() {
        return defaultRequireFileSystemStamper;
    }

    @Override public ResourceStamper<FileSystemResource> defaultProvideFileSystemStamper() {
        return defaultProvideFileSystemStamper;
    }

    @Override public Logger logger() {
        return logger;
    }


    public class Deps {
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
