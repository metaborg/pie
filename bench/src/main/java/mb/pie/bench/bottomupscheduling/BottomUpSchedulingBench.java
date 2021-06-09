package mb.pie.bench.bottomupscheduling;

import mb.common.util.ListView;
import mb.log.api.Logger;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.LambdaTaskDef;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.StatelessSerializableFunction;
import mb.pie.api.TaskDefs;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.bench.state.ChangesState;
import mb.pie.bench.state.LoggerState;
import mb.pie.bench.state.PieState;
import mb.pie.bench.state.ResourcesState;
import mb.pie.bench.state.TemporaryDirectoryState;
import mb.pie.bench.util.GarbageCollection;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("NotNullFieldNotInitialized")
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class BottomUpSchedulingBench {
    // Parameters

    @Param({"40"}) public int numFrontFiles;
    @Param({"2000"}) public int numFunctionsPerFrontFile;
    @Param({"4"}) public int changeFrontFileEvery;
    @Param({"100"}) public int changeBackFileEvery;


    // Trial

    protected LoggerState loggerState;
    protected LoggerComponent loggerComponent;
    protected Logger logger;
    protected ResourcesState resourcesState;
    protected TemporaryDirectoryState temporaryDirectoryState;
    protected LambdaTaskDef<ResourcePath, ListView<ResourcePath>> buildTaskDef;
    protected PieState pieState;

    @Setup(Level.Trial)
    public void setupTrial(
        LoggerState loggerState,
        ResourcesState resourcesState,
        TemporaryDirectoryState temporaryDirectoryState,
        PieState pieState
    ) throws IOException {
        this.loggerState = loggerState;
        this.loggerComponent = loggerState.setupTrial();
        this.logger = loggerComponent.getLoggerFactory().create(BottomUpSchedulingBench.class);
        this.logger.trace("{}.setupTrial", getClass().getName());
        this.resourcesState = resourcesState.setupTrial(loggerComponent);
        this.temporaryDirectoryState = temporaryDirectoryState;
        final HierarchicalResource temporaryDirectory = temporaryDirectoryState.setupTrial();
        this.pieState = pieState.setupTrial(loggerComponent, resourcesState.getResourceService(), temporaryDirectory, createTasks());
    }

    @SuppressWarnings("resource")
    public TaskDefs createTasks() {
        final MapTaskDefs taskDefs = new MapTaskDefs();
        final LambdaTaskDef<ResourceKey, ListView<String>> front = new LambdaTaskDef<>("Front", (context, file) -> {
            try {
                final String[] functions = context.require(file).readString().split("\n");
                return ListView.of(functions);
            } catch(IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        taskDefs.add(front);

        final LambdaTaskDef<ResourceKey, Integer> checkFile = new LambdaTaskDef<>("CheckFile", (context, file) -> {
            return context.require(front, file).stream().map(String::length).reduce(Integer::sum).orElse(0);
        });
        taskDefs.add(checkFile);

        final LambdaTaskDef<ResourcePath, ListView<String>> resolve = new LambdaTaskDef<>("Resolve", (context, dir) -> {
            try(final Stream<? extends HierarchicalResource> stream = context.require(dir.appendRelativePath("front")).walk(ResourceMatcher.ofFile())) {
                return ListView.of(stream.map(HierarchicalResource::getPath).flatMap(file -> context.require(front, file).stream()).collect(Collectors.toList()));
            } catch(IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        taskDefs.add(resolve);

        final LambdaTaskDef<ResourcePath, Integer> check = new LambdaTaskDef<>("Check", (context, dir) -> {
            return context.require(resolve, dir).stream().map(String::length).reduce(Integer::sum).orElse(0);
        });
        taskDefs.add(check);

        final StatelessSerializableFunction<Serializable, Serializable> checkLteZeroFunction = new StatelessSerializableFunction<Serializable, Serializable>() {
            @Override public Serializable apply(Serializable checkResult) {
                return (Integer)checkResult <= 0;
            }
        };

        final LambdaTaskDef<BackInput, ResourcePath> back = new LambdaTaskDef<>("Back", (context, input) -> {
            if(context.require(check, input.rootDirectory, OutputStampers.funcEquals(checkLteZeroFunction)) <= 0) {
                return null;
            }
            final HierarchicalResource file = context.getHierarchicalResource(input.rootDirectory.appendRelativePath("back/" + input.function + ".back"));
            try {
                file.ensureFileExists().writeString(input.function.toUpperCase(Locale.ROOT));
                context.provide(file);
            } catch(IOException e) {
                throw new UncheckedIOException(e);
            }
            return file.getKey();
        });
        taskDefs.add(back);

        final LambdaTaskDef<ResourcePath, ListView<ResourcePath>> build = new LambdaTaskDef<>("Build", (context, dir) -> {
            if(context.require(check, dir, OutputStampers.funcEquals(checkLteZeroFunction)) <= 0) {
                return ListView.of();
            }
            final ListView<String> functions = context.require(resolve, dir);
            return ListView.of(functions.stream().map(function -> context.require(back, new BackInput(dir, function))).collect(Collectors.toList()));
        });
        taskDefs.add(build);
        buildTaskDef = build;

        return taskDefs;
    }

    @SuppressWarnings("ConstantConditions")
    @TearDown(Level.Trial)
    public void tearDownTrial() throws IOException {
        pieState.tearDownTrial();
        pieState = null;
        temporaryDirectoryState.tearDownTrial();
        temporaryDirectoryState = null;
        resourcesState.tearDownTrial();
        resourcesState = null;
        loggerComponent = null;
        logger.trace("{}.tearDownTrial", getClass().getName());
        logger = null;
    }


    // Invocation

    protected ChangesState changesState;
    protected ResourcePath rootDirectory;
    protected ArrayList<HierarchicalResource> frontFiles = new ArrayList<>();

    @Setup(Level.Invocation)
    public void setupInvocation(ChangesState changesState) throws Exception {
        logger.trace("{}.setupInvocation", getClass().getName());
        final HierarchicalResource temporaryDirectory = temporaryDirectoryState.setupInvocation();
        rootDirectory = temporaryDirectory.getPath();
        this.changesState = changesState.setupInvocation(loggerComponent, temporaryDirectory);
        this.pieState.setupInvocation();

        // Create frontend files and store them in `frontFiles`.
        frontFiles = new ArrayList<>();
        final HierarchicalResource frontDir = temporaryDirectory.appendAsRelativePath("front");
        for(int iFile = 0; iFile < numFrontFiles; iFile++) {
            final HierarchicalResource file = frontDir.appendAsRelativePath(iFile + ".front");
            frontFiles.add(file);
            final StringBuilder sb = new StringBuilder();
            for(int iFunction = 0; iFunction < numFunctionsPerFrontFile; iFunction++) {
                sb
                    .append(iFile)
                    .append("f")
                    .append(iFunction)
                    .append('\n');
            }
            file.ensureFileExists().writeString(sb.toString());
        }
    }

    @SuppressWarnings("ConstantConditions") @TearDown(Level.Invocation)
    public void tearDownInvocation() throws Exception {
        this.changesState.tearDownInvocation();
        this.changesState = null;

        this.temporaryDirectoryState.tearDownInvocation();
        this.pieState.tearDownInvocation();
        logger.trace("{}.tearDownInvocation", getClass().getName());
    }


    // Benchmarks

    @Benchmark
    public void bottomUp(Blackhole blackhole) throws Exception {
        final ListView<ResourcePath> output = pieState.requireTopDownInNewSession(buildTaskDef.createTask(rootDirectory), "0_initial");
        makeChanges(output);
        blackhole.consume(output);
        gc(); // Run garbage collection to make memory usage deterministic.
        pieState.requireBottomUpInNewSession(changesState.getChangedResources(), "1_change_front_file"); // Run build and measure.
    }

    @Benchmark
    public void topDown(Blackhole blackhole) throws Exception {
        final ListView<ResourcePath> output = pieState.requireTopDownInNewSession(buildTaskDef.createTask(rootDirectory), "0_initial");
        makeChanges(output);
        blackhole.consume(output);
        gc(); // Run garbage collection to make memory usage deterministic.
        blackhole.consume(pieState.requireTopDownInNewSession(buildTaskDef.createTask(rootDirectory), "1_change_front_file")); // Run build and measure.
    }

    private void makeChanges(ListView<ResourcePath> output) throws IOException {
        changesState.reset(); // Reset change maker to clear changed resources.
        // Delete some backend files.
        for(int i = 0; i < output.size(); i++) {
            if(i % changeBackFileEvery == 0) {
                changesState.deleteFile(resourcesState.getResourceService().getHierarchicalResource(output.get(i)));
            }
        }
        // Change some frontend files.
        for(int i = 0; i < frontFiles.size(); i++) {
            if(i % changeFrontFileEvery == 0) {
                changesState.replaceAll(frontFiles.get(i), "f", "ff");
            }
        }
    }


    // Helper methods and classes

    private void reset() throws IOException {
        // Delete generated files and PIE storage to reset back to initial state.
        pieState.resetState();
    }

    private void gc() {
        GarbageCollection.run();
    }


    private static class BackInput implements Serializable {
        public final ResourcePath rootDirectory;
        public final String function;

        private BackInput(ResourcePath rootDirectory, String function) {
            this.rootDirectory = rootDirectory;
            this.function = function;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final BackInput backInput = (BackInput)o;
            if(!rootDirectory.equals(backInput.rootDirectory)) return false;
            return function.equals(backInput.function);
        }

        @Override public int hashCode() {
            int result = rootDirectory.hashCode();
            result = 31 * result + function.hashCode();
            return result;
        }

        @Override public String toString() {
            return "BackInput{" +
                "rootDirectory=" + rootDirectory +
                ", function='" + function + '\'' +
                '}';
        }
    }
}
