package mb.pie.runtime.util

import mb.pie.api.*
import mb.pie.api.exec.TopDownExecutor
import mb.pie.api.stamp.FileStampers
import mb.pie.runtime.PieImpl
import mb.pie.runtime.exec.*
import mb.pie.runtime.taskdefs.MutableMapTaskDefs
import mb.pie.vfs.path.PPath
import mb.pie.vfs.path.PPathImpl
import java.nio.file.*

open class TestCtx(
  private val pie: PieImpl,
  private val taskDefs: MutableMapTaskDefs,
  private val fs: FileSystem
) : AutoCloseable {
  init {
    pie.dropCache()
    pie.dropStore()
  }

  override fun close() {
    pie.close()
  }


  fun topDownExecutor(): TopDownExecutor {
    return pie.topDownExecutor
  }

  fun topDownExec(): TopDownSessionImpl {
    return pie.topDownExecutor.newSession() as TopDownSessionImpl
  }


  fun bottomUpExec(observers: Map<UTask, TaskObserver> = mapOf()): BottomUpSession {
    val bottomUpExecutor = pie.bottomUpExecutor as BottomUpExecutorImpl
    for(pair in observers) {
      bottomUpExecutor.setObserver(pair.key, pair.key, pair.value)
    }
    return bottomUpExecutor.newSession()
  }


  fun addTaskDef(taskDef: UTaskDef) {
    taskDefs.addTaskDef(taskDef.id, taskDef)
  }


  val toLowerCase = func<String, String>("toLowerCase", { "toLowerCase($it)" }) {
    it.toLowerCase()
  }
  val readPath = func<PPath, String>("read", { "read($it)" }) {
    require(it, FileStampers.modified)
    read(it)
  }
  val writePath = func<Pair<String, PPath>, None>("write", { "write$it" }) { (text, path) ->
    write(text, path)
    generate(path)
    None.instance
  }


  fun path(path: String): PPath {
    return PPathImpl(fs.getPath(path))
  }

  fun <I : In, O : Out> func(id: String, descFunc: (I) -> String, execFunc: ExecContext.(I) -> O): TaskDef<I, O> {
    return LambdaTaskDef(id, execFunc, descFunc)
  }

  fun <I : In, O : Out> app(taskDef: TaskDef<I, O>, input: I): Task<I, O> {
    return Task(taskDef, input)
  }

  fun <I : In, O : Out> app(builderId: String, input: I): Task<I, O> {
    return Task<I, O>(builderId, input)
  }


  fun read(path: PPath): String {
    Files.newInputStream(path.javaPath, StandardOpenOption.READ).use {
      return String(it.readBytes())
    }
  }

  fun write(text: String, path: PPath) {
    pie.logger.trace("Write $text")
    Files.newOutputStream(path.javaPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.SYNC).use {
      it.write(text.toByteArray())
    }
    // HACK: for some reason, sleeping is required for writes to the file to be picked up by reads...
    Thread.sleep(1)
  }


  inline fun <reified I : In, reified O : Out> requireOutputFunc(): TaskDef<Task<I, O>, O> {
    return func<Task<I, O>, O>("requireOutput(${I::class}):${O::class}", { "requireOutput($it)" }) {
      requireOutput(it)
    }
  }
}
