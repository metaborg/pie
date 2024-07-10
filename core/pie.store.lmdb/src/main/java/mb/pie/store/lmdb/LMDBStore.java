package mb.pie.store.lmdb;

import mb.log.api.LoggerFactory;
import mb.pie.api.PieBuilder;
import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.StoreWriteTxn;
import mb.pie.api.serde.Serde;
import org.lmdbjava.Dbi;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;
import org.lmdbjava.EnvFlags;
import org.lmdbjava.Txn;

import java.io.File;
import java.nio.ByteBuffer;

public class LMDBStore implements Store {
    public static final long defaultMaxDbSize = 1024L * 1024L * 1024L * 4L; // 4 GiB
    public static final int defaultMaxReaders = 64; // 64 reader threads

    /**
     * Sets the store of this builder to the [LMDBStore], stored at given [envDir], with parameters [maxDbSize]
     * determining the maximum database size and [maxReaders] determining the maximum concurrent readers.
     */
    public static PieBuilder withLMDBStore(PieBuilder pieBuilder, File envDir, long maxDbSize, int maxReaders) {
        pieBuilder.withStoreFactory((serde, resourceService, logger) -> new LMDBStore(serde, envDir, maxDbSize, maxReaders, logger));
        return pieBuilder;
    }

    /**
     * Sets the store of this builder to the [LMDBStore], stored at given [envDir], with default maximum database size
     * and number of readers.
     */
    public static PieBuilder withLMDBStore(PieBuilder pieBuilder, File envDir) {
        return withLMDBStore(pieBuilder, envDir, defaultMaxDbSize, defaultMaxReaders);
    }


    private final Env<ByteBuffer> env;
    private final Dbi<ByteBuffer> input;
    private final Dbi<ByteBuffer> output;
    private final Dbi<ByteBuffer> taskObservability;
    private final Dbi<ByteBuffer> taskRequires;
    private final Dbi<ByteBuffer> callersOf;
    private final Dbi<ByteBuffer> callersOfValues;
    private final Dbi<ByteBuffer> resourceRequires;
    private final Dbi<ByteBuffer> requireesOf;
    private final Dbi<ByteBuffer> requireesOfValues;
    private final Dbi<ByteBuffer> resourceProvides;
    private final Dbi<ByteBuffer> providerOf;
    private final SerializeUtil serializeUtil;

    public LMDBStore(Serde serde, File envDir, long maxDbSize, int maxReaders, LoggerFactory loggerFactory) {
        envDir.mkdirs();
        this.env = Env.create()
            .setMapSize(maxDbSize)
            .setMaxReaders(maxReaders)
            .setMaxDbs(11)
            .open(envDir, EnvFlags.MDB_MAPASYNC, EnvFlags.MDB_WRITEMAP);
        this.input = env.openDbi("input", DbiFlags.MDB_CREATE);
        this.output = env.openDbi("output", DbiFlags.MDB_CREATE);
        this.taskObservability = env.openDbi("taskObservability", DbiFlags.MDB_CREATE);
        this.taskRequires = env.openDbi("taskRequires", DbiFlags.MDB_CREATE);
        this.callersOf = env.openDbi("callersOf", DbiFlags.MDB_CREATE, DbiFlags.MDB_DUPSORT);
        this.callersOfValues = env.openDbi("callersOfValues", DbiFlags.MDB_CREATE);
        this.resourceRequires = env.openDbi("resourceRequires", DbiFlags.MDB_CREATE);
        this.requireesOf = env.openDbi("requireesOf", DbiFlags.MDB_CREATE, DbiFlags.MDB_DUPSORT);
        this.requireesOfValues = env.openDbi("requireesOfValues", DbiFlags.MDB_CREATE);
        this.resourceProvides = env.openDbi("resourceProvides", DbiFlags.MDB_CREATE);
        this.providerOf = env.openDbi("providerOf", DbiFlags.MDB_CREATE);
        this.serializeUtil = new SerializeUtil(serde, loggerFactory);
    }

    public LMDBStore(Serde serde, File envDir, LoggerFactory loggerFactory) {
        this(serde, envDir, defaultMaxDbSize, defaultMaxReaders, loggerFactory);
    }


    @Override public void close() {
        env.close();
    }

    @Override public StoreReadTxn readTxn() {
        final Txn<ByteBuffer> txn = env.txnRead();
        return new LMDBStoreTxn(env, txn, false,
            input,
            output,
            taskObservability,
            taskRequires,
            callersOf,
            callersOfValues,
            resourceRequires,
            requireesOf,
            requireesOfValues,
            resourceProvides,
            providerOf,
            serializeUtil
        );
    }

    @Override public StoreWriteTxn writeTxn() {
        final Txn<ByteBuffer> txn = env.txnWrite();
        return new LMDBStoreTxn(env, txn, true,
            input,
            output,
            taskObservability,
            taskRequires,
            callersOf,
            callersOfValues,
            resourceRequires,
            requireesOf,
            requireesOfValues,
            resourceProvides,
            providerOf,
            serializeUtil
        );
    }

    @Override public void sync() {
        env.sync(true);
    }

    @Override public String toString() {
        return "LMDBStore()";
    }
}
