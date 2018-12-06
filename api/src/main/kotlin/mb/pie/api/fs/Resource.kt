package mb.pie.api.fs

import mb.fs.api.FileSystem
import mb.fs.api.node.FSNode
import mb.fs.api.path.FSPath
import mb.fs.java.*
import mb.pie.api.*
import java.io.File
import java.nio.file.Path

/**
 * Resource for file system nodes.
 */
class FileSystemResource(val node: FSNode) : Resource {
  override fun key() = ResourceKey(node.fileSystem.id, node.path)
}

/**
 * Resource system for the general file system.
 */
class FileSystemResourceSystem(val fileSystem: FileSystem) : ResourceSystem {
  override val id: String = fileSystem.id

  override fun getResource(key: ResourceKey): FileSystemResource {
    if(key.id != id) {
      error("Attempting to get resource for key $key, which is not a file system resource key")
    }
    @Suppress("UNCHECKED_CAST")
    val path = key.key as FSPath
    val node = fileSystem.getNode(path)
    return FileSystemResource(node)
  }
}

fun FSPath.toResourceKey() = ResourceKey(this.fileSystemId, this)
fun FSNode.toResourceKey() = ResourceKey(this.fileSystem.id, this.path)
fun JavaFSPath.toResourceKey() = ResourceKey(JavaFileSystem.id, this)
fun JavaFSNode.toResourceKey() = ResourceKey(JavaFileSystem.id, this.path)
fun Path.toResourceKey() = ResourceKey(JavaFileSystem.id, JavaFSPath(this))
fun File.toResourceKey() = ResourceKey(JavaFileSystem.id, JavaFSPath(this))

fun FSPath.toResource(resourceSystems: ResourceSystems) = FileSystemResource(this.toNode(resourceSystems))
fun FSPath.toResource(resourceSystem: ResourceSystem) = FileSystemResource(this.toNode(resourceSystem))
fun FSPath.toResource(resourceSystem: FileSystemResourceSystem) = FileSystemResource(this.toNode(resourceSystem))
fun FSPath.toResource(fileSystem: FileSystem) = FileSystemResource(this.toNode(fileSystem))
fun FSNode.toResource() = FileSystemResource(this)
fun JavaFSNode.toResource() = FileSystemResource(this)
fun JavaFSPath.toResource() = FileSystemResource(this.toNode())
fun Path.toResource() = FileSystemResource(this.toNode())
fun File.toResource() = FileSystemResource(this.toNode())

fun FSPath.toNode(resourceSystems: ResourceSystems): FSNode = this.toNode((resourceSystems.getResourceSystem(this.fileSystemId) as FileSystemResourceSystem).fileSystem)
fun FSPath.toNode(resourceSystem: ResourceSystem): FSNode = this.toNode((resourceSystem as FileSystemResourceSystem).fileSystem)
fun FSPath.toNode(resourceSystem: FileSystemResourceSystem): FSNode = this.toNode(resourceSystem.fileSystem)
fun FSPath.toNode(fileSystem: FileSystem): FSNode = fileSystem.getNode(this)
fun Path.toNode() = JavaFSNode(this)
fun File.toNode() = JavaFSNode(this)
