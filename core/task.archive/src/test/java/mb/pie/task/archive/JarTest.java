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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static org.junit.jupiter.api.Assertions.*;

public class JarTest extends TestBase {
    @Test public void testArchiveUnarchiveJar() throws IOException, ExecException, InterruptedException {
        final FSResource classesDir = createDir(rootDirectory, "classes");
        final FSResource class1 = createFile(classesDir, "A", "A.class");
        final FSResource class2 = createFile(classesDir, "B", "B.class");
        final FSResource class3 = createFile(classesDir, "mb.A", "mb/A.class");
        final FSResource class4 = createFile(classesDir, "mb.B", "mb/B.class");
        final FSResource class5 = createFile(classesDir, "mb.pkg.C", "mb/pkg/C.class");
        final FSResource class6 = createFile(classesDir, "package-info", "package-info.class");
        final FSResource resourcesDir = createDir(rootDirectory, "resources");
        final FSResource resource1 = createFile(resourcesDir, "x", "x.txt");
        final FSResource resource2 = createFile(resourcesDir, "y", "y.md");
        final FSResource resource3 = createFile(resourcesDir, "z", "foo/z.yaml");
        final FSResource resource4 = createFile(resourcesDir, "z", "z.xml");
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.33.7");
        final FSResource manifestFile = rootDirectory.appendRelativePath("MANIFEST.MF").ensureFileExists();
        try(final BufferedOutputStream outputStream = manifestFile.openWriteBuffered()) {
            manifest.write(outputStream);
            outputStream.flush();
        }

        try(MixedSession session = pie.newSession()) {
            final FSResource jarFile = rootDirectory.appendRelativePath("library.jar");
            final Task<ResourceKey> jarTask = archiveToJar.createTask(new ArchiveToJar.Input(
                manifestFile.getPath(),
                createList(
                    ArchiveDirectory.ofClassFilesInDirectory(classesDir.getPath()),
                    ArchiveDirectory.ofDirectory(resourcesDir.getPath(), ResourceMatcher.ofPath(PathMatcher.ofExtensions("md", "yaml")).not())
                ),
                jarFile.getPath(),
                createList()
            ));
            session.require(jarTask);
            assertTrue(jarFile.exists());
            assertTrue(jarFile.isFile());

            final FSResource unarchiveDir = createDir(rootDirectory, "unarchive");
            session.require(unarchiveFromJar.createTask(new UnarchiveFromJar.Input(
                jarFile.getPath(),
                unarchiveDir.getPath(),
                PathStringMatcher.ofAntPattern("**/*.md").not(),
                jarTask.toSupplier()
            )));

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

            final FSResource unarchivedResource1 = unarchiveDir.appendRelativePath(resourcesDir.getPath().relativize(resource1.getPath()));
            assertTrue(unarchivedResource1.exists());
            assertTrue(unarchivedResource1.isFile());
            assertEquals(resource1.readString(), unarchivedResource1.readString());

            final FSResource unarchivedResource2 = unarchiveDir.appendRelativePath(resourcesDir.getPath().relativize(resource2.getPath()));
            assertFalse(unarchivedResource2.exists());

            final FSResource unarchivedResource3 = unarchiveDir.appendRelativePath(resourcesDir.getPath().relativize(resource3.getPath()));
            assertFalse(unarchivedResource3.exists());

            final FSResource unarchivedResource4 = unarchiveDir.appendRelativePath(resourcesDir.getPath().relativize(resource4.getPath()));
            assertTrue(unarchivedResource4.exists());
            assertTrue(unarchivedResource4.isFile());
            assertEquals(resource4.readString(), unarchivedResource4.readString());
        }
    }
}
