package mb.pie.example.helloworld.java;

import mb.pie.api.ExecContext;
import mb.pie.api.MixedSession;
import mb.pie.api.None;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.logger.StreamLogger;
import mb.pie.api.MapTaskDefs;
import mb.pie.store.lmdb.LMDBStore;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * This example demonstrates how to write a PIE build script in Kotlin with the PIE API, and how to incrementally execute that build script
 * with the PIE runtime.
 * <p>
 * The goal of the build script is to write "Hello, world!" to a file.
 */
public class Main {
    /**
     * The {@link WriteHelloWorld} {@link TaskDef task definition} takes as input a {@link File path} to a file, and then writes
     * "Hello, world!" to it. This task does not return a value, so we use {@link None} as output type.
     */
    public static class WriteHelloWorld implements TaskDef<File, None> {
        /**
         * The {@link TaskDef#getId} method must be overridden to provide a unique identifier for this task definition. In this case, we use
         * reflection to create a unique identifier.
         */
        @Override public String getId() {
            return getClass().getSimpleName();
        }

        /**
         * The {@link TaskDef#exec} method must be overridden to implement the logic of this task definition. This function is executed with an
         * {@link ExecContext execution context} object, which is used to tell PIE about dynamic task or file dependencies.
         */
        @Override public None exec(ExecContext context, File input) throws Exception {
            // We write "Hello, world!" to the file.
            try(final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(input))) {
                outputStream.write("Hello, world!".getBytes());
                outputStream.flush();
            }
            // Since we have written to or created a file, we need to tell PIE about this dynamic dependency, by calling `provide` on the context.
            context.provide(input);
            // Since this task does not generate a value, and we use the `None` type to indicate that, we need to return the singleton instance of `None`.
            return None.instance;
        }
    }

    public static void main(String[] args) throws Exception {
        // We expect one optional argument: the file to write hello world to.
        final File file;
        if(args.length > 0) {
            file = new File(args[0]);
        } else {
            file = new File("build/run/helloworld.txt");
        }

        // Now we instantiate the task definitions.
        final WriteHelloWorld writeHelloWorld = new WriteHelloWorld();

        // Then, we add them to a TaskDefs object, which tells PIE about which task definitions are available.
        final MapTaskDefs taskDefs = new MapTaskDefs();
        taskDefs.add(writeHelloWorld);

        // We need to create the PIE runtime, using a PieBuilderImpl.
        final PieBuilder pieBuilder = new PieBuilderImpl();
        // We pass in the TaskDefs object we created.
        pieBuilder.withTaskDefs(taskDefs);
        // For storing build results and the dependency graph, we will use the LMDB embedded database, stored at target/lmdb.
        LMDBStore.withLMDBStore(pieBuilder, new File("build/run/lmdb"));
        // For example purposes, we use verbose logging which will output to stdout.
        pieBuilder.withLogger(StreamLogger.verbose());
        // Then we build the PIE runtime.
        try(final Pie pie = pieBuilder.build()) {
            // Now we create concrete task instances from the task definitions.
            final Task<None> writeHelloWorldTask = writeHelloWorld.createTask(file);
            // We create a new session to perform an incremental build.
            try(final MixedSession session = pie.newSession()) {
                // We incrementally execute the hello world task by requiring it in a top-down fashion.
                // The first incremental execution will execute the task, since it is new.  When no changes to the written-to file are made, the task is
                // not executed since nothing has changed. When the written-to file is changed or deleted, the task is executed to re-generate the file.
                session.require(writeHelloWorldTask);

                // We print the text of the file to confirm that "Hello, world!" was indeed written to it.
                System.out.println("File contents: " + new String(Files.readAllBytes(file.toPath())));
            }
        }
        // Finally, we clean up our resources. PIE must be closed to ensure the database has been fully serialized.
        // Using a try-with-resources block is the best way to ensure that.
    }
}
