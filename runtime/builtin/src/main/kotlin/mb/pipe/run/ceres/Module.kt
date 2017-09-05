package mb.pipe.run.ceres

import com.google.inject.Binder
import mb.pie.runtime.core.PieModule
import mb.pie.runtime.core.bindBuilder
import mb.pie.runtime.core.builderMapBinder
import mb.pipe.run.ceres.path.*

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