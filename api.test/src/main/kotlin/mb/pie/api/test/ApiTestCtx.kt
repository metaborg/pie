package mb.pie.api.test

import mb.pie.api.*
import java.io.Serializable
import java.nio.file.FileSystem

open class ApiTestCtx(
  fileSystem: FileSystem,
  private val pieImpl: Pie
) : TestCtx(fileSystem), AutoCloseable {
  init {
    pieImpl.dropStore()
  }

  override fun close() {
    pie.close()
  }


  open val pie: Pie get() = pieImpl

  open fun newSession(): MixedSession = pie.newSession()


  fun <I : Serializable, O : Serializable?> taskDef(id: String, execFunc: ExecContext.(I) -> O): TaskDef<I, O> {
    return LambdaTaskDef(id, execFunc)
  }

  fun <I : Serializable, O : Serializable?> taskDef(id: String, keyFunc: (I) -> Serializable, execFunc: ExecContext.(I) -> O): TaskDef<I, O> {
    return LambdaTaskDef(id, execFunc, keyFunc)
  }

  fun <I : Serializable, O : Serializable?> taskDef(id: String, descFunc: (I, Int) -> String, execFunc: ExecContext.(I) -> O): TaskDef<I, O> {
    return LambdaTaskDef(id, execFunc, null, descFunc)
  }
}
