package mb.pie.task.java;

import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionPathMatcher;
import mb.resource.hierarchical.walk.TrueResourceWalker;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class CompileJava implements TaskDef<CompileJava.Input, None> {
    public static class Input implements Serializable {
        private final ArrayList<ResourcePath> sourceFiles;

        private final ArrayList<ResourcePath> sourcePath;
        private final ArrayList<ResourcePath> classPath;
        private final ArrayList<ResourcePath> annotationProcessorPath;

        private final @Nullable String sourceRelease;
        private final @Nullable String targetRelease;

        private final ResourcePath sourceFileOutputDir;
        private final ResourcePath classFileOutputDir;

        public Input(
            ArrayList<ResourcePath> sourceFiles,
            ArrayList<ResourcePath> sourcePath,
            ArrayList<ResourcePath> classPath,
            ArrayList<ResourcePath> annotationProcessorPath,
            @Nullable String sourceRelease,
            @Nullable String targetRelease,
            ResourcePath sourceFileOutputDir,
            ResourcePath classFileOutputDir
        ) {
            this.sourceFiles = sourceFiles;
            this.sourcePath = sourcePath;
            this.classPath = classPath;
            this.annotationProcessorPath = annotationProcessorPath;
            this.sourceRelease = sourceRelease;
            this.targetRelease = targetRelease;
            this.sourceFileOutputDir = sourceFileOutputDir;
            this.classFileOutputDir = classFileOutputDir;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return sourceFiles.equals(input.sourceFiles) &&
                sourcePath.equals(input.sourcePath) &&
                classPath.equals(input.classPath) &&
                annotationProcessorPath.equals(input.annotationProcessorPath) &&
                Objects.equals(sourceRelease, input.sourceRelease) &&
                Objects.equals(targetRelease, input.targetRelease) &&
                sourceFileOutputDir.equals(input.sourceFileOutputDir) &&
                classFileOutputDir.equals(input.classFileOutputDir);
        }

        @Override public int hashCode() {
            return Objects.hash(sourceFiles, sourcePath, classPath, annotationProcessorPath, sourceRelease, targetRelease, sourceFileOutputDir, classFileOutputDir);
        }

        @Override public String toString() {
            return "Input{" +
                "sourceFiles=" + sourceFiles +
                ", sourcePath=" + sourcePath +
                ", classPath=" + classPath +
                ", annotationProcessorPath=" + annotationProcessorPath +
                ", sourceRelease='" + sourceRelease + '\'' +
                ", targetRelease='" + targetRelease + '\'' +
                ", sourceFileOutputDir=" + sourceFileOutputDir +
                ", classFileOutputDir=" + classFileOutputDir +
                '}';
        }
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws Exception {
        final ArrayList<JavaResource> compilationUnits = new ArrayList<>();
        for(ResourcePath sourceFilePath : input.sourceFiles) {
            final HierarchicalResource sourceFile = context.require(sourceFilePath, ResourceStampers.<HierarchicalResource>modifiedFile());
            compilationUnits.add(new JavaResource(sourceFile));
        }
        final ArrayList<HierarchicalResource> sourcePath = new ArrayList<>();
        for(ResourcePath sourcePathPart : input.sourcePath) {
            final HierarchicalResource resource = context.require(sourcePathPart, ResourceStampers.modifiedDirRec(new TrueResourceWalker(), new PathResourceMatcher(new ExtensionPathMatcher("java"))));
            sourcePath.add(resource);
        }
        final ArrayList<HierarchicalResource> classPath = new ArrayList<>();
        for(ResourcePath classPathPart : input.classPath) {
            classPath.add(requireClassPath(context, classPathPart));
        }
        final ArrayList<HierarchicalResource> annotationProcessorPath = new ArrayList<>();
        for(ResourcePath annotationProcessorPathPart : input.annotationProcessorPath) {
            annotationProcessorPath.add(requireClassPath(context, annotationProcessorPathPart));
        }
        final HierarchicalResource sourceFileOutputDir = context.getHierarchicalResource(input.sourceFileOutputDir);
        final HierarchicalResource classFileOutputDir = context.getHierarchicalResource(input.classFileOutputDir);

        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final JavaResourceManager resourceManager = new JavaResourceManager(
            compiler.getStandardFileManager(null, null, null),
            context.getResourceService(),
            sourcePath,
            classPath,
            annotationProcessorPath,
            sourceFileOutputDir,
            classFileOutputDir
        );
        final CompilationTask compilationTask = compiler.getTask(null, resourceManager, null, null, null, compilationUnits);

        context.provide(sourceFileOutputDir, ResourceStampers.modifiedDirRec(new TrueResourceWalker(), new PathResourceMatcher(new ExtensionPathMatcher("java"))));
        context.provide(classFileOutputDir, ResourceStampers.modifiedDirRec(new TrueResourceWalker(), new PathResourceMatcher(new ExtensionPathMatcher("class"))));

        if(!compilationTask.call()) {
            // TODO: properly handle errors
            throw new RuntimeException("Java compilation failed");
        }
        return None.instance;
    }


    private HierarchicalResource requireClassPath(ExecContext context, ResourcePath path) throws IOException {
        final HierarchicalResource resource = context.getHierarchicalResource(path);
        if(resource.isDirectory()) {
            context.require(resource, ResourceStampers.modifiedDirRec(new TrueResourceWalker(), new PathResourceMatcher(new ExtensionPathMatcher("class"))));
        } else if(resource.isFile()) {
            context.require(resource, ResourceStampers.modifiedFile());
        } else {
            context.require(resource, ResourceStampers.exists());
        }
        return resource;
    }
}
