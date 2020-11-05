package mb.pie.bench.state;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.Properties;
import mb.esv.DaggerEsvComponent;
import mb.libspoofax2.DaggerLibSpoofax2Component;
import mb.libstatix.DaggerLibStatixComponent;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.Pie;
import mb.pie.api.Task;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.task.archive.UnarchiveCommon;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.DaggerSdf3Component;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.spoofax3.dagger.DaggerSpoofax3CompilerComponent;
import mb.spoofax.compiler.spoofax3.dagger.Spoofax3CompilerComponent;
import mb.spoofax.compiler.spoofax3.dagger.Spoofax3CompilerModule;
import mb.spoofax.compiler.spoofax3.language.CompilerException;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProject;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompiler;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.core.platform.DaggerPlatformComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.spoofax.core.platform.ResourceRegistriesModule;
import mb.statix.DaggerStatixComponent;
import mb.str.DaggerStrategoComponent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@State(Scope.Thread)
public class Spoofax3CompilerState {
    // Trial

    private @Nullable Logger logger;
    private @Nullable ClassLoaderResourceRegistry benchClassLoaderResourceRegistry;
    private @Nullable PlatformComponent platformComponent;
    private @Nullable Spoofax3CompilerComponent spoofax3CompilerComponent;

    public Spoofax3CompilerState setupTrial(LoggerFactory loggerFactory) {
        if(benchClassLoaderResourceRegistry != null && platformComponent != null && spoofax3CompilerComponent != null) {
            throw new IllegalStateException("setupTrial was called before tearDownTrial");
        }
        logger = loggerFactory.create(Spoofax3CompilerState.class);
        logger.trace("Spoofax3CompilerState.setupTrial");
        benchClassLoaderResourceRegistry = new ClassLoaderResourceRegistry("pie.bench", Spoofax3CompilerState.class.getClassLoader());
        platformComponent = DaggerPlatformComponent.builder()
            .loggerFactoryModule(new LoggerFactoryModule(loggerFactory))
            .resourceRegistriesModule(new ResourceRegistriesModule(benchClassLoaderResourceRegistry))
            .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
            .build();
        spoofax3CompilerComponent = DaggerSpoofax3CompilerComponent.builder()
            .spoofax3CompilerModule(new Spoofax3CompilerModule(new TemplateCompiler(StandardCharsets.UTF_8)))
            .platformComponent(platformComponent)
            .sdf3Component(DaggerSdf3Component.builder().platformComponent(platformComponent).build())
            .strategoComponent(DaggerStrategoComponent.builder().platformComponent(platformComponent).build())
            .esvComponent(DaggerEsvComponent.builder().platformComponent(platformComponent).build())
            .statixComponent(DaggerStatixComponent.builder().platformComponent(platformComponent).build())
            .libSpoofax2Component(DaggerLibSpoofax2Component.builder().platformComponent(platformComponent).build())
            .libStatixComponent(DaggerLibStatixComponent.builder().platformComponent(platformComponent).build())
            .build();
        return this;
    }

    @SuppressWarnings("ConstantConditions")
    public Pie getPie() {
        return spoofax3CompilerComponent.getPie();
    }

    public void tearDownTrial() {
        if(logger == null || benchClassLoaderResourceRegistry == null || platformComponent == null || spoofax3CompilerComponent == null) {
            throw new IllegalStateException("tearDownTrial was called before calling setupTrial");
        }
        benchClassLoaderResourceRegistry = null;
        platformComponent = null;
        spoofax3CompilerComponent = null;
        logger.trace("Spoofax3CompilerState.tearDownTrial");
        logger = null;
    }


    // Invocation

    private Spoofax3LanguageProjectCompiler.@Nullable Input input;

    public Task<Result<KeyedMessages, CompilerException>> setupInvocation(HierarchicalResource temporaryDirectory) throws IOException {
        if(logger == null || benchClassLoaderResourceRegistry == null || spoofax3CompilerComponent == null) {
            throw new IllegalStateException("setupInvocation was called before setupTrial");
        }
        if(input != null) {
            throw new IllegalStateException("setupInvocation was called before tearDownInvocation");
        }
        logger.trace("Spoofax3CompilerState.setupInvocation");
        language.unarchiveToTempDirectory(temporaryDirectory, benchClassLoaderResourceRegistry);
        input = language.getCompilerInput(temporaryDirectory);
        return spoofax3CompilerComponent.getSpoofax3LanguageProjectCompiler().createTask(input);
    }

    @SuppressWarnings("ConstantConditions")
    public void handleResult(Result<KeyedMessages, CompilerException> result) throws Exception {
        if(result.isErr()) {
            final CompilerException e = result.getErr();
            logger.trace(e.getMessage() + ". " + e.getSubMessage());
            e.getSubMessages().ifPresent((System.out::println));
            if(e.getSubCause() != null) {
                e.getSubCause().printStackTrace(System.out);
            }
            throw e; // TODO: allow errors
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
        platformComponent.getResourceService().getHierarchicalResource(path).delete(true);
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
                    .defaultPackageId("mb.char")
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
