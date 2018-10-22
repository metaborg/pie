package mb.pie.example.helloworld.kotlin

import mb.pie.api.*
import mb.pie.runtime.PieBuilderImpl
import mb.pie.runtime.logger.StreamLogger
import mb.pie.runtime.taskdefs.MutableMapTaskDefs
import mb.pie.store.lmdb.withLMDBStore
import mb.pie.vfs.path.PPath
import mb.pie.vfs.path.PathSrvImpl
import java.io.File
import kotlin.system.exitProcess

/**
 * This example demonstrates how to write a PIE build script in Kotlin with the PIE API, and how to incrementally execute that build script
 * with the PIE runtime.
 *
 * The goal of the build script is to write "Hello, world!" to a file.
 */

/**
 * The [WriteHelloWorld] [task definition][TaskDef] takes as input a [path][PPath] to a file, and then writes "Hello, world!" to it. This
 * task does not return a value, so we use [None] as output type.
 */
class WriteHelloWorld : TaskDef<PPath, None> {
  /**
   * The [id] property must be overridden to provide a unique identifier for this task definition. In this case, we use reflection to create
   * a unique identifier.
   */
  override val id: String = javaClass.simpleName

  /**
   * The [exec] method must be overridden to implement the logic of this task definition. This function is executed with an
   * [execution context][ExecContext] object as receiver, which is used to tell PIE about dynamic task or file dependencies.
   */
  override fun ExecContext.exec(input: PPath): None {
    // We write "Hello, world!" to the file.
    input.outputStream().buffered().use {
      it.write("Hello, world!".toByteArray())
    }
    // Since we have written to or created a file, we need to tell PIE about this dynamic dependency, by calling `generate`, which is
    // defined in `ExecContext`.
    generate(input)
    // Since this task does not generate a value, and we use the `None` type to indicate that, we need to return the singleton instance of `None`.
    return None.instance
  }
}

/**
 * The main function will start up the PIE runtime and execute the build script.
 */
fun main(args: Array<String>) {
  // We expect one argument: the file to write "Hello, world!" to.
  if(args.isEmpty()) {
    println("Expected 1 argument, got none")
    exitProcess(1)
  }
  val fileStr = args[0]

  // To work with paths that PIE can understand (PPath type), we create a PathSrv, and do some error checking.
  val pathSrv = PathSrvImpl()
  val file = pathSrv.resolveLocal(fileStr)
  if(file.exists() && file.isDir) {
    println("File $file is a directory")
    exitProcess(2)
  }

  // Now we instantiate the task definitions.
  val writeHelloWorld = WriteHelloWorld()

  // Then, we add them to a TaskDefs object, which tells PIE about which task definitions are available.
  val taskDefs = MutableMapTaskDefs()
  taskDefs.add(writeHelloWorld.id, writeHelloWorld)

  // We need to create the PIE runtime, using a PieBuilderImpl.
  val pieBuilder = PieBuilderImpl()
  // We pass in the TaskDefs object we created.
  pieBuilder.withTaskDefs(taskDefs)
  // For storing build results and the dependency graph, we will use the LMDB embedded database, stored at target/lmdb.
  pieBuilder.withLMDBStore(File("target/lmdb"))
  // For example purposes, we use verbose logging which will output to stdout.
  pieBuilder.withLogger(StreamLogger.verbose())
  // Then we build the PIE runtime.
  val pie = pieBuilder.build()

  // Now we create concrete task instances from the task definitions.
  val writeHelloWorldTask = writeHelloWorld.createTask(file)

  // We incrementally execute the hello world task using the top-down executor.
  // The first incremental execution will execute the task, since it is new.  When no changes to the written-to file are made, the task is
  // not executed since nothing has changed. When the written-to file is changed or deleted, the task is executed to re-generate the file.
  pie.topDownExecutor.newSession().requireInitial(writeHelloWorldTask)

  // We print the text of the file to confirm that "Hello, world!" was indeed written to it.
  println("File contents: ${String(file.readAllBytes())}")

  // Finally, we clean up our resources. PIE must be closed to ensure the database has been fully serialized. PathSrv must be closed to
  // clean up temporary files.
  pie.close()
  pathSrv.close()
}