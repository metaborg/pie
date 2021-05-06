package mb.pie.api

import mb.pie.api.stamp.resource.ResourceStampers
import mb.pie.api.test.TestBuilder
import mb.resource.fs.FSResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestFactory

class ModifiedStamperTests {
  private val builder = TestBuilder()


  @TestFactory
  fun testFileStampEqual() = builder.build("testFileStampEqual") {
    val file = resource("/file")
    write("Hello, world!", file)

    val stamper = ResourceStampers.modifiedFile<FSResource>()
    val stamp1 = stamper.stamp(file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testFileStampUnequal() = builder.build("testFileStampUnequal") {
    val file = resource("/file")
    write("Hello, world!", file)

    val stamper = ResourceStampers.modifiedFile<FSResource>()
    val stamp1 = stamper.stamp(file)
    write("Hello, world!", file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertNotEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testNonExistentFileStampEqual() = builder.build("testNonExistentFileStampEqual") {
    val file = resource("/file")

    val stamper = ResourceStampers.modifiedFile<FSResource>()
    val stamp1 = stamper.stamp(file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testNonExistentAndExistentFileStampUnequal() = builder.build("testNonExistentAndExistentFileStampUnequal") {
    val file = resource("/file")

    val stamper = ResourceStampers.modifiedFile<FSResource>()
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

    val stamper = ResourceStampers.modifiedDir()
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

    val stamper = ResourceStampers.modifiedDir()
    val stamp1 = stamper.stamp(dir)
    write("Hello, world 3!", file)
    val stamp2 = stamper.stamp(dir)
    Assertions.assertNotEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testDirStampFileChangedFlipEqual() = builder.build("testDirStampFileChangedFlipEqual") {
    val dir = resource("/dir")
    dir.createDirectory()
    val file1 = dir.appendSegment("file1")
    write("Hello, world 1!", file1)
    val file1LastModified = file1.lastModifiedTime
    val file2 = dir.appendSegment("file2")
    write("Hello, world 2!", file2)
    val file2LastModified = file2.lastModifiedTime

    val stamper = ResourceStampers.modifiedDir()
    val stamp1 = stamper.stamp(dir)
    file1.lastModifiedTime = file2LastModified
    file2.lastModifiedTime = file1LastModified
    val stamp2 = stamper.stamp(dir)
    // This should be equal because a directory modified stamp takes the maximum modified time. Even though the modified
    // times for the individual files have changed, the modified time for the directory has not.
    Assertions.assertEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testDirStampFileDeletedUnequal() = builder.build("testDirStampFileDeletedUnequal") {
    val dir = resource("/dir")
    dir.createDirectory()
    dir.appendSegment("file1").writeString("Hello, world 1!")
    dir.appendSegment("file2").writeString("Hello, world 2!")
    val file = dir.appendSegment("file3")
    write("Hello, world 3!", file)

    val dirStamper = ResourceStampers.modifiedDir()
    val fileStamper = ResourceStampers.modifiedFile<FSResource>()
    val dirStamp1 = dirStamper.stamp(dir)
    val fileStamp1 = fileStamper.stamp(dir)
    file.delete(true)
    val dirStamp2 = dirStamper.stamp(dir)
    val fileStamp2 = fileStamper.stamp(dir)
    Assertions.assertNotEquals(dirStamp1, dirStamp2)
    // File stamp should also be unequal, because the modified time of a directory changes when a file is created or deleted in that directory.
    Assertions.assertNotEquals(fileStamp1, fileStamp2)
  }

  @TestFactory
  fun testDirStampFileDeletedFlipUnequal() = builder.build("testDirStampFileDeletedFlipUnequal") {
    val dir = resource("/dir")
    dir.createDirectory()
    val file1 = dir.appendSegment("file1")
    write("Hello, world 1!", file1)
    val file1LastModified = file1.lastModifiedTime
    val file2 = dir.appendSegment("file2")
    write("Hello, world 2!", file2)

    val stamper = ResourceStampers.modifiedDir()
    file1.delete(true)
    val stamp1 = stamper.stamp(dir)
    // Restore file1, then delete file2.
    write("Hello, world 1!", file1)
    file1.lastModifiedTime = file1LastModified
    file2.delete(true)
    val stamp2 = stamper.stamp(dir)
    // This should be unequal because the maximum modified time changed.
    Assertions.assertNotEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testDirStampFileAddedUnequal() = builder.build("testDirStampFileAddedUnequal") {
    val dir = resource("/dir")
    dir.createDirectory()
    dir.appendSegment("file1").writeString("Hello, world 1!")
    dir.appendSegment("file2").writeString("Hello, world 2!")
    val file = dir.appendSegment("file3")

    val dirStamper = ResourceStampers.modifiedDir()
    val fileStamper = ResourceStampers.modifiedFile<FSResource>()
    val dirStamp1 = dirStamper.stamp(dir)
    val fileStamp1 = fileStamper.stamp(dir)
    write("Hello, world 3!", file)
    val dirStamp2 = dirStamper.stamp(dir)
    val fileStamp2 = fileStamper.stamp(dir)
    Assertions.assertNotEquals(dirStamp1, dirStamp2)
    // File stamp should also be unequal, because the modified time of a directory changes when a file is created or deleted in that directory.
    Assertions.assertNotEquals(fileStamp1, fileStamp2)
  }

  @TestFactory
  fun testDirStampDirDeletedUnequal() = builder.build("testDirStampDirDeletedUnequal") {
    val dir = resource("/dir")
    dir.createDirectory()
    dir.appendSegment("file1").writeString("Hello, world 1!")
    dir.appendSegment("file2").writeString("Hello, world 2!")
    dir.appendSegment("file3").writeString("Hello, world 3!")

    val stamper = ResourceStampers.modifiedDir()
    val stamp1 = stamper.stamp(dir)
    dir.delete(true)
    val stamp2 = stamper.stamp(dir)
    Assertions.assertNotEquals(stamp1, stamp2)
  }

  // TODO: tests for ResourceStampers.modifiedDir(ResourceMatcher)

  // TODO: tests for ResourceStampers.modifiedDirRec()

  // TODO: tests for ResourceStampers.modifiedDirRec(ResourceWalker, ResourceMatcher)
}
