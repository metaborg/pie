package mb.ceres

import com.nhaarman.mockito_kotlin.*
import mb.ceres.internal.BuildManagerImpl
import name.falgout.jeffrey.testing.junit5.GuiceExtension
import name.falgout.jeffrey.testing.junit5.IncludeModule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.FileTime

@ExtendWith(GuiceExtension::class)
@IncludeModule(TestModule::class)
internal class BuildManagerTests {
  @Test
  fun testBuild(bm: BuildManagerImpl) {
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

  @Test
  fun testMultipleBuilds(bm: BuildManagerImpl) {
    val sbm = spy(bm)
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

  @Test
  fun testReuse(bm: BuildManagerImpl) {
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

  @Test
  fun testPathReq(bm: BuildManagerImpl, fs: FileSystem) {
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

  @Test
  fun testPathGen(bm: BuildManagerImpl, fs: FileSystem) {
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

  @Test
  fun testBuildReq(bm: BuildManagerImpl, fs: FileSystem) {
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

  @Test
  fun testOverlappingGeneratedPath(bm: BuildManagerImpl, fs: FileSystem) {
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

  @Test
  fun testGenerateRequiredHiddenDep(bm: BuildManagerImpl, fs: FileSystem) {
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

  @Test
  fun testRequireGeneratedHiddenDep(bm: BuildManagerImpl, fs: FileSystem) {
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

  @Test
  fun testCyclicDependency(bm: BuildManagerImpl) {
    val b1 = b<None, None>("b1", { "b1" }, { requireOutput(BuildApp("b1", None.instance)) })
    bm.registerBuilder(b1)

    assertThrows(CyclicDependencyException::class.java) {
      bm.build(BuildApp(b1, None.instance))
    }
  }


  fun p(fs: FileSystem, path: String): CPath {
    return CPath(fs.getPath(path))
  }

  fun <I : In, O : Out> b(id: String, descFunc: (I) -> String, buildFunc: BuildContext.(I) -> O): Builder<I, O> {
    return LambdaBuilder(id, descFunc, buildFunc)
  }

  fun <I : In, O : Out> a(builder: Builder<I, O>, input: I): BuildApp<I, O> {
    return BuildApp(builder.id, input)
  }


  fun read(path: CPath): String {
    Files.newInputStream(path.javaPath, StandardOpenOption.READ).use {
      val bytes = it.readBytes()
      val text = String(bytes)
      return text
    }
  }

  fun write(text: String, path: CPath) {
    println("Write $text")
    Files.newOutputStream(path.javaPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.SYNC).use {
      it.write(text.toByteArray())
    }
    // HACK: for some reason, sleeping is required for writes to the file to be picked up by reads...
    Thread.sleep(1)
  }


  val toLowerCase = b<String, String>("toLowerCase", { "toLowerCase($it)" }) {
    it.toLowerCase()
  }
  val readPath = b<CPath, String>("read", { "read($it)" }) {
    require(it)
    read(it)
  }
  val writePath = b<Pair<String, CPath>, None>("write", { "write$it" }) { (text, path) ->
    write(text, path)
    generate(path)
    None.instance
  }

  inline fun <reified I : In, reified O : Out> requireOutputBuilder(): Builder<BuildApp<I, O>, O> {
    return b("requireOutput(${I::class}):${O::class}", { "requireOutput($it)" }) {
      requireOutput(it)
    }
  }
}