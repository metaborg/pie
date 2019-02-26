package mb.pie.runtime.share

import mb.pie.api.*
import java.util.function.Supplier

public class NonSharingShare : Share {
  override fun share(key: TaskKey, execFunc: Supplier<TaskData<*, *>>, visitedFunc: Supplier<TaskData<*, *>>?): TaskData<*, *> {
    val taskData: TaskData<*, *>?;
    if(visitedFunc != null) {
      taskData = visitedFunc.get();
    } else {
      taskData = null;
    }

    if(taskData != null) {
      return taskData;
    } else {
      return execFunc.get();
    }
  }

  override fun toString() = "NonSharingShare"
}
