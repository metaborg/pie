package mb.ceres

import com.google.inject.AbstractModule
import com.google.inject.Singleton
import mb.ceres.internal.BuildManagerImpl

open class Module : AbstractModule() {
  override fun configure() {
    bindBuildManager()
  }


  open protected fun bindBuildManager() {
    bind(BuildManagerImpl::class.java).`in`(Singleton::class.java)
    bind(BuildManager::class.java).to(BuildManagerImpl::class.java)
  }
}