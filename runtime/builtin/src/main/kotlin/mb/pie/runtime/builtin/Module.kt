package mb.pie.runtime.builtin

import com.google.inject.Binder
import com.google.inject.Module
import mb.pie.runtime.builtin.path.*
import mb.pie.runtime.core.bindTaskDef
import mb.pie.runtime.core.taskDefsBinder


open class PieBuiltinModule : Module {
  override fun configure(binder: Binder) {
    val builders = binder.taskDefsBinder()

    binder.bindTaskDef<Exists>(builders, Exists.id)
    binder.bindTaskDef<ListContents>(builders, ListContents.id)
    binder.bindTaskDef<WalkContents>(builders, WalkContents.id)
    binder.bindTaskDef<Read>(builders, Read.id)
    binder.bindTaskDef<Copy>(builders, Copy.id)
  }
}
