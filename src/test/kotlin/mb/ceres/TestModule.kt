package mb.ceres

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.google.inject.Provides
import mb.ceres.internal.BuildManagerImpl
import java.nio.file.FileSystem

internal class TestModule : Module() {
  override fun bindBuildManager() {
    bind(BuildManagerImpl::class.java)
    bind(BuildManager::class.java).to(BuildManagerImpl::class.java)
  }

  @Provides fun fileSystem(): FileSystem {
    return Jimfs.newFileSystem(Configuration.unix())
  }
}