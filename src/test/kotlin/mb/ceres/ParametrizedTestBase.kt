package mb.ceres

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import mb.ceres.impl.BuildCache
import mb.ceres.impl.MapBuildCache
import mb.ceres.impl.NoBuildCache
import mb.ceres.internal.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ContainerExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ObjectArrayArguments
import java.io.File
import java.nio.file.FileSystem
import java.util.stream.Stream

@Retention(AnnotationRetention.RUNTIME)
@ParameterizedTest(name = "{index}) {0}, {1}, {2}")
@ArgumentsSource(ArgumentProvider::class)
internal annotation class UseBuildVariability

object ArgumentProvider : ArgumentsProvider {
  override fun arguments(context: ContainerExtensionContext?): Stream<out Arguments> {
    val stores = arrayOf({ InMemoryBuildStore() }, { LMDBBuildStoreFactory().create(File("target/lmdbstore")) })
    val caches = arrayOf({ NoBuildCache() }, { MapBuildCache() })
    val shares = arrayOf({ BuildShareImpl() })
    val fs = { Jimfs.newFileSystem(Configuration.unix()) }

    return stores.flatMap { store ->
      caches.flatMap { cache ->
        shares.map { share ->
          ObjectArrayArguments.create(store(), cache(), share(), fs())
        }
      }
    }.stream()
  }
}

open internal class ParametrizedTestBase : TestBase() {
  protected lateinit var store: BuildStore
  protected lateinit var cache: BuildCache
  protected lateinit var share: BuildShare
  protected lateinit var fs: FileSystem


  @BeforeEach
  fun beforeEach(store: BuildStore, cache: BuildCache, share: BuildShare, fs: FileSystem) {
    this.store = store
    this.store.drop()
    this.cache = cache
    this.cache.drop()
    this.share = share
    this.fs = fs
  }

  @AfterEach
  fun afterEach() {
    store.close()
  }


  fun b(): BuildImpl {
    return b(store, cache, share)
  }

  fun bm(): BuildManager {
    return bm(store, cache, share)
  }
}