package mb.pie.runtime.core

import com.google.inject.*
import mb.log.LogModule
import mb.log.Logger
import mb.pie.runtime.core.impl.*
import mb.vfs.path.PPath
import mb.vfs.path.PPathImpl
import org.slf4j.LoggerFactory
import java.nio.file.*

open class ParametrizedTestCtx(
  logger: Logger,
  val store: Store,
  val cache: Cache,
  val share: BuildShare,
  val layerProvider: Provider<Layer>,
  val reporter: mb.pie.runtime.core.Logger,
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


  fun b(): PollingExec {
    return b(store, cache, share, layerProvider.get(), reporter)
  }

  fun bm(): PollingExecManager {
    return bm(store, cache, share, layerProvider)
  }
}

open class TestCtx {
  protected val inj: Injector = Guice.createInjector(PieModule(), LogModule(LoggerFactory.getLogger("root")))
  protected val builders = mutableMapOf<String, UFunc>()


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

  fun <I : In, O : Out> lb(id: String, descFunc: (I) -> String, execFunc: ExecContext.(I) -> O): Func<I, O> {
    return LambdaFunc(id, descFunc, execFunc)
  }

  fun <I : In, O : Out> a(func: Func<I, O>, input: I): FuncApp<I, O> {
    return FuncApp(func, input)
  }

  fun <I : In, O : Out> a(builderId: String, input: I): FuncApp<I, O> {
    return FuncApp<I, O>(builderId, input)
  }


  fun b(store: Store, cache: Cache, share: BuildShare, layer: Layer, logger: mb.pie.runtime.core.Logger): PollingExec {
    return PollingExec(store, cache, share, layer, logger, builders, inj)
  }

  fun bm(store: Store, cache: Cache, share: BuildShare, layerProvider: Provider<Layer>): PollingExecManager {
    return PollingExecManagerImpl(store, cache, share, layerProvider, builders, inj)
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


  fun registerBuilder(builder: UFunc) {
    builders[builder.id] = builder
  }

  inline fun <reified I : In, reified O : Out> requireOutputBuilder(): Func<FuncApp<I, O>, O> {
    return lb<FuncApp<I, O>, O>("requireOutput(${I::class}):${O::class}", { "requireOutput($it)" }) {
      requireOutput(it)
    }
  }
}