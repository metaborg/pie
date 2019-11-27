package mb.pie.api.test

import mb.resource.fs.FSPath
import mb.resource.fs.FSResource
import java.nio.file.FileSystem

open class TestCtx(
  private val fileSystem: FileSystem
) {
  fun path(path: String): FSPath {
    return FSPath(fileSystem.getPath(path))
  }

  fun resource(path: String): FSResource {
    return FSResource(fileSystem.getPath(path))
  }

  fun read(resource: FSResource): String {
    resource.openRead().buffered().use {
      return String(it.readBytes())
    }
  }

  fun write(text: String, resource: FSResource) {
    Thread.sleep(1) // HACK: sleep before/after writing, to ensure that timestamp of file has changed. JIMFS, which we use in tests, apparently needs this.
    resource.openWrite().buffered().use {
      it.write(text.toByteArray())
      it.flush()
    }
    Thread.sleep(1)
  }
}
