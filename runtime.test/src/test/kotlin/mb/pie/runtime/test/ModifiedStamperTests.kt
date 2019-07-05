package mb.pie.runtime.test

import mb.pie.api.stamp.resource.ResourceStampers
import mb.resource.fs.FSResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestFactory
import java.nio.charset.StandardCharsets

internal class ModifiedStamperTests {
  @TestFactory
  fun testFileStampEqual() = RuntimeTestGenerator.generate("testFileStampEqual") {
    val file = resource("/file")
    write("Hello, world!", file)

    val stamper = ResourceStampers.modifiedFile<FSResource>()
    val stamp1 = stamper.stamp(file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testFileStampUnequal() = RuntimeTestGenerator.generate("testFileStampUnequal") {
    val file = resource("/file")
    write("Hello, world!", file)

    val stamper = ResourceStampers.modifiedFile<FSResource>()
    val stamp1 = stamper.stamp(file)
    write("Hello, world!", file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertNotEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testNonExistentFileStampEqual() = RuntimeTestGenerator.generate("testNonExistentFileStampEqual") {
    val file = resource("/file")

    val stamper = ResourceStampers.modifiedFile<FSResource>()
    val stamp1 = stamper.stamp(file)
    val stamp2 = stamper.stamp(file)
    Assertions.assertEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testNonExistentAndExistentFileStampUnequal() = RuntimeTestGenerator.generate("testNonExistentAndExistentFileStampUnequal") {
    val file = resource("/file")

    val stamper = ResourceStampers.modifiedFile<FSResource>()
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

    val stamper = ResourceStampers.modifiedDir()
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

    val stamper = ResourceStampers.modifiedDir()
    val stamp1 = stamper.stamp(dir)
    write("Hello, world 3!", file)
    val stamp2 = stamper.stamp(dir)
    Assertions.assertNotEquals(stamp1, stamp2)
  }

  @TestFactory
  fun testDirStampFileChangedFlipEqual() = RuntimeTestGenerator.generate("testDirStampFileChangedFlipEqual") {
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
  fun testDirStampFileDeletedUnequal() = RuntimeTestGenerator.generate("testDirStampFileDeletedUnequal") {
    val dir = resource("/dir")
    dir.createDirectory()
    dir.appendSegment("file1").writeString("Hello, world 1!", StandardCharsets.UTF_8)
    dir.appendSegment("file2").writeString("Hello, world 2!", StandardCharsets.UTF_8)
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
  fun testDirStampFileDeletedFlipUnequal() = RuntimeTestGenerator.generate("testDirStampFileDeletedFlipUnequal") {
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
  fun testDirStampFileAddedUnequal() = RuntimeTestGenerator.generate("testDirStampFileAddedUnequal") {
    val dir = resource("/dir")
    dir.createDirectory()
    dir.appendSegment("file1").writeString("Hello, world 1!", StandardCharsets.UTF_8)
    dir.appendSegment("file2").writeString("Hello, world 2!", StandardCharsets.UTF_8)
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
  fun testDirStampDirDeletedUnequal() = RuntimeTestGenerator.generate("testDirStampDirDeletedUnequal") {
    val dir = resource("/dir")
    dir.createDirectory()
    dir.appendSegment("file1").writeString("Hello, world 1!", StandardCharsets.UTF_8)
    dir.appendSegment("file2").writeString("Hello, world 2!", StandardCharsets.UTF_8)
    dir.appendSegment("file3").writeString("Hello, world 3!", StandardCharsets.UTF_8)

    val stamper = ResourceStampers.modifiedDir()
    val stamp1 = stamper.stamp(dir)
    dir.delete(true)
    val stamp2 = stamper.stamp(dir)
    Assertions.assertNotEquals(stamp1, stamp2)
  }
}
