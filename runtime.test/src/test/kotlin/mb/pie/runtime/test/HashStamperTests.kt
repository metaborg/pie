package mb.pie.runtime.test

import mb.pie.api.stamp.resource.ResourceStampers
import mb.resource.fs.FSResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestFactory
import java.nio.charset.StandardCharsets

internal class HashStamperTests {
  @TestFactory
  fun testFileStampEqual1() = RuntimeTestGenerator.generate("testFileStampEqual1") {
    val file = resource("/file")
    write("Hello, world!", file)

    val stamper = ResourceStampers.hashFile<FSResource>()
    val stamp1 = stamper.stamp(file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testFileStampEqual2() = RuntimeTestGenerator.generate("testFileStampEqual2") {
    val file = resource("/file")
    write("Hello, world!", file)

    val stamper = ResourceStampers.hashFile<FSResource>()
    val stamp1 = stamper.stamp(file)
    write("Hello, world!", file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testFileStampUnequal() = RuntimeTestGenerator.generate("testFileStampUnequal") {
    val file = resource("/file")
    write("Hello, world 1!", file)

    val stamper = ResourceStampers.hashFile<FSResource>()
    val stamp1 = stamper.stamp(file)
    write("Hello, world 2!", file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertNotEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testNonExistentFileStampEqual() = RuntimeTestGenerator.generate("testNonExistentFileStampEqual") {
    val file = resource("/file")

    val stamper = ResourceStampers.hashFile<FSResource>()
    val stamp1 = stamper.stamp(file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testNonExistentAndExistentFileStampUnequal() = RuntimeTestGenerator.generate("testNonExistentAndExistentFileStampUnequal") {
    val file = resource("/file")
    
    val stamper = ResourceStampers.hashFile<FSResource>()
    val stamp1 = stamper.stamp(file)
    write("Hello, world!", file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertNotEquals(stamp1, stamp2)
  }


  @TestFactory
  fun testDirStampEqual() = RuntimeTestGenerator.generate("testDirStampEqual") {
    val dir = resource("/dir")
    dir.createDirectory()
    dir.appendSegment("file1").writeString("Hello, world 1!", StandardCharsets.UTF_8)
    dir.appendSegment("file2").writeString("Hello, world 2!", StandardCharsets.UTF_8)
    dir.appendSegment("file3").writeString("Hello, world 3!", StandardCharsets.UTF_8)

    val stamper = ResourceStampers.hashDir()
    val stamp1 = stamper.stamp(dir)
    val stamp2 = stamper.stamp(dir)
    Assertions.assertEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testDirStampFileChangedUnequal() = RuntimeTestGenerator.generate("testDirStampFileChangedUnequal") {
    val dir = resource("/dir")
    dir.createDirectory()
    dir.appendSegment("file1").writeString("Hello, world 1!", StandardCharsets.UTF_8)
    dir.appendSegment("file2").writeString("Hello, world 2!", StandardCharsets.UTF_8)
    val file = dir.appendSegment("file3")
    write("Hello, world 3!", file)

    val stamper = ResourceStampers.hashDir()
    val stamp1 = stamper.stamp(dir)
    write("Hello, world 3!!!!!", file)
    val stamp2 = stamper.stamp(dir)
    Assertions.assertNotEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testDirStampFileChangedFlipUnequal() = RuntimeTestGenerator.generate("testDirStampFileChangedFlipUnequal") {
    val dir = resource("/dir")
    dir.createDirectory()
    val file1 = dir.appendSegment("file1")
    write("Hello, world 1!", file1)
    val file2 = dir.appendSegment("file2")
    write("Hello, world 2!", file2)

    val stamper = ResourceStampers.hashDir()
    val stamp1 = stamper.stamp(dir)
    // Flip the contents of the files.
    write("Hello, world 2!", file1)
    write("Hello, world 1!", file2)
    val stamp2 = stamper.stamp(dir)
    Assertions.assertNotEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testDirStampFileDeletedUnequal() = RuntimeTestGenerator.generate("testDirStampFileDeletedUnequal") {
    val dir = resource("/dir")
    dir.createDirectory()
    dir.appendSegment("file1").writeString("Hello, world 1!", StandardCharsets.UTF_8)
    dir.appendSegment("file2").writeString("Hello, world 2!", StandardCharsets.UTF_8)
    val file = dir.appendSegment("file3")
    write("Hello, world 3!", file)

    val stamper = ResourceStampers.hashDir()
    val stamp1 = stamper.stamp(dir)
    file.delete(true)
    val stamp2 = stamper.stamp(dir)
    Assertions.assertNotEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testDirStampFileDeletedFlipUnequal() = RuntimeTestGenerator.generate("testDirStampFileDeletedFlipUnequal") {
    val dir = resource("/dir")
    dir.createDirectory()
    val file1 = dir.appendSegment("file1")
    write("Hello, world 1!", file1)
    val file2 = dir.appendSegment("file2")
    write("Hello, world 2!", file2)

    val stamper = ResourceStampers.hashDir()
    file1.delete(true)
    val stamp1 = stamper.stamp(dir)
    // Restore file1, then delete file2.
    write("Hello, world 1!", file1)
    file2.delete(true)
    val stamp2 = stamper.stamp(dir)
    Assertions.assertNotEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testDirStampFileAddedUnequal() = RuntimeTestGenerator.generate("testDirStampFileAddedUnequal") {
    val dir = resource("/dir")
    dir.createDirectory()
    dir.appendSegment("file1").writeString("Hello, world 1!", StandardCharsets.UTF_8)
    dir.appendSegment("file2").writeString("Hello, world 2!", StandardCharsets.UTF_8)
    val file = dir.appendSegment("file3")

    val stamper = ResourceStampers.hashDir()
    val stamp1 = stamper.stamp(dir)
    write("Hello, world 3!", file)
    val stamp2 = stamper.stamp(dir)
    Assertions.assertNotEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testDirStampDirDeletedUnequal() = RuntimeTestGenerator.generate("testDirStampDirDeletedUnequal") {
    val dir = resource("/dir")
    dir.createDirectory()
    dir.appendSegment("file1").writeString("Hello, world 1!", StandardCharsets.UTF_8)
    dir.appendSegment("file2").writeString("Hello, world 2!", StandardCharsets.UTF_8)
    dir.appendSegment("file3").writeString("Hello, world 3!", StandardCharsets.UTF_8)

    val stamper = ResourceStampers.hashDir()
    val stamp1 = stamper.stamp(dir)
    dir.delete(true)
    val stamp2 = stamper.stamp(dir)
    Assertions.assertNotEquals(stamp1, stamp2)
  }
}
