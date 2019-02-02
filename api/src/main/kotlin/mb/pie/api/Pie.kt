package mb.pie.api

import mb.pie.api.exec.BottomUpExecutor
import mb.pie.api.exec.TopDownExecutor
import mb.pie.api.fs.stamp.FileSystemStamper
import mb.pie.api.stamp.OutputStamper

/**
 * Facade for PIE.
 */
interface Pie : AutoCloseable {
  val topDownExecutor: TopDownExecutor
  val bottomUpExecutor: BottomUpExecutor

  fun dropStore()

  fun dropOutput(key: TaskKey)
  fun addOutput(key: TaskKey)

}

/**
 * Builder for [PIE][Pie] facade.
 */
interface PieBuilder {
  fun withTaskDefs(taskDefs: TaskDefs): PieBuilder
  fun withResourceSystems(resourceSystems: ResourceSystems): PieBuilder
  fun withStore(store: (Logger) -> Store): PieBuilder
  fun withShare(share: (Logger) -> Share): PieBuilder
  fun withDefaultOutputStamper(stamper: OutputStamper): PieBuilder
  fun withDefaultRequireFileSystemStamper(stamper: FileSystemStamper): PieBuilder
  fun withDefaultProvideFileSystemStamper(stamper: FileSystemStamper): PieBuilder
  fun withLayer(layer: (Logger) -> Layer): PieBuilder
  fun withLogger(logger: Logger): PieBuilder
  fun withExecutorLogger(executorLogger: (Logger) -> ExecutorLogger): PieBuilder
  fun build(): Pie
}
