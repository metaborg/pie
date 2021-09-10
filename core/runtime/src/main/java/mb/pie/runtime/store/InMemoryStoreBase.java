package mb.pie.runtime.store;

import mb.pie.api.Observability;
import mb.pie.api.Output;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.ResourceRequireDep;
import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.StoreWriteTxn;
import mb.pie.api.Task;
import mb.pie.api.TaskData;
import mb.pie.api.TaskKey;
import mb.pie.api.TaskRequireDep;
import mb.pie.runtime.exec.BottomUpShared;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.resource.WritableResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class InMemoryStoreBase implements Store, StoreReadTxn, StoreWriteTxn, Serializable {
    protected final HashMap<TaskKey, Serializable> taskInputs = new HashMap<>();
    protected final HashMap<TaskKey, Output> taskOutputs = new HashMap<>();

    protected final HashMap<TaskKey, Observability> taskObservability = new HashMap<>();

    protected final HashMap<TaskKey, Collection<TaskKey>> taskRequires = new HashMap<>();
    protected final HashMap<TaskKey, Collection<TaskRequireDep>> taskRequireDeps = new HashMap<>();
    protected final HashMap<TaskKey, Set<TaskKey>> callersOf = new HashMap<>();

    protected final HashMap<TaskKey, Collection<ResourceRequireDep>> resourceRequireDeps = new HashMap<>();
    protected final HashMap<ResourceKey, Set<TaskKey>> requireesOf = new HashMap<>();

    protected final HashMap<TaskKey, Collection<ResourceProvideDep>> resourceProvideDeps = new HashMap<>();
    protected final HashMap<ResourceKey, TaskKey> providerOf = new HashMap<>();

    protected final HashSet<TaskKey> deferredTasks = new HashSet<>();


    @Override public @Nullable Serializable getInput(TaskKey key) {
        return taskInputs.get(key);
    }


    @Override public @Nullable Output getOutput(TaskKey key) {
        final @Nullable Output wrapper = taskOutputs.get(key);
        if(wrapper != null) {
            return new Output(wrapper.output);
        } else {
            return null;
        }
    }

    @Override public void setOutput(TaskKey key, @Nullable Serializable output) {
        // ConcurrentHashMap does not support null values, so wrap outputs (which can be null) into an Output object.
        this.taskOutputs.put(key, new Output(output));
    }


    @Override public Observability getTaskObservability(TaskKey key) {
        return this.taskObservability.getOrDefault(key, Observability.Unobserved);
    }

    @Override public void setTaskObservability(TaskKey key, Observability observability) {
        this.taskObservability.put(key, observability);
    }


    @Override public Collection<TaskRequireDep> getTaskRequireDeps(TaskKey caller) {
        return getOrEmptyLinkedHashSet(this.taskRequireDeps, caller);
    }

    @Override public Collection<TaskKey> getRequiredTasks(TaskKey caller) {
        return getOrEmptyLinkedHashSet(this.taskRequires, caller);
    }

    @Override public Set<TaskKey> getCallersOf(TaskKey callee) {
        return getOrPutEmptyHashSet(this.callersOf, callee);
    }

    @Override public boolean doesRequireTransitively(TaskKey caller, TaskKey callee) {
        return BottomUpShared.hasTransitiveTaskReq(caller, callee, this);
    }

    @Override public boolean hasDependencyOrderBefore(TaskKey caller, TaskKey callee) {
        // Note: Naive implementation, better implementation to be overridden in superclass.
        return BottomUpShared.hasTransitiveTaskReq(caller, callee, this);
    }


    @Override public Collection<ResourceRequireDep> getResourceRequireDeps(TaskKey requirer) {
        return getOrEmptyLinkedHashSet(resourceRequireDeps, requirer);
    }

    @Override public Set<TaskKey> getRequirersOf(ResourceKey requiree) {
        return getOrPutEmptyHashSet(requireesOf, requiree);
    }


    @Override public Collection<ResourceProvideDep> getResourceProvideDeps(TaskKey provider) {
        return getOrEmptyLinkedHashSet(resourceProvideDeps, provider);
    }

    @Override public @Nullable TaskKey getProviderOf(ResourceKey providee) {
        return providerOf.get(providee);
    }


    @Override public void resetTask(Task<?> task) {
        final TaskKey key = task.key();
        this.taskInputs.put(key, task.input);
        this.taskOutputs.remove(key);
        this.taskObservability.remove(key);
        // Pass `false` to `removeTaskRequireDepsOf` to not remove `key` from the `callersOf` map, as we want to keep
        // dependencies from other tasks to `key` intact.
        removeTaskRequireDepsOf(key, false);
        removeResourceRequireDepsOf(key);
        removeResourceProvideDepsOf(key);
    }

    @Override public void addTaskRequire(TaskKey caller, TaskKey callee) {
        getOrPutEmptyLinkedHashSet(this.taskRequires, caller).add(callee);
        getOrPutEmptyHashSet(this.callersOf, callee).add(caller);
    }

    @Override public void addTaskRequireDep(TaskKey caller, TaskRequireDep dep) {
        getOrPutEmptyLinkedHashSet(this.taskRequireDeps, caller).add(dep);
    }

    @Override public void addResourceRequireDep(TaskKey requiree, ResourceRequireDep dep) {
        getOrPutEmptyLinkedHashSet(this.resourceRequireDeps, requiree).add(dep);
        getOrPutEmptyHashSet(this.requireesOf, dep.key).add(requiree);
    }

    @Override public void addResourceProvideDep(TaskKey provider, ResourceProvideDep dep) {
        getOrPutEmptyLinkedHashSet(this.resourceProvideDeps, provider).add(dep);
        this.providerOf.put(dep.key, provider);
    }

    @Override public @Nullable TaskData getData(TaskKey key) {
        final @Nullable Serializable input = getInput(key);
        if(input == null) {
            return null;
        }
        final @Nullable Output output = getOutput(key);
        if(output == null) {
            return null;
        }
        final Observability taskObservability = getTaskObservability(key);
        final Collection<TaskRequireDep> taskRequires = getTaskRequireDeps(key);
        final Collection<ResourceRequireDep> resourceRequires = getResourceRequireDeps(key);
        final Collection<ResourceProvideDep> resourceProvides = getResourceProvideDeps(key);
        return new TaskData(input, output.output, taskObservability, taskRequires, resourceRequires, resourceProvides);
    }

    @Override public @Nullable TaskData deleteData(TaskKey key) {
        final @Nullable Serializable input = taskInputs.remove(key);
        if(input == null) {
            return null;
        }
        final @Nullable Output output = taskOutputs.remove(key);
        if(output == null) {
            throw new IllegalStateException("BUG: deleting task data for '" + key + "', but no output was deleted");
        }
        final @Nullable Observability observability = taskObservability.remove(key);
        // Pass `true` to `removeTaskRequireDepsOf` to remove `key` from the `callersOf` map, as `key` will be removed
        // completely, thus it is not possible to call it any more.
        final @Nullable Collection<TaskRequireDep> removedTaskRequires = removeTaskRequireDepsOf(key, true);
        final @Nullable Collection<ResourceRequireDep> removedResourceRequires = removeResourceRequireDepsOf(key);
        final @Nullable Collection<ResourceProvideDep> removedResourceProvides = removeResourceProvideDepsOf(key);
        deferredTasks.remove(key);
        return new TaskData(
            input,
            output.output,
            observability != null ? observability : Observability.Unobserved,
            removedTaskRequires != null ? removedTaskRequires : new LinkedHashSet<>(),
            removedResourceRequires != null ? removedResourceRequires : new LinkedHashSet<>(),
            removedResourceProvides != null ? removedResourceProvides : new LinkedHashSet<>()
        );
    }


    private @Nullable Collection<TaskRequireDep> removeTaskRequireDepsOf(TaskKey key, boolean removeFromCallersOf) {
        this.taskRequires.remove(key);
        final @Nullable Collection<TaskRequireDep> removedDeps = this.taskRequireDeps.remove(key);
        if(removedDeps != null) {
            for(final TaskRequireDep removedDep : removedDeps) {
                final @Nullable Set<TaskKey> callers = this.callersOf.get(removedDep.callee);
                if(callers != null) {
                    callers.remove(key);
                }
            }
        }
        if(removeFromCallersOf) {
            this.callersOf.remove(key);
        }
        return removedDeps;
    }

    private @Nullable Collection<ResourceRequireDep> removeResourceRequireDepsOf(TaskKey key) {
        final @Nullable Collection<ResourceRequireDep> removedDeps = this.resourceRequireDeps.remove(key);
        if(removedDeps != null) {
            for(final ResourceRequireDep removedDep : removedDeps) {
                final @Nullable Set<TaskKey> requirees = this.requireesOf.get(removedDep.key);
                if(requirees != null) {
                    requirees.remove(key);
                }
            }
        }
        return removedDeps;
    }

    private @Nullable Collection<ResourceProvideDep> removeResourceProvideDepsOf(TaskKey key) {
        final @Nullable Collection<ResourceProvideDep> removedDeps = this.resourceProvideDeps.remove(key);
        if(removedDeps != null) {
            for(final ResourceProvideDep removedDep : removedDeps) {
                this.providerOf.remove(removedDep.key);
            }
        }
        return removedDeps;
    }


    @Override public Set<TaskKey> getDeferredTasks() {
        return deferredTasks;
    }

    @Override public void addDeferredTask(TaskKey key) {
        deferredTasks.add(key);
    }

    @Override public void removeDeferredTask(TaskKey key) {
        deferredTasks.remove(key);
    }


    @Override public Set<TaskKey> getTasksWithoutCallers() {
        return callersOf
            .entrySet()
            .stream()
            .filter((e) -> e.getValue().isEmpty())
            .map(Entry::getKey)
            .collect(Collectors.toCollection(HashSet::new));
    }


    @Override public int getNumSourceFiles() {
        int numSourceFiles = 0;
        for(ResourceKey file : requireesOf.keySet()) {
            if(!providerOf.containsKey(file)) {
                ++numSourceFiles;
            }
        }
        return numSourceFiles;
    }


    @Override public void drop() {
        taskInputs.clear();
        taskOutputs.clear();
        taskObservability.clear();
        taskRequires.clear();
        taskRequireDeps.clear();
        callersOf.clear();
        resourceRequireDeps.clear();
        requireesOf.clear();
        resourceProvideDeps.clear();
        providerOf.clear();
        deferredTasks.clear();
    }


    public void serialize(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.writeObject(this);
        objectOutputStream.flush();
    }

    public void serializeToBytes(OutputStream outputStream) throws IOException {
        try(
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)
        ) {
            serialize(objectOutputStream);
            outputStream.flush();
        }
    }

    public void serializeToResource(WritableResource resource) throws IOException {
        try(final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(resource.openWrite())) {
            serializeToBytes(bufferedOutputStream);
            bufferedOutputStream.flush();
        }
    }


    public static InMemoryStoreBase deserialize(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException, ClassCastException {
        return (InMemoryStoreBase)objectInputStream.readObject();
    }

    public static InMemoryStoreBase deserializeFromBytes(InputStream inputStream) throws IOException, ClassNotFoundException, ClassCastException {
        try(final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            return deserialize(objectInputStream);
        }
    }

    public static InMemoryStoreBase deserializeFromResource(ReadableResource resource) throws IOException, ClassNotFoundException, ClassCastException {
        try(final BufferedInputStream bufferedInputStream = new BufferedInputStream(resource.openRead())) {
            return deserializeFromBytes(bufferedInputStream);
        }
    }


    protected static <K, V> Set<V> getOrPutEmptyHashSet(HashMap<K, Set<V>> map, K key) {
        return map.computeIfAbsent(key, (k) -> new HashSet<>());
    }

    protected static <K, V> Collection<V> getOrPutEmptyLinkedHashSet(HashMap<K, Collection<V>> map, K key) {
        return map.computeIfAbsent(key, (k) -> new LinkedHashSet<>());
    }

    protected static <K, V> Collection<V> getOrEmptyLinkedHashSet(HashMap<K, Collection<V>> map, K key) {
        return map.getOrDefault(key, new LinkedHashSet<>());
    }


    @Override public InMemoryStoreBase readTxn() {
        return this;
    }

    @Override public InMemoryStoreBase writeTxn() {
        return this;
    }

    @Override public void sync() {}

    @Override public void close() {}
}
