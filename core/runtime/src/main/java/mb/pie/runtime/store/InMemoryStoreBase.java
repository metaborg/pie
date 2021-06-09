package mb.pie.runtime.store;

import mb.pie.api.Observability;
import mb.pie.api.Output;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.ResourceRequireDep;
import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.StoreWriteTxn;
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

    protected final HashMap<TaskKey, Collection<TaskRequireDep>> taskRequires = new HashMap<>();
    protected final HashMap<TaskKey, Set<TaskKey>> callersOf = new HashMap<>();

    protected final HashMap<TaskKey, Collection<ResourceRequireDep>> resourceRequires = new HashMap<>();
    protected final HashMap<ResourceKey, Set<TaskKey>> requireesOf = new HashMap<>();

    protected final HashMap<TaskKey, Collection<ResourceProvideDep>> resourceProvides = new HashMap<>();
    protected final HashMap<ResourceKey, TaskKey> providerOf = new HashMap<>();


    @Override public @Nullable Serializable input(TaskKey key) {
        return taskInputs.get(key);
    }

    @Override public void setInput(TaskKey key, Serializable input) {
        taskInputs.put(key, input);
    }


    @Override public @Nullable Output output(TaskKey key) {
        final @Nullable Output wrapper = taskOutputs.get(key);
        if(wrapper != null) {
            return new Output(wrapper.output);
        } else {
            return null;
        }
    }

    @Override public void setOutput(TaskKey key, @Nullable Serializable output) {
        // ConcurrentHashMap does not support null values, so wrap outputs (which can be null) into an Output object.
        taskOutputs.put(key, new Output(output));
    }


    @Override public Observability taskObservability(TaskKey key) {
        return taskObservability.getOrDefault(key, Observability.Unobserved);
    }

    @Override public void setTaskObservability(TaskKey key, Observability observability) {
        taskObservability.put(key, observability);
    }


    @Override public Collection<TaskRequireDep> taskRequires(TaskKey key) {
        return getOrEmptyLinkedHashSet(taskRequires, key);
    }

    @Override public Set<TaskKey> callersOf(TaskKey key) {
        return getOrPutEmptyHashSet(callersOf, key);
    }

    @Override public boolean requiresTransitively(TaskKey caller, TaskKey callee) {
        return BottomUpShared.hasTransitiveTaskReq(caller, callee, this);
    }

    @Override public boolean hasDependencyOrderBefore(TaskKey caller, TaskKey callee) {
        // Note: Naive implementation, better implementation to be overridden in superclass.
        return BottomUpShared.hasTransitiveTaskReq(caller, callee, this);
    }

    @Override public void setTaskRequires(TaskKey caller, Collection<TaskRequireDep> newDeps) {
        // Remove old task requirements.
        final @Nullable Collection<TaskRequireDep> oldTaskDeps = this.taskRequires.remove(caller);
        if(oldTaskDeps != null) {
            for(TaskRequireDep oldDep : oldTaskDeps) {
                getOrPutEmptyHashSet(callersOf, oldDep.callee).remove(caller);
            }
        }
        // Add new task requirements.
        this.taskRequires.put(caller, newDeps);
        for(TaskRequireDep taskRequire : newDeps) {
            getOrPutEmptyHashSet(callersOf, taskRequire.callee).add(caller);
        }
    }


    @Override public Collection<ResourceRequireDep> resourceRequires(TaskKey key) {
        return getOrEmptyLinkedHashSet(resourceRequires, key);
    }

    @Override public Set<TaskKey> requireesOf(ResourceKey key) {
        return getOrPutEmptyHashSet(requireesOf, key);
    }

    @Override public void setResourceRequires(TaskKey key, Collection<ResourceRequireDep> resourceRequires) {
        // Remove old resource requirements.
        final @Nullable Collection<ResourceRequireDep> oldResourceRequires = this.resourceRequires.remove(key);
        if(oldResourceRequires != null) {
            for(ResourceRequireDep resourceRequire : oldResourceRequires) {
                getOrPutEmptyHashSet(requireesOf, resourceRequire.key).remove(key);
            }
        }
        // Add new resource requirements.
        this.resourceRequires.put(key, resourceRequires);
        for(ResourceRequireDep resourceRequire : resourceRequires) {
            getOrPutEmptyHashSet(requireesOf, resourceRequire.key).add(key);
        }
    }


    @Override public Collection<ResourceProvideDep> resourceProvides(TaskKey key) {
        return getOrEmptyLinkedHashSet(resourceProvides, key);
    }

    @Override public @Nullable TaskKey providerOf(ResourceKey key) {
        return providerOf.get(key);
    }

    @Override public void setResourceProvides(TaskKey key, Collection<ResourceProvideDep> resourceProvides) {
        // Remove old resource providers.
        final @Nullable Collection<ResourceProvideDep> oldResourceProvides = this.resourceProvides.remove(key);
        if(oldResourceProvides != null) {
            for(ResourceProvideDep resourceProvide : oldResourceProvides) {
                providerOf.remove(resourceProvide.key);
            }
        }
        // Add new resource providers.
        this.resourceProvides.put(key, resourceProvides);
        for(ResourceProvideDep resourceProvide : resourceProvides) {
            providerOf.put(resourceProvide.key, key);
        }
    }


    @Override public @Nullable TaskData data(TaskKey key) {
        final @Nullable Serializable input = input(key);
        if(input == null) {
            return null;
        }
        final @Nullable Output output = output(key);
        if(output == null) {
            return null;
        }
        final Observability taskObservability = taskObservability(key);
        final Collection<TaskRequireDep> taskRequires = taskRequires(key);
        final Collection<ResourceRequireDep> resourceRequires = resourceRequires(key);
        final Collection<ResourceProvideDep> resourceProvides = resourceProvides(key);
        return new TaskData(input, output.output, taskObservability, taskRequires, resourceRequires, resourceProvides);
    }


    @Override public void setData(TaskKey key, TaskData data) {
        setInput(key, data.input);
        setOutput(key, data.output);
        setTaskObservability(key, data.taskObservability);
        setTaskRequires(key, data.taskRequires);
        setResourceRequires(key, data.resourceRequires);
        setResourceProvides(key, data.resourceProvides);
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

        final @Nullable Collection<TaskRequireDep> removedTaskRequires = taskRequires.remove(key);
        if(removedTaskRequires != null) {
            for(final TaskRequireDep taskRequire : removedTaskRequires) {
                final @Nullable Set<TaskKey> callers = callersOf.get(taskRequire.callee);
                if(callers != null) {
                    callers.remove(key);
                }
            }
        }
        callersOf.remove(key);

        final @Nullable Collection<ResourceRequireDep> removedResourceRequires = resourceRequires.remove(key);
        if(removedResourceRequires != null) {
            for(final ResourceRequireDep resourceRequire : removedResourceRequires) {
                final @Nullable Set<TaskKey> requirees = requireesOf.get(resourceRequire.key);
                if(requirees != null) {
                    requirees.remove(key);
                }
            }
        }

        final @Nullable Collection<ResourceProvideDep> removedResourceProvides = resourceProvides.remove(key);
        if(removedResourceProvides != null) {
            for(final ResourceProvideDep resourceProvide : removedResourceProvides) {
                providerOf.remove(resourceProvide.key);
            }
        }

        return new TaskData(
            input,
            output.output,
            observability != null ? observability : Observability.Unobserved,
            removedTaskRequires != null ? removedTaskRequires : new LinkedHashSet<>(),
            removedResourceRequires != null ? removedResourceRequires : new LinkedHashSet<>(),
            removedResourceProvides != null ? removedResourceProvides : new LinkedHashSet<>()
        );
    }


    @Override public Set<TaskKey> tasksWithoutCallers() {
        return callersOf
            .entrySet()
            .stream()
            .filter((e) -> e.getValue().isEmpty())
            .map(Entry::getKey)
            .collect(Collectors.toCollection(HashSet::new));
    }


    @Override public int numSourceFiles() {
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
        callersOf.clear();
        resourceRequires.clear();
        requireesOf.clear();
        resourceProvides.clear();
        providerOf.clear();
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
