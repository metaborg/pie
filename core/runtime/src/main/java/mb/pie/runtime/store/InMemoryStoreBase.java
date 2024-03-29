package mb.pie.runtime.store;

import mb.pie.api.Observability;
import mb.pie.api.Output;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.ResourceRequireDep;
import mb.pie.api.SerializableConsumer;
import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.StoreWriteTxn;
import mb.pie.api.Task;
import mb.pie.api.TaskData;
import mb.pie.api.TaskDeps;
import mb.pie.api.TaskKey;
import mb.pie.api.TaskRequireDep;
import mb.pie.runtime.exec.BottomUpShared;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class InMemoryStoreBase implements Store, StoreReadTxn, StoreWriteTxn, Serializable {
    protected final HashMap<TaskKey, Serializable> taskInputs = new HashMap<>();
    protected final HashMap<TaskKey, @Nullable Serializable> taskInternalObjects = new HashMap<>();
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

    protected final HashMap<TaskKey, SerializableConsumer<Serializable>> callbacks = new HashMap<>();


    @Override public @Nullable Serializable getInput(TaskKey key) {
        return this.taskInputs.get(key);
    }

    private void setInput(TaskKey key, Serializable input) {
        this.taskInputs.put(key, input);
    }


    @Override public @Nullable Serializable getInternalObject(TaskKey key) {
        return this.taskInternalObjects.get(key);
    }

    @Override public void setInternalObject(TaskKey key, @Nullable Serializable obj) {
        this.taskInternalObjects.put(key, obj);
    }

    @Override public void clearInternalObject(TaskKey key) {
        this.taskInternalObjects.remove(key);
    }


    @Override public @Nullable Output getOutput(TaskKey key) {
        final @Nullable Output wrapper = this.taskOutputs.get(key);
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
        return getOrEmptyLinkedHashSet(this.resourceRequireDeps, requirer);
    }

    @Override public Set<TaskKey> getRequirersOf(ResourceKey requiree) {
        return getOrPutEmptyHashSet(this.requireesOf, requiree);
    }


    @Override public Collection<ResourceProvideDep> getResourceProvideDeps(TaskKey provider) {
        return getOrEmptyLinkedHashSet(this.resourceProvideDeps, provider);
    }

    @Override public @Nullable TaskKey getProviderOf(ResourceKey providee) {
        return this.providerOf.get(providee);
    }


    @Override public @Nullable TaskData resetTask(Task<?> task) {
        final TaskKey key = task.key();
        final @Nullable Serializable previousInput = this.taskInputs.put(key, task.input);
        final @Nullable Serializable previousInternalObject = getInternalObject(key);
        final @Nullable Output previousOutput = this.taskOutputs.remove(key);
        final @Nullable Observability previousTaskObservability = this.taskObservability.remove(key);
        // Pass `false` to `removeTaskRequireDepsOf` to not remove `key` from the `callersOf` map, as we want to keep
        // dependencies from other tasks to `key` intact.
        final @Nullable Collection<TaskRequireDep> previousTaskRequireDeps = removeTaskRequiresAndDepsOf(key);
        final @Nullable Collection<ResourceRequireDep> previousResourceRequireDeps = removeResourceRequireDepsOf(key);
        final @Nullable Collection<ResourceProvideDep> previousResourceProvideDeps = removeResourceProvideDepsOf(key);
        if(previousInput != null) {
            return new TaskData(
                previousInput,
                previousInternalObject,
                previousOutput,
                previousTaskObservability != null ? previousTaskObservability : Observability.Unobserved,
                new TaskDeps(
                    previousTaskRequireDeps != null ? previousTaskRequireDeps : Collections.emptySet(),
                    previousResourceRequireDeps != null ? previousResourceRequireDeps : Collections.emptySet(),
                    previousResourceProvideDeps != null ? previousResourceProvideDeps : Collections.emptySet()
                )
            );
        }
        return null;
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
        final @Nullable Serializable internalObject = getInternalObject(key);
        final @Nullable Output output = getOutput(key);
        if(output == null) {
            return null;
        }
        final Observability taskObservability = getTaskObservability(key);
        final Collection<TaskRequireDep> taskRequireDeps = getTaskRequireDeps(key);
        final Collection<ResourceRequireDep> resourceRequireDeps = getResourceRequireDeps(key);
        final Collection<ResourceProvideDep> resourceProvideDeps = getResourceProvideDeps(key);
        return new TaskData(input, internalObject, output, taskObservability, new TaskDeps(taskRequireDeps, resourceRequireDeps, resourceProvideDeps));
    }

    @Override public void restoreData(TaskKey key, @Nullable TaskData data) {
        if(data != null) {
            setInput(key, data.input);
            setInternalObject(key, data.internalObject);
            if(data.hasOutput()) {
                setOutput(key, data.getOutput());
            } else {
                this.taskOutputs.remove(key);
            }
            setTaskObservability(key, data.taskObservability);
        } else {
            this.taskInputs.remove(key);
            clearInternalObject(key);
            this.taskOutputs.remove(key);
            this.taskObservability.remove(key);
        }
        removeTaskRequiresAndDepsOf(key);
        if(data != null) {
            for(TaskRequireDep dep : data.deps.taskRequireDeps) {
                addTaskRequire(key, dep.callee);
                addTaskRequireDep(key, dep);
            }
        } else {
            removeFromCallersOf(key);
        }
        removeResourceRequireDepsOf(key);
        if(data != null) {
            for(ResourceRequireDep dep : data.deps.resourceRequireDeps) {
                addResourceRequireDep(key, dep);
            }
        }
        removeResourceProvideDepsOf(key);
        if(data != null) {
            for(ResourceProvideDep dep : data.deps.resourceProvideDeps) {
                addResourceProvideDep(key, dep);
            }
        }
    }

    @Override public @Nullable TaskData deleteData(TaskKey key) {
        final @Nullable Serializable input = this.taskInputs.remove(key);
        if(input == null) {
            return null;
        }
        final @Nullable Serializable internalObject = this.taskInternalObjects.remove(key);
        final @Nullable Output output = this.taskOutputs.remove(key);
        if(output == null) {
            throw new IllegalStateException("BUG: deleting task data for '" + key + "', but no output was deleted");
        }
        final @Nullable Observability observability = this.taskObservability.remove(key);
        final @Nullable Collection<TaskRequireDep> removedTaskRequires = removeTaskRequiresAndDepsOf(key);
        removeFromCallersOf(key);
        final @Nullable Collection<ResourceRequireDep> removedResourceRequires = removeResourceRequireDepsOf(key);
        final @Nullable Collection<ResourceProvideDep> removedResourceProvides = removeResourceProvideDepsOf(key);
        deferredTasks.remove(key);
        return new TaskData(
            input,
            internalObject,
            output,
            observability != null ? observability : Observability.Unobserved,
            new TaskDeps(
                removedTaskRequires != null ? removedTaskRequires : Collections.emptySet(),
                removedResourceRequires != null ? removedResourceRequires : Collections.emptySet(),
                removedResourceProvides != null ? removedResourceProvides : Collections.emptySet()
            )
        );
    }


    private @Nullable Collection<TaskRequireDep> removeTaskRequiresAndDepsOf(TaskKey key) {
        final @Nullable Collection<TaskKey> removedCallees = this.taskRequires.remove(key);
        // Use the removed tasks from `taskRequires` instead of the removed deps from `taskRequireDeps` here, because
        // when a task requires (calls) another task, this is immediately recorded in `taskRequires`, but only after
        // the call completes in `taskRequireDeps`. This matters because tasks can be cancelled/interrupted, and in that
        // case no dependency will be added to `taskRequireDeps`. Therefore, we should use `taskRequires` to remove
        // entries from `callersOf`.
        if(removedCallees != null) {
            for(final TaskKey removedCallee : removedCallees) {
                final @Nullable Set<TaskKey> callersOfRemovedCallee = this.callersOf.get(removedCallee);
                if(callersOfRemovedCallee != null) {
                    callersOfRemovedCallee.remove(key);
                }
            }
        }
        final @Nullable Collection<TaskRequireDep> removedDeps = this.taskRequireDeps.remove(key);
        return removedDeps;
    }

    private void removeFromCallersOf(TaskKey key) {
        // Ensure that `key` is removed from `callersOf`, even though `callersOf.get(key)` may return an empty list,
        // because an empty list has different semantics than not being in the mapping at all: if
        // `callersOf.get(key)` returns an empty list, then key is eligible for garbage collection through
        // `getTasksWithoutCallers`, but that would fail when this task does not exist anymore.
        this.callersOf.remove(key);
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


    @Override public @Nullable SerializableConsumer<Serializable> getCallback(TaskKey key) {
        return callbacks.get(key);
    }

    @Override public void setCallback(TaskKey key, SerializableConsumer<Serializable> callback) {
        callbacks.put(key, callback);
    }

    @Override public void removeCallback(TaskKey key) {
        callbacks.remove(key);
    }

    @Override public void dropCallbacks() {
        callbacks.clear();
    }


    @Override public void drop() {
        taskInputs.clear();
        taskInternalObjects.clear();
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
        callbacks.clear();
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
