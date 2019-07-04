package mb.pie.runtime.test

import mb.pie.api.stamp.resource.FileSystemStampers
import mb.resource.fs.FSResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestFactory

internal class ModifiedStamperTests {
  @TestFactory
  fun testFileStampEqual() = RuntimeTestGenerator.generate("testFileStampEqual") {
    val stamper = FileSystemStampers.modified<FSResource>()
    val file = resource("/file")
    write("Hello, world!", file)
    val stamp1 = stamper.stamp(file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testFileStampUnequal() = RuntimeTestGenerator.generate("testFileStampUnequal") {
    val stamper = FileSystemStampers.modified<FSResource>()
    val file = resource("/file")
    write("Hello, world!", file)
    val stamp1 = stamper.stamp(file)
    write("Hello, world!", file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertNotEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testNonExistentFileStampEqual() = RuntimeTestGenerator.generate("testNonExistentFileStampEqual") {
    val stamper = FileSystemStampers.modified<FSResource>()
    val file = resource("/file")
    val stamp1 = stamper.stamp(file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testNonExistentAndExistentFileStampUnequal() = RuntimeTestGenerator.generate("testNonExistentAndExistentFileStampUnequal") {
    val stamper = FileSystemStampers.modified<FSResource>()
    val file = resource("/file")
    val stamp1 = stamper.stamp(file)
    write("Hello, world!", file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertNotEquals(stamp1, stamp2)
  }
}
