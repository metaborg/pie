package mb.pie.runtime.builtin

import com.google.inject.Binder
import mb.pie.runtime.builtin.path.*
import mb.pie.runtime.core.PieModule
import mb.pie.runtime.core.bindBuilder
import mb.pie.runtime.core.builderMapBinder

open class PieBuiltinModule : PieModule() {
  override fun configure(binder: Binder) {
    super.configure(binder)

    binder.bindBuilders()
  }

  open protected fun Binder.bindBuilders() {
    val builders = builderMapBinder()

    bindBuilder<Exists>(builders, Exists.id)
    bindBuilder<ListContents>(builders, ListContents.id)
    bindBuilder<WalkContents>(builders, WalkContents.id)
    bindBuilder<Read>(builders, Read.id)
    bindBuilder<Copy>(builders, Copy.id)
  }
}