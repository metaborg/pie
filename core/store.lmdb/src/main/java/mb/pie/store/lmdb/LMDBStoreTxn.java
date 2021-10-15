package mb.pie.store.lmdb;

import mb.pie.api.Observability;
import mb.pie.api.Output;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.ResourceRequireDep;
import mb.pie.api.SerializableConsumer;
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
import org.lmdbjava.CursorIterable;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class LMDBStoreTxn implements StoreReadTxn, StoreWriteTxn {
    private final Txn<ByteBuffer> txn;
    private final Dbi<ByteBuffer> inputDb;
    private final Dbi<ByteBuffer> outputDb;
    private final Dbi<ByteBuffer> taskObservabilityDb;
    private final Dbi<ByteBuffer> taskRequiresDb;
    private final Dbi<ByteBuffer> callersOfDb;
    private final Dbi<ByteBuffer> callersOfValuesDb;
    private final Dbi<ByteBuffer> resourceRequiresDb;
    private final Dbi<ByteBuffer> requireesOfDb;
    private final Dbi<ByteBuffer> requireesOfValuesDb;
    private final Dbi<ByteBuffer> resourceProvidesDb;
    private final Dbi<ByteBuffer> providerOfDb;
    private final DbiShared shared;
    private final SerializeUtil serializeUtil;

    LMDBStoreTxn(
        Env<ByteBuffer> env,
        Txn<ByteBuffer> txn,
        boolean isWriteTxn,
        Dbi<ByteBuffer> inputDb,
        Dbi<ByteBuffer> outputDb,
        Dbi<ByteBuffer> taskObservabilityDb,
        Dbi<ByteBuffer> taskRequiresDb,
        Dbi<ByteBuffer> callersOfDb,
        Dbi<ByteBuffer> callersOfValuesDb,
        Dbi<ByteBuffer> resourceRequiresDb,
        Dbi<ByteBuffer> requireesOfDb,
        Dbi<ByteBuffer> requireesOfValuesDb,
        Dbi<ByteBuffer> resourceProvidesDb,
        Dbi<ByteBuffer> providerOfDb,
        SerializeUtil serializeUtil
    ) {
        this.txn = txn;
        this.inputDb = inputDb;
        this.outputDb = outputDb;
        this.taskObservabilityDb = taskObservabilityDb;
        this.taskRequiresDb = taskRequiresDb;
        this.callersOfDb = callersOfDb;
        this.callersOfValuesDb = callersOfValuesDb;
        this.resourceRequiresDb = resourceRequiresDb;
        this.requireesOfDb = requireesOfDb;
        this.requireesOfValuesDb = requireesOfValuesDb;
        this.resourceProvidesDb = resourceProvidesDb;
        this.providerOfDb = providerOfDb;
        this.shared = new DbiShared(env, txn, isWriteTxn, serializeUtil);
        this.serializeUtil = serializeUtil;
    }

    @Override public void close() {
        txn.commit();
        txn.close();
    }


    @Override public @Nullable Serializable getInput(TaskKey key) {
        return De.orElseNull(shared.getOneObject(serializeUtil.serializeHashed(key), inputDb));
    }

    @Override public @Nullable Serializable getInternalObject(TaskKey key) {
        // TODO: implement
        throw new UnsupportedOperationException("getInternalObject has not been implemented for LMDB yet, sorry");
    }

    @Override public @Nullable Output getOutput(TaskKey key) {
        return De.mapOrElseNull(shared.getOneObject(serializeUtil.serializeHashed(key), outputDb), Output::new);
    }

    @Override public Observability getTaskObservability(TaskKey key) {
        return De.orElse(shared.getOne(Observability.class, serializeUtil.serializeHashed(key), taskObservabilityDb), Observability.Unobserved);
    }


    @Override public ArrayList<TaskRequireDep> getTaskRequireDeps(TaskKey caller) {
        return De.orElse(shared.getOne(ArrayList.class, serializeUtil.serializeHashed(caller), taskRequiresDb), new ArrayList<>());
    }

    @Override public Collection<TaskKey> getRequiredTasks(TaskKey caller) {
        // TODO: implement
        throw new UnsupportedOperationException("getRequiredTasks has not been implemented for LMDB yet, sorry");
    }

    @Override public Set<TaskKey> getCallersOf(TaskKey callee) {
        return shared.getMultiple(TaskKey.class, serializeUtil.serializeHashed(callee), callersOfDb, callersOfValuesDb);
    }

    @Override public boolean doesRequireTransitively(TaskKey caller, TaskKey callee) {
        return BottomUpShared.hasTransitiveTaskReq(caller, callee, this); // TODO: replace with more performant alternative.
    }

    @Override public boolean hasDependencyOrderBefore(TaskKey caller, TaskKey callee) {
        return doesRequireTransitively(caller, callee); // TODO: replace with more performant alternative.
    }


    @Override public ArrayList<ResourceRequireDep> getResourceRequireDeps(TaskKey requirer) {
        return De.orElse(shared.getOne(ArrayList.class, serializeUtil.serializeHashed(requirer), resourceRequiresDb), new ArrayList<>());
    }

    @Override public Set<TaskKey> getRequirersOf(ResourceKey requiree) {
        return shared.getMultiple(TaskKey.class, serializeUtil.serializeHashed(requiree), requireesOfDb, requireesOfValuesDb);
    }


    @Override public ArrayList<ResourceProvideDep> getResourceProvideDeps(TaskKey provider) {
        return De.orElse(shared.getOne(ArrayList.class, serializeUtil.serializeHashed(provider), resourceProvidesDb), new ArrayList<>());
    }

    @Override public @Nullable TaskKey getProviderOf(ResourceKey providee) {
        return De.orElse(shared.getOne(TaskKey.class, serializeUtil.serializeHashed(providee), providerOfDb), null);
    }


    @Override public @Nullable TaskData getData(TaskKey key) {
        // OPTO: reuse buffers? is that safe?
        final byte[] keyHashedBytes = serializeUtil.serializeHashedToBytes(key);
        final @Nullable De<Serializable> inputDeserialized = shared.getOneObject(BufferUtil.toBuffer(keyHashedBytes), inputDb);
        if(inputDeserialized == null || inputDeserialized.failed) {
            return null;
        }
        final @Nullable De<@Nullable Serializable> outputDeserialized = shared.getOneObject(BufferUtil.toBuffer(keyHashedBytes), outputDb);
        if(outputDeserialized == null || outputDeserialized.failed) {
            return null;
        }
        final @Nullable De<Observability> taskObservabilityDeserialized = shared.getOne(Observability.class, BufferUtil.toBuffer(keyHashedBytes), taskObservabilityDb);

        final Serializable input = inputDeserialized.deserialized;
        final @Nullable Serializable output = outputDeserialized.deserialized;
        final Observability taskObservability = De.orElse(taskObservabilityDeserialized, Observability.Unobserved);
        final ArrayList<TaskRequireDep> taskRequires = De.orElse(shared.getOne(ArrayList.class, BufferUtil.toBuffer(keyHashedBytes), taskRequiresDb), new ArrayList<>());
        final ArrayList<ResourceRequireDep> resourceRequires = De.orElse(shared.getOne(ArrayList.class, BufferUtil.toBuffer(keyHashedBytes), resourceRequiresDb), new ArrayList<>());
        final ArrayList<ResourceProvideDep> resourceProvides = De.orElse(shared.getOne(ArrayList.class, BufferUtil.toBuffer(keyHashedBytes), resourceProvidesDb), new ArrayList<>());
        return new TaskData(input, getInternalObject(key), output, taskObservability, new TaskDeps(taskRequires, resourceRequires, resourceProvides));
    }


    @Override public Set<TaskKey> getTasksWithoutCallers() {
        // TODO: implement
        throw new UnsupportedOperationException("tasksWithoutCallers has not been implemented for LMDB yet, sorry");
    }

    @Override public int getNumSourceFiles() {
        // Cannot use requireesOfValuesDb, as these are never cleaned up at the moment. Instead use values of resourceRequiresDb.
        final HashSet<ResourceKey> requiredResources = new HashSet<>();
        try(final CursorIterable<ByteBuffer> cursor = resourceRequiresDb.iterate(txn)) {
            for(final CursorIterable.KeyVal<ByteBuffer> keyval : cursor) {
                final ArrayList<ResourceRequireDep> resourceRequires = De.orElse(serializeUtil.deserialize(ArrayList.class, keyval.val()), new ArrayList<>());
                for(ResourceRequireDep resourceRequire : resourceRequires) {
                    requiredResources.add(resourceRequire.key);
                }
            }
        }

        int numSourceFiles = 0;
        for(ResourceKey file : requiredResources) {
            if(!shared.getBool(serializeUtil.serializeHashed(file), providerOfDb)) {
                ++numSourceFiles;
            }
        }
        return numSourceFiles;
    }



//    @Override public void setInput(TaskKey key, Serializable input) {
//        shared.setOne(serializeUtil.serializeHashed(key), serializeUtil.serializeObject(input), inputDb);
//    }

    @Override public void setOutput(TaskKey key, @Nullable Serializable output) {
        shared.setOne(serializeUtil.serializeHashed(key), serializeUtil.serializeObject(output), outputDb);
    }

    @Override public void setTaskObservability(TaskKey key, Observability observability) {
        shared.setOne(serializeUtil.serializeHashed(key), serializeUtil.serialize(observability), taskObservabilityDb);
    }

    @Override public void setInternalObject(TaskKey key, @Nullable Serializable obj) {
        // TODO: implement
        throw new UnsupportedOperationException("setInternalObject has not been implemented for LMDB yet, sorry");
    }

    @Override public void clearInternalObject(TaskKey key) {
        // TODO: implement
        throw new UnsupportedOperationException("clearInternalObject has not been implemented for LMDB yet, sorry");
    }

    @Override public void restoreData(TaskKey key, TaskData data) {
        // TODO: implement
        throw new UnsupportedOperationException("restoreData has not been implemented for LMDB yet, sorry");
    }

//    @Override public void setTaskRequires(TaskKey key, Collection<TaskRequireDep> taskRequires) {
//        // OPTO: reuse buffers? is that safe?
//        final SerializedAndHashed serializedAndHashed = serializeUtil.serializeAndHash(key);
//        final byte[] keyBytes = serializedAndHashed.serialized;
//        final byte[] keyHashedBytes = serializedAndHashed.hashed;
//
//        // Remove old inverse task requirements.
//        final ArrayList<TaskRequireDep> oldTaskRequires = De.orElse(shared.getOne(ArrayList.class, BufferUtil.toBuffer(keyHashedBytes), taskRequiresDb), new ArrayList<>());
//        for(TaskRequireDep taskRequire : oldTaskRequires) {
//            shared.deleteDup(serializeUtil.serializeHashed(taskRequire.callee), BufferUtil.toBuffer(keyHashedBytes), callersOfDb, callersOfValuesDb);
//        }
//
//        // Add new task requirements.
//        shared.setOne(BufferUtil.toBuffer(keyHashedBytes), serializeUtil.serialize(taskRequires), taskRequiresDb);
//        for(TaskRequireDep taskRequire : taskRequires) {
//            shared.setDup(serializeUtil.serializeHashed(taskRequire.callee), BufferUtil.toBuffer(keyBytes), BufferUtil.toBuffer(keyHashedBytes), callersOfDb, callersOfValuesDb);
//        }
//    }

//    @Override public void setResourceRequires(TaskKey key, Collection<ResourceRequireDep> resourceRequires) {
//        // OPTO: reuse buffers? is that safe?
//        final SerializedAndHashed serializedAndHashed = serializeUtil.serializeAndHash(key);
//        final byte[] keyBytes = serializedAndHashed.serialized;
//        final byte[] keyHashedBytes = serializedAndHashed.hashed;
//
//        // Remove old inverse file requirements.
//        final ArrayList<ResourceRequireDep> oldResourceRequires = De.orElse(shared.getOne(ArrayList.class, BufferUtil.toBuffer(keyHashedBytes), resourceRequiresDb), new ArrayList<>());
//        for(ResourceRequireDep resourceRequire : oldResourceRequires) {
//            shared.deleteDup(serializeUtil.serializeHashed(resourceRequire.key), BufferUtil.toBuffer(keyHashedBytes), requireesOfDb, requireesOfValuesDb);
//        }
//
//        // Add new file requirements.
//        shared.setOne(BufferUtil.toBuffer(keyHashedBytes), serializeUtil.serialize(resourceRequires), resourceRequiresDb);
//        for(ResourceRequireDep resourceRequire : resourceRequires) {
//            shared.setDup(serializeUtil.serializeHashed(resourceRequire.key), BufferUtil.toBuffer(keyBytes), BufferUtil.toBuffer(keyHashedBytes), requireesOfDb, requireesOfValuesDb);
//        }
//    }

//    @Override public void setResourceProvides(TaskKey key, Collection<ResourceProvideDep> resourceProvides) {
//        // OPTO: reuse buffers? is that safe?
//        final SerializedAndHashed serializedAndHashed = serializeUtil.serializeAndHash(key);
//        final byte[] keyBytes = serializedAndHashed.serialized;
//        final byte[] keyHashedBytes = serializedAndHashed.hashed;
//
//        // Remove old inverse file generates.
//        final ArrayList<ResourceProvideDep> oldResourceProvides = De.orElse(shared.getOne(ArrayList.class, BufferUtil.toBuffer(keyHashedBytes), resourceProvidesDb), new ArrayList<>());
//        for(ResourceProvideDep resourceProvide : oldResourceProvides) {
//            shared.deleteOne(serializeUtil.serializeHashed(resourceProvide.key), providerOfDb);
//        }
//
//        // Add new file generates.
//        shared.setOne(BufferUtil.toBuffer(keyHashedBytes), serializeUtil.serialize(resourceProvides), resourceProvidesDb);
//        for(ResourceProvideDep resourceProvide : resourceProvides) {
//            shared.setOne(serializeUtil.serializeHashed(resourceProvide.key), BufferUtil.toBuffer(keyBytes), providerOfDb);
//        }
//    }


    @Override public @Nullable TaskData resetTask(Task<?> task) {
        // TODO: implement
        throw new UnsupportedOperationException("clearTaskOutputAndDeps has not been implemented for LMDB yet, sorry");
    }

    @Override public void addTaskRequire(TaskKey caller, TaskKey callee) {
        // TODO: implement
        throw new UnsupportedOperationException("addTaskRequire has not been implemented for LMDB yet, sorry");
    }

    @Override public void addTaskRequireDep(TaskKey caller, TaskRequireDep dep) {
        // TODO: implement
        throw new UnsupportedOperationException("addTaskRequire has not been implemented for LMDB yet, sorry");
    }

    @Override public void addResourceRequireDep(TaskKey requiree, ResourceRequireDep dep) {
        // TODO: implement
        throw new UnsupportedOperationException("addResourceRequire has not been implemented for LMDB yet, sorry");
    }

    @Override public void addResourceProvideDep(TaskKey provider, ResourceProvideDep resourceProvide) {
        // TODO: implement
        throw new UnsupportedOperationException("addResourceProvide has not been implemented for LMDB yet, sorry");
    }


//    @Override public void setData(TaskKey key, TaskData data) {
//        // OPTO: serialize and hash task only once?
//        setInput(key, data.input);
//        setOutput(key, data.output);
//        setTaskObservability(key, data.taskObservability);
//        setTaskRequires(key, data.taskRequires);
//        setResourceRequires(key, data.resourceRequires);
//        setResourceProvides(key, data.resourceProvides);
//    }

    @Override public TaskData deleteData(TaskKey key) {
        // TODO: implement
        throw new UnsupportedOperationException("deleteData has not been implemented for LMDB yet, sorry");
    }


    @Override public Set<TaskKey> getDeferredTasks() {
        // TODO: implement
        throw new UnsupportedOperationException("deferredTasks has not been implemented for LMDB yet, sorry");
    }

    @Override public void addDeferredTask(TaskKey key) {
        // TODO: implement
        throw new UnsupportedOperationException("addDeferredTask has not been implemented for LMDB yet, sorry");
    }

    @Override public void removeDeferredTask(TaskKey key) {
        // TODO: implement
        throw new UnsupportedOperationException("removeDeferredTask has not been implemented for LMDB yet, sorry");
    }


    @Override public @Nullable SerializableConsumer<Serializable> getCallback(TaskKey key) {
        // TODO: implement
        throw new UnsupportedOperationException("getCallback has not been implemented for LMDB yet, sorry");
    }

    @Override public void setCallback(TaskKey key, SerializableConsumer<Serializable> callback) {
        // TODO: implement
        throw new UnsupportedOperationException("setCallback has not been implemented for LMDB yet, sorry");
    }

    @Override public void removeCallback(TaskKey key) {
        // TODO: implement
        throw new UnsupportedOperationException("removeCallback has not been implemented for LMDB yet, sorry");
    }

    @Override public void dropCallbacks() {
        // TODO: implement
        throw new UnsupportedOperationException("dropCallbacks has not been implemented for LMDB yet, sorry");
    }


    @Override public void drop() {
        inputDb.drop(txn);
        outputDb.drop(txn);
        taskObservabilityDb.drop(txn);
        taskRequiresDb.drop(txn);
        callersOfDb.drop(txn);
        callersOfValuesDb.drop(txn);
        resourceRequiresDb.drop(txn);
        requireesOfDb.drop(txn);
        requireesOfValuesDb.drop(txn);
        resourceProvidesDb.drop(txn);
        providerOfDb.drop(txn);
    }
}
