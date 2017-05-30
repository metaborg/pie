package mb.ceres

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.google.inject.Provides
import java.nio.file.FileSystem

open internal class TestModule : Module() {
  @Provides protected fun fileSystem(): FileSystem {
    return Jimfs.newFileSystem(Configuration.unix())
  }
}