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

import static org.junit.jupiter.api.Assertions.*;

class CompileTest {
    private final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
    private final FSResource rootDirectory = new FSResource(fileSystem.getPath("/"));

    private FSResource createTextFile(FSResource rootDirectory, String text, String relativePath) throws IOException {
        final FSResource resource = rootDirectory.appendRelativePath(relativePath);
        resource.writeString(text, StandardCharsets.UTF_8);
        return resource;
    }

    @Test void testCompileTask() throws Exception {
        final CompileJava compileJava = new CompileJava();
        final MapTaskDefs taskDefs = new MapTaskDefs(compileJava);
        final Pie pie = new PieBuilderImpl()
            .withTaskDefs(taskDefs)
            .build();

        final FSResource projectDir = rootDirectory.appendRelativePath("project").createDirectory(true);
        final FSResource testPackageDir = projectDir.appendRelativePath("test").createDirectory(true);
        final FSResource mainJavaFile = createTextFile(testPackageDir, "package test; public class Main {}", "Main.java");
        final FSResource sourceFileOutputDir = rootDirectory.appendRelativePath("build/sources").createDirectory(true);
        final FSResource classFileOutputDir = rootDirectory.appendRelativePath("build/classes").createDirectory(true);

        final ArrayList<ResourcePath> sourceFiles = new ArrayList<>();
        sourceFiles.add(mainJavaFile.getPath());
        final ArrayList<ResourcePath> sourcePath = new ArrayList<>();
        sourcePath.add(projectDir.getPath());
        final ArrayList<ResourcePath> classPath = new ArrayList<>();
        final ArrayList<ResourcePath> annotationProcessorPath = new ArrayList<>();

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
    }
}
