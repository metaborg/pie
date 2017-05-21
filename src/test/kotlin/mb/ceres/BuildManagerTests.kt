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
    val name = "toLowerCase"
    val path = p(fs, "/toLowerCase")
    val input = "CAPITALIZED"
    val builder = spy(b<String, String>(name, path) { it.toLowerCase() })
    val request = br(builder, input)

    assertTrue(Files.notExists(path.javaPath))

    val results = sbm.build(request)

    assertTrue(Files.exists(path.javaPath))

    assertEquals(1, results.size)
    val result = results[0]
    assertSame(builder, result.builder)
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

    verify(builder, atLeastOnce()).name(input)
    verify(builder, atLeastOnce()).path(input)
  }

  @Test
  fun testReuseResult(bm: BuildManagerImpl, fs: FileSystem) {
    val input = "CAPITALIZED"
    val path = p(fs, "/toLowerCase")
    val builder = spy(b<String, String>("toLowerCase", path) { it.toLowerCase() })
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

    verify(builder, atLeastOnce()).name(input)
    verify(builder, atLeastOnce()).path(input)
  }


  fun p(fs: FileSystem, path: String): CPath {
    return CPath(fs.getPath(path))
  }


  fun <I, O> b(name: String, path: CPath, build: (I) -> O): Builder<I, O> {
    return SimpleLambdaBuilder(name, path, build)
  }

  fun <I, O> b(name: (I) -> String, path: (I) -> CPath, build: (I) -> O): Builder<I, O> {
    return LambdaBuilder(name, path, { input, _ -> build(input) })
  }

  fun <I, O> b(name: (I) -> String, path: (I) -> CPath, build: (input: I, buildContext: BuildContext) -> O): Builder<I, O> {
    return LambdaBuilder(name, path, build)
  }


  fun <I, O> br(builder: Builder<I, O>, input: I): BuildRequest<I, O> {
    return BuildRequest(builder, input)
  }


  // Clean builds
  /// Simple build
  //// Do build
  ///// Input/output - require that build was executed with input and produced output
  ///// Multiple different inputs/outputs - require that builds were executed with different intputs and outputs

}