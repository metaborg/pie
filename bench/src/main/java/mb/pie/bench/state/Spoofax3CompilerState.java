package mb.pie.bench.state;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.Properties;
import mb.esv.DaggerEsvComponent;
import mb.libspoofax2.DaggerLibSpoofax2Component;
import mb.libstatix.DaggerLibStatixComponent;
import mb.log.noop.NoopLoggerFactory;
import mb.pie.api.Pie;
import mb.pie.api.Task;
import mb.pie.bench.util.ChangeMaker;
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
    // Trial set-up

    private ClassLoaderResourceRegistry benchClassLoaderResourceRegistry;
    private PlatformComponent platformComponent;
    private Spoofax3CompilerComponent spoofax3CompilerComponent;
    private @Nullable HierarchicalResource temporaryDirectory;
    private @Nullable ChangeMaker changeMaker;

    public Spoofax3CompilerState setupTrial(HierarchicalResource temporaryDirectory) throws IOException {
        benchClassLoaderResourceRegistry = new ClassLoaderResourceRegistry("pie.bench", Spoofax3CompilerState.class.getClassLoader());
        platformComponent = DaggerPlatformComponent.builder()
            .loggerFactoryModule(new LoggerFactoryModule(new NoopLoggerFactory()))
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
        language.unarchiveToTempDirectory(temporaryDirectory, benchClassLoaderResourceRegistry);
        this.temporaryDirectory = temporaryDirectory;
        changeMaker = new ChangeMaker(temporaryDirectory);
        return this;
    }

    public Pie getPie() {
        return spoofax3CompilerComponent.getPie();
    }


    // Invocation set-up

    private Spoofax3LanguageProjectCompiler.@Nullable Input input;

    public Task<Result<KeyedMessages, CompilerException>> setupInvocation() {
        if(temporaryDirectory == null || changeMaker == null) {
            throw new IllegalStateException("setupInvocation was called without first calling setupTrial");
        }
        input = language.getCompilerInput(temporaryDirectory);
        changeMaker.reset();
        return spoofax3CompilerComponent.getSpoofax3LanguageProjectCompiler().createTask(input);
    }


    // Invocation hot-path (during measurement)

    @SuppressWarnings("ConstantConditions")
    public void handleResult(Result<KeyedMessages, CompilerException> result) throws Exception {
        if(result.isErr()) {
            final CompilerException e = result.getErr();
            System.out.println(e.getMessage() + ". " + e.getSubMessage());
            e.getSubMessages().ifPresent((System.out::println));
            if(e.getSubCause() != null) {
                e.getSubCause().printStackTrace(System.out);
            }
            throw e;
        }
    }

    public ArrayList<Change> getChanges() {
        return language.getChanges();
    }

    @SuppressWarnings("ConstantConditions")
    public ChangeMaker getChangeMaker() {
        return new ChangeMaker(temporaryDirectory);
    }


    // Invocation tear-down

    public void deleteGeneratedFiles() throws IOException {
        if(input == null) {
            throw new IllegalStateException("deleteGeneratedFiles was called without first calling setupInvocation");
        }
        final Spoofax3LanguageProject project = input.spoofax3LanguageProject();
        delete(project.generatedResourcesDirectory());
        delete(project.generatedSourcesDirectory());
        delete(project.generatedJavaSourcesDirectory());
        delete(project.generatedStrategoSourcesDirectory());
        delete(project.unarchiveDirectory());
    }


    // Trial tear-down

    public void tearDownTrial() throws IOException {

    }


    // Helper methods

    private void delete(ResourcePath path) throws IOException {
        platformComponent.getResourceService().getHierarchicalResource(path).delete(true);
    }


    // Parameters

    @Param("calc") public LanguageKind language;

    public enum LanguageKind {
        calc {
            @Override
            public void unarchiveToTempDirectory(HierarchicalResource temporaryDirectory, ClassLoaderResourceRegistry classLoaderResourceRegistry) throws IOException {
                final ClassLoaderResource sourceFileDirectory = classLoaderResourceRegistry.getResource("mb/pie/bench/data/spoofax3/calc");
                final ClassLoaderResourceLocations locations = sourceFileDirectory.getLocations();
                for(FSResource directory : locations.directories) {
                    directory.copyRecursivelyTo(temporaryDirectory);
                }
                for(JarFileWithPath jarFileWithPath : locations.jarFiles) {
                    UnarchiveCommon.unarchiveJar(jarFileWithPath.file, temporaryDirectory, false, false);
                }
            }

            @Override
            public Spoofax3LanguageProjectCompiler.Input getCompilerInput(HierarchicalResource baseDirectory) {
                final Shared shared = Shared.builder()
                    .name("Calc")
                    .defaultPackageId("mb.calc")
                    .defaultClassPrefix("Calc")
                    .build();
                final LanguageProject languageProject = LanguageProject.builder().withDefaults(baseDirectory.getPath(), shared).build();
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
                changes.add(changeMaker -> {
                    changeMaker.replaceFirstLiteral("src/main/str/to-java.str", "exp-to-java : False() -> $[false]", "");
                    return "remove_str_false_rule";
                });
                changes.add(changeMaker -> {
                    changeMaker.replaceFirstLiteral("src/main/sdf3/start.sdf3", "Exp.False = <false>", "");
                    return "remove_sdf3_false_rule";
                });
                return changes;
            }
        };

        public abstract void unarchiveToTempDirectory(HierarchicalResource tempDirectory, ClassLoaderResourceRegistry classLoaderResourceRegistry) throws IOException;

        public abstract Spoofax3LanguageProjectCompiler.Input getCompilerInput(HierarchicalResource baseDirectory);

        public abstract ArrayList<Change> getChanges();
    }

    @FunctionalInterface public interface Change {
        String applyChange(ChangeMaker changeMaker) throws IOException;
    }
}
