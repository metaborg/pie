package mb.pie.api.test

import mb.fs.java.JavaFSNode
import mb.pie.api.*
import mb.pie.api.fs.stamp.FileSystemStampers
import javax.swing.text.html.HTML.Tag.I

val ApiTestCtx.toLowerCase
  get() = taskDef<String, String>("toLowerCase", { input, _ -> "toLowerCase($input)" }) {
    it.toLowerCase()
  }

val ApiTestCtx.readPath
  get() = taskDef<JavaFSNode, String>("read", { input, _ -> "read($input)" }) {
    require(it, FileSystemStampers.modified)
    read(it)
  }

val ApiTestCtx.writePath
  get() = taskDef<Pair<String, JavaFSNode>, None>("write", { input, _ -> "write($input)" }) { (text, path) ->
    write(text, path)
    provide(path)
    None.instance
  }

inline fun <reified I : In, reified O : Out> ApiTestCtx.requireOutputFunc(): TaskDef<STask<I>, O> {
  return taskDef<STask<I>, O>("require(${I::class}):${O::class}", { input, _ -> "require($input)" }) { task ->
    require(task) as O
  }
}
