package mb.pie.store.lmdb

import mb.pie.api.Logger
import org.lmdbjava.GetOp
import org.lmdbjava.PutFlags
import org.lmdbjava.SeekOp
import java.io.Serializable
import java.nio.ByteBuffer
import java.util.*

public class DbiShared {
  private val env: EnvB;
  private val txn: TxnB;
  private val isWriteTxn: Boolean;
  private val logger: Logger;

  public constructor(env: EnvB,txn: TxnB,isWriteTxn: Boolean,logger: Logger) {
    this.env = env;
    this.txn = txn;
    this.isWriteTxn = isWriteTxn
    this.logger = logger;
  }


  public fun getBool(keyHashedBuf: ByteBuffer,db: DbiB): Boolean {
    return db.get(txn,keyHashedBuf) != null;
  }

  public fun <R : Serializable?> getOne(keyHashedBuf: ByteBuffer,db: DbiB): Deserialized<R>? {
    val valueBuf: ByteBuffer? = db.get(txn,BufferUtil.copyBuffer(keyHashedBuf) /* OPTO: prevent copy? */)
    if(valueBuf == null) {
      return null;
    }
    return deserializeOrDelete<R>(keyHashedBuf,valueBuf,db);
  }

  public fun <R : Serializable?> getMultiple(keyHashedBuf: ByteBuffer,dbDup: DbiB,dbVal: DbiB): Set<R> {
    val hashedValueBufs: ArrayList<ByteBuffer> = ArrayList<ByteBuffer>();
    dbDup.openCursor(txn).use { cursor ->
      if(!cursor.get(BufferUtil.copyBuffer(keyHashedBuf) /* OPTO: prevent copy? */,GetOp.MDB_SET)) {
        return setOf();
      }
      do {
        val hashedValueBuf: ByteBuffer = cursor.`val`();
        hashedValueBufs.add(BufferUtil.copyBuffer(hashedValueBuf));
      } while(cursor.seek(SeekOp.MDB_NEXT_DUP))
    }
    val results = HashSet<R>(hashedValueBufs.size);
    for(hashedValueBuf: ByteBuffer in hashedValueBufs) {
      val valueBuf: ByteBuffer? = dbVal.get(txn,hashedValueBuf)
      if(valueBuf == null) {
        continue;
      }
      val deserializedValueBuf: Deserialized<R> = deserializeOrDelete<R>(hashedValueBuf,valueBuf,dbVal);
      if(!deserializedValueBuf.failed) {
        results.add(deserializedValueBuf.deserialized);
      } else {
        // Also delete key-value pair from dbDup when value could not be deserialized.
        if(isWriteTxn) {
          dbDup.delete(txn,BufferUtil.copyBuffer(keyHashedBuf) /* OPTO: prevent copy? */,hashedValueBuf);
        } else {
          env.txnWrite().use {
            dbDup.delete(it,BufferUtil.copyBuffer(keyHashedBuf) /* OPTO: prevent copy? */,hashedValueBuf);
          }
        }
      }
    }
    return results;
  }

  public fun <R : Serializable?> deserializeOrDelete(keyHashedBuf: ByteBuffer,valueBuf: ByteBuffer,db: DbiB): Deserialized<R> {
    val deserialized: Deserialized<R> = SerializeUtil.deserialize<R>(valueBuf,logger);
    if(deserialized.failed) {
      if(isWriteTxn) {
        db.delete(txn,keyHashedBuf);
      } else {
        env.txnWrite().use { txn ->
          // TODO: just deleting data that cannot be deserialized is unsound; it could silently delete dependencies which are then never recreated!
          db.delete(txn,keyHashedBuf);
          txn.commit();
        }
      }
    }
    return deserialized;
  }


  public fun setBool(keyHashedBuf: ByteBuffer,value: Boolean,db: DbiB): Boolean {
    return if(value) {
      db.put(txn,BufferUtil.copyBuffer(keyHashedBuf) /* OPTO: prevent copy? */,BufferUtil.emptyBuffer());
    } else {
      db.delete(txn,keyHashedBuf);
    }
  }

  public fun setOne(keyHashedBuf: ByteBuffer,valueBuf: ByteBuffer,db: DbiB): Boolean {
    return db.put(txn,keyHashedBuf,valueBuf);
  }

  public fun deleteOne(keyHashedBuf: ByteBuffer,db: DbiB): Boolean {
    return db.delete(txn,keyHashedBuf);
  }

  public fun setDup(keyHashedBuf: ByteBuffer,valueBuf: ByteBuffer,valueHashedBuf: ByteBuffer,dbDup: DbiB,dbVal: DbiB): Boolean {
    val put1: Boolean = dbDup.put(txn,keyHashedBuf,BufferUtil.copyBuffer(valueHashedBuf) /* OPTO: prevent copy? */,PutFlags.MDB_NODUPDATA);
    val put2: Boolean = dbVal.put(txn,valueHashedBuf,valueBuf);
    return put1 && put2;
  }

  public fun deleteDup(keyHashedBuf: ByteBuffer,hashedValue: ByteBuffer,dbDup: DbiB,dbVal: DbiB): Boolean {
    return dbDup.delete(txn,keyHashedBuf,hashedValue);
    // TODO: cannot delete (hashedValue, value) from value database, since multiple entries from dbDup may refer to it.
  }
}
