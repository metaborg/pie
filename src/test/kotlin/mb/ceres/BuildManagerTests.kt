package mb.ceres

import com.nhaarman.mockito_kotlin.*
import name.falgout.jeffrey.testing.junit5.GuiceExtension
import name.falgout.jeffrey.testing.junit5.IncludeModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.FileSystem
import java.nio.file.Files


@ExtendWith(GuiceExtension::class)
@IncludeModule(TestModule::class)
class BuildManagerTests {
  @Test
  fun testSingleBuild(bm: BuildManagerImpl, fs: FileSystem) {
    val sbm = spy(bm)
    val desc = "toLowerCase"
    val path = p(fs, "/toLowerCase")
    val input = "CAPITALIZED"
    val id = "toLowerCase"
    val builder = spy(b<String, String>(id, desc, path) { it.toLowerCase() })
    sbm.registerBuilder(builder)
    val request = br(builder, input)

    assertTrue(Files.notExists(path.javaPath))

    val results = sbm.build(request)
    assertEquals(1, results.size)

    assertTrue(Files.exists(path.javaPath))

    val result = results[0]
    assertEquals(builder.id, result.builderId)
    assertEquals(input, result.input)
    assertEquals("capitalized", result.output)
    assertEquals(0, result.reqs.size)
    assertEquals(0, result.gens.size)

    inOrder(sbm, builder) {
      verify(sbm, times(1)).build(request)
      verify(sbm, times(1)).require(request)
      verify(sbm, times(1)).readResult<String, String>(path)
      verify(sbm, times(1)).rebuild(request)
      verify(builder, times(1)).build(eq(input), anyOrNull())
      verify(sbm, times(1)).writeResult(result, path)
      verify(sbm, times(1)).validate(result, path)
    }

    verify(builder, atLeastOnce()).desc(input)
    verify(builder, atLeastOnce()).path(input)
  }

  @Test
  fun testMultipleBuilds(bm: BuildManagerImpl, fs: FileSystem) {
    val sbm = spy(bm)
    val id = "toLowerCase"
    val builder = spy(b<String, String>(id, { "toLowerCase($it)" }, { p(fs, "/toLowerCase/$it") }, { it.toLowerCase() }))
    sbm.registerBuilder(builder)
    val input1 = "CAPITALIZED"
    val expectedPath1 = p(fs, "/toLowerCase/$input1")
    val request1 = br(builder, input1)
    val input2 = "CAPITALIZED_EVEN_MORE"
    val expectedPath2 = p(fs, "/toLowerCase/$input2")
    val request2 = br(builder, input2)

    assertTrue(Files.notExists(expectedPath1.javaPath))
    assertTrue(Files.notExists(expectedPath2.javaPath))

    val results = sbm.build(request1, request2)
    assertEquals(2, results.size)

    assertTrue(Files.exists(expectedPath1.javaPath))
    assertTrue(Files.exists(expectedPath2.javaPath))

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
      verify(sbm, times(1)).build(request1, request2)

      verify(sbm, times(1)).require(request1)
      verify(sbm, times(1)).readResult<String, String>(expectedPath1)
      verify(sbm, times(1)).rebuild(request1)
      verify(builder, times(1)).build(eq(input1), anyOrNull())
      verify(sbm, times(1)).writeResult(result1, expectedPath1)
      verify(sbm, times(1)).validate(result1, expectedPath1)

      verify(sbm, times(1)).require(request2)
      verify(sbm, times(1)).readResult<String, String>(expectedPath2)
      verify(sbm, times(1)).rebuild(request2)
      verify(builder, times(1)).build(eq(input2), anyOrNull())
      verify(sbm, times(1)).writeResult(result2, expectedPath2)
      verify(sbm, times(1)).validate(result2, expectedPath2)
    }
  }

  @Test
  fun testReuseResult(bm: BuildManagerImpl, fs: FileSystem) {
    val id = "toLowerCase"
    val input = "CAPITALIZED"
    val path = p(fs, "/toLowerCase")
    val builder = spy(b<String, String>(id, "toLowerCase", path) { it.toLowerCase() })
    bm.registerBuilder(builder)
    val request = br(builder, input)
    val result1 = bm.build(request)[0]

    val sbm = spy(bm)
    val result2 = sbm.build(request)[0]

    assertEquals(result1, result2)

    inOrder(sbm, builder) {
      verify(sbm, times(1)).build(request)
      verify(sbm, times(1)).require(request)
      verify(sbm, times(1)).readResult<String, String>(path)
    }

    verify(sbm, never()).rebuild(request)
    verify(builder, atMost(1)).build(eq(input), anyOrNull())
    verify(sbm, never()).writeResult(result2, path)
    verify(sbm, never()).validate(result2, path)

    verify(builder, atLeastOnce()).desc(input)
    verify(builder, atLeastOnce()).path(input)
  }


  fun p(fs: FileSystem, path: String): CPath {
    return CPath(fs.getPath(path))
  }


  fun <I : In, O : Out> b(id: String, desc: String, path: CPath, buildFunc: (I) -> O): Builder<I, O> {
    return SimpleLambdaBuilder(id, desc, path, buildFunc)
  }

  fun <I : In, O : Out> b(id: String, descFunc: (I) -> String, pathFunc: (I) -> CPath, buildFunc: (I) -> O): Builder<I, O> {
    return LambdaBuilder(id, descFunc, pathFunc, { input, _ -> buildFunc(input) })
  }

  fun <I : In, O : Out> bc(id: String, descFunc: (I) -> String, pathFunc: (I) -> CPath, buildFunc: (input: I, buildContext: BuildContext) -> O): Builder<I, O> {
    return LambdaBuilder(id, descFunc, pathFunc, buildFunc)
  }


  fun <I : In, O : Out> br(builder: Builder<I, O>, input: I): BuildRequest<I, O> {
    return BuildRequest(builder.id, input)
  }
}