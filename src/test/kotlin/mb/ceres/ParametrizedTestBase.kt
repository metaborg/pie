package mb.ceres

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import mb.ceres.impl.*
import mb.ceres.impl.store.BuildStore
import mb.ceres.impl.store.InMemoryBuildStore
import mb.ceres.impl.store.LMDBBuildStoreFactory
import mb.log.Logger
import mb.log.SLF4JLogger
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ContainerExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ObjectArrayArguments
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.FileSystem
import java.util.stream.Stream

@Retention(AnnotationRetention.RUNTIME)
@ParameterizedTest(name = "{index}) {1}, {2}, {3}")
@ArgumentsSource(ArgumentProvider::class)
internal annotation class UseBuildVariability

object ArgumentProvider : ArgumentsProvider {
  override fun arguments(context: ContainerExtensionContext?): Stream<out Arguments> {
    val logger = SLF4JLogger(LoggerFactory.getLogger("root"))
    val stores = arrayOf({ InMemoryBuildStore() }, { LMDBBuildStoreFactory(logger).create(File("target/lmdbstore")) })
    val caches = arrayOf({ NoBuildCache() }, { MapBuildCache() })
    val shares = arrayOf({ BuildShareImpl() })
    val reporter = { StreamBuildReporter() }
    val fs = { Jimfs.newFileSystem(Configuration.unix()) }

    return stores.flatMap { store ->
      caches.flatMap { cache ->
        shares.map { share ->
          ObjectArrayArguments.create(logger, store(), cache(), share(), reporter(), fs())
        }
      }
    }.stream()
  }
}

open internal class ParametrizedTestBase : TestBase() {
  protected lateinit var logger: Logger
  protected lateinit var store: BuildStore
  protected lateinit var cache: BuildCache
  protected lateinit var share: BuildShare
  protected lateinit var reporter: BuildReporter
  protected lateinit var fs: FileSystem


  @BeforeEach
  fun beforeEach(logger: Logger, store: BuildStore, cache: BuildCache, share: BuildShare, reporter: BuildReporter, fs: FileSystem) {
    this.logger = logger.forContext("Test")
    this.store = store
    this.store.writeTxn().use { it.drop() }
    this.cache = cache
    this.cache.drop()
    this.share = share
    this.reporter = reporter
    this.fs = fs
  }

  @AfterEach
  fun afterEach() {
    store.close()
  }


  fun b(): BuildImpl {
    return b(store, cache, share, reporter)
  }

  fun bm(): BuildManager {
    return bm(store, cache, share)
  }
}