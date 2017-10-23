package mb.pie.runtime.core

import com.google.inject.*
import mb.log.LogModule
import mb.log.Logger
import mb.pie.runtime.core.impl.PullingExecImpl
import mb.pie.runtime.core.impl.PullingExecutorImpl
import mb.vfs.path.PPath
import mb.vfs.path.PPathImpl
import org.slf4j.LoggerFactory
import java.nio.file.*

open class ParametrizedTestCtx(
  metaborgLogger: Logger,
  val store: Store,
  val cache: Cache,
  val share: BuildShare,
  val layerProvider: Provider<Layer>,
  val logger: mb.pie.runtime.core.Logger,
  val fs: FileSystem
) : TestCtx(), AutoCloseable {
  val metaborgLogger: Logger = metaborgLogger.forContext("Test")

  init {
    store.writeTxn().use { it.drop() }
    cache.drop()
  }

  override fun close() {
    store.close()
  }


  fun pullingExec(): PullingExecImpl {
    return pullingExec(store, cache, share, layerProvider.get(), logger)
  }

  fun pullingExecutor(): PullingExecutor {
    return pullingExecutor(store, cache, share, layerProvider, logger)
  }
}

open class TestCtx {
  private val inj: Injector = Guice.createInjector(PieModule(), LogModule(LoggerFactory.getLogger("root")))
  private val funcs = mutableMapOf<String, UFunc>()


  val toLowerCase = func<String, String>("toLowerCase", { "toLowerCase($it)" }) {
    it.toLowerCase()
  }
  val readPath = func<PPath, String>("read", { "read($it)" }) {
    require(it)
    read(it)
  }
  val writePath = func<Pair<String, PPath>, None>("write", { "write$it" }) { (text, path) ->
    write(text, path)
    generate(path)
    None.instance
  }


  fun path(fs: FileSystem, path: String): PPath {
    return PPathImpl(fs.getPath(path))
  }

  fun <I : In, O : Out> func(id: String, descFunc: (I) -> String, execFunc: ExecContext.(I) -> O): Func<I, O> {
    return LambdaFunc(id, descFunc, execFunc)
  }

  fun <I : In, O : Out> app(func: Func<I, O>, input: I): FuncApp<I, O> {
    return FuncApp(func, input)
  }

  fun <I : In, O : Out> app(builderId: String, input: I): FuncApp<I, O> {
    return FuncApp<I, O>(builderId, input)
  }


  fun pullingExec(store: Store, cache: Cache, share: BuildShare, layer: Layer, logger: mb.pie.runtime.core.Logger): PullingExecImpl {
    return PullingExecImpl(store, cache, share, layer, logger, funcs)
  }

  fun pullingExecutor(store: Store, cache: Cache, share: BuildShare, layerProvider: Provider<Layer>, logger: mb.pie.runtime.core.Logger): PullingExecutor {
    return PullingExecutorImpl(store, cache, share, layerProvider, funcs, logger)
  }


  fun read(path: PPath): String {
    Files.newInputStream(path.javaPath, StandardOpenOption.READ).use {
      return String(it.readBytes())
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


  fun registerFunc(builder: UFunc) {
    funcs[builder.id] = builder
  }

  inline fun <reified I : In, reified O : Out> requireOutputFunc(): Func<FuncApp<I, O>, O> {
    return func<FuncApp<I, O>, O>("requireOutput(${I::class}):${O::class}", { "requireOutput($it)" }) {
      requireOutput(it)
    }
  }
}