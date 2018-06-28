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
  inline fun <T : Serializable> exists(key: T, db: DbiB): Boolean {
    return db.get(txn, key.serializeThenHash().toBuffer()) != null
  }


  inline fun <T : Serializable> getBool(key: T, db: DbiB): Boolean {
    val keyHashedBytes = key.serializeThenHash()
    val valueBuf = db.get(txn, keyHashedBytes.toBuffer())
    return valueBuf != null
  }


  inline fun <T : Serializable, R : Serializable?> getOne(key: T, db: DbiB): Deserialized<R>? {
    val keyHashedBytes = key.serializeThenHash()
    val keyHashedBuf = keyHashedBytes.toBuffer()
    val valueBuf = db.get(txn, keyHashedBuf) ?: return null
    return deserializeOrDelete<R>(keyHashedBuf, valueBuf, db)
  }

  inline fun <R : Serializable?> getOne(keyHashedBuf: Buf, db: DbiB): Deserialized<R>? {
    val valueBuf = db.get(txn, keyHashedBuf) ?: return null
    return deserializeOrDelete<R>(keyHashedBuf, valueBuf, db)
  }


  fun <T : Serializable, R : Serializable?> getMultiple(key: T, dbDup: DbiB, dbVal: DbiB): Set<R> {
    val keyHashedBytes = key.serializeThenHash()
    val hashedValueBufs = ArrayList<Buf>()
    dbDup.openCursor(txn).use { cursor ->
      if(!cursor.get(keyHashedBytes.toBuffer(), GetOp.MDB_SET)) {
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
          dbDup.delete(txn, keyHashedBytes.toBuffer(), hashedValueBuf)
        } else {
          env.txnWrite().use {
            dbDup.delete(it, keyHashedBytes.toBuffer(), hashedValueBuf)
          }
        }
      }
    }
    return results
  }

  private fun <R : Serializable?> deserializeOrDelete(keyBuf: Buf, valueBuf: Buf, db: DbiB): Deserialized<R> {
    val deserialized = valueBuf.deserialize<R>(logger)
    if(deserialized.failed) {
      if(isWriteTxn) {
        db.delete(txn, keyBuf)
      } else {
        env.txnWrite().use {
          db.delete(it, keyBuf)
        }
      }
    }
    return deserialized
  }


  inline fun <K : Serializable> setBool(key: K, value: Boolean, db: DbiB): Boolean {
    val keyHashedBytes = key.serializeThenHash()
    return if(value) {
      db.put(txn, keyHashedBytes.toBuffer(), emptyBuffer())
    } else {
      db.delete(txn, keyHashedBytes.toBuffer())
    }
  }


  inline fun <K : Serializable, V : Serializable?> setOne(key: K, value: V, db: DbiB): Boolean {
    val keyHashedBytes = key.serializeThenHash()
    val valueBytes = value.serialize()
    return setOne(keyHashedBytes.toBuffer(), valueBytes.toBuffer(), db)
  }

  inline fun setOne(keyHashedBuf: Buf, valueBuf: Buf, db: DbiB): Boolean {
    return db.put(txn, keyHashedBuf, valueBuf)
  }


  inline fun <K : Serializable> deleteOne(key: K, db: DbiB): Boolean {
    val keyHashedBytes = key.serializeThenHash()
    return deleteOne(keyHashedBytes.toBuffer(), db)
  }

  inline fun deleteOne(keyHashedBuf: Buf, db: DbiB): Boolean {
    return db.delete(txn, keyHashedBuf)
  }


  inline fun <K : Serializable, V : Serializable?> setDup(key: K, value: V, dbDup: DbiB, dbVal: DbiB): Boolean {
    val keyHashedBytes = key.serializeThenHash()
    val (valueBytes, valueHashedBytes) = value.serializeAndHash()
    return setDup(keyHashedBytes.toBuffer(), valueBytes.toBuffer(), valueHashedBytes.toBuffer(), dbDup, dbVal)
  }

  inline fun setDup(keyHashedBuf: Buf, valueBuf: Buf, valueHashedBuf: Buf, dbDup: DbiB, dbVal: DbiB): Boolean {
    // Put key.serialize().hash() -> value.serialize().hash()
    val put1 = dbDup.put(txn, keyHashedBuf, valueHashedBuf, PutFlags.MDB_NODUPDATA)
    // Put value.serialize().hash() -> value.serialize()
    val put2 = dbVal.put(txn, valueHashedBuf, valueBuf)
    return put1 && put2
  }


  inline fun <K : Serializable, V : Serializable?> deleteDup(key: K, value: V, dbDup: DbiB, dbVal: DbiB): Boolean {
    val keyHashedBytes = key.serializeThenHash()
    val valueHashedBytes = value.serializeThenHash()
    return deleteDup(keyHashedBytes.toBuffer(), valueHashedBytes.toBuffer(), dbDup, dbVal)
  }

  inline fun deleteDup(keyHashedBuf: Buf, hashedValue: Buf, dbDup: DbiB, @Suppress("UNUSED_PARAMETER") dbVal: DbiB): Boolean {
    return dbDup.delete(txn, keyHashedBuf, hashedValue)
    // TODO: cannot delete (hashedValue, value) from value database, since multiple entries from dbDup may refer to it.
  }
}
