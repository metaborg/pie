package mb.pie.runtime.core.test.util

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.google.inject.Provider
import mb.log.SLF4JLogger
import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.cache.MapCache
import mb.pie.runtime.core.impl.cache.NoopCache
import mb.pie.runtime.core.impl.layer.ValidationLayer
import mb.pie.runtime.core.impl.logger.StreamLogger
import mb.pie.runtime.core.impl.share.CoroutineShare
import mb.pie.runtime.core.impl.store.InMemoryStore
import mb.pie.runtime.core.impl.store.LMDBBuildStoreFactory
import org.junit.jupiter.api.*
import org.slf4j.LoggerFactory
import java.io.File
import java.util.stream.Stream

object TestGenerator {
  fun generate(
    name: String,
    dMetaborgLogger: mb.log.Logger? = null,
    dStoreGens: Array<() -> Store>? = null,
    dCacheGens: Array<() -> Cache>? = null,
    dShareGens: Array<() -> Share>? = null,
    dLayerGen: Provider<Layer>? = null,
    dLoggerGen: (() -> Logger)? = null,
    testFunc: ParametrizedTestCtx.() -> Unit
  ): Stream<out DynamicNode> {
    val metaborgLogger = dMetaborgLogger ?: SLF4JLogger(LoggerFactory.getLogger("root"))
    val storeGens = dStoreGens ?: arrayOf({ InMemoryStore() }, { LMDBBuildStoreFactory(metaborgLogger).create(File("target/lmdbstore")) })
    val cacheGens = dCacheGens ?: arrayOf({ NoopCache() }, { MapCache() })
    val shareGens = dShareGens ?: arrayOf({ CoroutineShare() })
    val layerGen = dLayerGen ?: Provider<Layer> { ValidationLayer(metaborgLogger) }
    val loggerGen = dLoggerGen ?: { StreamLogger() }
    val fsGen = { Jimfs.newFileSystem(Configuration.unix()) }

    val tests = storeGens.flatMap { storeGen ->
      cacheGens.flatMap { cacheGen ->
        shareGens.map { shareGen ->
          val store = storeGen()
          val cache = cacheGen()
          val share = shareGen()
          val reporter = loggerGen()
          val fs = fsGen()

          DynamicTest.dynamicTest("$store, $cache, $share", {
            val context = ParametrizedTestCtx(metaborgLogger, store, cache, share, layerGen, reporter, fs)
            context.testFunc()
            context.close()
          })
        }
      }
    }.stream()

    return Stream.of(DynamicContainer.dynamicContainer(name, tests))
  }
}
