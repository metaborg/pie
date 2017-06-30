package mb.ceres

import com.nhaarman.mockito_kotlin.*
import org.junit.jupiter.api.Assertions.*
import java.nio.file.Files
import java.nio.file.attribute.FileTime

internal class BuildManagerTests : ParametrizedTestBase() {
  @UseBuildVariability
  fun testBuild() {
    val input = "CAPITALIZED"
    val builder = spy(toLowerCase)
    registerBuilder(builder)
    val app = a(builder, input)

    val build = spy(b())
    val info = build.require(app)
    assertEquals(NoResultReason(), info.reason)
    val result = info.result
    assertEquals(builder.id, result.builderId)
    assertEquals(input, result.input)
    assertEquals("capitalized", result.output)
    assertEquals(0, result.reqs.size)
    assertEquals(0, result.gens.size)

    inOrder(build, builder) {
      verify(build, times(1)).require(eq(app))
      verify(build, times(1)).rebuild(eq(app), eq(NoResultReason()), any())
      verify(builder, times(1)).build(eq(input), anyOrNull())
    }

    verify(builder, atLeastOnce()).desc(input)
  }

  @UseBuildVariability
  fun testMultipleBuilds() {
    val builder = spy(toLowerCase)
    registerBuilder(builder)

    val input1 = "CAPITALIZED"
    val app1 = a(builder, input1)
    val build1 = spy(b())
    val info1 = build1.require(app1)
    assertEquals(NoResultReason(), info1.reason)
    val result1 = info1.result
    assertEquals(builder.id, result1.builderId)
    assertEquals(input1, result1.input)
    assertEquals("capitalized", result1.output)

    val input2 = "CAPITALIZED_EVEN_MORE"
    val app2 = a(builder, input2)
    val build2 = spy(b())
    val info2 = build2.require(app2)
    assertEquals(NoResultReason(), info2.reason)
    val result2 = info2.result
    assertEquals(builder.id, result2.builderId)
    assertEquals(input2, result2.input)
    assertEquals("capitalized_even_more", result2.output)

    assertNotEquals(result1, result2)

    inOrder(builder, build1, build2) {
      verify(build1, times(1)).require(eq(app1))
      verify(build1, times(1)).rebuild(eq(app1), eq(NoResultReason()), any())
      verify(builder, times(1)).build(eq(input1), anyOrNull())

      verify(build2, times(1)).require(eq(app2))
      verify(build2, times(1)).rebuild(eq(app2), eq(NoResultReason()), any())
      verify(builder, times(1)).build(eq(input2), anyOrNull())
    }
  }

  @UseBuildVariability
  fun testReuse() {
    val builder = spy(toLowerCase)
    registerBuilder(builder)

    val input = "CAPITALIZED"
    val app = a(builder, input)
    val build1 = b()
    val info1 = build1.require(app)
    assertEquals(NoResultReason(), info1.reason)
    val result1 = info1.result

    val build2 = spy(b())
    val info2 = build2.require(app)
    assertNull(info2.reason)
    val result2 = info2.result

    assertEquals(result1, result2)

    // Result is reused if rebuild is never called
    verify(build2, never()).rebuild(eq(app), eq(NoResultReason()), any())

    verify(builder, atMost(1)).build(eq(input), anyOrNull())
    verify(builder, atLeastOnce()).desc(input)
  }

  @UseBuildVariability
  fun testPathReq() {
    val readPath = spy(readPath)
    registerBuilder(readPath)

    val filePath = p(fs, "/file")
    write("HELLO WORLD!", filePath)

    // Build 'readPath', observe rebuild
    val build1 = spy(b())
    val result1 = build1.require(a(readPath, filePath)).result
    assertEquals("HELLO WORLD!", result1.output)
    verify(build1, times(1)).rebuild(eq(a(readPath, filePath)), eq(NoResultReason()), any())

    // No changes - build 'readPath', observe no rebuild
    val build2 = spy(b())
    val result2 = build2.require(a(readPath, filePath)).result
    assertEquals("HELLO WORLD!", result2.output)
    verify(build2, never()).rebuild(eq(a(readPath, filePath)), any(), any())

    // Change required file in such a way that the output of 'readPath' changes (change file content)
    write("!DLROW OLLEH", filePath)

    // Run again - expect rebuild
    val build3 = spy(b())
    val result3 = build3.require(a(readPath, filePath)).result
    assertEquals("!DLROW OLLEH", result3.output)
    verify(build3, times(1)).rebuild(eq(a(readPath, filePath)), check {
      val reason = it as? InconsistentPathReq<*, *>
      assertNotNull(reason)
      assertEquals(filePath, reason!!.req.path)
    }, any())
  }

  @UseBuildVariability
  fun testPathGen() {
    val writePath = spy(writePath)
    registerBuilder(writePath)

    val filePath = p(fs, "/file")
    assertTrue(Files.notExists(filePath.javaPath))

    // Build 'writePath', observe rebuild and existence of file
    val build1 = spy(b())
    build1.require(a(writePath, Pair("HELLO WORLD!", filePath)))
    verify(build1, times(1)).rebuild(eq(a(writePath, Pair("HELLO WORLD!", filePath))), eq(NoResultReason()), any())

    assertTrue(Files.exists(filePath.javaPath))
    assertEquals("HELLO WORLD!", read(filePath))

    // No changes - build 'writePath', observe no rebuild, no change to file
    val build2 = spy(b())
    build2.require(a(writePath, Pair("HELLO WORLD!", filePath)))
    verify(build2, never()).rebuild(eq(a(writePath, Pair("HELLO WORLD!", filePath))), any(), any())

    // Change generated file in such a way that 'writePath' is rebuilt (change file content)
    write("!DLROW OLLEH", filePath)

    // Build 'writePath', observe rebuild and change of file
    val build3 = spy(b())
    build3.require(a(writePath, Pair("HELLO WORLD!", filePath)))
    verify(build3, times(1)).rebuild(eq(a(writePath, Pair("HELLO WORLD!", filePath))), check {
      val reason = it as? InconsistentGenPath<*, *>
      assertNotNull(reason)
      assertEquals(filePath, reason!!.gen.path)
    }, any())

    assertEquals("HELLO WORLD!", read(filePath))
  }

  @UseBuildVariability
  fun testBuildReq() {
    val toLowerCase = spy(toLowerCase)
    registerBuilder(toLowerCase)
    val readPath = spy(readPath)
    registerBuilder(readPath)
    val combine = spy(lb<CPath, String>("combine", { "toLowerCase(read($it))" }) {
      val text = requireOutput(a(readPath, it))
      requireOutput(a(toLowerCase, text))
    })
    registerBuilder(combine)

    val filePath = p(fs, "/file")
    write("HELLO WORLD!", filePath)

    // Build 'combine', observe rebuild of all
    val build1 = spy(b())
    val result1 = build1.require(a(combine, filePath)).result
    assertEquals("hello world!", result1.output)
    inOrder(build1) {
      verify(build1, times(1)).rebuild(eq(a(combine, filePath)), eq(NoResultReason()), any())
      verify(build1, times(1)).rebuild(eq(a(readPath, filePath)), eq(NoResultReason()), any())
      verify(build1, times(1)).rebuild(eq(a(toLowerCase, "HELLO WORLD!")), eq(NoResultReason()), any())
    }

    // No changes - build 'combine', observe no rebuild
    val build2 = spy(b())
    val result2 = build2.require(a(combine, filePath)).result
    assertEquals("hello world!", result2.output)
    verify(build2, never()).rebuild(eq(a(combine, filePath)), any(), any())
    verify(build2, never()).rebuild(eq(a(readPath, filePath)), any(), any())
    verify(build2, never()).rebuild(eq(a(toLowerCase, "HELLO WORLD!")), any(), any())

    // Change required file in such a way that the output of 'readPath' changes (change file content)
    write("!DLROW OLLEH", filePath)

    // Build 'combine', observe rebuild of all in dependency order
    val build3 = spy(b())
    val result3 = build3.require(a(combine, filePath)).result
    assertEquals("!dlrow olleh", result3.output)
    inOrder(build3) {
      verify(build3, times(1)).require(a(combine, filePath))
      verify(build3, times(1)).rebuild(eq(a(readPath, filePath)), check {
        val reason = it as? InconsistentPathReq<*, *>
        assertNotNull(reason)
        assertEquals(filePath, reason!!.req.path)
      }, any())
      verify(build3, times(1)).rebuild(eq(a(combine, filePath)), check {
        val reason = it as? InconsistentBuildReq<*, *>
        assertNotNull(reason)
        assertEquals(a(readPath, filePath), reason!!.req.app)
      }, any())
      verify(build3, times(1)).rebuild(eq(a(toLowerCase, "!DLROW OLLEH")), eq(NoResultReason()), any())
    }

    // Change required file in such a way that the output of 'readPath' does not change (change modification date)
    val lastModified = Files.getLastModifiedTime(filePath.javaPath)
    val newLastModified = FileTime.fromMillis(lastModified.toMillis() + 1)
    Files.setLastModifiedTime(filePath.javaPath, newLastModified)

    // Build 'combine', observe rebuild of 'readPath' only
    val build4 = spy(b())
    val result4 = build4.require(a(combine, filePath)).result
    assertEquals("!dlrow olleh", result4.output)
    inOrder(build4) {
      verify(build4, times(1)).require(a(combine, filePath))
      verify(build4, times(1)).rebuild(eq(a(readPath, filePath)), check {
        val reason = it as? InconsistentPathReq<*, *>
        assertNotNull(reason)
        assertEquals(filePath, reason!!.req.path)
      }, any())
    }
    verify(build4, never()).rebuild(eq(a(combine, filePath)), any(), any())
    verify(build4, never()).rebuild(eq(a(toLowerCase, "!DLROW OLLEH")), any(), any())
  }

  @UseBuildVariability
  fun testOverlappingGeneratedPath() {
    registerBuilder(writePath)

    val bm = bm()

    val filePath = p(fs, "/file")
    assertThrows(OverlappingGeneratedPathException::class.java) {
      bm.buildAll(a(writePath, Pair("HELLO WORLD 1!", filePath)), a(writePath, Pair("HELLO WORLD 2!", filePath)))
    }

    // Overlapping generated path exception should also trigger between separate builds
    assertThrows(OverlappingGeneratedPathException::class.java) {
      bm.build(a(writePath, Pair("HELLO WORLD 3!", filePath)))
    }
  }

  @UseBuildVariability
  fun testGenerateRequiredHiddenDep() {
    registerBuilder(readPath)
    registerBuilder(writePath)

    val bm = bm()


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

  @UseBuildVariability
  fun testRequireGeneratedHiddenDep() {
    registerBuilder(writePath)
    registerBuilder(readPath)
    val indirection = requireOutputBuilder<Pair<String, CPath>, None>()
    registerBuilder(indirection)

    val bm = bm()

    val combineIncorrect = spy(lb<Pair<String, CPath>, String>("combineIncorrect", { "combine$it" }) { (text, path) ->
      requireBuild(a(indirection, a(writePath, Pair(text, path))))
      requireOutput(a(readPath, path))
    })
    registerBuilder(combineIncorrect)

    val filePath1 = p(fs, "/file1")
    assertThrows(HiddenDependencyException::class.java) {
      bm.build(a(combineIncorrect, Pair("HELLO WORLD!", filePath1)))
    }

    val combineStillIncorrect = spy(lb<Pair<String, CPath>, String>("combineStillIncorrect", { "combine$it" }) { (text, path) ->
      requireBuild(a(indirection, a(writePath, Pair(text, path))))
      requireBuild(a(writePath, Pair(text, path)))
      requireOutput(a(readPath, path))
    })
    registerBuilder(combineStillIncorrect)

    val filePath2 = p(fs, "/file2")
    assertThrows(HiddenDependencyException::class.java) {
      bm.build(a(combineStillIncorrect, Pair("HELLO WORLD!", filePath2)))
    }
  }

  @UseBuildVariability
  fun testCyclicDependency() {
    val b1 = lb<None, None>("b1", { "b1" }) { requireOutput(a("b1", None.instance)) }
    registerBuilder(b1)

    val bm = bm()

    assertThrows(CyclicDependencyException::class.java) {
      bm.build(a(b1, None.instance))
    }
  }
}