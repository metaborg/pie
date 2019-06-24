package mb.pie.store.lmdb;

import mb.pie.api.*;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.lmdbjava.CursorIterator;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LMDBStoreTxn implements StoreReadTxn, StoreWriteTxn {
    private final Txn<ByteBuffer> txn;
    private final Logger logger;
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

    LMDBStoreTxn(
        Env<ByteBuffer> env,
        Txn<ByteBuffer> txn,
        boolean isWriteTxn,
        Logger logger,
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
        Dbi<ByteBuffer> providerOfDb
    ) {
        this.txn = txn;
        this.logger = logger;
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
        this.shared = new DbiShared(env, txn, isWriteTxn, logger);
    }

    @Override public void close() {
        txn.commit();
        txn.close();
    }


    @Override public @Nullable Serializable input(TaskKey key) {
        return Deserialized.orElseNull(shared.getOne(SerializeUtil.serializeHashedToBuffer(key), inputDb));
    }

    @Override public @Nullable Output output(TaskKey key) {
        final ByteBuffer keyHashedBuf = SerializeUtil.serializeHashedToBuffer(key);
        final @Nullable Deserialized<@Nullable Serializable> deserialized = shared.getOne(keyHashedBuf, outputDb);
        return Deserialized.mapOrElseNull(deserialized, Output::new);
    }

    @Override public Observability taskObservability(TaskKey key) {
        return Deserialized.orElse(shared.getOne(SerializeUtil.serializeHashedToBuffer(key), taskObservabilityDb),
            Observability.Unobserved);
    }


    @Override public ArrayList<TaskRequireDep> taskRequires(TaskKey key) {
        return Deserialized.orElse(
            shared.getOne(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(key))), taskRequiresDb),
            new ArrayList<>());
    }

    @Override public Set<TaskKey> callersOf(TaskKey key) {
        return shared.getMultiple(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(key))), callersOfDb,
            callersOfValuesDb);
    }


    @Override public ArrayList<ResourceRequireDep> resourceRequires(TaskKey key) {
        return Deserialized.orElse(
            shared.getOne(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(key))), resourceRequiresDb),
            new ArrayList<>());
    }

    @Override public Set<TaskKey> requireesOf(ResourceKey key) {
        return shared.getMultiple(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(key))), requireesOfDb,
            requireesOfValuesDb);
    }


    @Override public ArrayList<ResourceProvideDep> resourceProvides(TaskKey key) {
        return Deserialized.orElse(
            shared.getOne(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(key))), resourceProvidesDb),
            new ArrayList<>());
    }

    @Override public @Nullable TaskKey providerOf(ResourceKey key) {
        return Deserialized.orElse(
            shared.getOne(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(key))), providerOfDb), null);
    }


    @Override public @Nullable TaskData data(TaskKey key) {
        // OPTO: reuse buffers? is that safe?
        final byte[] keyHashedBytes = SerializeUtil.hash(SerializeUtil.serialize(key));
        final @Nullable Deserialized<Serializable> inputDeserialized =
            shared.getOne(BufferUtil.toBuffer(keyHashedBytes), inputDb);
        if(inputDeserialized == null || inputDeserialized.failed) {
            return null;
        }
        final @Nullable Deserialized<@Nullable Serializable> outputDeserialized =
            shared.getOne(BufferUtil.toBuffer(keyHashedBytes), outputDb);
        if(outputDeserialized == null || outputDeserialized.failed) {
            return null;
        }
        final @Nullable Deserialized<Observability> taskObservabilityDeserialized =
            shared.getOne(BufferUtil.toBuffer(keyHashedBytes), taskObservabilityDb);

        final Serializable input = inputDeserialized.deserialized;
        final @Nullable Serializable output = outputDeserialized.deserialized;
        final Observability taskObservability =
            Deserialized.orElse(taskObservabilityDeserialized, Observability.Unobserved);
        final ArrayList<TaskRequireDep> taskRequires =
            Deserialized.orElse(shared.getOne(BufferUtil.toBuffer(keyHashedBytes), taskRequiresDb), new ArrayList<>());
        final ArrayList<ResourceRequireDep> resourceRequires =
            Deserialized.orElse(shared.getOne(BufferUtil.toBuffer(keyHashedBytes), resourceRequiresDb),
                new ArrayList<>());
        final ArrayList<ResourceProvideDep> resourceProvides =
            Deserialized.orElse(shared.getOne(BufferUtil.toBuffer(keyHashedBytes), resourceProvidesDb),
                new ArrayList<>());
        return new TaskData(input, output, taskObservability, taskRequires, resourceRequires, resourceProvides);
    }


    @Override public Set<TaskKey> tasksWithoutCallers() {
        // TODO: implement
        throw new UnsupportedOperationException("tasksWithoutCallers has not been implemented for LMDB yet, sorry");
    }

    @Override public int numSourceFiles() {
        // Cannot use requireesOfValuesDb, as these are never cleaned up at the moment. Instead use values of resourceRequiresDb.
        final HashSet<ResourceKey> requiredResources = new HashSet<>();
        try(final CursorIterator<ByteBuffer> cursor = resourceRequiresDb.iterate(txn)) {
            while(cursor.hasNext()) {
                final CursorIterator.KeyVal<ByteBuffer> next = cursor.next();
                final ArrayList<ResourceRequireDep> resourceRequires =
                    Deserialized.orElse(SerializeUtil.deserialize(next.val(), logger), new ArrayList<>());
                for(ResourceRequireDep resourceRequire : resourceRequires) {
                    requiredResources.add(resourceRequire.key);
                }
            }
        }

        int numSourceFiles = 0;
        for(ResourceKey file : requiredResources) {
            if(!shared.getBool(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(file))), providerOfDb)) {
                ++numSourceFiles;
            }
        }
        return numSourceFiles;
    }


    @Override public void setInput(TaskKey key, Serializable input) {
        shared.setOne(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(key))),
            BufferUtil.toBuffer(SerializeUtil.serialize(input)), inputDb);
    }

    @Override public void setOutput(TaskKey key, @Nullable Serializable output) {
        shared.setOne(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(key))),
            BufferUtil.toBuffer(SerializeUtil.serialize(output)), outputDb);
    }

    @Override public void setTaskObservability(TaskKey key, Observability observability) {
        shared.setOne(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(key))),
            BufferUtil.toBuffer(SerializeUtil.serialize(observability)), taskObservabilityDb);
    }


    @Override public void setTaskRequires(TaskKey key, ArrayList<TaskRequireDep> taskRequires) {
        // OPTO: reuse buffers? is that safe?
        final SerializedAndHashed serializedAndHashed = SerializeUtil.serializeAndHash(key);
        final byte[] keyBytes = serializedAndHashed.serialized;
        final byte[] keyHashedBytes = serializedAndHashed.hashed;

        // Remove old inverse task requirements.
        final ArrayList<TaskRequireDep> oldTaskRequires =
            Deserialized.orElse(shared.getOne(BufferUtil.toBuffer(keyHashedBytes), taskRequiresDb), new ArrayList<>());
        for(TaskRequireDep taskRequire : oldTaskRequires) {
            shared.deleteDup(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(taskRequire.callee))),
                BufferUtil.toBuffer(keyHashedBytes), callersOfDb, callersOfValuesDb);
        }

        // Add new task requirements.
        shared.setOne(BufferUtil.toBuffer(keyHashedBytes), BufferUtil.toBuffer(SerializeUtil.serialize(taskRequires)),
            taskRequiresDb);
        for(TaskRequireDep taskRequire : taskRequires) {
            shared.setDup(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(taskRequire.callee))),
                BufferUtil.toBuffer(keyBytes), BufferUtil.toBuffer(keyHashedBytes), callersOfDb, callersOfValuesDb);
        }
    }

    @Override public void setResourceRequires(TaskKey key, ArrayList<ResourceRequireDep> resourceRequires) {
        // OPTO: reuse buffers? is that safe?
        final SerializedAndHashed serializedAndHashed = SerializeUtil.serializeAndHash(key);
        final byte[] keyBytes = serializedAndHashed.serialized;
        final byte[] keyHashedBytes = serializedAndHashed.hashed;

        // Remove old inverse file requirements.
        final ArrayList<ResourceRequireDep> oldResourceRequires =
            Deserialized.orElse(shared.getOne(BufferUtil.toBuffer(keyHashedBytes), resourceRequiresDb),
                new ArrayList<>());
        for(ResourceRequireDep resourceRequire : oldResourceRequires) {
            shared.deleteDup(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(resourceRequire.key))),
                BufferUtil.toBuffer(keyHashedBytes), requireesOfDb, requireesOfValuesDb);
        }

        // Add new file requirements.
        shared.setOne(BufferUtil.toBuffer(keyHashedBytes),
            BufferUtil.toBuffer(SerializeUtil.serialize(resourceRequires)), resourceRequiresDb);
        for(ResourceRequireDep resourceRequire : resourceRequires) {
            shared.setDup(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(resourceRequire.key))),
                BufferUtil.toBuffer(keyBytes), BufferUtil.toBuffer(keyHashedBytes), requireesOfDb, requireesOfValuesDb);
        }
    }

    @Override public void setResourceProvides(TaskKey key, ArrayList<ResourceProvideDep> resourceProvides) {
        // OPTO: reuse buffers? is that safe?
        final SerializedAndHashed serializedAndHashed = SerializeUtil.serializeAndHash(key);
        final byte[] keyBytes = serializedAndHashed.serialized;
        final byte[] keyHashedBytes = serializedAndHashed.hashed;

        // Remove old inverse file generates.
        final ArrayList<ResourceProvideDep> oldResourceProvides =
            Deserialized.orElse(shared.getOne(BufferUtil.toBuffer(keyHashedBytes), resourceProvidesDb),
                new ArrayList<>());
        for(ResourceProvideDep resourceProvide : oldResourceProvides) {
            shared.deleteOne(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(resourceProvide.key))),
                providerOfDb);
        }

        // Add new file generates.
        shared.setOne(BufferUtil.toBuffer(keyHashedBytes),
            BufferUtil.toBuffer(SerializeUtil.serialize(resourceProvides)), resourceProvidesDb);
        for(ResourceProvideDep resourceProvide : resourceProvides) {
            shared.setOne(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(resourceProvide.key))),
                BufferUtil.toBuffer(keyBytes), providerOfDb);
        }
    }


    @Override public void setData(TaskKey key, TaskData data) {
        // OPTO: serialize and hash task only once?
        setInput(key, data.input);
        setOutput(key, data.output);
        setTaskObservability(key, data.taskObservability);
        setTaskRequires(key, data.taskRequires);
        setResourceRequires(key, data.resourceRequires);
        setResourceProvides(key, data.resourceProvides);
    }

    @Override public List<TaskRequireDep> deleteData(TaskKey key) {
        // TODO: implement
        throw new UnsupportedOperationException("deleteData has not been implemented for LMDB yet, sorry");
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