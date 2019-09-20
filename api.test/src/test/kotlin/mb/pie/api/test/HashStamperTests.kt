package mb.pie.api.test

import mb.pie.api.stamp.resource.ResourceStampers
import mb.resource.fs.FSResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestFactory
import java.nio.charset.StandardCharsets

class HashStamperTests {
  private val builder = TestBuilder()


  @TestFactory
  fun testFileStampEqual1() = builder.build("testFileStampEqual1") {
    val file = resource("/file")
    write("Hello, world!", file)

    val stamper = ResourceStampers.hashFile<FSResource>()
    val stamp1 = stamper.stamp(file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testFileStampEqual2() = builder.build("testFileStampEqual2") {
    val file = resource("/file")
    write("Hello, world!", file)

    val stamper = ResourceStampers.hashFile<FSResource>()
    val stamp1 = stamper.stamp(file)
    write("Hello, world!", file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testFileStampUnequal() = builder.build("testFileStampUnequal") {
    val file = resource("/file")
    write("Hello, world 1!", file)

    val stamper = ResourceStampers.hashFile<FSResource>()
    val stamp1 = stamper.stamp(file)
    write("Hello, world 2!", file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertNotEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testNonExistentFileStampEqual() = builder.build("testNonExistentFileStampEqual") {
    val file = resource("/file")

    val stamper = ResourceStampers.hashFile<FSResource>()
    val stamp1 = stamper.stamp(file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testNonExistentAndExistentFileStampUnequal() = builder.build("testNonExistentAndExistentFileStampUnequal") {
    val file = resource("/file")

    val stamper = ResourceStampers.hashFile<FSResource>()
    val stamp1 = stamper.stamp(file)
    write("Hello, world!", file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertNotEquals(stamp1, stamp2)
  }


  @TestFactory
  fun testDirStampEqual() = builder.build("testDirStampEqual") {
    val dir = resource("/dir")
    dir.createDirectory()
    dir.appendSegment("file1").writeString("Hello, world 1!")
    dir.appendSegment("file2").writeString("Hello, world 2!")
    dir.appendSegment("file3").writeString("Hello, world 3!")

    val stamper = ResourceStampers.hashDir()
    val stamp1 = stamper.stamp(dir)
    val stamp2 = stamper.stamp(dir)
    Assertions.assertEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testDirStampFileChangedUnequal() = builder.build("testDirStampFileChangedUnequal") {
    val dir = resource("/dir")
    dir.createDirectory()
    dir.appendSegment("file1").writeString("Hello, world 1!")
    dir.appendSegment("file2").writeString("Hello, world 2!")
    val file = dir.appendSegment("file3")
    write("Hello, world 3!", file)

    val stamper = ResourceStampers.hashDir()
    val stamp1 = stamper.stamp(dir)
    write("Hello, world 3!!!!!", file)
    val stamp2 = stamper.stamp(dir)
    Assertions.assertNotEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testDirStampFileChangedFlipUnequal() = builder.build("testDirStampFileChangedFlipUnequal") {
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
  fun testDirStampFileDeletedUnequal() = builder.build("testDirStampFileDeletedUnequal") {
    val dir = resource("/dir")
    dir.createDirectory()
    dir.appendSegment("file1").writeString("Hello, world 1!")
    dir.appendSegment("file2").writeString("Hello, world 2!")
    val file = dir.appendSegment("file3")
    write("Hello, world 3!", file)

    val stamper = ResourceStampers.hashDir()
    val stamp1 = stamper.stamp(dir)
    file.delete(true)
    val stamp2 = stamper.stamp(dir)
    Assertions.assertNotEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testDirStampFileDeletedFlipUnequal() = builder.build("testDirStampFileDeletedFlipUnequal") {
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
  fun testDirStampFileAddedUnequal() = builder.build("testDirStampFileAddedUnequal") {
    val dir = resource("/dir")
    dir.createDirectory()
    dir.appendSegment("file1").writeString("Hello, world 1!")
    dir.appendSegment("file2").writeString("Hello, world 2!")
    val file = dir.appendSegment("file3")

    val stamper = ResourceStampers.hashDir()
    val stamp1 = stamper.stamp(dir)
    write("Hello, world 3!", file)
    val stamp2 = stamper.stamp(dir)
    Assertions.assertNotEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testDirStampDirDeletedUnequal() = builder.build("testDirStampDirDeletedUnequal") {
    val dir = resource("/dir")
    dir.createDirectory()
    dir.appendSegment("file1").writeString("Hello, world 1!")
    dir.appendSegment("file2").writeString("Hello, world 2!")
    dir.appendSegment("file3").writeString("Hello, world 3!")

    val stamper = ResourceStampers.hashDir()
    val stamp1 = stamper.stamp(dir)
    dir.delete(true)
    val stamp2 = stamper.stamp(dir)
    Assertions.assertNotEquals(stamp1, stamp2)
  }

  // TODO: tests for ResourceStampers.hashDir(ResourceMatcher)

  // TODO: tests for ResourceStampers.hashDirRec()

  // TODO: tests for ResourceStampers.hashDirRec(ResourceWalker, ResourceMatcher)
}
