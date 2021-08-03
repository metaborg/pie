package mb.pie.example.helloworld.kotlin

import mb.log.stream.StreamLoggerFactory
import mb.pie.api.ExecContext
import mb.pie.api.MapTaskDefs
import mb.pie.api.None
import mb.pie.api.TaskDef
import mb.pie.runtime.PieBuilderImpl
import mb.pie.runtime.store.InMemoryStore
import mb.pie.runtime.store.SerializingStore
import mb.resource.ResourceKeyString
import mb.resource.fs.FSResource
import mb.resource.hierarchical.ResourcePath

/**
 * This example demonstrates how to write a PIE build script in Kotlin with the PIE API, and how to incrementally execute that build script
 * with the PIE runtime.
 *
 * The goal of the build script is to write "Hello, world!" to a file.
 */

/**
 * The [WriteHelloWorld] [task definition][TaskDef] takes as input a [path][ResourcePath] to a file, and then writes "Hello, world!" to it. This
 * task does not return a value, so we use [None] as output type.
 */
class WriteHelloWorld : TaskDef<ResourcePath, None> {
  /**
   * The [id] property must be overridden to provide a unique identifier for this task definition. In this case, we use reflection to create
   * a unique identifier.
   */
  override fun getId(): String = javaClass.simpleName

  /**
   * The [exec] method must be overridden to implement the logic of this task definition. This function is executed with an
   * [execution context][ExecContext] object as receiver, which is used to tell PIE about dynamic task or file dependencies.
   */
  override fun exec(context: ExecContext, input: ResourcePath): None {
    // We write "Hello, world!" to the file.
    val file = context.getWritableResource(input)
    file.openWrite().buffered().use {
      it.write("Hello, world!".toByteArray())
    }
    // Since we have written to or created a file, we need to tell PIE about this dynamic dependency, by calling `provide`, which is
    // defined in `ExecContext`.
    context.provide(file)
    // Since this task does not generate a value, and we use the `None` type to indicate that, we need to return the singleton instance of `None`.
    return None.instance
  }
}

/**
 * The main function will start up the PIE runtime and execute the build script.
 */
fun main(args: Array<String>) {
  // We expect one optional argument: the file to write hello world to.
  val file = FSResource(args.getOrElse(0) { "build/run/helloworld.txt" })
  file.createParents()

  // Now we instantiate the task definitions.
  val writeHelloWorld = WriteHelloWorld()

  // Then, we add them to a TaskDefs object, which tells PIE about which task definitions are available.
  val taskDefs = MapTaskDefs()
  taskDefs.add(writeHelloWorld)

  // We need to create the PIE runtime, using a PieBuilderImpl.
  val pieBuilder = PieBuilderImpl()
  // We pass in the TaskDefs object we created.
  pieBuilder.withTaskDefs(taskDefs)
  // For storing build results and the dependency graph, we will serialize the in-memory store on exit at build/store.
  pieBuilder.withStoreFactory { serde, resourceService, loggerFactory -> SerializingStore(serde, loggerFactory, resourceService.getHierarchicalResource(ResourceKeyString.of("build/store")).createParents(), { InMemoryStore() }, InMemoryStore::class.java) }
  // For example purposes, we use very verbose logging which will output to stdout.
  pieBuilder.withLoggerFactory(StreamLoggerFactory.stdOutVeryVerbose())
  // Then we build the PIE runtime.
  pieBuilder.build().use { pie ->
    // Now we create concrete task instances from the task definitions.
    val writeHelloWorldTask = writeHelloWorld.createTask(file.path)
    // We create a new session to perform an incremental build.
    pie.newSession().use { session ->
      // We incrementally execute the hello world task by requiring it in a top-down fashion.
      // The first incremental execution will execute the task, since it is new.  When no changes to the written-to file are made, the task is
      // not executed since nothing has changed. When the written-to file is changed or deleted, the task is executed to re-generate the file.
      session.require(writeHelloWorldTask)

      // We print the text of the file to confirm that "Hello, world!" was indeed written to it.
      println("File contents: ${file.readString()}")
    }
  }
  // Finally, we clean up our resources. PIE must be closed to ensure the database has been fully serialized. Using a
  // 'use' block is the best way to ensure that.
}
