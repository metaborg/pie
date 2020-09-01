package mb.pie.task.java;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.MixedSession;
import mb.pie.api.None;
import mb.pie.api.Pie;
import mb.pie.api.Task;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import static org.junit.jupiter.api.Assertions.*;

class CompileAndJarTest {
    private final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
    private final FSResource rootDirectory = new FSResource(fileSystem.getPath("/"));
    private final CompileJava compileJava = new CompileJava();
    private final CreateJar createJar = new CreateJar();
    private final MapTaskDefs taskDefs = new MapTaskDefs(compileJava, createJar);
    private final Pie pie = new PieBuilderImpl().withTaskDefs(taskDefs).build();

    private final FSResource sourceDir = createDir(rootDirectory, "src/main/java");
    private final FSResource buildDir = createDir(rootDirectory, "build");
    private final FSResource sourceFileOutputDir = createDir(buildDir, "generated/sources/annotationProcessor/java/main");
    private final FSResource classFileOutputDir = createDir(buildDir, "classes/java/main");
    private final FSResource libsDir = createDir(buildDir, "libs");

    CompileAndJarTest() throws IOException {}

    private FSResource createDir(FSResource parent, String relativePath) throws IOException {
        return parent.appendRelativePath(relativePath).createDirectory(true);
    }

    private FSResource createSource(FSResource parent, String text, String relativePath) throws IOException {
        final FSResource resource = parent.appendRelativePath(relativePath);
        resource.createParents();
        resource.writeString(text, StandardCharsets.UTF_8);
        return resource;
    }

    @SafeVarargs private final <T> ArrayList<T> createList(T... items) {
        final ArrayList<T> list = new ArrayList<>();
        Collections.addAll(list, items);
        return list;
    }

    @Test void testCompileTask() throws Exception {
        final FSResource mainJavaFile = createSource(sourceDir, "" +
                "package test; " +
                "" +
                "import test.data.ImmutableHelloWorld; " +
                "import mb.log.api.Logger; " +
                "import mb.log.slf4j.SLF4JLoggerFactory; " +
                "" +
                "public class Main { " +
                "  public static void main(String[] args) { " +
                "    final SLF4JLoggerFactory loggerFactory = new SLF4JLoggerFactory(); " +
                "    final Logger logger = loggerFactory.create(\"main\"); " +
                "    logger.info(ImmutableHelloWorld.builder().build().helloWorld()); " +
                "  } " +
                "} ",
            "test/Main.java");
        final FSResource helloWorldJavaFile = createSource(sourceDir, "" +
                "package test.data; " +
                "" +
                "import org.immutables.value.Value; " +
                "" +
                "@Value.Immutable " +
                "public interface HelloWorld { " +
                "  @Value.Default default String helloWorld() { " +
                "    return \"Hello, world!\"; " +
                "  } " +
                "} ",
            "test/data/HelloWorld.java");
        final ArrayList<ResourcePath> sourceFiles = createList(mainJavaFile.getPath(), helloWorldJavaFile.getPath());
        final ArrayList<ResourcePath> sourcePath = createList(sourceDir.getPath());

        final @Nullable String classPathProperty = System.getProperty("classPath");
        assertNotNull(classPathProperty);
        final ArrayList<File> classPath = createList();
        for(String classPathPart : classPathProperty.split(File.pathSeparator)) {
            classPath.add(new File(classPathPart));
        }

        final @Nullable String annotationProcessorPathProperty = System.getProperty("annotationProcessorPath");
        assertNotNull(annotationProcessorPathProperty);
        final ArrayList<File> annotationProcessorPath = createList();
        for(String annotationProcessorPathPart : annotationProcessorPathProperty.split(File.pathSeparator)) {
            annotationProcessorPath.add(new File(annotationProcessorPathPart));
        }
        classPath.addAll(annotationProcessorPath);

        try(MixedSession session = pie.newSession()) {
            final Task<None> compileJavaTask = compileJava.createTask(new CompileJava.Input(
                sourceFiles,
                sourcePath,
                classPath,
                annotationProcessorPath,
                null,
                null,
                sourceFileOutputDir.getPath(),
                classFileOutputDir.getPath(),
                createList()
            ));
            session.require(compileJavaTask);

            final FSResource immutableHelloWorldJavaFile = sourceFileOutputDir.appendRelativePath("test/data/ImmutableHelloWorld.java");
            assertTrue(immutableHelloWorldJavaFile.exists());
            assertTrue(immutableHelloWorldJavaFile.readString().contains("ImmutableHelloWorld"));

            final FSResource mainClassFile = classFileOutputDir.appendRelativePath("test/Main.class");
            assertTrue(mainClassFile.exists());
            final FSResource helloWorldClassFile = classFileOutputDir.appendRelativePath("test/data/HelloWorld.class");
            assertTrue(helloWorldClassFile.exists());
            final FSResource immutableHelloWorldClassFile = classFileOutputDir.appendRelativePath("test/data/ImmutableHelloWorld.class");
            assertTrue(immutableHelloWorldClassFile.exists());
            final FSResource immutableHelloWorldBuilderClassFile = classFileOutputDir.appendRelativePath("test/data/ImmutableHelloWorld$Builder.class");
            assertTrue(immutableHelloWorldBuilderClassFile.exists());
            final FSResource immutableHelloWorld1ClassFile = classFileOutputDir.appendRelativePath("test/data/ImmutableHelloWorld$1.class");
            assertTrue(immutableHelloWorld1ClassFile.exists());

            final FSResource jarFile = libsDir.appendRelativePath("lib.jar").createFile(true);
            session.require(createJar.createTask(new CreateJar.Input(
                null,
                createList(CreateJar.ArchiveDirectory.ofClassFilesInDirectory(classFileOutputDir.getPath())),
                jarFile.getPath(),
                createList(compileJavaTask.toSupplier())
            )));
            assertTrue(jarFile.exists());
            final HashSet<String> entryNames = new HashSet<>();
            try(final JarInputStream jarInputStream = new JarInputStream(jarFile.openReadBuffered())) {
                assertEquals("1.0", jarInputStream.getManifest().getMainAttributes().getValue(Attributes.Name.MANIFEST_VERSION));
                @Nullable JarEntry entry;
                while((entry = jarInputStream.getNextJarEntry()) != null) {
                    entryNames.add(entry.getName());
                }
            }
            assertTrue(entryNames.contains("test/"));
            assertTrue(entryNames.contains("test/Main.class"));
            assertTrue(entryNames.contains("test/data/"));
            assertTrue(entryNames.contains("test/data/HelloWorld.class"));
            assertTrue(entryNames.contains("test/data/ImmutableHelloWorld.class"));
            assertTrue(entryNames.contains("test/data/ImmutableHelloWorld$Builder.class"));
            assertTrue(entryNames.contains("test/data/ImmutableHelloWorld$1.class"));
        }
    }
}
