package mb.pie.example.helloworld.java;

import mb.pie.api.*;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.logger.StreamLogger;
import mb.pie.runtime.taskdefs.MutableMapTaskDefs;
import mb.pie.store.lmdb.LMDBStoreKt;
import mb.pie.vfs.path.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * This example demonstrates how to write a PIE build script in Kotlin with the PIE API, and how to incrementally execute that build script
 * with the PIE runtime.
 * <p>
 * The goal of the build script is to write "Hello, world!" to a file.
 */
public class Main {
    /**
     * The {@link WriteHelloWorld} {@link TaskDef task definition} takes as input a {@link PPath path} to a file, and then writes
     * "Hello, world!" to it. This task does not return a value, so we use {@link None} as output type.
     */
    public static class WriteHelloWorld implements TaskDef<PPath, None> {
        /**
         * The {@link #getId} method must be overridden to provide a unique identifier for this task definition. In this case, we use
         * reflection to create a unique identifier.
         */
        @Override public @NotNull String getId() {
            return getClass().getSimpleName();
        }

        /**
         * The {@link #exec} method must be overridden to implement the logic of this task definition. This function is executed with an
         * {@link ExecContext execution context} object, which is used to tell PIE about dynamic task or file dependencies.
         */
        @Override public None exec(@NotNull ExecContext context, @NotNull PPath input) throws ExecException {
            // We write "Hello, world!" to the file.
            try(final OutputStream outputStream = input.outputStream()) {
                outputStream.write("Hello, world!".getBytes());
            } catch(IOException e) {
                throw new ExecException("Could not write to file " + input, e);
            }
            // Since we have written to or created a file, we need to tell PIE about this dynamic dependency, by calling `generate` on the context.
            context.generate(input);
            // Since this task does not generate a value, and we use the `None` type to indicate that, we need to return the singleton instance of `None`.
            return None.getInstance();
        }
    }

    public static void main(String[] args) throws Exception {
        if(args.length < 1) {
            System.out.println("Expected 1 argument, got none");
            System.exit(1);
        }
        final String fileStr = args[0];

        // To work with paths that PIE can understand (PPath type), we create a PathSrv, and do some error checking.
        final PathSrv pathSrv = new PathSrvImpl();
        final PPath file = pathSrv.resolveLocal(fileStr);
        if(file.exists() && file.isDir()) {
            System.out.println("File " + file + " is a directory");
            System.exit(2);
        }

        // Now we instantiate the task definitions.
        final WriteHelloWorld writeHelloWorld = new WriteHelloWorld();

        // Then, we add them to a TaskDefs object, which tells PIE about which task definitions are available.
        final MutableMapTaskDefs taskDefs = new MutableMapTaskDefs();
        taskDefs.add(writeHelloWorld.getId(), writeHelloWorld);

        // We need to create the PIE runtime, using a PieBuilderImpl.
        final PieBuilder pieBuilder = new PieBuilderImpl();
        // We pass in the TaskDefs object we created.
        pieBuilder.withTaskDefs(taskDefs);
        // For storing build results and the dependency graph, we will use the LMDB embedded database, stored at target/lmdb.
        LMDBStoreKt.withLMDBStore(pieBuilder, new File("target/lmdb"));
        // For example purposes, we use verbose logging which will output to stdout.
        pieBuilder.withLogger(StreamLogger.verbose());
        // Then we build the PIE runtime.
        final Pie pie = pieBuilder.build();

        // Now we create concrete task instances from the task definitions.
        final Task<PPath, None> writeHelloWorldTask = writeHelloWorld.createTask(file);

        // We incrementally execute the hello world task using the top-down executor.
        // The first incremental execution will execute the task, since it is new.  When no changes to the written-to file are made, the task is
        // not executed since nothing has changed. When the written-to file is changed or deleted, the task is executed to re-generate the file.
        pie.getTopDownExecutor().newSession().requireInitial(writeHelloWorldTask);

        // We print the text of the file to confirm that "Hello, world!" was indeed written to it.
        System.out.println("File contents: " + new String(file.readAllBytes()));

        // Finally, we clean up our resources. PIE must be closed to ensure the database has been fully serialized. PathSrv must be closed to
        // clean up temporary files.
        pie.close();
        pathSrv.close();
    }
}
