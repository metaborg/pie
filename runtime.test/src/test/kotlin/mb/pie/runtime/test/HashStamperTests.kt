package mb.pie.runtime.test

import mb.pie.api.stamp.resource.ResourceStampers
import mb.resource.fs.FSResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestFactory

internal class HashStamperTests {
  @TestFactory
  fun testFileStampEqual1() = RuntimeTestGenerator.generate("testFileStampEqual1") {
    val stamper = ResourceStampers.hash<FSResource>()
    val file = resource("/file")
    write("Hello, world!", file)
    val stamp1 = stamper.stamp(file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testFileStampEqual2() = RuntimeTestGenerator.generate("testFileStampEqual2") {
    val stamper = ResourceStampers.hash<FSResource>()
    val file = resource("/file")
    write("Hello, world!", file)
    val stamp1 = stamper.stamp(file)
    write("Hello, world!", file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testFileStampUnequal() = RuntimeTestGenerator.generate("testFileStampUnequal") {
    val stamper = ResourceStampers.hash<FSResource>()
    val file = resource("/file")
    write("Hello, world 1!", file)
    val stamp1 = stamper.stamp(file)
    write("Hello, world 2!", file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertNotEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testNonExistentFileStampEqual() = RuntimeTestGenerator.generate("testNonExistentFileStampEqual") {
    val stamper = ResourceStampers.hash<FSResource>()
    val file = resource("/file")
    val stamp1 = stamper.stamp(file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testNonExistentAndExistentFileStampUnequal() = RuntimeTestGenerator.generate("testNonExistentAndExistentFileStampUnequal") {
    val stamper = ResourceStampers.hash<FSResource>()
    val file = resource("/file")
    val stamp1 = stamper.stamp(file)
    write("Hello, world!", file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertNotEquals(stamp1, stamp2)
  }
}
