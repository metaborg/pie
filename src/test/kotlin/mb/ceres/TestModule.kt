package mb.ceres

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.google.inject.AbstractModule
import com.google.inject.Provides
import java.nio.file.FileSystem

internal class TestModule : AbstractModule() {
  override fun configure() {
    bind(BuildManagerImpl::class.java)
    bind(BuildManager::class.java).to(BuildManagerImpl::class.java)
  }

  @Provides fun fileSystem(): FileSystem {
    return Jimfs.newFileSystem(Configuration.unix())
  }
}