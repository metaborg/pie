package mb.pie.api;

import mb.log.api.Logger;
import mb.pie.api.exec.CancelToken;
import mb.pie.api.exec.CanceledException;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.pie.api.stamp.output.FuncEqualsOutputStamper;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.api.stamp.resource.HashResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.resource.WritableResource;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
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
     * @return Up-to-date output object of the task. May be {@code null} when the task returns {@code null}.
     * @throws UncheckedExecException When an executing task throws an exception.
     * @throws CanceledException      When execution is cancelled.
     */
    <I extends Serializable, O extends Serializable> O require(TaskDef<I, O> taskDef, I input);

    /**
     * Requires task given by its {@code taskDef} and {@code input}, using given {@code stamper}, returning the
     * up-to-date output object of the task.
     *
     * @param <I>     Type of the input object.
     * @param <O>     Type of the output object.
     * @param taskDef Task definition of the task to require.
     * @param input   Input object of the task to require.
     * @param stamper {@link OutputStamper Output stamper} to use.
     * @return Up-to-date output object of the task. May be {@code null} when the task returns {@code null}.
     * @throws UncheckedExecException When an executing task throws an exception.
     * @throws CanceledException      When execution is cancelled.
     */
    <I extends Serializable, O extends Serializable> O require(TaskDef<I, O> taskDef, I input, OutputStamper stamper);

    /**
     * Requires task given by its {@code taskDef} and {@code input}, transforming the output with given {@code mapper}
     * and also using the same {@code mapper} with a {@link FuncEqualsOutputStamper function output stamper}, returning
     * the up-to-date transformed output object of the task.
     *
     * @param <I>     Type of the input object.
     * @param <O>     Type of the output object.
     * @param taskDef Task definition of the task to require.
     * @param input   Input object of the task to require.
     * @param mapping Mapping function to transform the task output and to use with the {@link FuncEqualsOutputStamper
     *                function output stamper}.
     * @return Up-to-date transformed output object of the task. May be {@code null} when the task returns {@code null}.
     * @throws UncheckedExecException When an executing task throws an exception.
     * @throws CanceledException      When execution is cancelled.
     */
    default <I extends Serializable, O extends Serializable, P extends Serializable> P requireMapping(TaskDef<I, O> taskDef, I input, SerializableFunction<O, P> mapping) {
        @SuppressWarnings("unchecked") final SerializableFunction<Serializable, Serializable> erased = (SerializableFunction<Serializable, Serializable>)mapping;
        return mapping.apply(require(taskDef, input, OutputStampers.funcEquals(erased)));
    }

    /**
     * Requires given {@code task}, using the {@link #getDefaultOutputStamper() default output stamper}, returning the
     * up-to-date output object of the task.
     *
     * @param <O>  Type of the output object.
     * @param task Task to require.
     * @return Up-to-date output object of {@code task}. May be {@code null} when the task returns {@code null}.
     * @throws UncheckedExecException When an executing task throws an exception.
     * @throws CanceledException      When execution is cancelled.
     */
    <O extends Serializable> O require(Task<O> task);

    /**
     * Requires given {@code task}, using given {@code stamper}, returning the up-to-date output object of the task.
     *
     * @param <O>     Type of the output object.
     * @param task    Task to require.
     * @param stamper {@link OutputStamper Output stamper} to use.
     * @return Up-to-date output object of {@code task}. May be {@code null} when the task returns {@code null}.
     * @throws UncheckedExecException When an executing task throws an exception.
     * @throws CanceledException      When execution is cancelled.
     */
    <O extends Serializable> O require(Task<O> task, OutputStamper stamper);

    /**
     * Requires given {@code task}, transforming the output with given {@code mapper} and also using the same {@code
     * mapper} with a {@link FuncEqualsOutputStamper function output stamper}, returning the up-to-date transformed
     * output object of the task.
     *
     * @param <O>     Type of the output object.
     * @param task    Task to require.
     * @param mapping Mapping function to transform the task output and to use with the {@link FuncEqualsOutputStamper
     *                function output stamper}.
     * @return Up-to-date transformed output object of {@code task}. May be {@code null} when the task returns {@code
     * null}.
     * @throws UncheckedExecException When an executing task throws an exception.
     * @throws CanceledException      When execution is cancelled.
     */
    default <O extends Serializable, P extends Serializable> P requireMapping(Task<O> task, SerializableFunction<O, P> mapping) {
        @SuppressWarnings("unchecked") final SerializableFunction<Serializable, Serializable> erased = (SerializableFunction<Serializable, Serializable>)mapping;
        return mapping.apply(require(task, OutputStampers.funcEquals(erased)));
    }

    /**
     * Requires task given by the {@link STaskDef serializable task definition} and {@code input} of the task, using the
     * {@link #getDefaultOutputStamper() default output stamper}, returning the up-to-date output object of the task.
     *
     * This method performs an unchecked cast from a task definition's actual input and output type, to {@link I} and
     * {@link O}. If these differ, execution may fail for example due to {@link ClassCastException} or {@link
     * NoSuchMethodError}. Additionally, your code may fail with the same exceptions if the returned object is not
     * actually of type {@link O}.
     *
     * Prefer {@link #require(Task)} or {@link #require(TaskDef, Serializable)} if possible, as this methods performs a
     * lookup which is less efficient.
     *
     * @param sTaskDef {@link STaskDef Serializable task definition} of the task to require.
     * @param input    Input object of the task to require.
     * @return Up-to-date output object of the task, assumed to be of type {@link O}. May be {@code null} when the task
     * returns {@code null}.
     * @throws UncheckedExecException When an executing task throws an exception.
     * @throws CanceledException      When execution is cancelled.
     */
    <I extends Serializable, O extends Serializable> O require(STaskDef<I, O> sTaskDef, I input);

    /**
     * Requires task given by the {@link STaskDef serializable task definition} and {@code input} of the task, using
     * given {@code stamper}, returning the up-to-date output object of the task.
     *
     * This method performs an unchecked cast from a task definition's actual input and output type, to {@link I} and
     * {@link O}. If these differ, execution may fail for example due to {@link ClassCastException} or {@link
     * NoSuchMethodError}. Additionally, your code may fail with the same exceptions if the returned object is not
     * actually of type {@link O}.
     *
     * Prefer {@link #require(Task, OutputStamper)} or {@link #require(TaskDef, Serializable, OutputStamper)} if
     * possible, as this methods performs a lookup which is less efficient.
     *
     * @param sTaskDef {@link STaskDef Serializable task definition} of the task to require.
     * @param input    Input object of the task to require.
     * @param stamper  {@link OutputStamper Output stamper} to use.
     * @return Up-to-date output object of the task, assumed to be of type {@link O}. May be {@code null} when the task
     * returns {@code null}.
     * @throws UncheckedExecException When an executing task throws an exception.
     * @throws CanceledException      When execution is cancelled.
     */
    <I extends Serializable, O extends Serializable> O require(STaskDef<I, O> sTaskDef, I input, OutputStamper stamper);

    /**
     * Requires task given by the {@link STaskDef serializable task definition} and {@code input} of the task,
     * transforming the output with given {@code mapper} and also using the same {@code mapper} with a {@link
     * FuncEqualsOutputStamper function output stamper}, returning the up-to-date transformed output object of the
     * task.
     *
     * This method performs an unchecked cast from a task definition's actual input and output type, to {@link I} and
     * {@link O}. If these differ, execution may fail for example due to {@link ClassCastException} or {@link
     * NoSuchMethodError}. Additionally, your code may fail with the same exceptions if the returned object is not
     * actually of type {@link O}.
     *
     * Prefer {@link #require(Task, OutputStamper)} or {@link #require(TaskDef, Serializable, OutputStamper)} if
     * possible, as this methods performs a lookup which is less efficient.
     *
     * @param sTaskDef {@link STaskDef Serializable task definition} of the task to require.
     * @param input    Input object of the task to require.
     * @param mapping  Mapping function to transform the task output and to use with the {@link FuncEqualsOutputStamper
     *                 function output stamper}.
     * @return Up-to-date transformed output object of the task, assumed to be of type {@link O}. May be {@code null}
     * when the task returns {@code null}.
     * @throws UncheckedExecException When an executing task throws an exception.
     * @throws CanceledException      When execution is cancelled.
     */
    default <I extends Serializable, O extends Serializable, P extends Serializable> P requireMapping(STaskDef<I, O> sTaskDef, I input, SerializableFunction<O, P> mapping) {
        @SuppressWarnings("unchecked") final SerializableFunction<Serializable, Serializable> erased = (SerializableFunction<Serializable, Serializable>)mapping;
        return mapping.apply(require(sTaskDef, input, OutputStampers.funcEquals(erased)));
    }

    /**
     * Requires task given by its {@link STask serializable task form}, using the {@link #getDefaultOutputStamper()
     * default output stamper}, returning the up-to-date output object of the task.
     *
     * This method performs an unchecked cast from a task's output type, to {@link O}. If these differ, execution may
     * fail for example due to {@link ClassCastException} or {@link NoSuchMethodError}. Additionally, your code may fail
     * with the same exceptions if the returned object is not actually of type {@link O}.
     *
     * Prefer {@link #require(Task)} or {@link #require(TaskDef, Serializable)} if possible, as this methods performs a
     * lookup which is less efficient.
     *
     * @param sTask {@link STask Serializable task form} of the task to require.
     * @return Up-to-date output object of the task, assumed to be of type {@link O}. May be {@code null} when the task
     * returns {@code null}.
     * @throws UncheckedExecException When an executing task throws an exception.
     * @throws CanceledException      When execution is cancelled.
     */
    <O extends Serializable> O require(STask<O> sTask);

    /**
     * Requires task given by its {@link STask serializable task form}, using given {@code stamper}, returning the
     * up-to-date output object of the task.
     *
     * This method performs an unchecked cast from a task's output type, to {@link O}. If these differ, execution may
     * fail for example due to {@link ClassCastException} or {@link NoSuchMethodError}. Additionally, your code may fail
     * with the same exceptions if the returned object is not actually of type {@link O}.
     *
     * Prefer {@link #require(Task, OutputStamper)} or {@link #require(TaskDef, Serializable, OutputStamper)} if
     * possible, as this methods performs a lookup and cast of the task definition, which is less efficient.
     *
     * @param sTask   {@link STask Serializable task form} of the task to require.
     * @param stamper {@link OutputStamper Output stamper} to use.
     * @return Up-to-date output object of the task, assumed to be of type {@link O}. May be {@code null} when the task
     * returns {@code null}.
     * @throws UncheckedExecException When an executing task throws an exception.
     * @throws CanceledException      When execution is cancelled.
     */
    <O extends Serializable> O require(STask<O> sTask, OutputStamper stamper);

    /**
     * Requires task given by its {@link STask serializable task form}, transforming the output with given {@code
     * mapper} and also using the same {@code mapper} with a {@link FuncEqualsOutputStamper function output stamper},
     * returning the up-to-date transformed output object of the task.
     *
     * This method performs an unchecked cast from a task's output type, to {@link O}. If these differ, execution may
     * fail for example due to {@link ClassCastException} or {@link NoSuchMethodError}. Additionally, your code may fail
     * with the same exceptions if the returned object is not actually of type {@link O}.
     *
     * Prefer {@link #require(Task, OutputStamper)} or {@link #require(TaskDef, Serializable, OutputStamper)} if
     * possible, as this methods performs a lookup and cast of the task definition, which is less efficient.
     *
     * @param sTask   {@link STask Serializable task form} of the task to require.
     * @param mapping Mapping function to transform the task output and to use with the {@link FuncEqualsOutputStamper
     *                function output stamper}.
     * @return Up-to-date transformed output object of the task, assumed to be of type {@link O}. May be {@code null}
     * when the task returns {@code null}.
     * @throws UncheckedExecException When an executing task throws an exception.
     * @throws CanceledException      When execution is cancelled.
     */
    default <O extends Serializable, P extends Serializable> P requireMapping(STask<O> sTask, SerializableFunction<O, P> mapping) {
        @SuppressWarnings("unchecked") final SerializableFunction<Serializable, Serializable> erased = (SerializableFunction<Serializable, Serializable>)mapping;
        return mapping.apply(require(sTask, OutputStampers.funcEquals(erased)));
    }

    /**
     * Returns output of given {@link Supplier incremental supplier}, which may in turn require the output of a task, or
     * require/provide and read/write resources, using this execution context.
     *
     * @param <O>      Type of the output object.
     * @param supplier {@link Supplier} to get output of.
     * @return Up-to-date output object of {@code supplier}. May be {@code null} when the supplier returns {@code null}.
     * @throws UncheckedExecException When the supplier requires a task that throws an exception.
     * @throws UncheckedIOException   When the supplier requires/provides and reads/writes a resource, but fails to do
     *                                so.
     * @throws CanceledException      When execution is cancelled.
     */
    <O extends Serializable> O require(Supplier<O> supplier);

    /**
     * Returns output of given {@link Function incremental function} applied to given {@code input}, which may in turn
     * require the output of a task, or require/provide and read/write resources, using this execution context.
     *
     * @param <I>      Type of the input object.
     * @param <O>      Type of the output object.
     * @param function {@link Function} to get output of.
     * @param input    Input to apply function to.
     * @return Up-to-date output object of {@code function}. May be {@code null} when the function returns {@code null}.
     * @throws UncheckedExecException When the function requires a task that throws an exception.
     * @throws UncheckedIOException   When the function requires/provides and reads/writes a resource, but fails to do
     *                                so.
     * @throws CanceledException      When execution is cancelled.
     */
    <I extends Serializable, O extends Serializable> O require(Function<I, O> function, I input);

    /**
     * Gets the default output stamper.
     *
     * @return Default output stamper.
     */
    OutputStamper getDefaultOutputStamper();


    //
    // Getting resources.
    //

    /**
     * Gets the resource service
     *
     * @return Resource service.
     */
    ResourceService getResourceService();

    /**
     * Gets resource for given key.
     *
     * @param key Key to get resource for.
     * @return Resource for {@code key}.
     * @throws ResourceRuntimeException when given {@code key} cannot be resolved to a resource.
     */
    default Resource getResource(ResourceKey key) {
        return getResourceService().getResource(key);
    }

    /**
     * Gets readable resource for given key.
     *
     * @param key Key to get readable resource for.
     * @return Readable resource for {@code key}.
     * @throws ResourceRuntimeException when given {@code key} cannot be resolved to a readable resource.
     */
    default ReadableResource getReadableResource(ResourceKey key) {
        return getResourceService().getReadableResource(key);
    }

    /**
     * Gets writable resource for given key.
     *
     * @param key Key to get writable resource for.
     * @return Writable resource for {@code key}.
     * @throws ResourceRuntimeException when given {@code key} cannot be resolved to a writable resource.
     */
    default WritableResource getWritableResource(ResourceKey key) {
        return getResourceService().getWritableResource(key);
    }

    /**
     * Gets a hierarchical resource for given path.
     *
     * @param path Path to get resource for.
     * @return Hierarchical resource for {@code path}.
     * @throws ResourceRuntimeException when given {@code path} cannot be resolved to a hierarchical resource.
     */
    default HierarchicalResource getHierarchicalResource(ResourcePath path) {
        return getResourceService().getHierarchicalResource(path);
    }


    //
    // Recording dependencies to resources.
    //

    /**
     * Marks given {@code resource} as required (read), using given {@code stamper}, creating a required resource
     * dependency.
     *
     * The current contents of the resource may be used for change detection, so be sure to call this method directly
     * *BEFORE* reading the resource.
     *
     * @param <R>      Type of the resource.
     * @param resource Resource to require.
     * @param stamper  {@link ResourceStamper Resource stamper} to use.
     * @return {@code true} if, compared to the previous execution of the task, the resource was changed (i.e., stamp
     * has changed) or the dependency is new. {@code false} otherwise: if the dependency is not new and the resource was
     * not changed.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    <R extends Resource> boolean require(R resource, ResourceStamper<R> stamper) throws IOException;

    /**
     * Marks given {@code resource} as provided (written to/created), using given {@code stamper}, creating a provided
     * resource dependency.
     *
     * The current contents of the resource may be used for change detection, so be sure to call this method *AFTER* all
     * modifications to the resource.
     *
     * @param <R>      Type of the resource.
     * @param resource Resource to provide.
     * @param stamper  {@link ResourceStamper Resource stamper} to use.
     * @return {@code true} if, compared to the previous execution of the task, the resource was changed (i.e., stamp
     * has changed) or the dependency is new. {@code false} otherwise: if the dependency is not new and the resource was
     * not changed.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    <R extends Resource> boolean provide(R resource, ResourceStamper<R> stamper) throws IOException;


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
    default boolean require(ReadableResource resource) throws IOException {
        return require(resource, getDefaultRequireReadableResourceStamper());
    }

    /**
     * Marks resource with given {@code key} as required (read), using the {@link #getDefaultRequireReadableResourceStamper
     * default require resource stamper for readable resources}, creating a required resource dependency.
     *
     * @param key Key of the resource to mark as required.
     * @return resource for given key.
     * @throws IOException              When stamping the resource fails unexpectedly.
     * @throws ResourceRuntimeException when given {@code key} cannot be resolved to a resource.
     */
    default ReadableResource require(ResourceKey key) throws IOException {
        return require(key, getDefaultRequireReadableResourceStamper());
    }

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
     * Marks hierarchical resource with given {@code path} as required (read), using the {@link
     * #getDefaultRequireHierarchicalResourceStamper default require resource stamper for hierarchical resources},
     * creating a required resource dependency.
     *
     * @param path Path of the hierarchical resource to mark as required.
     * @return hierarchical resource for given key.
     * @throws IOException              When stamping the resource fails unexpectedly.
     * @throws ResourceRuntimeException when given {@code path} cannot be resolved to a resource.
     */
    default HierarchicalResource require(ResourcePath path) throws IOException {
        return require(path, getDefaultRequireHierarchicalResourceStamper());
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
    default boolean provide(ReadableResource resource) throws IOException {
        return provide(resource, getDefaultProvideReadableResourceStamper());
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
    default boolean provide(FSPath path) throws IOException {
        return provide(new FSResource(path), getDefaultProvideHierarchicalResourceStamper());
    }

    /**
     * Marks given {@code path} as provided (written to/created)), using given {@code stamper}, creating a provided
     * resource dependency.
     *
     * @param path    Path of the resource to provide.
     * @param stamper {@link ResourceStamper Resource stamper} to use.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default boolean provide(FSPath path, ResourceStamper<HierarchicalResource> stamper) throws IOException {
        return provide(new FSResource(path), stamper);
    }

    /**
     * Marks given {@code resource} as provided (written to/created)), using the {@link
     * #getDefaultProvideHierarchicalResourceStamper default provide file system resource stamper}, creating a provided
     * resource dependency.
     *
     * @param resource Resource to provide.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default boolean provide(HierarchicalResource resource) throws IOException {
        return provide(resource, getDefaultProvideHierarchicalResourceStamper());
    }

    /**
     * Marks resource for given {@code path} as provided (written to/created)), using the {@link
     * #getDefaultProvideHierarchicalResourceStamper default provide file system resource stamper}, creating a provided
     * resource dependency.
     *
     * @param path Path of the resource to provide.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default boolean provide(Path path) throws IOException {
        return provide(new FSResource(path), getDefaultProvideHierarchicalResourceStamper());
    }

    /**
     * Marks resource for given {@code path} as provided (written to/created)), using given {@code stamper}, creating a
     * provided resource dependency.
     *
     * @param path    Path of the resource to provide.
     * @param stamper {@link ResourceStamper Resource stamper} to use.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default boolean provide(Path path, ResourceStamper<HierarchicalResource> stamper) throws IOException {
        return provide(new FSResource(path), stamper);
    }

    /**
     * Marks resource for given {@code file} as provided (written to/created), using the {@link
     * #getDefaultProvideHierarchicalResourceStamper default provide file system resource stamper}, creating a provided
     * resource dependency.
     *
     * @param file File to provide.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default boolean provide(File file) throws IOException {
        return provide(new FSResource(file), getDefaultProvideHierarchicalResourceStamper());
    }

    /**
     * Marks resource for given {@code file} as provided (written to/created), using given {@code stamper}, creating a
     * provided resource dependency.
     *
     * @param file    File to provide.
     * @param stamper {@link ResourceStamper Resource stamper} to use.
     * @throws IOException When stamping the resource fails unexpectedly.
     */
    default boolean provide(File file, ResourceStamper<HierarchicalResource> stamper) throws IOException {
        return provide(new FSResource(file), stamper);
    }

    /**
     * Gets the default provide resource stamper for {@link FSResource file system resources}.
     *
     * @return Default provide resource stamper for {@link FSResource file system resources}.
     */
    ResourceStamper<HierarchicalResource> getDefaultProvideHierarchicalResourceStamper();


    //
    // Internal object storage.
    //


    /**
     * Gets the stored internal object for the currently executing task, or {@code null} if no internal object was
     * stored or when {@code null} was explicitly stored as the internal object.
     *
     * @return Internal object for the currently executing task
     */
    @Nullable Serializable getInternalObject();

    /**
     * Sets the stored internal object for the currently executing task to {@code obj}.
     */
    void setInternalObject(@Nullable Serializable obj);

    /**
     * Clears the stored internal object for the currently executing task.
     */
    void clearInternalObject();


    //
    // Task data from previous execution.
    //


    /**
     * Returns the previous input of the currently executing task, or {@code null} if the task was not previously
     * executed.
     */
    @Nullable Serializable getPreviousInput();

    /**
     * Returns the previous output of the currently executing task, or {@code null} if the task was not previously
     * executed.
     */
    @Nullable Serializable getPreviousOutput();

    /**
     * Returns the previous observability of the currently executing task, or {@code null} if the task was not
     * previously executed.
     */
    @Nullable Observability getPreviousObservability();

    /**
     * Returns the previous task require dependencies of the currently executing task. The returned {@link Iterable}
     * will be empty if the task was not previously executed, or when it did not make any task require dependencies.
     */
    Iterable<TaskRequireDep> getPreviousTaskRequireDeps();

    /**
     * Returns the previous resource require dependencies of the currently executing task. The returned {@link Iterable}
     * will be empty if the task was not previously executed, or when it did not make any resource require
     * dependencies.
     */
    Iterable<ResourceRequireDep> getPreviousResourceRequireDeps();

    /**
     * Returns the previous resource provide dependencies of the currently executing task. The returned {@link Iterable}
     * will be empty if the task was not previously executed, or when it did not make any resource provide
     * dependencies.
     */
    Iterable<ResourceProvideDep> getPreviousResourceProvideDeps();


    //
    // Other.
    //

    /**
     * Gets the cancel token.
     *
     * @return Cancel token.
     */
    CancelToken cancelToken();

    /**
     * Gets the logger.
     *
     * @return Logger.
     * @deprecated Getting a logger will be removed in a future version. Please attain a logger instance externally.
     */
    @Deprecated
    Logger logger();
}
