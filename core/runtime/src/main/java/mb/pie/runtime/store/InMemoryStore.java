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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class InMemoryStore implements Store, StoreReadTxn, StoreWriteTxn, Serializable {
    private final HashMap<TaskKey, Serializable> taskInputs = new HashMap<>();
    private final HashMap<TaskKey, Output> taskOutputs = new HashMap<>();

    private final HashMap<TaskKey, Observability> taskObservability = new HashMap<>();

    private final HashMap<TaskKey, ArrayList<TaskRequireDep>> taskRequires = new HashMap<>();
    private final HashMap<TaskKey, Set<TaskKey>> callersOf = new HashMap<>();

    private final HashMap<TaskKey, ArrayList<ResourceRequireDep>> resourceRequires = new HashMap<>();
    private final HashMap<ResourceKey, Set<TaskKey>> requireesOf = new HashMap<>();

    private final HashMap<TaskKey, ArrayList<ResourceProvideDep>> resourceProvides = new HashMap<>();
    private final HashMap<ResourceKey, TaskKey> providerOf = new HashMap<>();


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


    @Override public ArrayList<TaskRequireDep> taskRequires(TaskKey key) {
        return getOrEmptyArrayList(taskRequires, key);
    }

    @Override public Set<TaskKey> callersOf(TaskKey key) {
        return getOrPutEmptyHashSet(callersOf, key);
    }

    @Override public void setTaskRequires(TaskKey key, ArrayList<TaskRequireDep> taskRequires) {
        // Remove old task requirements.
        final @Nullable ArrayList<TaskRequireDep> oldTaskRequires = this.taskRequires.remove(key);
        if(oldTaskRequires != null) {
            for(TaskRequireDep taskRequire : oldTaskRequires) {
                getOrPutEmptyHashSet(callersOf, taskRequire.callee).remove(key);
            }
        }
        // Add new task requirements.
        this.taskRequires.put(key, taskRequires);
        for(TaskRequireDep taskRequire : taskRequires) {
            getOrPutEmptyHashSet(callersOf, taskRequire.callee).add(key);
        }
    }


    @Override public ArrayList<ResourceRequireDep> resourceRequires(TaskKey key) {
        return getOrEmptyArrayList(resourceRequires, key);
    }

    @Override public Set<TaskKey> requireesOf(ResourceKey key) {
        return getOrPutEmptyHashSet(requireesOf, key);
    }

    @Override public void setResourceRequires(TaskKey key, ArrayList<ResourceRequireDep> resourceRequires) {
        // Remove old resource requirements.
        final @Nullable ArrayList<ResourceRequireDep> oldResourceRequires = this.resourceRequires.remove(key);
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


    @Override public ArrayList<ResourceProvideDep> resourceProvides(TaskKey key) {
        return getOrEmptyArrayList(resourceProvides, key);
    }

    @Override public @Nullable TaskKey providerOf(ResourceKey key) {
        return providerOf.get(key);
    }

    @Override public void setResourceProvides(TaskKey key, ArrayList<ResourceProvideDep> resourceProvides) {
        // Remove old resource providers.
        final @Nullable ArrayList<ResourceProvideDep> oldResourceProvides = this.resourceProvides.remove(key);
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
        final ArrayList<TaskRequireDep> taskRequires = taskRequires(key);
        final ArrayList<ResourceRequireDep> resourceRequires = resourceRequires(key);
        final ArrayList<ResourceProvideDep> resourceProvides = resourceProvides(key);
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

        final @Nullable ArrayList<TaskRequireDep> removedTaskRequires = taskRequires.remove(key);
        if(removedTaskRequires != null) {
            for(final TaskRequireDep taskRequire : removedTaskRequires) {
                final @Nullable Set<TaskKey> callers = callersOf.get(taskRequire.callee);
                if(callers != null) {
                    callers.remove(key);
                }
            }
        }
        callersOf.remove(key);

        final @Nullable ArrayList<ResourceRequireDep> removedResourceRequires = resourceRequires.remove(key);
        if(removedResourceRequires != null) {
            for(final ResourceRequireDep resourceRequire : removedResourceRequires) {
                final @Nullable Set<TaskKey> requirees = requireesOf.get(resourceRequire.key);
                if(requirees != null) {
                    requirees.remove(key);
                }
            }
        }

        final @Nullable ArrayList<ResourceProvideDep> removedResourceProvides = resourceProvides.remove(key);
        if(removedResourceProvides != null) {
            for(final ResourceProvideDep resourceProvide : removedResourceProvides) {
                providerOf.remove(resourceProvide.key);
            }
        }

        return new TaskData(
            input,
            output.output,
            observability != null ? observability : Observability.Unobserved,
            removedTaskRequires != null ? removedTaskRequires : new ArrayList<>(),
            removedResourceRequires != null ? removedResourceRequires : new ArrayList<>(),
            removedResourceProvides != null ? removedResourceProvides : new ArrayList<>()
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


    public static InMemoryStore deserialize(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException, ClassCastException {
        return (InMemoryStore)objectInputStream.readObject();
    }

    public static InMemoryStore deserializeFromBytes(InputStream inputStream) throws IOException, ClassNotFoundException, ClassCastException {
        try(final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            return deserialize(objectInputStream);
        }
    }

    public static InMemoryStore deserializeFromResource(ReadableResource resource) throws IOException, ClassNotFoundException, ClassCastException {
        try(final BufferedInputStream bufferedInputStream = new BufferedInputStream(resource.openRead())) {
            return deserializeFromBytes(bufferedInputStream);
        }
    }


    private static <K, V> Set<V> getOrPutEmptyHashSet(HashMap<K, Set<V>> map, K key) {
        return map.computeIfAbsent(key, (k) -> new HashSet<>());
    }

    private static <K, V> ArrayList<V> getOrEmptyArrayList(HashMap<K, ArrayList<V>> map, K key) {
        return map.getOrDefault(key, new ArrayList<>());
    }


    @Override public InMemoryStore readTxn() {
        return this;
    }

    @Override public InMemoryStore writeTxn() {
        return this;
    }

    @Override public void sync() {}

    @Override public void close() {}


    @Override public String toString() {
        return "InMemoryStore()";
    }
}
