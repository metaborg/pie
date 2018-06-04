package mb.pie.api.test

import mb.pie.api.*
import mb.pie.api.exec.*
import mb.pie.vfs.path.PPath
import mb.pie.vfs.path.PPathImpl
import java.nio.file.*

open class ApiTestCtx(
  private val pieImpl: Pie,
  private val fs: FileSystem
) : AutoCloseable {
  init {
    pieImpl.dropStore()
  }

  override fun close() {
    pie.close()
  }


  open val pie: Pie get() = pieImpl
  open val topDownExecutor: TopDownExecutor get() = pie.topDownExecutor
  open val bottomUpExecutor: BottomUpExecutor get() = pie.bottomUpExecutor
  open fun topDownSession(): TopDownSession = pie.topDownExecutor.newSession()


  fun file(path: String): PPath {
    return PPathImpl(fs.getPath(path))
  }

  fun <I : In, O : Out> taskDef(id: String, descFunc: (I, Int) -> String, execFunc: ExecContext.(I) -> O): TaskDef<I, O> {
    return LambdaTaskDef(id, execFunc, null, descFunc)
  }

  fun <I : In, O : Out> task(taskDef: TaskDef<I, O>, input: I): Task<I, O> {
    return Task(taskDef, input)
  }

  fun <I : In> stask(taskDef: TaskDef<I, *>, input: I): STask<I> {
    return STask(taskDef.id, input)
  }

  fun <I : In> stask(taskDefId: String, input: I): STask<I> {
    return STask(taskDefId, input)
  }


  fun read(path: PPath): String {
    Files.newInputStream(path.javaPath, StandardOpenOption.READ).use {
      return String(it.readBytes())
    }
  }

  fun write(text: String, path: PPath) {
    Files.newOutputStream(path.javaPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.SYNC).use {
      it.write(text.toByteArray())
    }
    // HACK: for some reason, sleeping is required for writes to the file to be picked up by reads...
    Thread.sleep(1)
  }
}
