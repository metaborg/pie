package mb.pie.task.java.jdk;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.pie.task.java.CompileJava;
import mb.pie.task.java.JavaCompiler;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.tools.Diagnostic;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class JdkJavaCompiler implements JavaCompiler {
    private final javax.tools.JavaCompiler compiler;
    private final FileManagerFactory fileManagerFactory;
    private final JavaFileObjectFactory javaFileObjectFactory;

    public JdkJavaCompiler(javax.tools.JavaCompiler compiler, FileManagerFactory fileManagerFactory, JavaFileObjectFactory javaFileObjectFactory) {
        this.compiler = compiler;
        this.fileManagerFactory = fileManagerFactory;
        this.javaFileObjectFactory = javaFileObjectFactory;
    }

    public JdkJavaCompiler() {
        this(ToolProvider.getSystemJavaCompiler(), JavaResourceManager::new, new JavaResource.Factory());
    }

    @Override
    public KeyedMessages compile(
        ExecContext context,
        ListView<CompileJava.Sources> allSources,
        ListView<String> classPaths,
        ListView<String> annotationProcessorPaths,
        @Nullable String release,
        ResourcePath sourceFileOutputDirectory,
        ResourcePath classFileOutputDirectory,
        boolean reportWarnings,
        boolean emitDebuggingAttributes,
        ListView<String> additionalOptions
    ) throws IOException {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final ArrayList<JavaFileObject> compilationUnits = new ArrayList<>();
        final ArrayList<HierarchicalResource> sourcePath = new ArrayList<>();
        for(CompileJava.Sources sources : allSources) {
            for(ResourcePath sourceFilePath : sources.sourceFiles()) {
                final HierarchicalResource sourceFile = context.require(sourceFilePath, ResourceStampers.<HierarchicalResource>modifiedFile());
                compilationUnits.add(javaFileObjectFactory.create(sourceFile));
            }
            for(ResourcePath sourceFilesFromPath : sources.sourceFilesFromPaths()) {
                final HierarchicalResource sourceFilesFromDirectory = context.getHierarchicalResource(sourceFilesFromPath);
                // Require directories recursively, so we re-execute whenever a file is added/removed from a directory.
                sourceFilesFromDirectory.walkForEach(ResourceMatcher.ofDirectory(), context::require);
                // Require all Java source files recursively, so we re-execute whenever a file changes.
                sourceFilesFromDirectory.walkForEach(ResourceMatcher.ofFileExtension("java"), javaSourceFile -> {
                    context.require(javaSourceFile, ResourceStampers.<HierarchicalResource>modifiedFile());
                    compilationUnits.add(javaFileObjectFactory.create(javaSourceFile));
                });
            }
            for(ResourcePath sourcePathPart : sources.sourcePaths()) {
                final HierarchicalResource sourceDirectory = context.getHierarchicalResource(sourcePathPart);
                // Require directories recursively, so we re-execute whenever a file is added/removed from a directory.
                sourceDirectory.walkForEach(ResourceMatcher.ofDirectory(), context::require);
                // Require all Java source files recursively, so we re-execute whenever a file changes.
                sourceDirectory.walkForEach(ResourceMatcher.ofFileExtension("java"), context::require);
                sourcePath.add(sourceDirectory);
            }
            for(ResourcePath sourceDirectoryPath : sources.packagePaths()) {
                final HierarchicalResource sourceDirectory = context.getHierarchicalResource(sourceDirectoryPath);
                // Require directories recursively, so we re-execute whenever a file is added/removed from a directory.
                sourceDirectory.walkForEach(ResourceMatcher.ofDirectory(), context::require);
                sourcePath.add(sourceDirectory);
            }
        }
        if(compilationUnits.isEmpty()) {
            // Compiler throws exception if there are no source files. Return early.
            return messagesBuilder.build();
        }

        final HierarchicalResource sourceFileOutputDir = context.getHierarchicalResource(sourceFileOutputDirectory);
        final HierarchicalResource classFileOutputDir = context.getHierarchicalResource(classFileOutputDirectory);

        final JavaFileManager fileManager = fileManagerFactory.create(
            compiler.getStandardFileManager(null, null, null),
            javaFileObjectFactory,
            context.getResourceService(),
            sourcePath,
            sourceFileOutputDir,
            classFileOutputDir
        );
        final ArrayList<String> options = new ArrayList<>();
        if(release != null) {
            if(compiler.isSupportedOption("--release") != -1) {
                options.add("--release");
                options.add(release);
            } else {
                options.add("-source");
                options.add(release);
                options.add("-target");
                options.add(release);
            }
        }

        if(!classPaths.isEmpty()) {
            options.add("-classpath");
            options.add(String.join(File.pathSeparator, classPaths));
        }
        if(!annotationProcessorPaths.isEmpty()) {
            options.add("-processorpath");
            options.add(String.join(File.pathSeparator, annotationProcessorPaths));
        }

        if(!reportWarnings) {
            options.add("-nowarn");
        }
        if(!emitDebuggingAttributes) {
            options.add("-g:none");
        }
        additionalOptions.addAllTo(options);

        final javax.tools.JavaCompiler.CompilationTask compilationTask = compiler.getTask(null, fileManager, d -> collectDiagnostic(d, messagesBuilder), options, null, compilationUnits);
        compilationTask.call();

        // Provide generated Java source files.
        provideFilesInDirectoryOfExtension(context, sourceFileOutputDir, "java");
        // Provide compiled Java class files.
        provideFilesInDirectoryOfExtension(context, classFileOutputDir, "class");

        return messagesBuilder.build();
    }

    private static void provideFilesInDirectoryOfExtension(ExecContext context, HierarchicalResource directory, String extension) throws IOException {
        directory.walkForEach(ResourceMatcher.ofFile().and(ResourceMatcher.ofPath(PathMatcher.ofExtension(extension))), context::provide);
    }

    private static void collectDiagnostic(Diagnostic<? extends JavaFileObject> diagnostic, KeyedMessagesBuilder messagesBuilder) {
        final String text = diagnostic.getMessage(null);
        final Severity severity = toSeverity(diagnostic.getKind());
        final @Nullable ResourcePath resource;
        final @Nullable JavaFileObject source = diagnostic.getSource();
        if(source instanceof JavaResource) {
            resource = ((JavaResource)source).resource.getPath();
        } else {
            resource = null;
        }
        final @Nullable Region region;
        if(diagnostic.getPosition() != Diagnostic.NOPOS) {
            int startPosition = (int)diagnostic.getStartPosition();
            int endPosition = (int) Math.max(diagnostic.getEndPosition(), startPosition); // sometimes end position is unknown, so use start position
            int lineNumber = (int)diagnostic.getLineNumber();
            region = Region.fromOffsets(startPosition, endPosition, lineNumber);
        } else {
            region = null;
        }
        messagesBuilder.addMessage(text, severity, resource, region);
    }

    private static Severity toSeverity(Diagnostic.Kind kind) {
        switch(kind) {
            case ERROR:
                return Severity.Error;
            case WARNING:
            case MANDATORY_WARNING:
                return Severity.Warning;
            case NOTE:
                return Severity.Info;
            default:
                return Severity.Debug;
        }
    }
}
