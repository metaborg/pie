package mb.pie.api;

import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.pie.api.stamp.resource.HashResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.ResourceRuntimeException;
import mb.resource.WritableResource;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

/**
 * Execution context for requiring (creating a dependency to, and getting the up-to-date output object of) tasks, and
 * for requiring (creating a read dependency to) and providing (creating a write dependency to) resources.
 * <p>
 * {@link OutputStamper Output stampers} are use to stamp output objects of tasks, such that a task is only marked
 * out-of-date when the output stamp changes, providing more fine-grained control over incrementality.
 * <p>
 * Similarly, {@link ResourceStamper Resource stampers} are use to stamp required (read) and provided (written)
 * resources (e.g., files), such that a task is only marked out-of-date when the resource stamp changes. For example,
 * the {@link HashResourceStamper} creates a stamp with the hash of a resource, and is only invalidated when the
 * contents of the resource actually change, even if its last modification date changes.
 */
public interface ExecContext {
    //
    // Recording dependencies to tasks, and getting their up-to-date output.
    //

    /**
     * Requires task given by its {@code taskDef} and {@code input}, using the {@link #getDefaultOutputStamper() default
     * output stamper}, returning the up-to-date output object of the task.
     *
     * @param <I>     Type of the input object.
     * @param <O>     Type of the output object.
     * @param taskDef Task definition of the task to require.
     * @param input   Input object of the task to require.
     * @return Up-to-date output object of the task.
     * @throws ExecException        When an executing task throws an exception.
     * @throws InterruptedException When execution is cancelled.
     */
    <I extends Serializable, O extends @Nullable Serializable> O require(TaskDef<I, O> taskDef, I input) throws ExecException, InterruptedException;

    /**
     * Requires task given by its {@code taskDef} and {@code input}, using given {@code stamper}, returning the
     * up-to-date output object of the task.
     *
     * @param <I>     Type of the input object.
     * @param <O>     Type of the output object.
     * @param taskDef Task definition of the task to require.
     * @param input   Input object of the task to require.
     * @param stamper {@link OutputStamper Output stamper} to use.
     * @return Up-to-date output object of the task.
     * @throws ExecException        When an executing task throws an exception.
     * @throws InterruptedException When execution is cancelled.
     */
    <I extends Serializable, O extends @Nullable Serializable> O require(TaskDef<I, O> taskDef, I input, OutputStamper stamper) throws ExecException, InterruptedException;

    /**
     * Requires given {@code task}, using the {@link #getDefaultOutputStamper() default output stamper}, returning the
     * up-to-date output object of the task.
     *
     * @param <O>  Type of the output object.
     * @param task Task to require.
     * @return Up-to-date output object of {@code task}.
     * @throws ExecException        When an executing task throws an exception.
     * @throws InterruptedException When execution is cancelled.
     */
    <O extends @Nullable Serializable> O require(Task<O> task) throws ExecException, InterruptedException;

    /**
     * Requires given {@code task}, using given {@code stamper}, returning the up-to-date output object of the task.
     *
     * @param <O>     Type of the output object.
     * @param task    Task to require.
     * @param stamper {@link OutputStamper Output stamper} to use.
     * @return Up-to-date output object of {@code task}.
     * @throws ExecException        When an executing task throws an exception.
     * @throws InterruptedException When execution is cancelled.
     */
    <O extends @Nullable Serializable> O require(Task<O> task, OutputStamper stamper) throws ExecException, InterruptedException;

    /**
     * Requires task given by the {@link STaskDef serializable task definition} and {@code input} of the task, using the
     * {@link #getDefaultOutputStamper() default output stamper}, returning the up-to-date output object of the task.
     * <p>
     * Prefer {@link #require(Task)} or {@link #require(TaskDef, Serializable)} if possible, as this methods performs a
     * lookup and cast of the task definition, which is less efficient.
     *
     * @param sTaskDef {@link STaskDef Serializable task definition} of the task to require.
     * @param input    Input object of the task to require.
     * @return Up-to-date output object of the task, which must be casted to the correct type.
     * @throws ExecException        When an executing task throws an exception.
     * @throws InterruptedException When execution is cancelled.
     */
    <I extends Serializable, O extends @Nullable Serializable> O require(STaskDef<I, O> sTaskDef, I input) throws ExecException, InterruptedException;

    /**
     * Requires task given by the {@link STaskDef serializable task definition} and {@code input} of the task, using
     * given {@code stamper}, returning the up-to-date output object of the task.
     * <p>
     * Prefer {@link #require(Task, OutputStamper)} or {@link #require(TaskDef, Serializable, OutputStamper)} if
     * possible, as this methods performs a lookup and cast of the task definition, which is less efficient.
     *
     * @param sTaskDef {@link STaskDef Serializable task definition} of the task to require.
     * @param input    Input object of the task to require.
     * @param stamper  {@link OutputStamper Output stamper} to use.
     * @return Up-to-date output object of the task, which must be casted to the correct type.
     * @throws ExecException        When an executing task throws an exception.
     * @throws InterruptedException When execution is cancelled.
     */
    <I extends Serializable, O extends @Nullable Serializable> O require(STaskDef<I, O> sTaskDef, I input, OutputStamper stamper) throws ExecException, InterruptedException;

    /**
     * Requires task given by its {@link STask serializable task form}, using the {@link #getDefaultOutputStamper()
     * default output stamper}, returning the up-to-date output object of the task.
     * <p>
     * Prefer {@link #require(Task)} or {@link #require(TaskDef, Serializable)} if possible, as this methods performs a
     * lookup and cast of the task definition, which is less efficient.
     *
     * @param sTask {@link STask Serializable task form} of the task to require.
     * @return Up-to-date output object of the task, which must be casted to the correct type.
     * @throws ExecException        When an executing task throws an exception.
     * @throws InterruptedException When execution is cancelled.
     */
    <O extends @Nullable Serializable> O require(STask<O> sTask) throws ExecException, InterruptedException;

    /**
     * Requires task given by its {@link STask serializable task form}, using given {@code stamper}, returning the
     * up-to-date output object of the task.
     * <p>
     * Prefer {@link #require(Task, OutputStamper)} or {@link #require(TaskDef, Serializable, OutputStamper)} if
     * possible, as this methods performs a lookup and cast of the task definition, which is less efficient.
     *
     * @param sTask   {@link STask Serializable task form} of the task to require.
     * @param stamper {@link OutputStamper Output stamper} to use.
     * @return Up-to-date output object of the task, which must be casted to the correct type.
     * @throws ExecException        When an executing task throws an exception.
     * @throws InterruptedException When execution is cancelled.
     */
    <O extends @Nullable Serializable> O require(STask<O> sTask, OutputStamper stamper) throws ExecException, InterruptedException;

    /**
     * Returns output of given {@link Supplier incremental supplier}, which may in turn require the output of a task, or
     * require and read a resource, using this execution context.
     *
     * @param <O>      Type of the output object.
     * @param supplier {@link Supplier} to get output of.
     * @return Up-to-date output object of {@code supplier}.
     * @throws ExecException        When an executing task throws an exception.
     * @throws InterruptedException When execution is cancelled.
     */
    <O extends @Nullable Serializable> O require(Supplier<O> supplier) throws ExecException, IOException, InterruptedException;

    /**
     * Returns output of given {@link Function incremental function} applied to given {@code input}, which may in turn
     * require other tasks and resources using this execution context.
     *
     * @param <I>      Type of the input object.
     * @param <O>      Type of the output object.
     * @param function {@link Function} to get output of.
     * @param input    Input to apply function to.
     * @return Up-to-date output object of {@code function}.
     * @throws Exception When an executing task throws an exception.
     */
    <I extends Serializable, O extends @Nullable Serializable> O require(Function<I, O> function, I input) throws Exception;

    /**
     * Gets the default output stamper.
     *
     * @return Default output stamper.
     */
    OutputStamper getDefaultOutputStamper();


    //
    // Recording dependencies to resources.
    //


    /**
     * Marks given {@code resource} as required (read), using given {@code stamper}, creating a required resource
     * dependency.
     *
     * @param <R>      Type of the resource.
     * @param resource Resource to require.
     * @param stamper  {@link ResourceStamper Resource stamper} to use.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    <R extends Resource> void require(R resource, ResourceStamper<R> stamper) throws IOException;

    /**
     * Marks given {@code resource} as provided (written to/created), using given {@code stamper}, creating a provided
     * resource dependency.
     * <p>
     * The current contents of the resource may be used for change detection, so be sure to call this method *AFTER*
     * modifying the resource.
     *
     * @param <R>      Type of the resource.
     * @param resource Resource to provide.
     * @param stamper  {@link ResourceStamper Resource stamper} to use.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    <R extends Resource> void provide(R resource, ResourceStamper<R> stamper) throws IOException;

    /**
     * Gets resource for given key.
     *
     * @param key Key to get resource for.
     * @return Resource for {@code key}.
     * @throws ResourceRuntimeException when given {@code key} cannot be resolved to a resource.
     */
    Resource getResource(ResourceKey key);

    /**
     * Gets readable resource for given key.
     *
     * @param key Key to get readable resource for.
     * @return Readable resource for {@code key}.
     * @throws ResourceRuntimeException when given {@code key} cannot be resolved to a readable resource.
     */
    ReadableResource getReadableResource(ResourceKey key);

    /**
     * Gets writable resource for given key.
     *
     * @param key Key to get writable resource for.
     * @return Writable resource for {@code key}.
     * @throws ResourceRuntimeException when given {@code key} cannot be resolved to a writable resource.
     */
    WritableResource getWritableResource(ResourceKey key);

    /**
     * Gets a hierarchical resource for given path.
     *
     * @param path Path to get resource for.
     * @return Hierarchical resource for {@code path}.
     * @throws ResourceRuntimeException when given {@code path} cannot be resolved to a hierarchical resource.
     */
    HierarchicalResource getHierarchicalResource(ResourcePath path);

    /**
     * Marks resource with given {@code key} as required (read), using given {@code stamper}, creating a required
     * resource dependency.
     *
     * @param key     Key of the resource to mark as required.
     * @param stamper {@link ResourceStamper Resource stamper} to use.
     * @return resource for given key.
     * @throws IOException              When stamping the resource fails unexpectedly.
     * @throws ResourceRuntimeException when given {@code key} cannot be resolved to a resource.
     */
    default ReadableResource require(ResourceKey key, ResourceStamper<ReadableResource> stamper) throws IOException {
        final ReadableResource resource = getReadableResource(key);
        require(resource, stamper);
        return resource;
    }

    /**
     * Marks hierarchical resource with given {@code path} as required (read), using given {@code stamper}, creating a
     * required resource dependency.
     *
     * @param path    Path of the hierarchical resource to mark as required.
     * @param stamper {@link ResourceStamper Resource stamper} to use.
     * @return hierarchical resource for given key.
     * @throws IOException              When stamping the resource fails unexpectedly.
     * @throws ResourceRuntimeException when given {@code path} cannot be resolved to a resource.
     */
    default HierarchicalResource require(ResourcePath path, ResourceStamper<HierarchicalResource> stamper) throws IOException {
        final HierarchicalResource resource = getHierarchicalResource(path);
        require(resource, stamper);
        return resource;
    }


    //
    // Recording required (read) dependencies to readable resources.
    //


    /**
     * Marks given {@code resource} as required (read), using the {@link #getDefaultRequireReadableResourceStamper
     * default require resource stamper for readable resources}, creating a required resource dependency.
     *
     * @param resource {@link ReadableResource Readable resource} to create a require dependency for.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default void require(ReadableResource resource) throws IOException {
        require(resource, getDefaultRequireReadableResourceStamper());
    }

    /**
     * Gets the default require resource stamper for {@link ReadableResource readable resources}.
     *
     * @return Default require resource stamper for {@link ReadableResource readable resource}.
     */
    ResourceStamper<ReadableResource> getDefaultRequireReadableResourceStamper();


    //
    // Recording provided (write) dependencies to writable resources.
    //


    /**
     * Marks given {@code resource} as provided (write), using the {@link #getDefaultProvideReadableResourceStamper
     * default provide resource stamper for readable resources}, creating a provided resource dependency.
     *
     * @param resource {@link ReadableResource Readable resource} to create a provide dependency for.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default void provide(ReadableResource resource) throws IOException {
        provide(resource, getDefaultProvideReadableResourceStamper());
    }

    /**
     * Gets the default provide resource stamper for {@link ReadableResource readable resource}.
     *
     * @return Default provide resource stamper for {@link ReadableResource readable resource}.
     */
    ResourceStamper<ReadableResource> getDefaultProvideReadableResourceStamper();


    //
    // Recording required (read) dependencies to files and directories of file systems.
    //


    /**
     * Marks resource for given {@code path} as required (read), using the {@link #getDefaultRequireHierarchicalResourceStamper
     * default require file system resource stamper}, creating a required resource dependency.
     *
     * @param path Path of the resource to require.
     * @return {@link FSResource File system resource} for given {@code path}.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default FSResource require(FSPath path) throws IOException {
        final FSResource resource = new FSResource(path);
        require(resource, getDefaultRequireHierarchicalResourceStamper());
        return resource;
    }

    /**
     * Marks resource for given {@code path} as required (read), using given {@code stamper}, creating a required
     * resource dependency.
     *
     * @param path    Path of the resource to require.
     * @param stamper {@link ResourceStamper Resource stamper} to use.
     * @return file system resource for given path.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default FSResource require(FSPath path, ResourceStamper<HierarchicalResource> stamper) throws IOException {
        final FSResource resource = new FSResource(path);
        require(resource, stamper);
        return resource;
    }

    /**
     * Marks given {@code resource} as required (read), using the {@link #getDefaultRequireHierarchicalResourceStamper
     * default require file system resource stamper}, creating a required resource dependency.
     *
     * @param resource Resource to require.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default void require(HierarchicalResource resource) throws IOException {
        require(resource, getDefaultRequireHierarchicalResourceStamper());
    }

    /**
     * Marks resource for given {@code path} as required (read), using the {@link #getDefaultRequireHierarchicalResourceStamper
     * default require file system resource stamper}, creating a required resource dependency.
     *
     * @param path Path of the resource to require.
     * @return {@link FSResource File system resource} for given {@code path}.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default FSResource require(Path path) throws IOException {
        final FSResource resource = new FSResource(path);
        require(resource, getDefaultRequireHierarchicalResourceStamper());
        return resource;
    }

    /**
     * Marks resource for given {@code path} as required (read), using given {@code stamper}, creating a required
     * resource dependency.
     *
     * @param path    Path of the resource to require.
     * @param stamper {@link ResourceStamper Resource stamper} to use.
     * @return file system resource for given path.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default FSResource require(Path path, ResourceStamper<HierarchicalResource> stamper) throws IOException {
        final FSResource resource = new FSResource(path);
        require(resource, stamper);
        return resource;
    }

    /**
     * Marks resource for given {@code file} as required (read), using the {@link #getDefaultRequireHierarchicalResourceStamper
     * default require file system resource stamper}, creating a required resource dependency.
     *
     * @param file File to require.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default FSResource require(File file) throws IOException {
        final FSResource resource = new FSResource(file);
        require(resource, getDefaultRequireHierarchicalResourceStamper());
        return resource;
    }

    /**
     * Marks resource for given {@code file} as required (read), using given {@code stamper}, creating a required
     * resource dependency.
     *
     * @param file    File to require.
     * @param stamper {@link ResourceStamper Resource stamper} to use.
     * @return file system resource for given path.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default FSResource require(File file, ResourceStamper<HierarchicalResource> stamper) throws IOException {
        final FSResource resource = new FSResource(file);
        require(resource, stamper);
        return resource;
    }

    /**
     * Gets the default require resource stamper for {@link FSResource file system resources}.
     *
     * @return Default require resource stamper for {@link FSResource file system resources}.
     */
    ResourceStamper<HierarchicalResource> getDefaultRequireHierarchicalResourceStamper();


    //
    // Recording provided (written to/created) dependencies to files and directories of file systems.
    //


    /**
     * Marks resource for given {@code path} as provided (written to/created)), using the {@link
     * #getDefaultProvideHierarchicalResourceStamper default provide file system resource stamper}, creating a provided
     * resource dependency.
     *
     * @param path Path of the resource to provide.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default void provide(FSPath path) throws IOException {
        provide(new FSResource(path), getDefaultProvideHierarchicalResourceStamper());
    }

    /**
     * Marks given {@code path} as provided (written to/created)), using given {@code stamper}, creating a provided
     * resource dependency.
     *
     * @param path    Path of the resource to provide.
     * @param stamper {@link ResourceStamper Resource stamper} to use.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default void provide(FSPath path, ResourceStamper<HierarchicalResource> stamper) throws IOException {
        provide(new FSResource(path), stamper);
    }

    /**
     * Marks given {@code resource} as provided (written to/created)), using the {@link
     * #getDefaultProvideHierarchicalResourceStamper default provide file system resource stamper}, creating a provided
     * resource dependency.
     *
     * @param resource Resource to provide.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default void provide(HierarchicalResource resource) throws IOException {
        provide(resource, getDefaultProvideHierarchicalResourceStamper());
    }

    /**
     * Marks resource for given {@code path} as provided (written to/created)), using the {@link
     * #getDefaultProvideHierarchicalResourceStamper default provide file system resource stamper}, creating a provided
     * resource dependency.
     *
     * @param path Path of the resource to provide.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default void provide(Path path) throws IOException {
        provide(new FSResource(path), getDefaultProvideHierarchicalResourceStamper());
    }

    /**
     * Marks resource for given {@code path} as provided (written to/created)), using given {@code stamper}, creating a
     * provided resource dependency.
     *
     * @param path    Path of the resource to provide.
     * @param stamper {@link ResourceStamper Resource stamper} to use.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default void provide(Path path, ResourceStamper<HierarchicalResource> stamper) throws IOException {
        provide(new FSResource(path), stamper);
    }

    /**
     * Marks resource for given {@code file} as provided (written to/created), using the {@link
     * #getDefaultProvideHierarchicalResourceStamper default provide file system resource stamper}, creating a provided
     * resource dependency.
     *
     * @param file File to provide.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default void provide(File file) throws IOException {
        provide(new FSResource(file), getDefaultProvideHierarchicalResourceStamper());
    }

    /**
     * Marks resource for given {@code file} as provided (written to/created), using given {@code stamper}, creating a
     * provided resource dependency.
     *
     * @param file    File to provide.
     * @param stamper {@link ResourceStamper Resource stamper} to use.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default void provide(File file, ResourceStamper<HierarchicalResource> stamper) throws IOException {
        provide(new FSResource(file), stamper);
    }

    /**
     * Gets the default provide resource stamper for {@link FSResource file system resources}.
     *
     * @return Default provide resource stamper for {@link FSResource file system resources}.
     */
    ResourceStamper<HierarchicalResource> getDefaultProvideHierarchicalResourceStamper();


    //
    // Other.
    //


    /**
     * Gets the logger.
     *
     * @return Logger.
     */
    Logger logger();
}
