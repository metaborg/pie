package mb.pie.task.archive;

import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.resource.hierarchical.match.path.string.PathStringMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ZipTest extends TestBase {
    final FSResource archiveDir1 = createDir(rootDirectory, "archive1");

    final String archiveFile1Text = "archive file 1";
    final String archiveFile1Path = "file1.txt";
    final FSResource archiveFile1 = createFile(archiveDir1, archiveFile1Text, archiveFile1Path);

    final String archiveFile3Text = "archive file 3";
    final String archiveFile3Path = "subdir1/file3.txt";
    final FSResource archiveFile3 = createFile(archiveDir1, archiveFile3Text, archiveFile3Path);

    final String archiveFile6Text = "# archive file 6";
    final String archiveFile6Path = "subdir2/file6.md";
    final FSResource archiveFile6 = createFile(archiveDir1, archiveFile6Text, archiveFile6Path);


    final FSResource archiveDir2 = createDir(rootDirectory, "archive2");

    final String archiveFile2Text = "# archive file 2";
    final String archiveFile2Path = "file2.md";
    final FSResource archiveFile2 = createFile(archiveDir2, archiveFile2Text, archiveFile2Path);

    final String archiveFile4Text = "# archive file 4";
    final String archiveFile4Sdir = "subdir3";
    final String archiveFile4Path = archiveFile4Sdir + "/file4.md";
    final FSResource archiveFile4 = createFile(archiveDir2, archiveFile4Text, archiveFile4Path);

    final String archiveFile5Text = "archive file 5";
    final String archiveFile5Path = "subdir4/file5.txt";
    final FSResource archiveFile5 = createFile(archiveDir2, archiveFile5Text, archiveFile5Path);


    ZipTest() throws IOException {}

    @Test public void testArchiveUnarchiveAll() throws IOException, ExecException, InterruptedException {
        try(MixedSession session = pie.newSession()) {
            final FSResource zipFile = rootDirectory.appendRelativePath("archive.zip");
            final Task<ResourceKey> zipTask = archiveToZip.createTask(new ArchiveToZip.Input(
                createList(
                    ArchiveDirectory.ofDirectory(archiveDir1.getPath()),
                    ArchiveDirectory.ofDirectory(archiveDir2.getPath())
                ),
                zipFile.getPath(),
                createList()
            ));
            session.require(zipTask);
            assertTrue(zipFile.exists());
            assertTrue(zipFile.isFile());

            final FSResource unarchiveDir = createDir(rootDirectory, "unarchive");
            session.require(unarchiveFromZip.createTask(new UnarchiveFromZip.Input(
                zipFile.getPath(),
                unarchiveDir.getPath(),
                PathStringMatcher.ofTrue(),
                zipTask.toSupplier()
            )));

            final FSResource unarchivedFile1 = unarchiveDir.appendRelativePath(archiveFile1Path);
            assertTrue(unarchivedFile1.exists());
            assertTrue(unarchivedFile1.isFile());
            assertEquals(archiveFile1Text, unarchivedFile1.readString());

            final FSResource unarchivedFile2 = unarchiveDir.appendRelativePath(archiveFile2Path);
            assertTrue(unarchivedFile2.exists());
            assertTrue(unarchivedFile2.isFile());
            assertEquals(archiveFile2Text, unarchivedFile2.readString());

            final FSResource unarchivedFile3 = unarchiveDir.appendRelativePath(archiveFile3Path);
            assertTrue(unarchivedFile3.exists());
            assertTrue(unarchivedFile3.isFile());
            assertEquals(archiveFile3Text, unarchivedFile3.readString());

            final FSResource unarchivedFile4 = unarchiveDir.appendRelativePath(archiveFile4Path);
            assertTrue(unarchivedFile4.exists());
            assertTrue(unarchivedFile4.isFile());
            assertEquals(archiveFile4Text, unarchivedFile4.readString());

            final FSResource unarchivedFile5 = unarchiveDir.appendRelativePath(archiveFile5Path);
            assertTrue(unarchivedFile5.exists());
            assertTrue(unarchivedFile5.isFile());
            assertEquals(archiveFile5Text, unarchivedFile5.readString());

            final FSResource unarchivedFile6 = unarchiveDir.appendRelativePath(archiveFile6Path);
            assertTrue(unarchivedFile6.exists());
            assertTrue(unarchivedFile6.isFile());
            assertEquals(archiveFile6Text, unarchivedFile6.readString());
        }
    }

    @Test public void testArchiveUnarchiveSome() throws IOException, ExecException, InterruptedException {
        try(MixedSession session = pie.newSession()) {
            final FSResource zipFile = rootDirectory.appendRelativePath("archive.zip");
            final Task<ResourceKey> zipTask = archiveToZip.createTask(new ArchiveToZip.Input(
                createList(
                    ArchiveDirectory.ofDirectory(archiveDir1.getPath(), ResourceMatcher.ofPath(PathMatcher.ofExtension("txt"))),
                    ArchiveDirectory.ofDirectory(archiveDir2.getPath(), ResourceWalker.ofPath(PathMatcher.ofAntPattern(archiveFile4Sdir))),
                    ArchiveDirectory.ofDirectory(archiveDir2.getPath(), ResourceMatcher.ofPath(PathMatcher.ofExtension("md")))
                ),
                zipFile.getPath(),
                createList())
            );
            session.require(zipTask);
            assertTrue(zipFile.exists());
            assertTrue(zipFile.isFile());

            final FSResource unarchiveDir = createDir(rootDirectory, "unarchive");
            session.require(unarchiveFromZip.createTask(new UnarchiveFromZip.Input(
                zipFile.getPath(),
                unarchiveDir.getPath(),
                PathStringMatcher.ofAntPattern("**/file4.md").not(),
                zipTask.toSupplier()
            )));

            final FSResource unarchivedFile1 = unarchiveDir.appendRelativePath(archiveFile1Path);
            assertTrue(unarchivedFile1.exists());
            assertTrue(unarchivedFile1.isFile());
            assertEquals(archiveFile1Text, unarchivedFile1.readString());

            final FSResource unarchivedFile2 = unarchiveDir.appendRelativePath(archiveFile2Path);
            assertTrue(unarchivedFile2.exists());
            assertTrue(unarchivedFile2.isFile());
            assertEquals(archiveFile2Text, unarchivedFile2.readString());

            final FSResource unarchivedFile3 = unarchiveDir.appendRelativePath(archiveFile3Path);
            assertTrue(unarchivedFile3.exists());
            assertTrue(unarchivedFile3.isFile());
            assertEquals(archiveFile3Text, unarchivedFile3.readString());

            final FSResource unarchivedFile4 = unarchiveDir.appendRelativePath(archiveFile4Path);
            assertFalse(unarchivedFile4.exists());

            final FSResource unarchivedFile5 = unarchiveDir.appendRelativePath(archiveFile5Path);
            assertFalse(unarchivedFile5.exists());

            final FSResource unarchivedFile6 = unarchiveDir.appendRelativePath(archiveFile6Path);
            assertFalse(unarchivedFile6.exists());
        }
    }
}
