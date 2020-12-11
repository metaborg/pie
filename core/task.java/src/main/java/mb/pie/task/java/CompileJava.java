package mb.pie.task.java;

import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionPathMatcher;
import mb.resource.hierarchical.walk.TrueResourceWalker;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class CompileJava implements TaskDef<CompileJava.Input, ArrayList<CompileJava.Message>> {
    public static class Input implements Serializable {
        private final ArrayList<ResourcePath> sourceFiles;

        private final ArrayList<ResourcePath> sourcePath;
        // Using File for classPath and annotationProcessorPath, as handling this with ResourcePath takes too much effort at the moment.
        private final ArrayList<File> classPath; // If empty, passes no classpath, which makes javac use the current system classloader as the classpath.
        private final ArrayList<File> annotationProcessorPath; // If empty, passes no processorpath, which makes javac use the current system classloader as the processorpath.

        private final @Nullable String sourceRelease;
        private final @Nullable String targetRelease;

        private final ResourcePath sourceFileOutputDir;
        private final ResourcePath classFileOutputDir;

        private final ArrayList<Supplier<?>> originTasks;

        public Input(
            ArrayList<ResourcePath> sourceFiles,
            ArrayList<ResourcePath> sourcePath,
            ArrayList<File> classPath,
            ArrayList<File> annotationProcessorPath,
            @Nullable String sourceRelease,
            @Nullable String targetRelease,
            ResourcePath sourceFileOutputDir,
            ResourcePath classFileOutputDir,
            ArrayList<Supplier<?>> originTasks
        ) {
            this.sourceFiles = sourceFiles;
            this.sourcePath = sourcePath;
            this.classPath = classPath;
            this.annotationProcessorPath = annotationProcessorPath;
            this.sourceRelease = sourceRelease;
            this.targetRelease = targetRelease;
            this.sourceFileOutputDir = sourceFileOutputDir;
            this.classFileOutputDir = classFileOutputDir;
            this.originTasks = originTasks;
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
                classFileOutputDir.equals(input.classFileOutputDir) &&
                originTasks.equals(input.originTasks);
        }

        @Override public int hashCode() {
            return Objects.hash(sourceFiles, sourcePath, classPath, annotationProcessorPath, sourceRelease, targetRelease, sourceFileOutputDir, classFileOutputDir, originTasks);
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
                ", originTasks=" + originTasks +
                '}';
        }
    }

    public static class Message implements Serializable {
        public final String text;
        public final Diagnostic.Kind kind;
        public final long startOffset;
        public final long endOffset;
        public final long line;
        public final long column;
        public final @Nullable ResourcePath resource;

        public Message(String text, Diagnostic.Kind kind, long startOffset, long endOffset, long line, long column, @Nullable ResourcePath resource) {
            this.text = text;
            this.kind = kind;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.line = line;
            this.column = column;
            this.resource = resource;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Message message = (Message)o;
            if(startOffset != message.startOffset) return false;
            if(endOffset != message.endOffset) return false;
            if(line != message.line) return false;
            if(column != message.column) return false;
            if(!text.equals(message.text)) return false;
            if(kind != message.kind) return false;
            return resource != null ? resource.equals(message.resource) : message.resource == null;
        }

        @Override public int hashCode() {
            int result = text.hashCode();
            result = 31 * result + kind.hashCode();
            result = 31 * result + (int)(startOffset ^ (startOffset >>> 32));
            result = 31 * result + (int)(endOffset ^ (endOffset >>> 32));
            result = 31 * result + (int)(line ^ (line >>> 32));
            result = 31 * result + (int)(column ^ (column >>> 32));
            result = 31 * result + (resource != null ? resource.hashCode() : 0);
            return result;
        }

        @Override public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(kind.toString());
            sb.append(": ");
            if(resource != null) {
                sb.append(resource);
                if(line != Diagnostic.NOPOS) {
                    sb.append(":");
                    sb.append(line);
                    if(column != Diagnostic.NOPOS) {
                        sb.append("@");
                        sb.append(column);
                    }
                }
                sb.append(": ");
            }
            sb.append(text);
            return sb.toString();
        }
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public ArrayList<Message> exec(ExecContext context, Input input) throws Exception {
        for(final Supplier<?> originTask : input.originTasks) {
            context.require(originTask);
        }

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
        final HierarchicalResource sourceFileOutputDir = context.getHierarchicalResource(input.sourceFileOutputDir);
        final HierarchicalResource classFileOutputDir = context.getHierarchicalResource(input.classFileOutputDir);

        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final JavaResourceManager resourceManager = new JavaResourceManager(
            compiler.getStandardFileManager(null, null, null),
            context.getResourceService(),
            sourcePath,
            sourceFileOutputDir,
            classFileOutputDir
        );
        final ArrayList<String> options = new ArrayList<>();
        if(!input.classPath.isEmpty()) {
            options.add("-classpath");
            options.add(input.classPath.stream().map(File::toString).collect(Collectors.joining(File.pathSeparator)));
        }
        if(!input.annotationProcessorPath.isEmpty()) {
            options.add("-processorpath");
            options.add(input.annotationProcessorPath.stream().map(File::toString).collect(Collectors.joining(File.pathSeparator)));
        }
        final ArrayList<Message> messages = new ArrayList<>();
        final CompilationTask compilationTask = compiler.getTask(null, resourceManager, d -> collectDiagnostic(d, messages), options, null, compilationUnits);

        compilationTask.call();

        context.provide(sourceFileOutputDir, ResourceStampers.modifiedDirRec(new TrueResourceWalker(), new PathResourceMatcher(new ExtensionPathMatcher("java"))));
        context.provide(classFileOutputDir, ResourceStampers.modifiedDirRec(new TrueResourceWalker(), new PathResourceMatcher(new ExtensionPathMatcher("class"))));

        return messages; // TODO: handle messages using Result and list of KeyedMessage (but this requires them to be put into a common/util library)
    }

    private static void collectDiagnostic(Diagnostic<? extends JavaFileObject> diagnostic, ArrayList<Message> messages) {
        messages.add(toMessage(diagnostic));
    }

    private static Message toMessage(Diagnostic<? extends JavaFileObject> diagnostic) {
        final String text = diagnostic.getMessage(null);
        final Diagnostic.Kind kind = diagnostic.getKind();
        final long startOffset = diagnostic.getStartPosition();
        final long endOffset = diagnostic.getEndPosition();
        final long line = diagnostic.getLineNumber();
        final long column = diagnostic.getColumnNumber();
        final @Nullable ResourcePath resource;
        final @Nullable JavaFileObject source = diagnostic.getSource();
        if(source instanceof JavaResource) {
            resource = ((JavaResource)source).resource.getPath();
        } else {
            resource = null;
        }
        return new Message(text, kind, startOffset, endOffset, line, column, resource);
    }
}
