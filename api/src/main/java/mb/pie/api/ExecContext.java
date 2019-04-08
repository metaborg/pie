package mb.pie.api;

import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceRuntimeException;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

public interface ExecContext {
    //
    // Executing and recording dependencies to tasks.
    //

    /**
     * Requires given [task], using the default [output stamper][OutputStamper], and returns its output.
     */
    <I extends Serializable, O extends @Nullable Serializable> O require(Task<I, O> task) throws ExecException, InterruptedException;

    /**
     * Requires given [task], using given [output stamper][stamper], and returns its output.
     */
    <I extends Serializable, O extends @Nullable Serializable> O require(Task<I, O> task, OutputStamper stamper) throws ExecException, InterruptedException;

    /**
     * Requires task given by its [task definition][taskDef] and [input], using the default [output stamper][OutputStamper], and returns its
     * output.
     */
    <I extends Serializable, O extends @Nullable Serializable> O require(TaskDef<I, O> taskDef, I input) throws ExecException, InterruptedException;

    /**
     * Requires task given by its [task definition][taskDef] and [input], using given [output stamper][stamper], and returns its output.
     */
    <I extends Serializable, O extends @Nullable Serializable> O require(TaskDef<I, O> taskDef, I input, OutputStamper stamper) throws ExecException, InterruptedException;

    /**
     * Requires task given by its [serializable task form][task], using the default [output stamper][OutputStamper], and returns its output.
     * Requires lookup and cast of a task definition, prefer [require] with [Task] or [TaskDef] if possible.
     */
    <I extends Serializable> @Nullable Serializable require(STask<I> task) throws ExecException, InterruptedException;

    /**
     * Requires task given by its [serializable task form][task], using given [output stamper][stamper], and returns its output.
     * Requires lookup and cast of a task definition, prefer [require] with [Task] or [TaskDef] if possible.
     */
    <I extends Serializable> @Nullable Serializable require(STask<I> task, OutputStamper stamper) throws ExecException, InterruptedException;

    /**
     * Requires task given by the [identifier of its task definition][taskDefId] and [input], using the default
     * [output stamper][OutputStamper], and returns its output. Requires lookup and cast of a task definition, prefer [require] with [Task] or
     * [TaskDef] if possible.
     */
    <I extends Serializable> @Nullable Serializable require(String taskDefId, I input) throws ExecException, InterruptedException;

    /**
     * Requires task given by the [identifier of its task definition][taskDefId] and [input], using given [output stamper][stamper],
     * and returns its output. Requires lookup and cast of a task definition, prefer [require] with [Task] or [TaskDef] if possible.
     */
    <I extends Serializable> @Nullable Serializable require(String taskDefId, I input, OutputStamper stamper) throws ExecException, InterruptedException;


    //
    // Recording dependencies to resources.
    //


    /**
     * Marks given {@code resource} as required (read), using given {@code stamper}, creating a required resource
     * dependency.
     */
    <R extends Resource> void require(R resource, ResourceStamper<R> stamper) throws IOException;

    /**
     * Marks resource with given {@code key} as required (read), using given {@code stamper}, creating a required
     * resource dependency.
     *
     * @return resource for given key.
     */
    default Resource require(ResourceKey key, ResourceStamper<Resource> stamper) throws IOException {
        final Resource resource = getResource(key);
        require(resource, stamper);
        return resource;
    }

    /**
     * Marks given {@code resource} as provided (written to/created), using given {@code stamper}, creating a provided
     * resource dependency. The current contents of the resource may be used for change detection, so be sure to call
     * this method *AFTER* modifying the resource.
     */
    <R extends Resource> void provide(R resource, ResourceStamper<R> stamper) throws IOException;


    //
    // Recording required (read) dependencies to files and directories of file systems.
    //

    /**
     * Marks given {@code path} as required (read), using the {@link #defaultRequireFileSystemStamper}, creating a
     * required resource dependency.
     *
     * @return file system resource for given path.
     */
    default FSResource require(FSPath path) throws IOException {
        final FSResource resource = new FSResource(path);
        require(resource, defaultRequireFileSystemStamper());
        return resource;
    }

    /**
     * Marks given {@code path} as required (read), using given {@code stamper}, creating a required resource
     * dependency.
     *
     * @return file system resource for given path.
     */
    default FSResource require(FSPath path, ResourceStamper<FSResource> stamper) throws IOException {
        final FSResource resource = new FSResource(path);
        require(resource, stamper);
        return resource;
    }

    /**
     * Marks given {@code resource} as required (read), using the {@link #defaultRequireFileSystemStamper}, creating a
     * required resource dependency.
     */
    default void require(FSResource resource) throws IOException {
        require(resource, defaultRequireFileSystemStamper());
    }

    /**
     * Marks given {@code path} as required (read), using the {@link #defaultRequireFileSystemStamper}, creating a
     * required resource dependency.
     *
     * @return file system resource for given path.
     */
    default FSResource require(Path path) throws IOException {
        final FSResource resource = new FSResource(path);
        require(resource, defaultRequireFileSystemStamper());
        return resource;
    }

    /**
     * Marks given {@code path} as required (read), using the give {@code stamper}, creating a required resource
     * dependency.
     *
     * @return file system resource for given path.
     */
    default FSResource require(Path path, ResourceStamper<FSResource> stamper) throws IOException {
        final FSResource resource = new FSResource(path);
        require(resource, stamper);
        return resource;
    }

    /**
     * Marks given {@code file} as required (read), using the {@link #defaultRequireFileSystemStamper}, creating a
     * required resource dependency.
     *
     * @return file system resource for given file object.
     */
    default FSResource require(File file) throws IOException {
        final FSResource resource = new FSResource(file);
        require(resource, defaultRequireFileSystemStamper());
        return resource;
    }

    /**
     * Marks given {@code file} as required (read), using the give {@code stamper}, creating a required resource
     * dependency.
     *
     * @return file system resource for given file object.
     */
    default FSResource require(File file, ResourceStamper<FSResource> stamper) throws IOException {
        final FSResource resource = new FSResource(file);
        require(resource, stamper);
        return resource;
    }

    /**
     * Default 'require' resource stamper for file system resources.
     */
    ResourceStamper<FSResource> defaultRequireFileSystemStamper();


    //
    // Recording provided (written to/created) dependencies to files and directories of file systems.
    //


    /**
     * Marks given {@code path} as provided (write), using the {@link #defaultProvideFileSystemStamper}, creating a
     * provided resource dependency. The current contents of the resource may be used for change detection, so be sure
     * to call this method *AFTER* modifying the resource.
     */
    default void provide(FSPath path) throws IOException {
        provide(new FSResource(path), defaultProvideFileSystemStamper());
    }

    /**
     * Marks given {@code path} as provided (write), using given {@code stamper}, creating a provided resource
     * dependency. The current contents of the resource may be used for change detection, so be sure to call this method
     * *AFTER* modifying the resource.
     */
    default void provide(FSPath path, ResourceStamper<FSResource> stamper) throws IOException {
        provide(new FSResource(path), stamper);
    }

    /**
     * Marks given {@code resource} as provided (write), using the {@link #defaultProvideFileSystemStamper}, creating a
     * provided resource dependency. The current contents of the resource may be used for change detection, so be sure
     * to call this method *AFTER* modifying the resource.
     */
    default void provide(FSResource resource) throws IOException {
        provide(resource, defaultProvideFileSystemStamper());
    }

    /**
     * Marks given {@code path} as provided (write), using the {@link #defaultProvideFileSystemStamper}, creating a
     * provided resource dependency. The current contents of the resource may be used for change detection, so be sure
     * to call this method *AFTER* modifying the resource.
     */
    default void provide(Path path) throws IOException {
        provide(new FSResource(path), defaultProvideFileSystemStamper());
    }

    /**
     * Marks given {@code path} as provided (write), using the give {@code stamper}, creating a provided resource
     * dependency. The current contents of the resource may be used for change detection, so be sure to call this method
     * *AFTER* modifying the resource.
     */
    default void provide(Path path, ResourceStamper<FSResource> stamper) throws IOException {
        provide(new FSResource(path), stamper);
    }

    /**
     * Marks given {@code file} as provided (write), using the {@link #defaultProvideFileSystemStamper}, creating a
     * provided resource dependency. The current contents of the resource may be used for change detection, so be sure
     * to call this method *AFTER* modifying the resource.
     */
    default void provide(File file) throws IOException {
        provide(new FSResource(file), defaultProvideFileSystemStamper());
    }

    /**
     * Marks given {@code file} as provided (write), using the give {@code stamper}, creating a provided resource
     * dependency. The current contents of the resource may be used for change detection, so be sure to call this method
     * *AFTER* modifying the resource.
     */
    default void provide(File file, ResourceStamper<FSResource> stamper) throws IOException {
        provide(new FSResource(file), stamper);
    }

    /**
     * Default 'provide' resource stamper for file system resources.
     */
    ResourceStamper<FSResource> defaultProvideFileSystemStamper();


    //
    // Resource key to resource.
    //


    /**
     * Gets resource for given key.
     *
     * @param key Key to get resource for.
     * @return Resource for {@code key}.
     * @throws ResourceRuntimeException when given {@code key} cannot be handled by the {@link ResourceRegistry}.
     */
    Resource getResource(ResourceKey key);


    //
    // Logging.
    //


    /**
     * Logger.
     */
    Logger logger();
}
