package mb.pie.task.java;

import mb.common.message.KeyedMessages;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;

public interface JavaCompiler {
    KeyedMessages compile(
        ExecContext context,
        ListView<CompileJava.Sources> sources,
        ListView<String> classPaths,
        ListView<String> annotationProcessorPaths,
        @Nullable String release,
        ResourcePath sourceFileOutputDirectory,
        ResourcePath classFileOutputDirectory,
        boolean reportWarnings,
        boolean emitDebuggingAttributes,
        ListView<String> additionalOptions
    ) throws IOException;
}
