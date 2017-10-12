package mb.pie.runtime.core

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.google.inject.Provider
import mb.log.SLF4JLogger
import mb.pie.runtime.core.impl.cache.MapBuildCache
import mb.pie.runtime.core.impl.cache.NoopBuildCache
import mb.pie.runtime.core.impl.logger.StreamBuildLogger
import mb.pie.runtime.core.impl.share.CoroutineBuildShare
import mb.pie.runtime.core.impl.store.InMemoryBuildStore
import mb.pie.runtime.core.impl.store.LMDBBuildStoreFactory
import mb.pie.runtime.core.impl.layer.ValidationBuildLayer
import org.junit.jupiter.api.*
import org.slf4j.LoggerFactory
import java.io.File
import java.util.stream.Stream

object TestGenerator {
  fun generate(name: String, testFunc: ParametrizedTestCtx.() -> Unit): Stream<out DynamicNode> {
    val logger = SLF4JLogger(LoggerFactory.getLogger("root"))
    val stores = arrayOf({ InMemoryBuildStore() }, { LMDBBuildStoreFactory(logger).create(File("target/lmdbstore")) })
    val caches = arrayOf({ NoopBuildCache() }, { MapBuildCache() })
    val shares = arrayOf({ CoroutineBuildShare() })
    val validationLayerProvider = Provider<BuildLayer> { ValidationBuildLayer(logger) }
    val reporterGen = { StreamBuildLogger() }
    val fsGen = { Jimfs.newFileSystem(Configuration.unix()) }

    val tests = stores.flatMap { storeGen ->
      caches.flatMap { cacheGen ->
        shares.map { shareGen ->
          val store = storeGen()
          val cache = cacheGen()
          val share = shareGen()
          val reporter = reporterGen()
          val fs = fsGen()

          DynamicTest.dynamicTest("$store, $cache, $share", {
            val context = ParametrizedTestCtx(logger, store, cache, share, validationLayerProvider, reporter, fs)
            context.testFunc()
            context.close()
          })
        }
      }
    }.stream()

    return Stream.of(DynamicContainer.dynamicContainer(name, tests))
  }
}