package mb.pie.store.lmdb

import mb.pie.api.*
import org.lmdbjava.*
import java.io.File

/**
 * Sets the store of this builder to the [LMDBStore], stored at given [envDir], with parameters [maxDbSize] determining the maximum
 * database size and [maxReaders] determining the maximum concurrent readers.
 */
@JvmOverloads
fun PieBuilder.withLMDBStore(envDir: File, maxDbSize: Int = 1024 * 1024 * 1024, maxReaders: Int = 1024): PieBuilder {
  this.withStore { logger -> LMDBStore(logger, envDir, maxDbSize, maxReaders) }
  return this
}

typealias EnvB = Env<Buf>
typealias DbiB = Dbi<Buf>
typealias TxnB = Txn<Buf>

class LMDBStore(
  private val logger: Logger,
  envDir: File,
  maxDbSize: Int = 1024 * 1024 * 1024, // 1 GiB
  maxReaders: Int = 64 // 64 concurrent read threads
) : Store {
  private val env: EnvB
  private val input: DbiB
  private val output: DbiB
  private val taskRequires: DbiB
  private val callersOf: DbiB
  private val callersOfValues: DbiB
  private val resourceRequires: DbiB
  private val requireesOf: DbiB
  private val requireesOfValues: DbiB
  private val resourceProvides: DbiB
  private val providerOf: DbiB


  init {
    envDir.mkdirs()
    env = Env.create()
      .setMapSize(maxDbSize.toLong())
      .setMaxReaders(maxReaders)
      .setMaxDbs(10)
      .open(envDir)
    input = env.openDbi("input", DbiFlags.MDB_CREATE)
    output = env.openDbi("output", DbiFlags.MDB_CREATE)
    taskRequires = env.openDbi("taskRequires", DbiFlags.MDB_CREATE)
    callersOf = env.openDbi("callersOf", DbiFlags.MDB_CREATE, DbiFlags.MDB_DUPSORT)
    callersOfValues = env.openDbi("callersOfValues", DbiFlags.MDB_CREATE)
    resourceRequires = env.openDbi("resourceRequires", DbiFlags.MDB_CREATE)
    requireesOf = env.openDbi("requireesOf", DbiFlags.MDB_CREATE, DbiFlags.MDB_DUPSORT)
    requireesOfValues = env.openDbi("requireesOfValues", DbiFlags.MDB_CREATE)
    resourceProvides = env.openDbi("resourceProvides", DbiFlags.MDB_CREATE)
    providerOf = env.openDbi("providerOf", DbiFlags.MDB_CREATE)
  }

  override fun close() {
    env.close()
  }


  override fun readTxn(): StoreReadTxn {
    val txn = env.txnRead()
    return LMDBStoreTxn(env, txn, false, logger,
      inputDb = input,
      outputDb = output,
      taskRequiresDb = taskRequires,
      callersOfDb = callersOf,
      callersOfValuesDb = callersOfValues,
      resourceRequiresDb = resourceRequires,
      requireesOfDb = requireesOf,
      requireesOfValuesDb = requireesOfValues,
      resourceProvidesDb = resourceProvides,
      providerOfDb = providerOf
    )
  }

  override fun writeTxn(): StoreWriteTxn {
    val txn = env.txnWrite()
    return LMDBStoreTxn(env, txn, true, logger,
      inputDb = input,
      outputDb = output,
      taskRequiresDb = taskRequires,
      callersOfDb = callersOf,
      callersOfValuesDb = callersOfValues,
      resourceRequiresDb = resourceRequires,
      requireesOfDb = requireesOf,
      requireesOfValuesDb = requireesOfValues,
      resourceProvidesDb = resourceProvides,
      providerOfDb = providerOf
    )
  }

  override fun sync() {
    env.sync(false)
  }


  override fun toString(): String {
    return "LMDBStore"
  }
}
