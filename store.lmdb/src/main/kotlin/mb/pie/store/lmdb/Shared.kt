package mb.pie.store.lmdb

import mb.pie.api.Logger
import org.lmdbjava.*
import java.io.Serializable
import java.util.*

internal class DbiShared(
  private val env: EnvB,
  private val txn: TxnB,
  private val isWriteTxn: Boolean,
  private val logger: Logger
) {
  @Suppress("NOTHING_TO_INLINE")
  inline fun getBool(keyHashedBuf: Buf, db: DbiB): Boolean {
    return db.get(txn, keyHashedBuf) != null
  }

  @Suppress("NOTHING_TO_INLINE")
  inline fun <R : Serializable?> getOne(keyHashedBuf: Buf, db: DbiB): Deserialized<R>? {
    val valueBuf = db.get(txn, keyHashedBuf.copyBuffer() /* OPTO: prevent copy? */) ?: return null
    return deserializeOrDelete<R>(keyHashedBuf, valueBuf, db)
  }

  fun <R : Serializable?> getMultiple(keyHashedBuf: Buf, dbDup: DbiB, dbVal: DbiB): Set<R> {
    val hashedValueBufs = ArrayList<Buf>()
    dbDup.openCursor(txn).use { cursor ->
      if(!cursor.get(keyHashedBuf.copyBuffer() /* OPTO: prevent copy? */, GetOp.MDB_SET)) {
        return setOf()
      }
      do {
        val hashedValueBuf = cursor.`val`()
        hashedValueBufs.add(hashedValueBuf.copyBuffer())
      } while(cursor.seek(SeekOp.MDB_NEXT_DUP))
    }
    val results = HashSet<R>(hashedValueBufs.size)
    for(hashedValueBuf in hashedValueBufs) {
      val valueBuf = dbVal.get(txn, hashedValueBuf) ?: continue
      val deserializedValueBuf = deserializeOrDelete<R>(hashedValueBuf, valueBuf, dbVal)
      if(!deserializedValueBuf.failed) {
        results.add(deserializedValueBuf.deserialized)
      } else {
        // Also delete key-value pair from dbDup when value could not be deserialized.
        if(isWriteTxn) {
          dbDup.delete(txn, keyHashedBuf.copyBuffer() /* OPTO: prevent copy? */, hashedValueBuf)
        } else {
          env.txnWrite().use {
            dbDup.delete(it, keyHashedBuf.copyBuffer() /* OPTO: prevent copy? */, hashedValueBuf)
          }
        }
      }
    }
    return results
  }

  @Suppress("NOTHING_TO_INLINE")
  private inline fun <R : Serializable?> deserializeOrDelete(keyHashedBuf: Buf, valueBuf: Buf, db: DbiB): Deserialized<R> {
    val deserialized = valueBuf.deserialize<R>(logger)
    if(deserialized.failed) {
      if(isWriteTxn) {
        db.delete(txn, keyHashedBuf)
      } else {
        env.txnWrite().use { txn ->
          // TODO: just deleting data that cannot be deserialized is unsound; it could silently delete dependencies which are then never recreated!
          db.delete(txn, keyHashedBuf)
          txn.commit()
        }
      }
    }
    return deserialized
  }


  @Suppress("NOTHING_TO_INLINE")
  inline fun setBool(keyHashedBuf: Buf, value: Boolean, db: DbiB): Boolean {
    return if(value) {
      db.put(txn, keyHashedBuf.copyBuffer() /* OPTO: prevent copy? */, emptyBuffer())
    } else {
      db.delete(txn, keyHashedBuf)
    }
  }

  @Suppress("NOTHING_TO_INLINE")
  inline fun setOne(keyHashedBuf: Buf, valueBuf: Buf, db: DbiB): Boolean {
    return db.put(txn, keyHashedBuf, valueBuf)
  }

  @Suppress("NOTHING_TO_INLINE")
  inline fun deleteOne(keyHashedBuf: Buf, db: DbiB): Boolean {
    return db.delete(txn, keyHashedBuf)
  }

  @Suppress("NOTHING_TO_INLINE")
  inline fun setDup(keyHashedBuf: Buf, valueBuf: Buf, valueHashedBuf: Buf, dbDup: DbiB, dbVal: DbiB): Boolean {
    // Put key.serialize().hash() -> value.serialize().hash()
    val put1 = dbDup.put(txn, keyHashedBuf, valueHashedBuf.copyBuffer() /* OPTO: prevent copy? */, PutFlags.MDB_NODUPDATA)
    // Put value.serialize().hash() -> value.serialize()
    val put2 = dbVal.put(txn, valueHashedBuf, valueBuf)
    return put1 && put2
  }

  @Suppress("NOTHING_TO_INLINE")
  inline fun deleteDup(keyHashedBuf: Buf, hashedValue: Buf, dbDup: DbiB, @Suppress("UNUSED_PARAMETER") dbVal: DbiB): Boolean {
    return dbDup.delete(txn, keyHashedBuf, hashedValue)
    // TODO: cannot delete (hashedValue, value) from value database, since multiple entries from dbDup may refer to it.
  }
}
