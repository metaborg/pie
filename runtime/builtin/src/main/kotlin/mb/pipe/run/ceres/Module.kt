package mb.pipe.run.ceres

import com.google.inject.Binder
import mb.ceres.BuildReporter
import mb.ceres.CeresModule
import mb.ceres.asSingleton
import mb.ceres.bind
import mb.ceres.bindBuilder
import mb.ceres.builderMapBinder
import mb.ceres.impl.BuildCache
import mb.ceres.impl.MapBuildCache
import mb.ceres.to
import mb.pipe.run.ceres.path.Copy
import mb.pipe.run.ceres.path.Exists
import mb.pipe.run.ceres.path.ListContents
import mb.pipe.run.ceres.path.Read
import mb.pipe.run.ceres.path.WalkContents
import mb.pipe.run.ceres.util.LoggerBuildReporter

open class PipeCeresModule : CeresModule() {
  override fun configure(binder: Binder) {
    super.configure(binder)

    binder.bindCache()
    binder.bindCeres()
    binder.bindBuilders()
  }

  open protected fun Binder.bindCache() {
    bind<BuildCache>().to<MapBuildCache>()
  }

  override fun Binder.bindReporter() {
    bind<BuildReporter>().to<LoggerBuildReporter>()
  }

  open protected fun Binder.bindCeres() {
    bind<CeresSrv>().to<CeresSrvImpl>().asSingleton()
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