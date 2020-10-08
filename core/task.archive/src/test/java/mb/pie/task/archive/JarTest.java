package mb.pie.task.archive;

import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.None;
import mb.pie.api.Task;
import mb.resource.fs.FSResource;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static org.junit.jupiter.api.Assertions.*;

public class JarTest extends TestBase {
    @Test public void testArchiveUnarchiveClasses() throws IOException, ExecException, InterruptedException {
        final FSResource classesDir = createDir(rootDirectory, "classes");
        final FSResource class1 = createFile(classesDir, "A", "A.class");
        final FSResource class2 = createFile(classesDir, "B", "B.class");
        final FSResource class3 = createFile(classesDir, "mb.A", "mb/A.class");
        final FSResource class4 = createFile(classesDir, "mb.B", "mb/B.class");
        final FSResource class5 = createFile(classesDir, "mb.pkg.C", "mb/pkg/C.class");
        final FSResource class6 = createFile(classesDir, "package-info", "package-info.class");
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.33.7");
        final FSResource manifestFile = rootDirectory.appendRelativePath("MANIFEST.MF").ensureFileExists();
        try(final BufferedOutputStream outputStream = manifestFile.openWriteBuffered()) {
            manifest.write(outputStream);
            outputStream.flush();
        }

        try(MixedSession session = pie.newSession()) {
            final FSResource jarFile = rootDirectory.appendRelativePath("library.jar");
            final Task<None> jarTask = archiveToJar.createTask(new ArchiveToJar.Input(
                manifestFile.getPath(),
                createList(ArchiveDirectory.ofClassFilesInDirectory(classesDir.getPath())),
                jarFile.getPath(),
                createList()
            ));
            session.require(jarTask);
            assertTrue(jarFile.exists());
            assertTrue(jarFile.isFile());

            final FSResource unarchiveDir = createDir(rootDirectory, "unarchive");
            session.require(unarchiveFromJar.createTask(new UnarchiveFromJar.Input(jarFile.getPath(), unarchiveDir.getPath(), jarTask.toSupplier())));

            final FSResource unarchivedClass1 = unarchiveDir.appendRelativePath(classesDir.getPath().relativize(class1.getPath()));
            assertTrue(unarchivedClass1.exists());
            assertTrue(unarchivedClass1.isFile());
            assertEquals(class1.readString(), unarchivedClass1.readString());

            final FSResource unarchivedClass2 = unarchiveDir.appendRelativePath(classesDir.getPath().relativize(class2.getPath()));
            assertTrue(unarchivedClass2.exists());
            assertTrue(unarchivedClass2.isFile());
            assertEquals(class2.readString(), unarchivedClass2.readString());

            final FSResource unarchivedClass3 = unarchiveDir.appendRelativePath(classesDir.getPath().relativize(class3.getPath()));
            assertTrue(unarchivedClass3.exists());
            assertTrue(unarchivedClass3.isFile());
            assertEquals(class3.readString(), unarchivedClass3.readString());

            final FSResource unarchivedClass4 = unarchiveDir.appendRelativePath(classesDir.getPath().relativize(class4.getPath()));
            assertTrue(unarchivedClass4.exists());
            assertTrue(unarchivedClass4.isFile());
            assertEquals(class4.readString(), unarchivedClass4.readString());

            final FSResource unarchivedClass5 = unarchiveDir.appendRelativePath(classesDir.getPath().relativize(class5.getPath()));
            assertTrue(unarchivedClass5.exists());
            assertTrue(unarchivedClass5.isFile());
            assertEquals(class5.readString(), unarchivedClass5.readString());

            final FSResource unarchivedClass6 = unarchiveDir.appendRelativePath(classesDir.getPath().relativize(class6.getPath()));
            assertTrue(unarchivedClass6.exists());
            assertTrue(unarchivedClass6.isFile());
            assertEquals(class6.readString(), unarchivedClass6.readString());

            final FSResource unarchivedManifestFile = unarchiveDir.appendRelativePath("META-INF/MANIFEST.MF");
            assertTrue(unarchivedManifestFile.exists());
            assertTrue(unarchivedManifestFile.isFile());
            try(final BufferedInputStream inputStream = unarchivedManifestFile.openReadBuffered()) {
                final Manifest unarchivedManifest = new Manifest(inputStream);
                assertEquals("1.33.7", unarchivedManifest.getMainAttributes().getValue(Attributes.Name.MANIFEST_VERSION));
            }
        }
    }
}
