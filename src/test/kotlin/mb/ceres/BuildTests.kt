package mb.ceres

import com.nhaarman.mockito_kotlin.*
import mb.ceres.internal.*
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

@ExtendWith(GuiceExtension::class)
@IncludeModule(TestModule::class)
internal class BuildManagerTests : TestBase() {
  @UseBuildStores
  fun testBuild(store: Store, bStore: BuilderStore, share: BuildShare) {
    store.use {
      store.reset()

      val input = "CAPITALIZED"
      val builder = spy(toLowerCase)
      bStore.registerBuilder(builder)
      val app = a(builder, input)

      val build = spy(b(store, bStore, share))
      val result = build.require(app)
      assertEquals(builder.id, result.builderId)
      assertEquals(input, result.input)
      assertEquals("capitalized", result.output)
      assertEquals(0, result.reqs.size)
      assertEquals(0, result.gens.size)

      inOrder(build, builder) {
        verify(build, times(1)).require(app)
        verify(build, times(1)).rebuild(app)
        verify(builder, times(1)).build(eq(input), anyOrNull())
      }

      verify(builder, atLeastOnce()).desc(input)
    }
  }

  @UseBuildStores
  fun testMultipleBuilds(store: Store, bStore: BuilderStore, share: BuildShare) {
    store.use {
      store.reset()
      val builder = spy(toLowerCase)
      bStore.registerBuilder(builder)

      val input1 = "CAPITALIZED"
      val app1 = a(builder, input1)
      val build1 = spy(b(store, bStore, share))
      val result1 = build1.require(app1)
      assertEquals(builder.id, result1.builderId)
      assertEquals(input1, result1.input)
      assertEquals("capitalized", result1.output)

      val input2 = "CAPITALIZED_EVEN_MORE"
      val app2 = a(builder, input2)
      val build2 = spy(b(store, bStore, share))
      val result2 = build2.require(app2)
      assertEquals(builder.id, result2.builderId)
      assertEquals(input2, result2.input)
      assertEquals("capitalized_even_more", result2.output)

      assertNotEquals(result1, result2)

      inOrder(builder, build1, build2) {
        verify(build1, times(1)).require(app1)
        verify(build1, times(1)).rebuild(app1)
        verify(builder, times(1)).build(eq(input1), anyOrNull())

        verify(build2, times(1)).require(app2)
        verify(build2, times(1)).rebuild(app2)
        verify(builder, times(1)).build(eq(input2), anyOrNull())
      }
    }
  }

  @UseBuildStores
  fun testReuse(store: Store, bStore: BuilderStore, share: BuildShare) {
    store.use {
      store.reset()

      val builder = spy(toLowerCase)
      bStore.registerBuilder(builder)

      val input = "CAPITALIZED"
      val app = a(builder, input)
      val build1 = b(store, bStore, share)
      val output1 = build1.require(app)

      val build2 = spy(b(store, bStore, share))
      val output2 = build2.require(app)

      assertEquals(output1, output2)

      // Result is reused if rebuild is never called
      verify(build2, never()).rebuild(app)

      verify(builder, atMost(1)).build(eq(input), anyOrNull())
      verify(builder, atLeastOnce()).desc(input)
    }
  }

  @UseBuildStores
  fun testPathReq(store: Store, bStore: BuilderStore, share: BuildShare, fs: FileSystem) {
    store.use {
      store.reset()

      val readPath = spy(readPath)
      bStore.registerBuilder(readPath)

      val filePath = p(fs, "/file")
      write("HELLO WORLD!", filePath)

      // Build 'readPath', observe rebuild
      val build1 = spy(b(store, bStore, share))
      val result1 = build1.require(a(readPath, filePath))
      assertEquals("HELLO WORLD!", result1.output)
      verify(build1, times(1)).rebuild(a(readPath, filePath))

      // No changes - build 'readPath', observe no rebuild
      val build2 = spy(b(store, bStore, share))
      val result2 = build2.require(a(readPath, filePath))
      assertEquals("HELLO WORLD!", result2.output)
      verify(build2, never()).rebuild(a(readPath, filePath))

      // Change required file in such a way that the output of 'readPath' changes (change file content)
      write("!DLROW OLLEH", filePath)

      // Run again - expect rebuild
      val build3 = spy(b(store, bStore, share))
      val result3 = build3.require(a(readPath, filePath))
      assertEquals("!DLROW OLLEH", result3.output)
      verify(build3, times(1)).rebuild(a(readPath, filePath))
    }
  }

  @UseBuildStores
  fun testPathGen(store: Store, bStore: BuilderStore, share: BuildShare, fs: FileSystem) {
    store.use {
      store.reset()

      val writePath = spy(writePath)
      bStore.registerBuilder(writePath)

      val filePath = p(fs, "/file")
      assertTrue(Files.notExists(filePath.javaPath))

      // Build 'writePath', observe rebuild and existence of file
      val build1 = spy(b(store, bStore, share))
      build1.require(a(writePath, Pair("HELLO WORLD!", filePath)))
      verify(build1, times(1)).rebuild(a(writePath, Pair("HELLO WORLD!", filePath)))

      assertTrue(Files.exists(filePath.javaPath))
      assertEquals("HELLO WORLD!", read(filePath))

      // No changes - build 'writePath', observe no rebuild, no change to file
      val build2 = spy(b(store, bStore, share))
      build2.require(a(writePath, Pair("HELLO WORLD!", filePath)))
      verify(build2, never()).rebuild(a(writePath, Pair("HELLO WORLD!", filePath)))

      // Change generated file in such a way that 'writePath' is rebuilt (change file content)
      write("!DLROW OLLEH", filePath)

      // Build 'writePath', observe rebuild and change of file
      val build3 = spy(b(store, bStore, share))
      build3.require(a(writePath, Pair("HELLO WORLD!", filePath)))
      verify(build3, times(1)).rebuild(a(writePath, Pair("HELLO WORLD!", filePath)))

      assertEquals("HELLO WORLD!", read(filePath))
    }
  }

  @UseBuildStores
  fun testBuildReq(store: Store, bStore: BuilderStore, share: BuildShare, fs: FileSystem) {
    store.use {
      store.reset()

      val toLowerCase = spy(toLowerCase)
      bStore.registerBuilder(toLowerCase)
      val readPath = spy(readPath)
      bStore.registerBuilder(readPath)
      val combine = spy(lb<CPath, String>("combine", { "toLowerCase(read($it))" }) {
        val text = requireOutput(a(readPath, it))
        requireOutput(a(toLowerCase, text))
      })
      bStore.registerBuilder(combine)

      val filePath = p(fs, "/file")
      write("HELLO WORLD!", filePath)

      // Build 'combine', observe rebuild of all
      val build1 = spy(b(store, bStore, share))
      val result1 = build1.require(a(combine, filePath))
      assertEquals("hello world!", result1.output)
      inOrder(build1) {
        verify(build1, times(1)).rebuild(a(combine, filePath))
        verify(build1, times(1)).rebuild(a(readPath, filePath))
        verify(build1, times(1)).rebuild(a(toLowerCase, "HELLO WORLD!"))
      }

      // No changes - build 'combine', observe no rebuild
      val build2 = spy(b(store, bStore, share))
      val result2 = build2.require(a(combine, filePath))
      assertEquals("hello world!", result2.output)
      verify(build2, never()).rebuild(a(combine, filePath))
      verify(build2, never()).rebuild(a(readPath, filePath))
      verify(build2, never()).rebuild(a(toLowerCase, "HELLO WORLD!"))

      // Change required file in such a way that the output of 'readPath' changes (change file content)
      write("!DLROW OLLEH", filePath)

      // Build 'combine', observe rebuild of all in dependency order
      val build3 = spy(b(store, bStore, share))
      val result3 = build3.require(a(combine, filePath))
      assertEquals("!dlrow olleh", result3.output)
      inOrder(build3) {
        verify(build3, times(1)).require(a(combine, filePath))
        verify(build3, times(1)).rebuild(a(readPath, filePath))
        verify(build3, times(1)).rebuild(a(combine, filePath))
        verify(build3, times(1)).rebuild(a(toLowerCase, "!DLROW OLLEH"))
      }

      // Change required file in such a way that the output of 'readPath' does not change (change modification date)
      val lastModified = Files.getLastModifiedTime(filePath.javaPath)
      val newLastModified = FileTime.fromMillis(lastModified.toMillis() + 1)
      Files.setLastModifiedTime(filePath.javaPath, newLastModified)

      // Build 'combine', observe rebuild of 'readPath' only
      val build4 = spy(b(store, bStore, share))
      val result4 = build4.require(a(combine, filePath))
      assertEquals("!dlrow olleh", result4.output)
      inOrder(build4) {
        verify(build4, times(1)).require(a(combine, filePath))
        verify(build4, times(1)).rebuild(a(readPath, filePath))
      }
      verify(build4, never()).rebuild(a(combine, filePath))
      verify(build4, never()).rebuild(a(toLowerCase, "!DLROW OLLEH"))
    }
  }

  @UseBuildStores
  fun testOverlappingGeneratedPath(store: Store, bStore: BuilderStore, share: BuildShare, fs: FileSystem) {
    store.use {
      store.reset()

      val bm = BuildManagerImpl(store, bStore, share)

      bm.registerBuilder(writePath)

      val filePath = p(fs, "/file")
      assertThrows(OverlappingGeneratedPathException::class.java) {
        bm.buildAll(a(writePath, Pair("HELLO WORLD 1!", filePath)), a(writePath, Pair("HELLO WORLD 2!", filePath)))
      }

      // Overlapping generated path exception should also trigger between separate builds
      assertThrows(OverlappingGeneratedPathException::class.java) {
        bm.build(a(writePath, Pair("HELLO WORLD 3!", filePath)))
      }
    }
  }

  @UseBuildStores
  fun testGenerateRequiredHiddenDep(store: Store, bStore: BuilderStore, share: BuildShare, fs: FileSystem) {
    store.use {
      store.reset()

      val bm = BuildManagerImpl(store, bStore, share)

      bm.registerBuilder(readPath)
      bm.registerBuilder(writePath)

      val filePath = p(fs, "/file")
      write("HELLO WORLD!", filePath)

      assertThrows(HiddenDependencyException::class.java) {
        bm.build(a(readPath, filePath))
        bm.build(a(writePath, Pair("HELLO WORLD!", filePath)))
      }

      // Hidden dependency exception should also trigger between separate builds
      assertThrows(HiddenDependencyException::class.java) {
        bm.build(a(readPath, filePath))
      }
    }
  }

  @UseBuildStores
  fun testRequireGeneratedHiddenDep(store: Store, bStore: BuilderStore, share: BuildShare, fs: FileSystem) {
    store.use {
      store.reset()

      val bm = BuildManagerImpl(store, bStore, share)

      val indirection = requireOutputBuilder<Pair<String, CPath>, None>()
      bm.registerBuilder(indirection)
      bm.registerBuilder(writePath)
      bm.registerBuilder(readPath)

      val combineIncorrect = spy(lb<Pair<String, CPath>, String>("combineIncorrect", { "combine$it" }) { (text, path) ->
        requireBuild(a(indirection, a(writePath, Pair(text, path))))
        requireOutput(a(readPath, path))
      })
      bm.registerBuilder(combineIncorrect)

      val filePath1 = p(fs, "/file1")
      assertThrows(HiddenDependencyException::class.java) {
        bm.build(a(combineIncorrect, Pair("HELLO WORLD!", filePath1)))
      }

      val combineStillIncorrect = spy(lb<Pair<String, CPath>, String>("combineStillIncorrect", { "combine$it" }) { (text, path) ->
        requireBuild(a(indirection, a(writePath, Pair(text, path))))
        requireBuild(a(writePath, Pair(text, path)))
        requireOutput(a(readPath, path))
      })
      bm.registerBuilder(combineStillIncorrect)

      val filePath2 = p(fs, "/file2")
      assertThrows(HiddenDependencyException::class.java) {
        bm.build(a(combineStillIncorrect, Pair("HELLO WORLD!", filePath2)))
      }
    }
  }

  @UseBuildStores
  fun testCyclicDependency(store: Store, bStore: BuilderStore, share: BuildShare) {
    store.use {
      it.reset()
      val bm = BuildManagerImpl(store, bStore, share)

      val b1 = lb<None, None>("b1", { "b1" }) { requireOutput(a("b1", None.instance)) }
      bm.registerBuilder(b1)

      assertThrows(CyclicDependencyException::class.java) {
        bm.build(a(b1, None.instance))
      }
    }
  }
}