package mb.pie.api.test

import mb.pie.api.*
import mb.pie.api.stamp.resource.FileSystemStampers
import mb.resource.fs.FSResource
import java.io.Serializable
import javax.swing.text.html.HTML.Tag.I

val ApiTestCtx.toLowerCase
  get() = taskDef<String, String>("toLowerCase", { input, _ -> "toLowerCase($input)" }) {
    it.toLowerCase()
  }

val ApiTestCtx.readPath
  get() = taskDef<FSResource, String>("read", { input, _ -> "read($input)" }) {
    require(it, FileSystemStampers.modified())
    read(it)
  }

val ApiTestCtx.writePath
  get() = taskDef<Pair<String, FSResource>, None>("write", { input, _ -> "write($input)" }) { (text, path) ->
    write(text, path)
    provide(path)
    None.instance
  }

inline fun <reified I : Serializable, reified O : Serializable?> ApiTestCtx.requireOutputFunc(): TaskDef<STask<I>, O> {
  return taskDef<STask<I>, O>("require(${I::class}):${O::class}", { input, _ -> "require($input)" }) { task ->
    require(task) as O
  }
}
