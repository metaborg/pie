package mb.ceres

import com.nhaarman.mockito_kotlin.*
import mb.ceres.internal.BuildManagerImpl
import mb.ceres.internal.BuildStore
import mb.ceres.internal.InMemoryBuildStore
import mb.ceres.internal.LMDBBuildStore
import name.falgout.jeffrey.testing.junit5.GuiceExtension
import name.falgout.jeffrey.testing.junit5.IncludeModule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.attribute.FileTime

@Retention(AnnotationRetention.RUNTIME)
@ParameterizedTest()
@MethodSource(names = arrayOf("createBuildStores"))
internal annotation class UseBuildStores

@ExtendWith(GuiceExtension::class)
@IncludeModule(TestModule::class)
internal class BuildManagerTests : TestBase() {
  companion object {
    @Suppress("unused")
    @JvmStatic fun createBuildStores(): Array<BuildStore> {
      return arrayOf(InMemoryBuildStore(), LMDBBuildStore(File("build/test/lmdbstore")))
    }
  }

  @UseBuildStores
  fun testBuild(store: BuildStore) {
    store.reset()
    val bm = BuildManagerImpl(store)

    val sbm = spy(bm)
    val input = "CAPITALIZED"
    val builder = spy(toLowerCase)
    sbm.registerBuilder(builder)
    val request = a(builder, input)

    val result = sbm.buildInternal(request)
    assertEquals(builder.id, result.builderId)
    assertEquals(input, result.input)
    assertEquals("capitalized", result.output)
    assertEquals(0, result.reqs.size)
    assertEquals(0, result.gens.size)

    inOrder(sbm, builder) {
      verify(sbm, times(1)).require(request)
      verify(sbm, times(1)).rebuild(request)
      verify(builder, times(1)).build(eq(input), anyOrNull())
      verify(sbm, times(1)).validate(result)
    }

    verify(builder, atLeastOnce()).desc(input)
  }

  @UseBuildStores
  fun testMultipleBuilds(store: BuildStore) {
    store.reset()
    val sbm = spy(BuildManagerImpl(store))
    val builder = spy(toLowerCase)
    sbm.registerBuilder(builder)
    val input1 = "CAPITALIZED"
    val request1 = a(builder, input1)
    val input2 = "CAPITALIZED_EVEN_MORE"
    val request2 = a(builder, input2)

    val results = sbm.buildAllInternal(request1, request2)
    assertEquals(2, results.size)

    val result1 = results[0]
    assertEquals(builder.id, result1.builderId)
    assertEquals(input1, result1.input)
    assertEquals("capitalized", result1.output)

    val result2 = results[1]
    assertEquals(builder.id, result2.builderId)
    assertEquals(input2, result2.input)
    assertEquals("capitalized_even_more", result2.output)

    assertNotEquals(result1, result2)

    inOrder(sbm, builder) {
      verify(sbm, times(1)).require(request1)
      verify(sbm, times(1)).rebuild(request1)
      verify(builder, times(1)).build(eq(input1), anyOrNull())
      verify(sbm, times(1)).validate(result1)

      verify(sbm, times(1)).require(request2)
      verify(sbm, times(1)).rebuild(request2)
      verify(builder, times(1)).build(eq(input2), anyOrNull())
      verify(sbm, times(1)).validate(result2)
    }
  }

  @UseBuildStores
  fun testReuse(store: BuildStore) {
    store.reset()
    val bm = BuildManagerImpl(store)

    val input = "CAPITALIZED"
    val builder = spy(toLowerCase)
    bm.registerBuilder(builder)
    val request = a(builder, input)
    val output1 = bm.build(request)

    val sbm = spy(bm)
    val output2 = sbm.build(request)

    assertEquals(output1, output2)

    // Result is reused if rebuild is never called
    verify(sbm, never()).rebuild(request)

    verify(builder, atMost(1)).build(eq(input), anyOrNull())
    verify(builder, atLeastOnce()).desc(input)
  }

  @UseBuildStores
  fun testPathReq(store: BuildStore, fs: FileSystem) {
    store.reset()
    val bm = BuildManagerImpl(store)

    val readPath = spy(readPath)
    bm.registerBuilder(readPath)

    val filePath = p(fs, "/file")
    write("HELLO WORLD!", filePath)

    // Build 'readPath', observe rebuild
    val sbm1 = spy(bm)
    val output1 = sbm1.build(BuildApp(readPath, filePath))
    assertEquals("HELLO WORLD!", output1)
    verify(sbm1, times(1)).rebuild(BuildApp(readPath, filePath))

    // No changes - build 'readPath', observe no rebuild
    val sbm2 = spy(bm)
    val output2 = sbm1.build(BuildApp(readPath, filePath))
    assertEquals("HELLO WORLD!", output2)
    verify(sbm2, never()).rebuild(BuildApp(readPath, filePath))

    // Change required file in such a way that the output of 'readPath' changes (change file content)
    write("!DLROW OLLEH", filePath)

    // Run again - expect rebuild
    val sbm3 = spy(bm)
    val output3 = sbm3.build(BuildApp(readPath, filePath))
    assertEquals("!DLROW OLLEH", output3)
    verify(sbm3, times(1)).rebuild(BuildApp(readPath, filePath))
  }

  @UseBuildStores
  fun testPathGen(store: BuildStore, fs: FileSystem) {
    store.reset()
    val bm = BuildManagerImpl(store)

    val writePath = spy(writePath)
    bm.registerBuilder(writePath)

    val filePath = p(fs, "/file")
    assertTrue(Files.notExists(filePath.javaPath))

    // Build 'writePath', observe rebuild and existence of file
    val sbm1 = spy(bm)
    sbm1.build(BuildApp(writePath, Pair("HELLO WORLD!", filePath)))
    verify(sbm1, times(1)).rebuild(BuildApp(writePath, Pair("HELLO WORLD!", filePath)))

    assertTrue(Files.exists(filePath.javaPath))
    assertEquals("HELLO WORLD!", read(filePath))

    // No changes - build 'writePath', observe no rebuild, no change to file
    val sbm2 = spy(bm)
    sbm2.build(BuildApp(writePath, Pair("HELLO WORLD!", filePath)))
    verify(sbm2, never()).rebuild(BuildApp(writePath, Pair("HELLO WORLD!", filePath)))

    // Change generated file in such a way that 'writePath' is rebuilt (change file content)
    write("!DLROW OLLEH", filePath)

    // Build 'writePath', observe rebuild and change of file
    val sbm3 = spy(bm)
    sbm3.build(BuildApp(writePath, Pair("HELLO WORLD!", filePath)))
    verify(sbm3, times(1)).rebuild(BuildApp(writePath, Pair("HELLO WORLD!", filePath)))

    assertEquals("HELLO WORLD!", read(filePath))
  }

  @UseBuildStores
  fun testBuildReq(store: BuildStore, fs: FileSystem) {
    store.reset()
    val bm = BuildManagerImpl(store)

    val toLowerCase = spy(toLowerCase)
    bm.registerBuilder(toLowerCase)
    val readPath = spy(readPath)
    bm.registerBuilder(readPath)
    val combine = spy(b<CPath, String>("combine", { "toLowerCase(read($it))" }) {
      val text = requireOutput(BuildApp(readPath, it))
      requireOutput(BuildApp(toLowerCase, text))
    })
    bm.registerBuilder(combine)

    val filePath = p(fs, "/file")
    write("HELLO WORLD!", filePath)

    // Build 'combine', observe rebuild of all
    val sbm1 = spy(bm)
    val output1 = sbm1.build(BuildApp(combine, filePath))
    assertEquals("hello world!", output1)
    inOrder(sbm1) {
      verify(sbm1, times(1)).rebuild(BuildApp(combine, filePath))
      verify(sbm1, times(1)).rebuild(BuildApp(readPath, filePath))
      verify(sbm1, times(1)).rebuild(BuildApp(toLowerCase, "HELLO WORLD!"))
    }

    // No changes - build 'combine', observe no rebuild
    val sbm2 = spy(bm)
    val output2 = sbm2.build(BuildApp(combine, filePath))
    assertEquals("hello world!", output2)
    verify(sbm2, never()).rebuild(BuildApp(combine, filePath))
    verify(sbm2, never()).rebuild(BuildApp(readPath, filePath))
    verify(sbm2, never()).rebuild(BuildApp(toLowerCase, "HELLO WORLD!"))

    // Change required file in such a way that the output of 'readPath' changes (change file content)
    write("!DLROW OLLEH", filePath)

    // Build 'combine', observe rebuild of all in dependency order
    val sbm3 = spy(bm)
    val output3 = sbm3.build(BuildApp(combine, filePath))
    assertEquals("!dlrow olleh", output3)
    inOrder(sbm3) {
      verify(sbm3, times(1)).require(BuildApp(combine, filePath))
      verify(sbm3, times(1)).rebuild(BuildApp(readPath, filePath))
      verify(sbm3, times(1)).rebuild(BuildApp(combine, filePath))
      verify(sbm3, times(1)).rebuild(BuildApp(toLowerCase, "!DLROW OLLEH"))
    }

    // Change required file in such a way that the output of 'readPath' does not change (change modification date)
    val lastModified = Files.getLastModifiedTime(filePath.javaPath)
    val newLastModified = FileTime.fromMillis(lastModified.toMillis() + 1)
    Files.setLastModifiedTime(filePath.javaPath, newLastModified)

    // Build 'combine', observe rebuild of 'readPath' only
    val sbm4 = spy(bm)
    val output4 = sbm4.build(BuildApp(combine, filePath))
    assertEquals("!dlrow olleh", output4)
    inOrder(sbm4) {
      verify(sbm4, times(1)).require(BuildApp(combine, filePath))
      verify(sbm4, times(1)).rebuild(BuildApp(readPath, filePath))
    }
    verify(sbm4, never()).rebuild(BuildApp(combine, filePath))
    verify(sbm4, never()).rebuild(BuildApp(toLowerCase, "!DLROW OLLEH"))
  }

  @UseBuildStores
  fun testOverlappingGeneratedPath(store: BuildStore, fs: FileSystem) {
    store.reset()
    val bm = BuildManagerImpl(store)

    bm.registerBuilder(writePath)

    val filePath = p(fs, "/file")
    assertThrows(OverlappingGeneratedPathException::class.java) {
      bm.buildAll(BuildApp(writePath, Pair("HELLO WORLD 1!", filePath)), BuildApp(writePath, Pair("HELLO WORLD 2!", filePath)))
    }

    // Overlapping generated path exception should also trigger between separate builds
    assertThrows(OverlappingGeneratedPathException::class.java) {
      bm.build(BuildApp(writePath, Pair("HELLO WORLD 3!", filePath)))
    }
  }

  @UseBuildStores
  fun testGenerateRequiredHiddenDep(store: BuildStore, fs: FileSystem) {
    store.reset()
    val bm = BuildManagerImpl(store)

    bm.registerBuilder(readPath)
    bm.registerBuilder(writePath)

    val filePath = p(fs, "/file")
    write("HELLO WORLD!", filePath)

    assertThrows(HiddenDependencyException::class.java) {
      bm.buildAll(BuildApp(readPath, filePath), BuildApp(writePath, Pair("HELLO WORLD!", filePath)))
    }

    // Hidden dependency exception should also trigger between separate builds
    assertThrows(HiddenDependencyException::class.java) {
      bm.build(BuildApp(readPath, filePath))
    }
  }

  @UseBuildStores
  fun testRequireGeneratedHiddenDep(store: BuildStore, fs: FileSystem) {
    store.reset()
    val bm = BuildManagerImpl(store)

    val indirection = requireOutputBuilder<Pair<String, CPath>, None>()
    bm.registerBuilder(indirection)
    bm.registerBuilder(writePath)
    bm.registerBuilder(readPath)

    val combineIncorrect = spy(b<Pair<String, CPath>, String>("combineIncorrect", { "combine$it" }) { (text, path) ->
      requireBuild(BuildApp(indirection, BuildApp(writePath, Pair(text, path))))
      requireOutput(BuildApp(readPath, path))
    })
    bm.registerBuilder(combineIncorrect)

    val filePath1 = p(fs, "/file1")
    assertThrows(HiddenDependencyException::class.java) {
      bm.build(BuildApp(combineIncorrect, Pair("HELLO WORLD!", filePath1)))
    }

    val combineStillIncorrect = spy(b<Pair<String, CPath>, String>("combineStillIncorrect", { "combine$it" }) { (text, path) ->
      requireBuild(BuildApp(indirection, BuildApp(writePath, Pair(text, path))))
      requireBuild(BuildApp(writePath, Pair(text, path)))
      requireOutput(BuildApp(readPath, path))
    })
    bm.registerBuilder(combineStillIncorrect)

    val filePath2 = p(fs, "/file2")
    assertThrows(HiddenDependencyException::class.java) {
      bm.build(BuildApp(combineStillIncorrect, Pair("HELLO WORLD!", filePath2)))
    }
  }

  @UseBuildStores
  fun testCyclicDependency(store: BuildStore) {
    store.reset()
    val bm = BuildManagerImpl(store)

    val b1 = b<None, None>("b1", { "b1" }, { requireOutput(BuildApp("b1", None.instance)) })
    bm.registerBuilder(b1)

    assertThrows(CyclicDependencyException::class.java) {
      bm.build(BuildApp(b1, None.instance))
    }
  }
}