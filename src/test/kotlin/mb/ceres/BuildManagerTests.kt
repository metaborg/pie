package mb.ceres

import com.nhaarman.mockito_kotlin.*
import name.falgout.jeffrey.testing.junit5.GuiceExtension
import name.falgout.jeffrey.testing.junit5.IncludeModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.FileSystem


@ExtendWith(GuiceExtension::class)
@IncludeModule(TestModule::class)
class BuildManagerTests {
  @Test
  fun testSingleBuild(bm: BuildManagerImpl) {
    val sbm = spy(bm)
    val desc = "toLowerCase"
    val input = "CAPITALIZED"
    val id = "toLowerCase"
    val builder = spy(b<String, String>(id, desc) { it.toLowerCase() })
    sbm.registerBuilder(builder)
    val request = br(builder, input)

    val results = sbm.build(request)
    assertEquals(1, results.size)

    val result = results[0]
    assertEquals(builder.id, result.builderId)
    assertEquals(input, result.input)
    assertEquals("capitalized", result.output)
    assertEquals(0, result.reqs.size)
    assertEquals(0, result.gens.size)

    inOrder(sbm, builder) {
      verify(sbm, times(1)).build(request)
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

    val results = sbm.build(request1, request2)
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
      verify(sbm, times(1)).build(request1, request2)

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
  fun testReuseResult(bm: BuildManagerImpl) {
    val id = "toLowerCase"
    val input = "CAPITALIZED"
    val builder = spy(b<String, String>(id, "toLowerCase") { it.toLowerCase() })
    bm.registerBuilder(builder)
    val request = br(builder, input)
    val result1 = bm.build(request)[0]

    val sbm = spy(bm)
    val result2 = sbm.build(request)[0]

    assertEquals(result1, result2)

    // Result is reused if rebuild is never called
    verify(sbm, never()).rebuild(request)

    verify(builder, atMost(1)).build(eq(input), anyOrNull())
    verify(builder, atLeastOnce()).desc(input)
  }

  fun testRequirementsIncrementality() {
    // Setup:
    // Builder A, requires builder B, which requires file F
    // Run:
    // Build A - observe rebuild of A and B
    // Build A again unchanged - observe no rebuilds
    // Change required file F in such a way that its output changes, build A again - observe rebuild of A and B
    // Change required file F in such aw ay that its output does not change, build A again - observe rebuild of B only
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