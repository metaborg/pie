package mb.pie.runtime.core

import com.google.inject.*
import mb.log.LogModule
import mb.log.Logger
import mb.pie.runtime.core.impl.*
import mb.pie.runtime.core.impl.share.BuildShare
import mb.vfs.path.PPath
import mb.vfs.path.PPathImpl
import org.slf4j.LoggerFactory
import java.nio.file.*

open class ParametrizedTestCtx(
  logger: Logger,
  val store: BuildStore,
  val cache: BuildCache,
  val share: BuildShare,
  val buildLayerProvider: Provider<BuildLayer>,
  val reporter: BuildLogger,
  val fs: FileSystem
) : TestCtx(), AutoCloseable {
  val logger: Logger = logger.forContext("Test")

  init {
    store.writeTxn().use { it.drop() }
    cache.drop()
  }

  override fun close() {
    store.close()
  }


  fun b(): BuildImpl {
    return b(store, cache, share, buildLayerProvider.get(), reporter)
  }

  fun bm(): BuildManager {
    return bm(store, cache, share, buildLayerProvider)
  }
}

open class TestCtx {
  protected val inj: Injector = Guice.createInjector(PieModule(), LogModule(LoggerFactory.getLogger("root")))
  protected val builders = mutableMapOf<String, UBuilder>()


  val toLowerCase = lb<String, String>("toLowerCase", { "toLowerCase($it)" }) {
    it.toLowerCase()
  }
  val readPath = lb<PPath, String>("read", { "read($it)" }) {
    require(it)
    read(it)
  }
  val writePath = lb<Pair<String, PPath>, None>("write", { "write$it" }) { (text, path) ->
    write(text, path)
    generate(path)
    None.instance
  }


  fun p(fs: FileSystem, path: String): PPath {
    return PPathImpl(fs.getPath(path))
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


  fun b(store: BuildStore, cache: BuildCache, share: BuildShare, buildLayer: BuildLayer, logger: BuildLogger): BuildImpl {
    return BuildImpl(store, cache, share, buildLayer, logger, builders, inj)
  }

  fun bm(store: BuildStore, cache: BuildCache, share: BuildShare, buildLayerProvider: Provider<BuildLayer>): BuildManager {
    return BuildManagerImpl(store, cache, share, buildLayerProvider, builders, inj)
  }


  fun read(path: PPath): String {
    Files.newInputStream(path.javaPath, StandardOpenOption.READ).use {
      val bytes = it.readBytes()
      val text = String(bytes)
      return text
    }
  }

  fun write(text: String, path: PPath) {
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