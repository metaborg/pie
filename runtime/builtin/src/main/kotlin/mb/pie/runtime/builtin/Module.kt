package mb.pie.runtime.builtin

import com.google.inject.Binder
import com.google.inject.Module
import mb.pie.runtime.builtin.path.Copy
import mb.pie.runtime.builtin.path.Exists
import mb.pie.runtime.builtin.path.ListContents
import mb.pie.runtime.builtin.path.Read
import mb.pie.runtime.builtin.path.WalkContents
import mb.pie.runtime.core.bindFunc
import mb.pie.runtime.core.funcsMapBinder

open class PieBuiltinModule : Module {
  override fun configure(binder: Binder) {
    val builders = binder.funcsMapBinder()

    binder.bindFunc<Exists>(builders, Exists.id)
    binder.bindFunc<ListContents>(builders, ListContents.id)
    binder.bindFunc<WalkContents>(builders, WalkContents.id)
    binder.bindFunc<Read>(builders, Read.id)
    binder.bindFunc<Copy>(builders, Copy.id)
  }
}