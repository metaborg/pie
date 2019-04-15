package mb.pie.runtime.store;

import mb.pie.api.Output;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.ResourceRequireDep;
import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.StoreWriteTxn;
import mb.pie.api.TaskData;
import mb.pie.api.TaskKey;
import mb.pie.api.TaskRequireDep;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStore implements Store, StoreReadTxn, StoreWriteTxn {
    private final ConcurrentHashMap<TaskKey, Serializable> inputs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<TaskKey, Output> outputs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<TaskKey, ArrayList<TaskRequireDep>> taskReqs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<TaskKey, Set<TaskKey>> callersOf = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<TaskKey, ArrayList<ResourceRequireDep>> fileReqs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ResourceKey, Set<TaskKey>> requireesOf = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<TaskKey, ArrayList<ResourceProvideDep>> fileGens = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ResourceKey, TaskKey> generatorOf = new ConcurrentHashMap<>();


    @Override public @Nullable Serializable input(TaskKey key) {
        return inputs.get(key);
    }

    @Override public void setInput(TaskKey key, Serializable input) {
        inputs.put(key, input);
    }

    @Override public @Nullable Output output(TaskKey key) {
        final @Nullable Output wrapper = outputs.get(key);
        if(wrapper != null) {
            return new Output(wrapper.output);
        } else {
            return null;
        }
    }

    @Override public void setOutput(TaskKey key, @Nullable Serializable output) {
        // ConcurrentHashMap does not support null values, so wrap outputs (which can be null) into an Output object.
        outputs.put(key, new Output(output));
    }

    @Override public ArrayList<TaskRequireDep> taskRequires(TaskKey key) {
        return getOrEmptyArrayList(taskReqs, key);
    }

    @Override public Set<TaskKey> callersOf(TaskKey key) {
        return getOrPutEmptyConcurrentHashSet(callersOf, key);
    }

    @Override public void setTaskRequires(TaskKey key, ArrayList<TaskRequireDep> taskRequires) {
        // Remove old task requirements.
        final @Nullable ArrayList<TaskRequireDep> oldTaskReqs = this.taskReqs.remove(key);
        if(oldTaskReqs != null) {
            for(TaskRequireDep taskReq : oldTaskReqs) {
                getOrPutEmptyConcurrentHashSet(callersOf, taskReq.callee).remove(key);
            }
        }
        // Add new task requirements.
        this.taskReqs.put(key, taskRequires);
        for(TaskRequireDep taskReq : taskRequires) {
            getOrPutEmptyConcurrentHashSet(callersOf, taskReq.callee).add(key);
        }
    }

    @Override public ArrayList<ResourceRequireDep> resourceRequires(TaskKey key) {
        return getOrEmptyArrayList(fileReqs, key);
    }

    @Override public Set<TaskKey> requireesOf(ResourceKey key) {
        return getOrPutEmptyConcurrentHashSet(requireesOf, key);
    }

    @Override public void setResourceRequires(TaskKey key, ArrayList<ResourceRequireDep> resourceRequires) {
        // Remove old resource requirements.
        final @Nullable ArrayList<ResourceRequireDep> oldFileReqs = this.fileReqs.remove(key);
        if(oldFileReqs != null) {
            for(ResourceRequireDep fileReq : oldFileReqs) {
                getOrPutEmptyConcurrentHashSet(requireesOf, fileReq.key).remove(key);
            }
        }
        // Add new resource requirements.
        this.fileReqs.put(key, resourceRequires);
        for(ResourceRequireDep fileReq : resourceRequires) {
            getOrPutEmptyConcurrentHashSet(requireesOf, fileReq.key).add(key);
        }
    }

    @Override public ArrayList<ResourceProvideDep> resourceProvides(TaskKey key) {
        return getOrEmptyArrayList(fileGens, key);
    }

    @Override public @Nullable TaskKey providerOf(ResourceKey key) {
        return generatorOf.get(key);
    }

    @Override public void setResourceProvides(TaskKey key, ArrayList<ResourceProvideDep> resourceProvides) {
        // Remove old resource providers.
        final @Nullable ArrayList<ResourceProvideDep> oldFileGens = this.fileGens.remove(key);
        if(oldFileGens != null) {
            for(ResourceProvideDep fileGen : oldFileGens) {
                generatorOf.remove(fileGen.key);
            }
        }
        // Add new resource providers.
        this.fileGens.put(key, resourceProvides);
        for(ResourceProvideDep fileGen : resourceProvides) {
            generatorOf.put(fileGen.key, key);
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
        final ArrayList<TaskRequireDep> callReqs = taskRequires(key);
        final ArrayList<ResourceRequireDep> pathReqs = resourceRequires(key);
        final ArrayList<ResourceProvideDep> pathGens = resourceProvides(key);
        return new TaskData(input, output.output, callReqs, pathReqs, pathGens);
    }

    @Override public void setData(TaskKey key, TaskData data) {
        setInput(key, data.input);
        setOutput(key, data.output);
        setTaskRequires(key, data.taskRequires);
        setResourceRequires(key, data.resourceRequires);
        setResourceProvides(key, data.resourceProvides);
    }


    @Override public int numSourceFiles() {
        int numSourceFiles = 0;
        for(ResourceKey file : requireesOf.keySet()) {
            if(!generatorOf.containsKey(file)) {
                ++numSourceFiles;
            }
        }
        return numSourceFiles;
    }


    @Override public void drop() {
        outputs.clear();
        taskReqs.clear();
        callersOf.clear();
        fileReqs.clear();
        requireesOf.clear();
        fileGens.clear();
        generatorOf.clear();
    }


    private static <K, V> Set<V> getOrPutEmptyConcurrentHashSet(ConcurrentHashMap<K, Set<V>> map, K key) {
        return map.computeIfAbsent(key, (k) -> ConcurrentHashMap.newKeySet());
    }

    private static <K, V> ArrayList<V> getOrEmptyArrayList(ConcurrentHashMap<K, ArrayList<V>> map, K key) {
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
