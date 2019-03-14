package mb.pie.store.lmdb;

import mb.pie.api.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.lmdbjava.*;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DbiShared {
    private final Env<ByteBuffer> env;
    private final Txn<ByteBuffer> txn;
    private final boolean isWriteTxn;
    private final Logger logger;

    public DbiShared(Env<ByteBuffer> env, Txn<ByteBuffer> txn, boolean isWriteTxn, Logger logger) {
        this.env = env;
        this.txn = txn;
        this.isWriteTxn = isWriteTxn;
        this.logger = logger;
    }


    public boolean getBool(ByteBuffer keyHashedBuf, Dbi<ByteBuffer> db) {
        return db.get(txn, keyHashedBuf) != null;
    }

    public <R extends @Nullable Serializable> @Nullable Deserialized<R> getOne(ByteBuffer keyHashedBuf, Dbi<ByteBuffer> db) {
        final @Nullable ByteBuffer valueBuf = db.get(txn, BufferUtil.copyBuffer(keyHashedBuf) /* OPTO: prevent copy? */);
        if(valueBuf == null) {
            return null;
        }
        return deserializeOrDelete(keyHashedBuf, valueBuf, db);
    }

    public <R extends @Nullable Serializable> Set<R> getMultiple(ByteBuffer keyHashedBuf, Dbi<ByteBuffer> dbDup, Dbi<ByteBuffer> dbVal) {
        final ArrayList<ByteBuffer> hashedValueBufs = new ArrayList<>();
        try(final Cursor<ByteBuffer> cursor = dbDup.openCursor(txn)) {
            if(!cursor.get(BufferUtil.copyBuffer(keyHashedBuf) /* OPTO: prevent copy? */, GetOp.MDB_SET)) {
                return new HashSet<>();
            }
            do {
                final ByteBuffer hashedValueBuf = cursor.val();
                hashedValueBufs.add(BufferUtil.copyBuffer(hashedValueBuf));
            } while(cursor.seek(SeekOp.MDB_NEXT_DUP));
        }
        final HashSet<R> results = new HashSet<>(hashedValueBufs.size());
        for(ByteBuffer hashedValueBuf : hashedValueBufs) {
            final @Nullable ByteBuffer valueBuf = dbVal.get(txn, hashedValueBuf);
            if(valueBuf == null) {
                continue;
            }
            final Deserialized<R> deserializedValueBuf = deserializeOrDelete(hashedValueBuf, valueBuf, dbVal);
            if(!deserializedValueBuf.failed) {
                results.add(deserializedValueBuf.deserialized);
            } else {
                // Also delete key-value pair from dbDup when value could not be deserialized.
                if(isWriteTxn) {
                    dbDup.delete(txn, BufferUtil.copyBuffer(keyHashedBuf) /* OPTO: prevent copy? */, hashedValueBuf);
                } else {
                    try(final Txn<ByteBuffer> txn = env.txnWrite()) {
                        dbDup.delete(txn, BufferUtil.copyBuffer(keyHashedBuf) /* OPTO: prevent copy? */, hashedValueBuf);
                    }
                }
            }
        }
        return results;
    }

    private <R extends @Nullable Serializable> Deserialized<R> deserializeOrDelete(ByteBuffer keyHashedBuf, ByteBuffer valueBuf, Dbi<ByteBuffer> db) {
        final Deserialized<R> deserialized = SerializeUtil.deserialize(valueBuf, logger);
        if(deserialized.failed) {
            if(isWriteTxn) {
                db.delete(txn, keyHashedBuf);
            } else {
                try(final Txn<ByteBuffer> txn = env.txnWrite()) {
                    // TODO: just deleting data that cannot be deserialized is unsound; it could silently delete dependencies which are then never recreated!
                    db.delete(txn, keyHashedBuf);
                    txn.commit();
                }
            }
        }
        return deserialized;
    }


    public boolean setBool(ByteBuffer keyHashedBuf, boolean value, Dbi<ByteBuffer> db) {
        if(value) {
            return db.put(txn, BufferUtil.copyBuffer(keyHashedBuf) /* OPTO: prevent copy? */, BufferUtil.emptyBuffer());
        } else {
            return db.delete(txn, keyHashedBuf);
        }
    }

    public boolean setOne(ByteBuffer keyHashedBuf, ByteBuffer valueBuf, Dbi<ByteBuffer> db) {
        return db.put(txn, keyHashedBuf, valueBuf);
    }

    public boolean deleteOne(ByteBuffer keyHashedBuf, Dbi<ByteBuffer> db) {
        return db.delete(txn, keyHashedBuf);
    }

    public boolean setDup(ByteBuffer keyHashedBuf, ByteBuffer valueBuf, ByteBuffer valueHashedBuf, Dbi<ByteBuffer> dbDup, Dbi<ByteBuffer> dbVal) {
        final boolean put1 = dbDup.put(txn, keyHashedBuf, BufferUtil.copyBuffer(valueHashedBuf) /* OPTO: prevent copy? */, PutFlags.MDB_NODUPDATA);
        final boolean put2 = dbVal.put(txn, valueHashedBuf, valueBuf);
        return put1 && put2;
    }

    public boolean deleteDup(ByteBuffer keyHashedBuf, ByteBuffer hashedValue, Dbi<ByteBuffer> dbDup, Dbi<ByteBuffer> dbVal) {
        return dbDup.delete(txn, keyHashedBuf, hashedValue);
        // TODO: cannot delete (hashedValue, value) from value database, since multiple entries from dbDup may refer to it.
    }
}
