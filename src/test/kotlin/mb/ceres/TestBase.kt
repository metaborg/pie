package mb.ceres

import mb.ceres.internal.BuildImpl
import mb.ceres.internal.Store
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.StandardOpenOption

open internal class TestBase {
  fun p(fs: FileSystem, path: String): CPath {
    return CPath(fs.getPath(path))
  }

  fun <I : In, O : Out> lb(id: String, descFunc: (I) -> String, buildFunc: BuildContext.(I) -> O): Builder<I, O> {
    return LambdaBuilder(id, descFunc, buildFunc)
  }

  fun <I : In, O : Out> a(builder: Builder<I, O>, input: I): BuildApp<I, O> {
    return BuildApp(builder, input)
  }

  fun <I : In, O : Out> a(builderId: String, input: I): BuildApp<I, O> {
    return BuildApp(builderId, input)
  }

  fun b(store: Store, bStore: BuilderStore): BuildImpl {
    return BuildImpl(store, bStore)
  }


  fun read(path: CPath): String {
    Files.newInputStream(path.javaPath, StandardOpenOption.READ).use {
      val bytes = it.readBytes()
      val text = String(bytes)
      return text
    }
  }

  fun write(text: String, path: CPath) {
    println("Write $text")
    Files.newOutputStream(path.javaPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.SYNC).use {
      it.write(text.toByteArray())
    }
    // HACK: for some reason, sleeping is required for writes to the file to be picked up by reads...
    Thread.sleep(1)
  }


  val toLowerCase = lb<String, String>("toLowerCase", { "toLowerCase($it)" }) {
    it.toLowerCase()
  }
  val readPath = lb<CPath, String>("read", { "read($it)" }) {
    require(it)
    read(it)
  }
  val writePath = lb<Pair<String, CPath>, None>("write", { "write$it" }) { (text, path) ->
    write(text, path)
    generate(path)
    None.instance
  }

  inline fun <reified I : In, reified O : Out> requireOutputBuilder(): Builder<BuildApp<I, O>, O> {
    return lb("requireOutput(${I::class}):${O::class}", { "requireOutput($it)" }) {
      requireOutput(it)
    }
  }
}