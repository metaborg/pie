package mb.pie.example.spreadsheet

import mb.fs.java.JavaFSNode
import mb.pie.api.StoreReadTxn
import mb.pie.api.TaskKey
import mb.pie.runtime.PieBuilderImpl
import mb.pie.runtime.logger.StreamLogger
import mb.pie.runtime.taskdefs.MutableMapTaskDefs
import javax.swing.SwingUtilities


fun main(args: Array<String>) {


  val workspace = JavaFSNode("./workspace")

  // Now we instantiate the task definitions.
  val cell = Cell()
  val sheet = Sheet()
  val multiSheet = MultiSheet()

  // Then, we add them to a TaskDefs object, which tells PIE about which task definitions are available.
  val taskDefs = MutableMapTaskDefs()
  taskDefs.add(cell.id, cell)
  taskDefs.add(sheet.id,sheet)
  taskDefs.add(multiSheet.id,multiSheet)



  // We need to create the PIE runtime, using a PieBuilderImpl.
  val pieBuilder = PieBuilderImpl()
  // We pass in the TaskDefs object we created.
  pieBuilder.withTaskDefs(taskDefs)
  // For storing build results and the dependency graph, we will use the LMDB embedded database, stored at target/lmdb.
  //pieBuilder.withLMDBStore(File("target/lmdb"))
  // For example purposes, we use verbose logging which will output to stdout.


  pieBuilder.withLogger(StreamLogger.verbose()  )
  // Then we build the PIE runtime.
  val pie = pieBuilder.build()

  // Now we create concrete task instances from the task definitions.


  val workspace_task = multiSheet.createTask(MultiSheet.Input(workspace.path) )

  //val fileCopierTask = fileCopier.createTask(FileCopier.Input(sourceFile, fileCreatorTask.toSTask(), destinationFile))

  // We (incrementally) execute the file copier task using the top-down executor.
  pie.topDownExecutor.newSession().requireInitial(workspace_task)

  SwingUtilities.invokeAndWait {
    val inspector = StoreInspector()
    SpreadSheet(pie,workspace_task.key(),inspector)
  }

  pie.close()
}



fun all_dependents(tx : StoreReadTxn, key : TaskKey) : Set<TaskKey> {
  var unchecked = tx.taskRequires(key).map { k -> k.callee }.toMutableList()
  var result = mutableSetOf<TaskKey>()

  while ( unchecked.size > 0 ) {
    val key = unchecked.removeAt(unchecked.size-1)
    result.add(key)
    unchecked.addAll( tx.taskRequires(key).map { k -> k.callee })
  }
  return result
}
