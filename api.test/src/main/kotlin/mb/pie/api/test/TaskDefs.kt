package mb.pie.api.test

import mb.pie.api.*
import mb.pie.api.stamp.FileStampers
import mb.pie.vfs.path.PPath

val ApiTestCtx.toLowerCase
  get() = func<String, String>("toLowerCase", { "toLowerCase($it)" }) {
    it.toLowerCase()
  }

val ApiTestCtx.readPath
  get() = func<PPath, String>("read", { "read($it)" }) {
    require(it, FileStampers.modified)
    read(it)
  }

val ApiTestCtx.writePath
  get() = func<Pair<String, PPath>, None>("write", { "write$it" }) { (text, path) ->
    write(text, path)
    generate(path)
    None.instance
  }

inline fun <reified I : In, reified O : Out> ApiTestCtx.requireOutputFunc(): TaskDef<Task<I, O>, O> {
  return func<Task<I, O>, O>("requireOutput(${I::class}):${O::class}", { "requireOutput($it)" }) {
    requireOutput(it)
  }
}
