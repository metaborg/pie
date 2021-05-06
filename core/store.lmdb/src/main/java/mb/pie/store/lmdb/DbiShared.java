package mb.pie.store.lmdb;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.lmdbjava.Cursor;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.GetOp;
import org.lmdbjava.PutFlags;
import org.lmdbjava.SeekOp;
import org.lmdbjava.Txn;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class DbiShared {
    private final Env<ByteBuffer> env;
    private final Txn<ByteBuffer> txn;
    private final boolean isWriteTxn;
    private final SerializeUtil serializeUtil;


    DbiShared(Env<ByteBuffer> env, Txn<ByteBuffer> txn, boolean isWriteTxn, SerializeUtil serializeUtil) {
        this.env = env;
        this.txn = txn;
        this.isWriteTxn = isWriteTxn;
        this.serializeUtil = serializeUtil;
    }


    boolean getBool(ByteBuffer keyHashedBuf, Dbi<ByteBuffer> db) {
        return db.get(txn, keyHashedBuf) != null;
    }

    <T> @Nullable De<T> getOne(Class<T> type, ByteBuffer keyHashedBuf, Dbi<ByteBuffer> db) {
        final @Nullable ByteBuffer valueBuf = db.get(txn, BufferUtil.copyBuffer(keyHashedBuf) /* OPTO: prevent copy? */);
        if(valueBuf == null) {
            return null;
        }
        return deserializeOrDelete(type, keyHashedBuf, valueBuf, db);
    }

    <T> @Nullable De<@Nullable T> getOneObject(ByteBuffer keyHashedBuf, Dbi<ByteBuffer> db) {
        final @Nullable ByteBuffer valueBuf = db.get(txn, BufferUtil.copyBuffer(keyHashedBuf) /* OPTO: prevent copy? */);
        if(valueBuf == null) {
            return null;
        }
        return deserializeObjectOrDelete(keyHashedBuf, valueBuf, db);
    }

    <T> Set<T> getMultiple(Class<T> type, ByteBuffer keyHashedBuf, Dbi<ByteBuffer> dbDup, Dbi<ByteBuffer> dbVal) {
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
        final HashSet<T> results = new HashSet<>(hashedValueBufs.size());
        for(ByteBuffer hashedValueBuf : hashedValueBufs) {
            final @Nullable ByteBuffer valueBuf = dbVal.get(txn, hashedValueBuf);
            if(valueBuf == null) {
                continue;
            }
            final De<T> deserializedValueBuf = deserializeOrDelete(type, hashedValueBuf, valueBuf, dbVal);
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


    private <T> De<T> deserializeOrDelete(Class<T> type, ByteBuffer keyHashedBuf, ByteBuffer valueBuf, Dbi<ByteBuffer> db) {
        final De<T> deserialized = serializeUtil.deserialize(type, valueBuf);
        handleDeserializeFailure(deserialized, keyHashedBuf, db);
        return deserialized;
    }

    private <T> De<@Nullable T> deserializeObjectOrDelete(ByteBuffer keyHashedBuf, ByteBuffer valueBuf, Dbi<ByteBuffer> db) {
        final De<T> deserialized = serializeUtil.deserializeObject(valueBuf);
        handleDeserializeFailure(deserialized, keyHashedBuf, db);
        return deserialized;
    }

    private <T> void handleDeserializeFailure(De<T> deserialized, ByteBuffer keyHashedBuf, Dbi<ByteBuffer> db) {
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
    }


    boolean setBool(ByteBuffer keyHashedBuf, boolean value, Dbi<ByteBuffer> db) {
        if(value) {
            return db.put(txn, BufferUtil.copyBuffer(keyHashedBuf) /* OPTO: prevent copy? */, BufferUtil.emptyBuffer());
        } else {
            return db.delete(txn, keyHashedBuf);
        }
    }

    boolean setOne(ByteBuffer keyHashedBuf, ByteBuffer valueBuf, Dbi<ByteBuffer> db) {
        return db.put(txn, keyHashedBuf, valueBuf);
    }

    boolean deleteOne(ByteBuffer keyHashedBuf, Dbi<ByteBuffer> db) {
        return db.delete(txn, keyHashedBuf);
    }

    boolean setDup(ByteBuffer keyHashedBuf, ByteBuffer valueBuf, ByteBuffer valueHashedBuf, Dbi<ByteBuffer> dbDup, Dbi<ByteBuffer> dbVal) {
        final boolean put1 = dbDup.put(txn, keyHashedBuf, BufferUtil.copyBuffer(valueHashedBuf) /* OPTO: prevent copy? */, PutFlags.MDB_NODUPDATA);
        final boolean put2 = dbVal.put(txn, valueHashedBuf, valueBuf);
        return put1 && put2;
    }

    boolean deleteDup(ByteBuffer keyHashedBuf, ByteBuffer hashedValue, Dbi<ByteBuffer> dbDup, Dbi<ByteBuffer> dbVal) {
        return dbDup.delete(txn, keyHashedBuf, hashedValue);
        // TODO: cannot delete (hashedValue, value) from value database, since multiple entries from dbDup may refer to it.
    }
}
