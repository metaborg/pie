package mb.ceres

import com.nhaarman.mockito_kotlin.*
import name.falgout.jeffrey.testing.junit5.GuiceExtension
import name.falgout.jeffrey.testing.junit5.IncludeModule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.charset.Charset
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.FileTime


@ExtendWith(GuiceExtension::class)
@IncludeModule(TestModule::class)
class BuildManagerTests {
  @Test
  fun testBuild(bm: BuildManagerImpl) {
    val sbm = spy(bm)
    val desc = "toLowerCase"
    val input = "CAPITALIZED"
    val id = "toLowerCase"
    val builder = spy(b<String, String>(id, desc) { it.toLowerCase() })
    sbm.registerBuilder(builder)
    val request = br(builder, input)

    val result = sbm.buildInternal(request)
    assertEquals(builder.id, result.builderId)
    assertEquals(input, result.input)
    assertEquals("capitalized", result.output)
    assertEquals(0, result.reqs.size)
    assertEquals(0, result.gens.size)

    inOrder(sbm, builder) {
      verify(sbm, times(1)).buildInternal(request)
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
    val id = "toLowerCase"
    val builder = spy(b<String, String>(id, { "toLowerCase($it)" }, { it.toLowerCase() }))
    sbm.registerBuilder(builder)
    val input1 = "CAPITALIZED"
    val request1 = br(builder, input1)
    val input2 = "CAPITALIZED_EVEN_MORE"
    val request2 = br(builder, input2)

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
    val id = "toLowerCase"
    val input = "CAPITALIZED"
    val builder = spy(b<String, String>(id, "toLowerCase") { it.toLowerCase() })
    bm.registerBuilder(builder)
    val request = br(builder, input)
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
    val readPath = spy(bc<CPath, String>("read", { "read($it)" }, { path, context ->
      context.require(path)
      String(Files.readAllBytes(path.javaPath), Charset.defaultCharset())
    }))
    bm.registerBuilder(readPath)

    val filePath = CPath(fs.getPath("/file"))
    Files.write(filePath.javaPath, "HELLO WORLD!".toByteArray(), StandardOpenOption.CREATE)

    // Build 'readPath', observe rebuild
    val sbm1 = spy(bm)
    val output1 = sbm1.build(BuildRequest(readPath, filePath))
    assertEquals("HELLO WORLD!", output1)
    verify(sbm1, times(1)).rebuild(BuildRequest(readPath, filePath))

    // No changes - build 'readPath', observe no rebuild
    val sbm2 = spy(bm)
    val output2 = sbm1.build(BuildRequest(readPath, filePath))
    assertEquals("HELLO WORLD!", output2)
    verify(sbm2, never()).rebuild(BuildRequest(readPath, filePath))

    // Change required file in such a way that the output of 'readPath' changes (change file content)
    Files.write(filePath.javaPath, "!DLROW OLLEH".toByteArray())

    // Run again - expect rebuild
    val sbm3 = spy(bm)
    val output3 = sbm3.build(BuildRequest(readPath, filePath))
    assertEquals("!DLROW OLLEH", output3)
    verify(sbm3, times(1)).rebuild(BuildRequest(readPath, filePath))
  }

  @Test
  fun testPathGen(bm: BuildManagerImpl, fs: FileSystem) {
    val writePath = spy(bc<Pair<String, CPath>, None>("write", { "write$it" }, { (text, path), context ->
      Files.write(path.javaPath, text.toByteArray(), StandardOpenOption.CREATE)
      context.generate(path)
      None.instance
    }))
    bm.registerBuilder(writePath)

    val filePath = CPath(fs.getPath("/file"))
    assertTrue(Files.notExists(filePath.javaPath))

    // Build 'writePath', observe rebuild and existence of file
    val sbm1 = spy(bm)
    sbm1.build(BuildRequest(writePath, Pair("HELLO WORLD!", filePath)))
    verify(sbm1, times(1)).rebuild(BuildRequest(writePath, Pair("HELLO WORLD!", filePath)))

    assertTrue(Files.exists(filePath.javaPath))
    assertEquals("HELLO WORLD!", String(Files.readAllBytes(filePath.javaPath), Charset.defaultCharset()))

    // No changes - build 'writePath', observe no rebuild, no change to file
    val sbm2 = spy(bm)
    sbm2.build(BuildRequest(writePath, Pair("HELLO WORLD!", filePath)))
    verify(sbm2, never()).rebuild(BuildRequest(writePath, Pair("HELLO WORLD!", filePath)))

    // Change generated file in such a way that 'writePath' is rebuilt (change file content)
    Files.write(filePath.javaPath, "!DLROW OLLEH".toByteArray())

    // Build 'writePath', observe rebuild and change of file
    val sbm3 = spy(bm)
    sbm3.build(BuildRequest(writePath, Pair("HELLO WORLD!", filePath)))
    verify(sbm3, times(1)).rebuild(BuildRequest(writePath, Pair("HELLO WORLD!", filePath)))

    assertEquals("HELLO WORLD!", String(Files.readAllBytes(filePath.javaPath), Charset.defaultCharset()))
  }

  @Test
  fun testBuildReq(bm: BuildManagerImpl, fs: FileSystem) {
    val toLowerCase = spy(b<String, String>("toLowerCase", { "toLowerCase($it)" }, { it.toLowerCase() }))
    bm.registerBuilder(toLowerCase)
    val readPath = spy(bc<CPath, String>("read", { "read($it)" }, { path, context ->
      context.require(path, ModifiedPathStamper())
      String(Files.readAllBytes(path.javaPath), Charset.defaultCharset())
    }))
    bm.registerBuilder(readPath)
    val combine = spy(bc<CPath, String>("combine", { "toLowerCase(read($it))" }, { path, context ->
      val text = context.require(BuildRequest(readPath, path))
      context.require(BuildRequest(toLowerCase, text))
    }))
    bm.registerBuilder(combine)

    val filePath = CPath(fs.getPath("/file"))
    Files.write(filePath.javaPath, "HELLO WORLD!".toByteArray(), StandardOpenOption.CREATE)

    // Build 'combine', observe rebuild of all
    val sbm1 = spy(bm)
    val output1 = sbm1.build(BuildRequest(combine, filePath))
    assertEquals("hello world!", output1)
    inOrder(sbm1) {
      verify(sbm1, times(1)).rebuild(BuildRequest(combine, filePath))
      verify(sbm1, times(1)).rebuild(BuildRequest(readPath, filePath))
      verify(sbm1, times(1)).rebuild(BuildRequest(toLowerCase, "HELLO WORLD!"))
    }

    // No changes - build 'combine', observe no rebuild
    val sbm2 = spy(bm)
    val output2 = sbm2.build(BuildRequest(combine, filePath))
    assertEquals("hello world!", output2)
    verify(sbm2, never()).rebuild(BuildRequest(combine, filePath))
    verify(sbm2, never()).rebuild(BuildRequest(readPath, filePath))
    verify(sbm2, never()).rebuild(BuildRequest(toLowerCase, "HELLO WORLD!"))

    // Change required file in such a way that the output of 'readPath' changes (change file content)
    Files.write(filePath.javaPath, "!DLROW OLLEH".toByteArray())

    // Build 'combine', observe rebuild of all in dependency order
    val sbm3 = spy(bm)
    val output3 = sbm3.build(BuildRequest(combine, filePath))
    assertEquals("!dlrow olleh", output3)
    inOrder(sbm3) {
      verify(sbm3, times(1)).require(BuildRequest(combine, filePath))
      verify(sbm3, times(1)).rebuild(BuildRequest(readPath, filePath))
      verify(sbm3, times(1)).rebuild(BuildRequest(combine, filePath))
      verify(sbm3, times(1)).rebuild(BuildRequest(toLowerCase, "!DLROW OLLEH"))
    }

    // Change required file in such a way that the output of 'readPath' does not change (change modification date)
    val lastModified = Files.getLastModifiedTime(filePath.javaPath)
    val newLastModified = FileTime.fromMillis(lastModified.toMillis() + 1)
    Files.setLastModifiedTime(filePath.javaPath, newLastModified)

    // Build 'combine', observe rebuild of 'readPath' only
    val sbm4 = spy(bm)
    val output4 = sbm4.build(BuildRequest(combine, filePath))
    assertEquals("!dlrow olleh", output4)
    inOrder(sbm4) {
      verify(sbm4, times(1)).require(BuildRequest(combine, filePath))
      verify(sbm4, times(1)).rebuild(BuildRequest(readPath, filePath))
    }
    verify(sbm4, never()).rebuild(BuildRequest(combine, filePath))
    verify(sbm4, never()).rebuild(BuildRequest(toLowerCase, "!DLROW OLLEH"))
  }


  fun p(fs: FileSystem, path: String): CPath {
    return CPath(fs.getPath(path))
  }


  fun <I : In, O : Out> b(id: String, desc: String, buildFunc: (I) -> O): Builder<I, O> {
    return SimpleLambdaBuilder(id, desc, buildFunc)
  }

  fun <I : In, O : Out> b(id: String, descFunc: (I) -> String, buildFunc: (I) -> O): Builder<I, O> {
    return LambdaBuilder(id, descFunc, { input, _ -> buildFunc(input) })
  }

  fun <I : In, O : Out> bc(id: String, descFunc: (I) -> String, buildFunc: (input: I, context: BuildContext) -> O): Builder<I, O> {
    return LambdaBuilder(id, descFunc, buildFunc)
  }


  fun <I : In, O : Out> br(builder: Builder<I, O>, input: I): BuildRequest<I, O> {
    return BuildRequest(builder.id, input)
  }
}