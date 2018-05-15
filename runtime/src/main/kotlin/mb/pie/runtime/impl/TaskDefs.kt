package mb.pie.runtime.impl

import mb.pie.runtime.*


interface TaskDefs {
  fun <I : In, O : Out> getTaskDef(id: String): TaskDef<I, O>
  fun getUTaskDef(id: String): UTaskDef
  fun getGTaskDef(id: String): GTaskDef
}

class TaskDefsImpl(private val funcs: Map<String, UTaskDef>) : TaskDefs {
  override fun getUTaskDef(id: String): UTaskDef {
    return (funcs[id] ?: error("Function with identifier '$id' does not exist"))
  }

  override fun getGTaskDef(id: String): GTaskDef {
    @Suppress("UNCHECKED_CAST")
    return getUTaskDef(id) as GTaskDef
  }

  override fun <I : In, O : Out> getTaskDef(id: String): TaskDef<I, O> {
    @Suppress("UNCHECKED_CAST")
    return getUTaskDef(id) as TaskDef<I, O>
  }
}
