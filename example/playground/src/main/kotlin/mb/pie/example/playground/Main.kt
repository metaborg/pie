package mb.pie.example.playground

import mb.pie.api.*
import mb.pie.api.stamp.resource.ResourceStampers
import mb.pie.runtime.PieBuilderImpl
import mb.pie.runtime.logger.StreamLogger
import mb.pie.api.MapTaskDefs
import java.io.File
import java.io.Serializable

class CreateFile : TaskDef<File, None> {
    override fun getId() = "CreateFile"

    override fun exec(context: ExecContext, input: File): None {
        input.outputStream().buffered().use {
            it.write("Hello world".toByteArray())
        }
        context.provide(input)
        return None.instance
    }
}

class TransformFile : TaskDef<TransformFile.Input, File> {
    override fun getId() = "TransformFile"

    data class Input(
            val sourceFile: File,
            val sourceTask: STask,
            val destinationFile: File
    ) : Serializable

  override fun exec(context: ExecContext, input: Input): File {
    val (sourceFile, sourceTask, destination) = input
    context.require(sourceTask)
    context.require(sourceFile, ResourceStampers.hashFile())
    val sourceText = sourceFile.readText() + ", and universe!"
    destination.outputStream().buffered().use {
      it.write(sourceText.toByteArray())
      it.flush()
    }
    return destination
  }
}

/**
 * The main function will start up the PIE runtime and execute the build script.
 */
fun main(args: Array<String>) {
    val sourceFile = File(args.getOrElse(0) { "build/run/source.txt" })
    val destinationFile = File(args.getOrElse(1) { "build/run/destination.txt" })

    val createFile = CreateFile()
    val transformFile = TransformFile()
    val taskDefs = MapTaskDefs()
    taskDefs.add(createFile)
    taskDefs.add(transformFile)

  val pieBuilder = PieBuilderImpl()
  pieBuilder.withTaskDefs(taskDefs)
  //LMDBStore.withLMDBStore(pieBuilder, File("build/run/lmdb"))
  pieBuilder.withLogger(StreamLogger.verbose())
  pieBuilder.build().use { pie ->
    val fileCreatorTask = createFile.createTask(sourceFile)
    val transformFileTask = transformFile.createTask(
      TransformFile.Input(sourceFile, fileCreatorTask.toSerializableTask(), destinationFile))
    val output = pie.newSession().require(transformFileTask)
    println("Transformed '$sourceFile' ('${sourceFile.readText()}') to '$output' ('${output.readText()}')")
  }
}
