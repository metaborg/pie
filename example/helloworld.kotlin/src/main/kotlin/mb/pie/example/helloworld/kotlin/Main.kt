package mb.pie.example.helloworld.kotlin

import mb.pie.api.ExecContext
import mb.pie.api.None
import mb.pie.api.TaskDef
import mb.pie.runtime.PieBuilderImpl
import mb.pie.runtime.logger.StreamLogger
import mb.pie.runtime.taskdefs.MapTaskDefs
import mb.pie.store.lmdb.LMDBStore
import java.io.File

/**
 * This example demonstrates how to write a PIE build script in Kotlin with the PIE API, and how to incrementally execute that build script
 * with the PIE runtime.
 *
 * The goal of the build script is to write "Hello, world!" to a file.
 */

/**
 * The [WriteHelloWorld] [task definition][TaskDef] takes as input a [path][File] to a file, and then writes "Hello, world!" to it. This
 * task does not return a value, so we use [None] as output type.
 */
class WriteHelloWorld : TaskDef<File,None> {
  /**
   * The [id] property must be overridden to provide a unique identifier for this task definition. In this case, we use reflection to create
   * a unique identifier.
   */
  override fun getId(): String = javaClass.simpleName

  /**
   * The [exec] method must be overridden to implement the logic of this task definition. This function is executed with an
   * [execution context][ExecContext] object as receiver, which is used to tell PIE about dynamic task or file dependencies.
   */
  override fun exec(context: ExecContext,input: File): None {
    // We write "Hello, world!" to the file.
    input.outputStream().buffered().use {
      it.write("Hello, world!".toByteArray())
    }
    // Since we have written to or created a file, we need to tell PIE about this dynamic dependency, by calling `provide`, which is
    // defined in `ExecContext`.
    context.provide(input)
    // Since this task does not generate a value, and we use the `None` type to indicate that, we need to return the singleton instance of `None`.
    return None.instance
  }
}

/**
 * The main function will start up the PIE runtime and execute the build script.
 */
fun main(args: Array<String>) {
  // We expect one optional argument: the file to write hello world to.
  val file = File(args.getOrElse(0) { "build/run/helloworld.txt" })

  // Now we instantiate the task definitions.
  val writeHelloWorld = WriteHelloWorld()

  // Then, we add them to a TaskDefs object, which tells PIE about which task definitions are available.
  val taskDefs = MapTaskDefs()
  taskDefs.add(writeHelloWorld.id,writeHelloWorld)

  // We need to create the PIE runtime, using a PieBuilderImpl.
  val pieBuilder = PieBuilderImpl()
  // We pass in the TaskDefs object we created.
  pieBuilder.withTaskDefs(taskDefs)
  // For storing build results and the dependency graph, we will use the LMDB embedded database, stored at target/lmdb.
  LMDBStore.withLMDBStore(pieBuilder,File("build/run/lmdb"))
  // For example purposes, we use verbose logging which will output to stdout.
  pieBuilder.withLogger(StreamLogger.verbose())
  // Then we build the PIE runtime.
  pieBuilder.build().use { pie ->
    // Now we create concrete task instances from the task definitions.
    val writeHelloWorldTask = writeHelloWorld.createTask(file)

    // We incrementally execute the hello world task using the top-down executor.
    // The first incremental execution will execute the task, since it is new.  When no changes to the written-to file are made, the task is
    // not executed since nothing has changed. When the written-to file is changed or deleted, the task is executed to re-generate the file.
    pie.topDownExecutor.newSession().requireInitial(writeHelloWorldTask)

    // We print the text of the file to confirm that "Hello, world!" was indeed written to it.
    println("File contents: ${file.readText()}")
  }
  // Finally, we clean up our resources. PIE must be closed to ensure the database has been fully serialized. Using a
  // 'use' block is the best way to ensure that.
}
