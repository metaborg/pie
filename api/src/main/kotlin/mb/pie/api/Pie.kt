package mb.pie.api

import mb.fs.api.GeneralFileSystem
import mb.pie.api.exec.BottomUpExecutor
import mb.pie.api.exec.TopDownExecutor
import mb.pie.api.stamp.OutputStamper
import mb.pie.api.stamp.ResourceStamper

/**
 * Facade for PIE.
 */
interface Pie : AutoCloseable {
  val topDownExecutor: TopDownExecutor
  val bottomUpExecutor: BottomUpExecutor

  fun dropStore()
}

/**
 * Builder for [PIE][Pie] facade.
 */
interface PieBuilder {
  fun withTaskDefs(taskDefs: TaskDefs): PieBuilder
  fun withGeneralFileSystem(generalFileSystem: GeneralFileSystem): PieBuilder
  fun withResourceSystems(resourceSystems: ResourceSystems): PieBuilder
  fun withStore(store: (Logger) -> Store): PieBuilder
  fun withShare(share: (Logger) -> Share): PieBuilder
  fun withDefaultOutputStamper(stamper: OutputStamper): PieBuilder
  fun withDefaultResourceRequireStamper(stamper: ResourceStamper): PieBuilder
  fun withDefaultResourceProvideStamper(stamper: ResourceStamper): PieBuilder
  fun withLayer(layer: (Logger) -> Layer): PieBuilder
  fun withLogger(logger: Logger): PieBuilder
  fun withExecutorLogger(executorLogger: (Logger) -> ExecutorLogger): PieBuilder
  fun build(): Pie
}
