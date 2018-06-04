package mb.pie.lang.runtime

import com.google.inject.Binder
import com.google.inject.multibindings.MapBinder
import mb.pie.api.TaskDef
import mb.pie.lang.runtime.path.*
import mb.pie.taskdefs.guice.TaskDefsModule

class PieLangRuntimeModule : TaskDefsModule() {
  override fun Binder.bindTaskDefs(taskDefsBinder: MapBinder<String, TaskDef<*, *>>) {
    bindTaskDef<Exists>(taskDefsBinder, Exists.id)
    bindTaskDef<ListContents>(taskDefsBinder, ListContents.id)
    bindTaskDef<WalkContents>(taskDefsBinder, WalkContents.id)
    bindTaskDef<Read>(taskDefsBinder, Read.id)
    bindTaskDef<Copy>(taskDefsBinder, Copy.id)
  }
}
