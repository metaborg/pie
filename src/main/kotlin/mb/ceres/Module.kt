package mb.ceres

import com.google.inject.AbstractModule
import com.google.inject.Singleton
import mb.ceres.internal.BuildManagerImpl
import mb.ceres.internal.BuildStore
import mb.ceres.internal.InMemoryBuildStore

open class Module : AbstractModule() {
  override fun configure() {
    bindBuildManager()
    bindBuildStore()
  }


  open protected fun bindBuildManager() {
    bind(BuildManagerImpl::class.java).`in`(Singleton::class.java)
    bind(BuildManager::class.java).to(BuildManagerImpl::class.java)
  }

  open protected fun bindBuildStore() {
    bind(BuildStore::class.java).to(InMemoryBuildStore::class.java)
  }
}
