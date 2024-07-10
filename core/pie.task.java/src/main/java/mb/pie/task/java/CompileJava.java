package mb.pie.task.java;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.task.java.jdk.JdkJavaCompiler;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Value.Enclosing
public class CompileJava implements TaskDef<CompileJava.Input, KeyedMessages> {
    @Value.Immutable
    public interface Sources extends Serializable {
        class Builder extends CompileJavaData.Sources.Builder {}

        static Builder builder() {return new Builder();}

        /**
         * Java source files to compile (i.e., the compilation units). The Java compiler will start compiling from these
         * source files, and also compile all recursively imported source files. A dependency to these files will be
         * added, such that a change in these files will trigger recompilation. Additionally, changing the files in this
         * list will also trigger recompilation.
         */
        List<ResourcePath> sourceFiles();

        /**
         * Directory with Java source files to compile. These directories will be recursively scanned for Java files
         * which are treated as compilation units (as if the individual files would be returned from {@link
         * #sourceFiles()}. A dependency to all recursive directories and recursive .java files will be added, such that
         * an added/changed/remove Java file will trigger recompilation. Additionally, changing the directories in this
         * list will also trigger recompilation.
         */
        List<ResourcePath> sourceFilesFromPaths();

        /**
         * Directories to include on the source path, used to resolve imports in Java source files. A dependency to all
         * recursive directories and recursive .java files will be added, such that an added/changed/remove Java file
         * will trigger recompilation. Additionally, changing the directories in this list will also trigger
         * recompilation.
         */
        List<ResourcePath> sourcePaths();

        /**
         * Directories to include on the source path, but only used to resolve packages in Java source files. A
         * dependency to all recursive directories will be added, such that an added/changed/removed package will
         * trigger recompilation. Additionally, changing the directories in this list will also trigger recompilation.
         */
        List<ResourcePath> packagePaths();
    }

    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends CompileJavaData.Input.Builder {}

        static Builder builder() {return new Builder();}


        Optional<Sources> sources();

        List<Supplier<Result<Sources, ?>>> sourceTasks();


        // Using File for classPath and annotationProcessorPath, as handling this with ResourcePath takes too much effort at the moment.

        List<Supplier<ListView<File>>> classPathSuppliers();

        /**
         * Whether the paths from system property "java.class.path" are added to the class path. Defaults to false.
         */
        @Value.Default default boolean addEnvironmentToClassPaths() {
            return false;
        }

        List<Supplier<ListView<File>>> annotationProcessorPathSuppliers();

        /**
         * Whether the paths from system property "java.class.path" are added to the annotation processor path. Defaults
         * to false.
         */
        @Value.Default default boolean addEnvironmentToAnnotationProcessorPaths() {
            return false;
        }


        Optional<String> release();


        ResourcePath sourceFileOutputDirectory();

        ResourcePath classFileOutputDirectory();


        @Value.Default default boolean reportWarnings() {return true;}

        @Value.Default default boolean emitDebuggingAttributes() {return true;}


        List<String> additionalOptions();


        List<Supplier<?>> originTasks();

        Set<Serializable> shouldExecWhenAffectedTags();

        Optional<Serializable> key();
    }


    private final mb.pie.task.java.JavaCompiler compiler;

    public CompileJava(mb.pie.task.java.JavaCompiler compiler) {
        this.compiler = compiler;
    }

    public CompileJava() {
        this(new JdkJavaCompiler());
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public KeyedMessages exec(ExecContext context, Input input) throws Exception {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final ArrayList<Sources> allSources = new ArrayList<>();
        input.sources().ifPresent(allSources::add);
        for(final Supplier<Result<Sources, ?>> sourceTask : input.sourceTasks()) {
            context.require(sourceTask).ifElse(
                allSources::add,
                e -> messagesBuilder.addMessage("Failed to get Java sources from '" + sourceTask + "'", e, Severity.Error)
            );
        }
        for(final Supplier<?> originTask : input.originTasks()) {
            context.require(originTask);
        }

        final ArrayList<String> classPaths = input.classPathSuppliers().stream()
            .flatMap(s -> context.require(s).stream())
            .map(File::toString)
            .collect(Collectors.toCollection(ArrayList::new));
        if(input.addEnvironmentToClassPaths()) {
            final @Nullable String envClassPath = System.getProperty("java.class.path");
            if(envClassPath != null) {
                final String[] entries = envClassPath.split(File.pathSeparator);
                Collections.addAll(classPaths, entries);
            }
        }

        final ArrayList<String> annotationProcessorPaths = input.annotationProcessorPathSuppliers().stream()
            .flatMap(s -> context.require(s).stream())
            .map(File::toString)
            .collect(Collectors.toCollection(ArrayList::new));
        if(input.addEnvironmentToAnnotationProcessorPaths()) {
            final @Nullable String envClassPath = System.getProperty("java.class.path");
            if(envClassPath != null) {
                final String[] entries = envClassPath.split(File.pathSeparator);
                Collections.addAll(annotationProcessorPaths, entries);
            }
        }

        return compiler.compile(
            context,
            ListView.of(allSources),
            ListView.of(classPaths),
            ListView.of(annotationProcessorPaths),
            input.release().orElse(null),
            input.sourceFileOutputDirectory(),
            input.classFileOutputDirectory(),
            input.reportWarnings(),
            input.emitDebuggingAttributes(),
            ListView.of(input.additionalOptions())
        );
    }

    @Override public boolean shouldExecWhenAffected(Input input, Set<?> tags) {
        return tags.isEmpty() || input.shouldExecWhenAffectedTags().isEmpty() || !Collections.disjoint(input.shouldExecWhenAffectedTags(), tags);
    }
}
