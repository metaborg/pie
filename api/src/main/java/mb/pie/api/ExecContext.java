package mb.pie.api;

import mb.fs.api.node.FSNode;
import mb.fs.api.path.FSPath;
import mb.fs.java.JavaFSNode;
import mb.fs.java.JavaFSPath;
import mb.pie.api.fs.FileSystemResource;
import mb.pie.api.fs.ResourceUtils;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
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

    <I extends Serializable> Serializable require(STask<I> task) throws ExecException, InterruptedException;

    /**
     * Requires task given by its [serializable task form][task], using given [output stamper][stamper], and returns its output.
     * Requires lookup and cast of a task definition, prefer [require] with [Task] or [TaskDef] if possible.
     */

    <I extends Serializable> Serializable require(STask<I> task, OutputStamper stamper) throws ExecException, InterruptedException;

    /**
     * Requires task given by the [identifier of its task definition][taskDefId] and [input], using the default
     * [output stamper][OutputStamper], and returns its output. Requires lookup and cast of a task definition, prefer [require] with [Task] or
     * [TaskDef] if possible.
     */

    <I extends Serializable> Serializable require(String taskDefId, I input) throws ExecException, InterruptedException;

    /**
     * Requires task given by the [identifier of its task definition][taskDefId] and [input], using given [output stamper][stamper],
     * and returns its output. Requires lookup and cast of a task definition, prefer [require] with [Task] or [TaskDef] if possible.
     */

    <I extends Serializable> Serializable require(String taskDefId, I input, OutputStamper stamper) throws ExecException, InterruptedException;


    //
    // Recording dependencies to resources.
    //


    /**
     * Marks given [resource] as required (read), using given [resource stamper][stamper], creating a required resource dependency.
     */
    <R extends Resource> void require(R resource, ResourceStamper<R> stamper);

    /**
     * Marks given [resource] as provided (written to/created), using given [resource stamper][stamper], creating a provided resource
     * dependency. The current contents of the resource may be used for change detection, so be sure to call [provide] AFTER modifying the
     * resource.
     */
    <R extends Resource> void provide(R resource, ResourceStamper<R> stamper);


    //
    // Recording required (read) dependencies to files and directories of file systems.
    //


    /**
     * Marks given [file system path][path] as required (read), using the
     * [default 'require' file system stamper][defaultRequireFileSystemStamper], creating a required resource dependency.
     *
     * @return resolved file system node.
     */
    FSNode require(FSPath path);

    /**
     * Marks given [file system path][path] as required (read), using given [file system stamper][stamper], creating a required resource
     * dependency.
     *
     * @return resolved file system node.
     */
    FSNode require(FSPath path, ResourceStamper<FileSystemResource> stamper);

    /**
     * Marks given [file system node][node] as required (read), using the
     * [default 'require' file system stamper][defaultRequireFileSystemStamper], creating a required resource dependency.
     */
    default void require(FSNode node) {
        require(ResourceUtils.toResource(node), defaultRequireFileSystemStamper());
    }

    /**
     * Marks given [file system node][node] as required (read), using given [file system stamper][stamper], creating a required resource
     * dependency.
     */
    default void require(FSNode node, ResourceStamper<FileSystemResource> stamper) {
        require(ResourceUtils.toResource(node), stamper);
    }

    /**
     * Marks given [Java file system path][path] as required (read), using the
     * [default 'require' file system stamper][defaultRequireFileSystemStamper], creating a required resource dependency.
     *
     * @return resolved Java file system node.
     */
    default JavaFSNode require(JavaFSPath path) {
        return require(path, defaultRequireFileSystemStamper());
    }

    /**
     * Marks given [Java file system path][path] as required (read), using given [file system stamper][stamper], creating a required
     * resource dependency.
     *
     * @return resolved Java file system node.
     */
    default JavaFSNode require(JavaFSPath path, ResourceStamper<FileSystemResource> stamper) {
        final JavaFSNode node = path.toNode();
        require(node, stamper);
        return node;
    }

    /**
     * Marks given [Java file system node][node] as required (read), using the
     * [default 'require' file system stamper][defaultRequireFileSystemStamper], creating a required resource dependency.
     */
    default void require(JavaFSNode node) {
        require(ResourceUtils.toResource(node), defaultRequireFileSystemStamper());
    }

    /**
     * Marks given [Java file system node][node] as required (read), using given [file system stamper][stamper], creating a required
     * resource dependency.
     */
    default void require(JavaFSNode node, ResourceStamper<FileSystemResource> stamper) {
        require(ResourceUtils.toResource(node), stamper);
    }

    /**
     * Marks given [Java file system (java.nio.file) path][path] as required (read), using the
     * [default 'require' file system stamper][defaultRequireFileSystemStamper], creating a required resource dependency.
     */
    default void require(Path path) {
        require(ResourceUtils.toResource(path), defaultRequireFileSystemStamper());
    }

    /**
     * Marks given [Java file system (java.nio.file) path][path] as required (read), using given [file system stamper][stamper], creating a
     * required resource dependency.
     */
    default void require(Path path, ResourceStamper<FileSystemResource> stamper) {
        require(ResourceUtils.toResource(path), stamper);
    }

    /**
     * Marks given [Java local file (java.io) path][file] as required (read), using the
     * [default 'require' file system stamper][defaultRequireFileSystemStamper], creating a required resource dependency.
     */
    default void require(File file) {
        require(ResourceUtils.toResource(file), defaultRequireFileSystemStamper());
    }

    /**
     * Marks given [Java local file (java.io) path][file] as required (read), using given [file system stamper][stamper], creating a required
     * resource dependency.
     */
    default void require(File file, ResourceStamper<FileSystemResource> stamper) {
        require(ResourceUtils.toResource(file), stamper);
    }

    /**
     * Default 'require' file system stamper.
     */
    ResourceStamper<FileSystemResource> defaultRequireFileSystemStamper();


    //
    // Recording provided (written to/created) dependencies to files and directories of file systems.
    //


    /**
     * Marks given [file system path][path] as provided (written to/created), using the
     * [default 'provide' file system stamper][defaultProvideFileSystemStamper], creating a provided resource dependency. The current contents
     * of the file or directory may be used for change detection, so be sure to call [provide] AFTER writing to the file or directory.
     */
    void provide(FSPath path);

    /**
     * Marks given [file system path][path] as provided (written to/created), using given [file system stamper][stamper], creating a provided
     * resource dependency. The current contents of the file or directory may be used for change detection, so be sure to call [provide] AFTER
     * writing to the file or directory.
     */
    void provide(FSPath path, ResourceStamper<FileSystemResource> stamper);

    /**
     * Marks given [file system node][node] as provided (written to/created), using the
     * [default 'provide' file system stamper][defaultProvideFileSystemStamper], creating a provided resource dependency. The current contents
     * of the file or directory may be used for change detection, so be sure to call [provide] AFTER writing to the file or directory.
     */
    default void provide(FSNode node) {
        provide(ResourceUtils.toResource(node), defaultProvideFileSystemStamper());
    }

    /**
     * Marks given [file system node][node] as provided (written to/created), using given [file system stamper][stamper], creating a provided
     * resource dependency. The current contents of the file or directory may be used for change detection, so be sure to call [provide] AFTER
     * writing to the file or directory.
     */
    default void provide(FSNode node, ResourceStamper<FileSystemResource> stamper) {
        provide(ResourceUtils.toResource(node), stamper);
    }

    /**
     * Marks given [Java file system path][path] as provided (written to/created), using the
     * [default 'provide' file system stamper][defaultProvideFileSystemStamper], creating a provided resource dependency. The current contents
     * of the file or directory may be used for change detection, so be sure to call [provide] AFTER writing to the file or directory.
     */
    default void provide(JavaFSPath path) {
        provide(ResourceUtils.toResource(path), defaultProvideFileSystemStamper());
    }

    /**
     * Marks given [Java file system path][path] as provided (written to/created), using given [file system stamper][stamper], creating a
     * provided resource dependency. The current contents of the file or directory may be used for change detection, so be sure to call
     * [provide] AFTER writing to the file or directory.
     */
    default void provide(JavaFSPath path, ResourceStamper<FileSystemResource> stamper) {
        provide(ResourceUtils.toResource(path), stamper);
    }

    /**
     * Marks given [Java file system node][node] as provided (written to/created), using the
     * [default 'provide' file system stamper][defaultProvideFileSystemStamper], creating a provided resource dependency. The current contents
     * of the file or directory may be used for change detection, so be sure to call [provide] AFTER writing to the file or directory.
     */
    default void provide(JavaFSNode node) {
        provide(ResourceUtils.toResource(node), defaultProvideFileSystemStamper());
    }

    /**
     * Marks given [Java file system node][node] as provided (written to/created), using given [file system stamper][stamper], creating a
     * provided resource dependency. The current contents of the file or directory may be used for change detection, so be sure to call
     * [provide] AFTER writing to the file or directory.
     */
    default void provide(JavaFSNode node, ResourceStamper<FileSystemResource> stamper) {
        provide(ResourceUtils.toResource(node), stamper);
    }

    /**
     * Marks given [Java file system (java.nio.file) path][path] as provided (written to/created), using the
     * [default 'provide' file system stamper][defaultProvideFileSystemStamper], creating a provided resource dependency. The current contents
     * of the file or directory may be used for change detection, so be sure to call [provide] AFTER writing to the file or directory.
     */
    default void provide(Path path) {
        provide(ResourceUtils.toResource(path), defaultProvideFileSystemStamper());
    }

    /**
     * Marks given [Java file system (java.nio.file) path][path] as provided (written to/created), using given
     * [file system stamper][stamper], creating a provided resource dependency. The current contents of the file or directory may be used for
     * change detection, so be sure to call [provide] AFTER writing to the file or directory.
     */
    default void provide(Path path, ResourceStamper<FileSystemResource> stamper) {
        provide(ResourceUtils.toResource(path), stamper);
    }

    /**
     * Marks given [Java local file (java.io) path][file] as provided (written to/created), using the
     * [default 'provide' file system stamper][defaultProvideFileSystemStamper], creating a provided resource dependency. The current contents
     * of the file or directory may be used for change detection, so be sure to call [provide] AFTER writing to the file or directory.
     */
    default void provide(File file) {
        provide(ResourceUtils.toResource(file), defaultProvideFileSystemStamper());
    }

    /**
     * Marks given [Java local file (java.io) path][file] as provided (written to/created), using given [file system stamper][stamper],
     * creating a provided resource dependency. The current contents of the file or directory may be used for change detection, so be sure to
     * call [provide] AFTER writing to the file or directory.
     */
    default void provide(File file, ResourceStamper<FileSystemResource> stamper) {
        provide(ResourceUtils.toResource(file), stamper);
    }

    /**
     * Default 'provide' file system stamper.
     */
    ResourceStamper<FileSystemResource> defaultProvideFileSystemStamper();


    //
    // Resolving file system paths to file system nodes.
    //


    /**
     * Resolves a file system path into a file system node, providing I/O. Does not create a dependency, use [require] or [provide] to record
     * a dependency.
     *
     * @return resolved file system node.
     */
    FSNode toNode(FSPath path);

    /**
     * Resolves a Java file system path into a Java file system node, providing I/O. Does not create a dependency, use [require] or [provide]
     * to record a dependency.
     *
     * @return resolved Java file system node.
     */
    default JavaFSNode toNode(JavaFSPath path) {
        return new JavaFSNode(path);
    }


    //
    // Logging.
    //


    /**
     * Logger.
     */
    Logger logger();
}
