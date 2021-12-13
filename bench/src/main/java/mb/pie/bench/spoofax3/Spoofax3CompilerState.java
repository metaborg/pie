package mb.pie.bench.spoofax3;

import mb.common.result.Result;
import mb.common.util.ExceptionPrinter;
import mb.log.api.Logger;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.Pie;
import mb.pie.api.Task;
import mb.pie.bench.state.ChangesState;
import mb.pie.bench.state.ResourcesState;
import mb.pie.dagger.PieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.task.archive.UnarchiveCommon;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.CompileLanguage;
import mb.spoofax.lwb.compiler.CompileLanguageException;
import mb.spoofax.lwb.compiler.dagger.StandaloneSpoofax3Compiler;
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
    private @Nullable ClassLoaderResourceRegistry classLoaderResourceRegistry;
    private @Nullable StandaloneSpoofax3Compiler spoofax3Compiler;

    public Spoofax3CompilerState setupTrial(
        LoggerComponent loggerComponent,
        ResourcesState resourcesState
    ) {
        if(logger != null && classLoaderResourceRegistry != null && spoofax3Compiler != null) {
            throw new IllegalStateException("setupTrial was called before tearDownTrial");
        }
        logger = loggerComponent.getLoggerFactory().create(Spoofax3CompilerState.class);
        logger.trace("Spoofax3CompilerState.setupTrial");
        classLoaderResourceRegistry = resourcesState.getClassLoaderResourceRegistry();
        spoofax3Compiler = new StandaloneSpoofax3Compiler(
            loggerComponent,
            resourcesState.getResourceServiceComponent().createChildModule(),
            new PieModule(PieBuilderImpl::new)
        );
        return this;
    }

    @SuppressWarnings("ConstantConditions")
    public Pie getPie() {
        return spoofax3Compiler.pieComponent.getPie();
    }

    public void tearDownTrial() {
        if(logger == null || classLoaderResourceRegistry == null || spoofax3Compiler == null) {
            throw new IllegalStateException("tearDownTrial was called before calling setupTrial");
        }
        spoofax3Compiler.close();
        spoofax3Compiler = null;
        classLoaderResourceRegistry = null;
        logger.trace("Spoofax3CompilerState.tearDownTrial");
        logger = null;
    }


    // Invocation

    private @Nullable ResourcePath rootDirectory;

    public Task<Result<CompileLanguage.Output, CompileLanguageException>> setupInvocation(HierarchicalResource temporaryDirectory) throws IOException {
        if(logger == null || classLoaderResourceRegistry == null || spoofax3Compiler == null) {
            throw new IllegalStateException("setupInvocation was called before setupTrial");
        }
        if(rootDirectory != null) {
            throw new IllegalStateException("setupInvocation was called before tearDownInvocation");
        }
        logger.trace("Spoofax3CompilerState.setupInvocation");
        language.unarchiveToTempDirectory(temporaryDirectory, classLoaderResourceRegistry);
        rootDirectory = temporaryDirectory.getPath();
        return spoofax3Compiler.compiler.component.getCompileLanguage().createTask(CompileLanguage.Args.builder().rootDirectory(rootDirectory).build());
    }

    @SuppressWarnings("ConstantConditions")
    public void handleResult(Result<CompileLanguage.Output, CompileLanguageException> result) {
        if(result.isErr()) {
            final CompileLanguageException e = result.getErr();
            final ExceptionPrinter exceptionPrinter = new ExceptionPrinter();
            exceptionPrinter.addCurrentDirectoryContext(rootDirectory);
            logger.error(exceptionPrinter.printExceptionToString(e));
        }
    }

    public ArrayList<Change> getChanges() {
        return language.getChanges();
    }

    @SuppressWarnings("ConstantConditions")
    public void deleteGeneratedFiles() throws IOException {
        // TODO: get the directories to delete from the compiler input/output.
        delete(rootDirectory.appendRelativePath("build"));
    }

    public void tearDownInvocation() {
        if(logger == null) {
            throw new IllegalStateException("tearDownInvocation was called before setupTrial");
        }
        if(rootDirectory == null) {
            throw new IllegalStateException("tearDownInvocation was called before setupInvocation");
        }
        rootDirectory = null;
        logger.trace("Spoofax3CompilerState.tearDownInvocation");
    }


    // Helper methods

    @SuppressWarnings("ConstantConditions")
    private void delete(ResourcePath path) throws IOException {
        spoofax3Compiler.compiler.resourceServiceComponent.getResourceService().getHierarchicalResource(path).delete(true);
    }


    // Parameters

    @Param("chars") public LanguageKind language;

    public enum LanguageKind {
        chars {
            @Override
            public void unarchiveToTempDirectory(HierarchicalResource tempDir, ClassLoaderResourceRegistry registry) throws IOException {
                copyResourcesToTemporaryDirectory("mb/pie/bench/data/spoofax3/chars", tempDir, registry);
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

        public abstract ArrayList<Change> getChanges();


        private static void copyResourcesToTemporaryDirectory(String sourceFilesPath, HierarchicalResource temporaryDirectory, ClassLoaderResourceRegistry classLoaderResourceRegistry) throws IOException {
            final ClassLoaderResource sourceFilesDirectory = classLoaderResourceRegistry.getResource(sourceFilesPath);
            final ClassLoaderResourceLocations<FSResource> locations = sourceFilesDirectory.getLocations();
            for(FSResource directory : locations.directories) {
                directory.copyRecursivelyTo(temporaryDirectory);
            }
            for(JarFileWithPath<FSResource> jarFileWithPath : locations.jarFiles) {
                UnarchiveCommon.unarchiveJar(jarFileWithPath.file, temporaryDirectory, false, false);
            }
        }
    }

    @FunctionalInterface public interface Change {
        String applyChange(ChangesState changesState) throws IOException;
    }
}
