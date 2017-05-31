package mb.ceres.internal

import mb.ceres.Builder
import mb.ceres.BuilderStore
import mb.ceres.In
import mb.ceres.Out
import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentHashMap

class BuilderStoreImpl : BuilderStore {
  private val builders = ConcurrentHashMap<String, Builder<*, *>>()

  override fun <I : In, O : Out> registerBuilder(builder: Builder<I, O>) {
    val id = builder.id
    if (builders.containsKey(id)) {
      throw IllegalStateException("Builder with id $id already exists")
    }
    builders.put(id, builder)
  }

  override fun <I : In, O : Out> unregisterBuilder(builder: Builder<I, O>) {
    val id = builder.id
    if (!builders.containsKey(id)) {
      throw IllegalStateException("Builder with id $id does not exist")
    }
    builders.remove(id)
  }

  override fun <I : In, O : Out> getBuilder(id: String): Builder<I, O> {
    if (!builders.containsKey(id)) {
      throw IllegalStateException("Builder with id $id does not exist")
    }
    @Suppress("UNCHECKED_CAST")
    return builders[id] as Builder<I, O>
  }
}