package mb.pie.lang.runtime

import com.google.inject.Binder
import com.google.inject.multibindings.MapBinder
import mb.pie.api.UTaskDef
import mb.pie.lang.runtime.path.*
import mb.pie.taskdefs.guice.TaskDefsModule
import mb.pie.taskdefs.guice.bindTaskDef

class PieLangRuntimeModule : TaskDefsModule() {
  override fun Binder.bindTaskDefs(builders: MapBinder<String, UTaskDef>) {
    bindTaskDef<Exists>(builders, Exists.id)
    bindTaskDef<ListContents>(builders, ListContents.id)
    bindTaskDef<WalkContents>(builders, WalkContents.id)
    bindTaskDef<Read>(builders, Read.id)
    bindTaskDef<Copy>(builders, Copy.id)
  }
}
