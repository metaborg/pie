package mb.ceres

import com.google.inject.Guice
import com.google.inject.Injector
import mb.ceres.impl.BuildCache
import mb.ceres.internal.BuildImpl
import mb.ceres.internal.BuildManagerImpl
import mb.ceres.internal.BuildShare
import mb.ceres.internal.BuildStore
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.StandardOpenOption

open class TestBase {
  protected val inj: Injector = Guice.createInjector(CeresModule())
  protected val builders = mutableMapOf<String, UBuilder>()


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
    return BuildApp<I, O>(builderId, input)
  }


  fun b(store: BuildStore, cache: BuildCache, share: BuildShare): BuildImpl {
    return BuildImpl(store, cache, share, builders, inj)
  }

  fun bm(store: BuildStore, cache: BuildCache, share: BuildShare): BuildManager {
    return BuildManagerImpl(store, cache, share, builders, inj)
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


  fun registerBuilder(builder: UBuilder) {
    builders[builder.id] = builder
  }

  inline fun <reified I : In, reified O : Out> requireOutputBuilder(): Builder<BuildApp<I, O>, O> {
    return lb<BuildApp<I, O>, O>("requireOutput(${I::class}):${O::class}", { "requireOutput($it)" }) {
      requireOutput(it)
    }
  }
}