package mb.pie.task.java;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.common.message.KeyedMessages;
import mb.common.util.ExceptionPrinter;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.Task;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.fs.FSResource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class CompileTest {
    private final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
    private final FSResource rootDirectory = new FSResource(fileSystem.getPath("/"));
    private final CompileJava compileJava = new CompileJava();
    private final MapTaskDefs taskDefs = new MapTaskDefs(compileJava);
    private final Pie pie = new PieBuilderImpl().withTaskDefs(taskDefs).build();

    private final FSResource sourceDirectory = createDir(rootDirectory, "src/main/java");
    private final FSResource buildDirectory = createDir(rootDirectory, "build");
    private final FSResource sourceFileOutputDirectory = createDir(buildDirectory, "generated/sources/annotationProcessor/java/main");
    private final FSResource classFileOutputDirecotry = createDir(buildDirectory, "classes/java/main");

    CompileTest() throws IOException {}

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
        final FSResource mainJavaFile = createSource(sourceDirectory, "" +
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
        final FSResource helloWorldJavaFile = createSource(sourceDirectory, "" +
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
        final CompileJava.Input.Builder inputBuilder = CompileJava.Input.builder()
            .sources(CompileJava.Sources.builder()
                .addSourceFiles(mainJavaFile.getPath(), helloWorldJavaFile.getPath())
                .addSourcePaths(sourceDirectory.getPath()).build()
            );
        final @Nullable String classPathProperty = System.getProperty("classPath");
        assertNotNull(classPathProperty);
        for(String classPathPart : classPathProperty.split(File.pathSeparator)) {
            inputBuilder.addClassPaths(new File(classPathPart));
        }
        final @Nullable String annotationProcessorPathProperty = System.getProperty("annotationProcessorPath");
        assertNotNull(annotationProcessorPathProperty);
        for(String annotationProcessorPathPart : annotationProcessorPathProperty.split(File.pathSeparator)) {
            inputBuilder.addAnnotationProcessorPaths(new File(annotationProcessorPathPart));
        }
        inputBuilder
            .sourceFileOutputDirectory(sourceFileOutputDirectory.getPath())
            .classFileOutputDirectory(classFileOutputDirecotry.getPath())
        ;
        try(MixedSession session = pie.newSession()) {
            final Task<KeyedMessages> compileJavaTask = compileJava.createTask(inputBuilder.build());
            final KeyedMessages messages = session.require(compileJavaTask);
            assertFalse(messages.containsError(), () -> new ExceptionPrinter().addCurrentDirectoryContext(rootDirectory).printMessagesToString(messages));
            assertFalse(messages.containsWarning(), () -> new ExceptionPrinter().addCurrentDirectoryContext(rootDirectory).printMessagesToString(messages));

            final FSResource immutableHelloWorldJavaFile = sourceFileOutputDirectory.appendRelativePath("test/data/ImmutableHelloWorld.java");
            assertTrue(immutableHelloWorldJavaFile.exists());
            assertTrue(immutableHelloWorldJavaFile.readString().contains("ImmutableHelloWorld"));

            final FSResource mainClassFile = classFileOutputDirecotry.appendRelativePath("test/Main.class");
            assertTrue(mainClassFile.exists());
            final FSResource helloWorldClassFile = classFileOutputDirecotry.appendRelativePath("test/data/HelloWorld.class");
            assertTrue(helloWorldClassFile.exists());
            final FSResource immutableHelloWorldClassFile = classFileOutputDirecotry.appendRelativePath("test/data/ImmutableHelloWorld.class");
            assertTrue(immutableHelloWorldClassFile.exists());
            final FSResource immutableHelloWorldBuilderClassFile = classFileOutputDirecotry.appendRelativePath("test/data/ImmutableHelloWorld$Builder.class");
            assertTrue(immutableHelloWorldBuilderClassFile.exists());
        }
    }
}
