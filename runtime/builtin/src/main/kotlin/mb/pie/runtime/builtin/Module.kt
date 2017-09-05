package mb.pie.runtime.builtin

import com.google.inject.Binder
import com.google.inject.Module
import mb.pie.runtime.builtin.path.Copy
import mb.pie.runtime.builtin.path.Exists
import mb.pie.runtime.builtin.path.ListContents
import mb.pie.runtime.builtin.path.Read
import mb.pie.runtime.builtin.path.WalkContents
import mb.pie.runtime.core.bindBuilder
import mb.pie.runtime.core.builderMapBinder

open class PieBuiltinModule : Module {
  override fun configure(binder: Binder) {
    val builders = binder.builderMapBinder()

    binder.bindBuilder<Exists>(builders, Exists.id)
    binder.bindBuilder<ListContents>(builders, ListContents.id)
    binder.bindBuilder<WalkContents>(builders, WalkContents.id)
    binder.bindBuilder<Read>(builders, Read.id)
    binder.bindBuilder<Copy>(builders, Copy.id)
  }
}