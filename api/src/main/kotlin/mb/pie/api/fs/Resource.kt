package mb.pie.api.fs

import mb.fs.api.GeneralFileSystem
import mb.fs.api.node.FSNode
import mb.fs.api.path.FSPath
import mb.fs.java.*
import mb.pie.api.*

import java.io.File
import java.nio.file.Path

/**
 * Interface for file system resources.
 */
interface FileSystemResource : Resource {
  val node: FSNode
}

/**
 * Resource for file system nodes.
 */
class FileSystemNodeResource(val id: String, override val node: FSNode) : FileSystemResource {
  override fun key() = ResourceKey(id, node.path)
}

/**
 * Resource system for the general file system.
 */
class GeneralFileSystemResourceSystem(private val fileSystem: GeneralFileSystem) : ResourceSystem {
  companion object {
    const val id = "gfs"
  }

  override val id: String = Companion.id

  override fun getResource(key: ResourceKey): FileSystemNodeResource {
    if(key.id != id) {
      error("Attempting to get resource for key $key, which is not a general resource key")
    }
    @Suppress("UNCHECKED_CAST")
    val path = key.key as FSPath
    val node = fileSystem.getNode(path)
    return FileSystemNodeResource(id, node)
  }
}

fun FSPath.toResourceKey() = ResourceKey(GeneralFileSystemResourceSystem.id, this)
fun FSNode.toResourceKey() = ResourceKey(GeneralFileSystemResourceSystem.id, this.path)

fun FSNode.toResource() = FileSystemNodeResource(GeneralFileSystemResourceSystem.id, this)


/**
 * Resource for Java file system (java.nio.file) nodes.
 */
class JavaFileSystemNodeResource(override val node: JavaFSNode) : FileSystemResource {
  override fun key() = ResourceKey(JavaFileSystem.rootSelector, node.javaPath)
}

/**
 * Resource system for the Java file system (java.nio.file).
 */
class JavaFileSystemResourceSystem : ResourceSystem {
  companion object {
    const val id = JavaFileSystem.rootSelector
  }

  override val id: String = Companion.id

  override fun getResource(key: ResourceKey): JavaFileSystemNodeResource {
    if(key.id != id) {
      error("Attempting to get local resource for key $key, which is not a local resource key")
    }
    @Suppress("UNCHECKED_CAST")
    val path = key.key as JavaFSPath
    val node = JavaFSNode(path)
    return JavaFileSystemNodeResource(node)
  }
}

fun JavaFSPath.toResourceKey() = ResourceKey(JavaFileSystem.rootSelector, this)
fun JavaFSNode.toResourceKey() = ResourceKey(JavaFileSystem.rootSelector, this.javaPath)
fun Path.toResourceKey() = ResourceKey(JavaFileSystem.rootSelector, JavaFSPath(this))
fun File.toResourceKey() = ResourceKey(JavaFileSystem.rootSelector, JavaFSPath(this))

fun JavaFSNode.toResource() = JavaFileSystemNodeResource(this)
fun JavaFSPath.toNode() = JavaFSNode(this)
fun JavaFSPath.toResource() = JavaFileSystemNodeResource(this.toNode())
fun Path.toNode() = JavaFSNode(this)
fun Path.toResource() = JavaFileSystemNodeResource(this.toNode())
fun File.toNode() = JavaFSNode(this)
fun File.toResource() = JavaFileSystemNodeResource(this.toNode())
