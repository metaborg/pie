package mb.pie.bench.state;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.Properties;
import mb.log.api.Logger;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.Pie;
import mb.pie.api.Task;
import mb.pie.dagger.PieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.task.archive.UnarchiveCommon;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.dagger.DaggerRootResourceServiceComponent;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.spoofax3.dagger.Spoofax3Compiler;
import mb.spoofax.compiler.spoofax3.language.CompilerException;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProject;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompiler;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.spoofax3.standalone.dagger.Spoofax3CompilerStandalone;
import mb.spoofax.compiler.util.Shared;
import mb.resource.dagger.RootResourceServiceComponent;
import mb.resource.dagger.RootResourceServiceModule;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.util.ArrayList;

@State(Scope.Thread)
public class Spoofax3CompilerState {
    // Trial

    private @Nullable Logger logger;
    private @Nullable ClassLoaderResourceRegistry benchClassLoaderResourceRegistry;
    private @Nullable Spoofax3CompilerStandalone spoofax3CompilerStandalone;

    public Spoofax3CompilerState setupTrial(LoggerComponent loggerComponent) {
        if(benchClassLoaderResourceRegistry != null && spoofax3CompilerStandalone != null) {
            throw new IllegalStateException("setupTrial was called before tearDownTrial");
        }
        logger = loggerComponent.getLoggerFactory().create(Spoofax3CompilerState.class);
        logger.trace("Spoofax3CompilerState.setupTrial");
        benchClassLoaderResourceRegistry = new ClassLoaderResourceRegistry("pie.bench", Spoofax3CompilerState.class.getClassLoader());
        final RootResourceServiceComponent resourceServiceComponent = DaggerRootResourceServiceComponent.builder()
            .loggerComponent(loggerComponent)
            .rootResourceServiceModule(new RootResourceServiceModule(benchClassLoaderResourceRegistry))
            .build();
        final Spoofax3Compiler spoofax3Compiler = new Spoofax3Compiler(
            loggerComponent,
            resourceServiceComponent.createChildModule(),
            new PieModule(PieBuilderImpl::new)
        );
        this.spoofax3CompilerStandalone = new Spoofax3CompilerStandalone(spoofax3Compiler);
        return this;
    }

    @SuppressWarnings("ConstantConditions")
    public Pie getPie() {
        return spoofax3CompilerStandalone.pieComponent.getPie();
    }

    public void tearDownTrial() {
        if(logger == null || benchClassLoaderResourceRegistry == null || spoofax3CompilerStandalone == null) {
            throw new IllegalStateException("tearDownTrial was called before calling setupTrial");
        }
        spoofax3CompilerStandalone.close();
        spoofax3CompilerStandalone = null;
        benchClassLoaderResourceRegistry = null;
        logger.trace("Spoofax3CompilerState.tearDownTrial");
        logger = null;
    }


    // Invocation

    private Spoofax3LanguageProjectCompiler.@Nullable Input input;

    public Task<Result<KeyedMessages, CompilerException>> setupInvocation(HierarchicalResource temporaryDirectory) throws IOException {
        if(logger == null || benchClassLoaderResourceRegistry == null || spoofax3CompilerStandalone == null) {
            throw new IllegalStateException("setupInvocation was called before setupTrial");
        }
        if(input != null) {
            throw new IllegalStateException("setupInvocation was called before tearDownInvocation");
        }
        logger.trace("Spoofax3CompilerState.setupInvocation");
        language.unarchiveToTempDirectory(temporaryDirectory, benchClassLoaderResourceRegistry);
        input = language.getCompilerInput(temporaryDirectory);
        // TODO: change to use Spoofax 3 compiler standalone.
        return spoofax3CompilerStandalone.spoofax3Compiler.component.getSpoofax3LanguageProjectCompiler().createTask(input);
    }

    @SuppressWarnings("ConstantConditions")
    public void handleResult(Result<KeyedMessages, CompilerException> result) {
        if(result.isErr()) {
            final CompilerException e = result.getErr();
            logger.error(e.getMessage() + ". " + e.getSubMessage());
            e.getSubMessages().ifPresent((m) -> logger.error(m.toString()));
            if(e.getSubCause() != null) {
                logger.error("", e.getSubCause());
            }
        }
    }

    public ArrayList<Change> getChanges() {
        return language.getChanges();
    }

    @SuppressWarnings("ConstantConditions")
    public void deleteGeneratedFiles() throws IOException {
        final Spoofax3LanguageProject project = input.spoofax3LanguageProject();
        delete(project.generatedResourcesDirectory());
        delete(project.generatedSourcesDirectory());
        delete(project.generatedJavaSourcesDirectory());
        delete(project.generatedStrategoSourcesDirectory());
        delete(project.unarchiveDirectory());
    }

    public void tearDownInvocation() {
        if(logger == null) {
            throw new IllegalStateException("tearDownInvocation was called before setupTrial");
        }
        if(input == null) {
            throw new IllegalStateException("tearDownInvocation was called before setupInvocation");
        }
        input = null;
        logger.trace("Spoofax3CompilerState.tearDownInvocation");
    }


    // Helper methods

    @SuppressWarnings("ConstantConditions")
    private void delete(ResourcePath path) throws IOException {
        spoofax3CompilerStandalone.spoofax3Compiler.resourceServiceComponent.getResourceService().getHierarchicalResource(path).delete(true);
    }


    // Parameters

    @Param("chars") public LanguageKind language;

    public enum LanguageKind {
        chars {
            @Override
            public void unarchiveToTempDirectory(HierarchicalResource tempDir, ClassLoaderResourceRegistry registry) throws IOException {
                copyResourcesToTemporaryDirectory("mb/pie/bench/data/spoofax3/chars", tempDir, registry);
            }

            @Override
            public Spoofax3LanguageProjectCompiler.Input getCompilerInput(HierarchicalResource baseDir) {
                final Shared shared = Shared.builder()
                    .name("Chars")
                    .defaultPackageId("mb.chars")
                    .defaultClassPrefix("Chars")
                    .build();
                final LanguageProject languageProject = LanguageProject.builder().withDefaults(baseDir.getPath(), shared).build();
                final Spoofax3LanguageProject spoofax3LanguageProject = Spoofax3LanguageProject.builder().languageProject(languageProject).build();
                final Spoofax3LanguageProjectCompilerInputBuilder inputBuilder = new Spoofax3LanguageProjectCompilerInputBuilder();
                inputBuilder.withParser();
                inputBuilder.withStyler();
                return inputBuilder.build(new Properties(), shared, spoofax3LanguageProject);
            }

            @Override public ArrayList<Change> getChanges() {
                final ArrayList<Change> changes = new ArrayList<>();
                changes.add(changesState -> "no_change");
                return changes;
            }
        },
        calc {
            @Override
            public void unarchiveToTempDirectory(HierarchicalResource tempDir, ClassLoaderResourceRegistry registry) throws IOException {
                copyResourcesToTemporaryDirectory("mb/pie/bench/data/spoofax3/calc", tempDir, registry);
            }

            @Override
            public Spoofax3LanguageProjectCompiler.Input getCompilerInput(HierarchicalResource baseDir) {
                final Shared shared = Shared.builder()
                    .name("Calc")
                    .defaultPackageId("mb.calc")
                    .defaultClassPrefix("Calc")
                    .build();
                final LanguageProject languageProject = LanguageProject.builder().withDefaults(baseDir.getPath(), shared).build();
                final Spoofax3LanguageProject spoofax3LanguageProject = Spoofax3LanguageProject.builder().languageProject(languageProject).build();
                final Spoofax3LanguageProjectCompilerInputBuilder inputBuilder = new Spoofax3LanguageProjectCompilerInputBuilder();
                inputBuilder.withParser();
                inputBuilder.withStyler();
                inputBuilder.withConstraintAnalyzer();
                inputBuilder.withStrategoRuntime();
                return inputBuilder.build(new Properties(), shared, spoofax3LanguageProject);
            }

            @Override public ArrayList<Change> getChanges() {
                final ArrayList<Change> changes = new ArrayList<>();
                changes.add(changesState -> "no_change");
                changes.add(changesState -> {
                    changesState.replaceFirstLiteral("src/main/str/to-java.str", "exp-to-java : False() -> $[false]", "");
                    return "remove_str_false_rule";
                });
                changes.add(changesState -> {
                    changesState.replaceFirstLiteral("src/main/sdf3/start.sdf3", "Exp.False = <false>", "");
                    return "remove_sdf3_false_rule";
                });
                return changes;
            }
        };

        public abstract void unarchiveToTempDirectory(HierarchicalResource tempDir, ClassLoaderResourceRegistry registry) throws IOException;

        public abstract Spoofax3LanguageProjectCompiler.Input getCompilerInput(HierarchicalResource baseDir);

        public abstract ArrayList<Change> getChanges();


        private static void copyResourcesToTemporaryDirectory(String sourceFilesPath, HierarchicalResource temporaryDirectory, ClassLoaderResourceRegistry classLoaderResourceRegistry) throws IOException {
            final ClassLoaderResource sourceFilesDirectory = classLoaderResourceRegistry.getResource(sourceFilesPath);
            final ClassLoaderResourceLocations locations = sourceFilesDirectory.getLocations();
            for(FSResource directory : locations.directories) {
                directory.copyRecursivelyTo(temporaryDirectory);
            }
            for(JarFileWithPath jarFileWithPath : locations.jarFiles) {
                UnarchiveCommon.unarchiveJar(jarFileWithPath.file, temporaryDirectory, false, false);
            }
        }
    }

    @FunctionalInterface public interface Change {
        String applyChange(ChangesState changesState) throws IOException;
    }
}
