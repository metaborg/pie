package mb.pie.api.test

import mb.pie.api.*
import mb.resource.fs.FSResource
import java.io.Serializable
import java.nio.file.FileSystem

open class ApiTestCtx(
  private val pieImpl: Pie,
  private val javaFs: FileSystem
) : AutoCloseable {
  init {
    pieImpl.dropStore()
  }

  override fun close() {
    pie.close()
  }


  open val pie: Pie get() = pieImpl
  open fun newSession(): PieSession = pie.newSession()


  fun resource(path: String): FSResource {
    return FSResource(javaFs.getPath(path))
  }

  fun <I : Serializable, O : Serializable?> taskDef(id: String, descFunc: (I, Int) -> String, execFunc: ExecContext.(I) -> O): TaskDef<I, O> {
    return LambdaTaskDef(id, execFunc, null, descFunc)
  }

  fun <I : Serializable, O : Serializable?> task(taskDef: TaskDef<I, O>, input: I): Task<O> {
    return Task(taskDef, input)
  }

  fun stask(taskDef: TaskDef<*, *>, input: Serializable): STask {
    return STask(taskDef.id, input)
  }

  fun stask(taskDefId: String, input: Serializable): STask {
    return STask(taskDefId, input)
  }


  fun read(resource: FSResource): String {
    resource.newInputStream().use {
      return String(it.readBytes())
    }
  }

  fun write(text: String, resource: FSResource) {
    resource.newOutputStream().use {
      it.write(text.toByteArray())
    }
    // HACK: for some reason, sleeping is required for writes to the file to be picked up by reads...
    Thread.sleep(1)
  }
}
