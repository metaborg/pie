package mb.pie.runtime.core.test.util

import com.google.inject.*
import mb.log.LogModule
import mb.pie.runtime.core.*
import mb.pie.runtime.core.exec.*
import mb.pie.runtime.core.impl.exec.*
import mb.vfs.path.PPath
import mb.vfs.path.PPathImpl
import org.slf4j.LoggerFactory
import java.nio.file.*


open class ParametrizedTestCtx(
  mbLogger: mb.log.Logger,
  val store: Store,
  val cache: Cache,
  val share: Share,
  val layerProvider: Provider<Layer>,
  val loggerProvider: Provider<Logger>,
  val fs: FileSystem
) : TestCtx(), AutoCloseable {
  val mbLogger: mb.log.Logger = mbLogger.forContext("Test")

  init {
    store.writeTxn().use { it.drop() }
    cache.drop()
  }

  override fun close() {
    store.close()
  }

  fun topDownExecutor(): TopDownExecutor {
    return pullingExecutor(store, cache, share, layerProvider, loggerProvider)
  }

  fun topDownExec(): TopDownExecImpl {
    return pullingExec(store, cache, share, layerProvider.get(), loggerProvider.get())
  }


  fun bottomUpExecutor(): BottomUpExecutor {
    return observingExecutor(store, cache, share, layerProvider, loggerProvider)
  }

  fun bottomUpExec(observers: Map<UTask, TaskObserver> = mapOf()): BottomUpExec {
    return observingExec(store, cache, share, layerProvider.get(), loggerProvider.get(), observers)
  }
}

open class TestCtx {
  private val inj: Injector = Guice.createInjector(PieModule(), LogModule(LoggerFactory.getLogger("root")))
  private val funcs = mutableMapOf<String, UTaskDef>()


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

  fun <I : In, O : Out> func(id: String, descFunc: (I) -> String, execFunc: ExecContext.(I) -> O): TaskDef<I, O> {
    return LambdaTaskDef(id, execFunc, descFunc)
  }

  fun <I : In, O : Out> app(taskDef: TaskDef<I, O>, input: I): Task<I, O> {
    return Task(taskDef, input)
  }

  fun <I : In, O : Out> app(builderId: String, input: I): Task<I, O> {
    return Task<I, O>(builderId, input)
  }


  fun pullingExecutor(store: Store, cache: Cache, share: Share, layerProvider: Provider<Layer>, loggerProvider: Provider<Logger>): TopDownExecutor {
    return TopDownExecutorImpl(store, cache, share, layerProvider, loggerProvider, funcs)
  }

  fun pullingExec(store: Store, cache: Cache, share: Share, layer: Layer, logger: Logger): TopDownExecImpl {
    return TopDownExecImpl(store, cache, share, layer, logger, funcs)
  }


  fun observingExecutor(store: Store, cache: Cache, share: Share, layerProvider: Provider<Layer>, loggerProvider: Provider<Logger>): BottomUpExecutorImpl {
    return BottomUpExecutorImpl(store, cache, share, layerProvider, loggerProvider, funcs)
  }

  fun observingExec(store: Store, cache: Cache, share: Share, layer: Layer, logger: Logger, observers: Map<UTask, TaskObserver>): BottomUpExec {
    return BottomUpExec(store, cache, share, layer, logger, funcs, observers)
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


  fun registerFunc(builder: UTaskDef) {
    funcs[builder.id] = builder
  }

  inline fun <reified I : In, reified O : Out> requireOutputFunc(): TaskDef<Task<I, O>, O> {
    return func<Task<I, O>, O>("requireOutput(${I::class}):${O::class}", { "requireOutput($it)" }) {
      requireOutput(it)
    }
  }
}
