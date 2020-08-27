package mb.pie.task.java;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.ResourcePath;
import org.junit.jupiter.api.Test;

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

    private final FSResource projectDir = createDir(rootDirectory, "project");
    private final FSResource sourceFileOutputDir = createDir(rootDirectory, "build/sources");
    private final FSResource classFileOutputDir = createDir(rootDirectory, "build/classes");

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
        final FSResource mainJavaFile = createSource(projectDir, "package test; public class Main { public static void main(String [] args) { System.out.println(HelloWorld.helloWorld()); } }", "test/Main.java");
        final FSResource helloWorldJavaFile = createSource(projectDir, "package test; public class HelloWorld { public static String helloWorld() { return \"Hello, world!\"; } }", "test/HelloWorld.java");
        final ArrayList<ResourcePath> sourceFiles = createList(mainJavaFile.getPath());
        final ArrayList<ResourcePath> sourcePath = createList(projectDir.getPath());
        final ArrayList<ResourcePath> classPath = createList();
        final ArrayList<ResourcePath> annotationProcessorPath = createList();

        try(MixedSession session = pie.newSession()) {
            session.require(compileJava.createTask(new CompileJava.Input(
                sourceFiles,
                sourcePath,
                classPath,
                annotationProcessorPath,
                null,
                null,
                sourceFileOutputDir.getPath(),
                classFileOutputDir.getPath()
            )));
        }

        final FSResource mainClassFile = classFileOutputDir.appendRelativePath("test/Main.class");
        assertTrue(mainClassFile.exists());
        final FSResource helloWorldClassFile = classFileOutputDir.appendRelativePath("test/HelloWorld.class");
        assertTrue(helloWorldClassFile.exists());
    }
}
