package mb.pie.runtime.layer

import mb.pie.api.*
import mb.pie.runtime.exec.hasTransitiveTaskReq
import java.io.*
import java.util.*

class ValidationLayer constructor(
  private val logger: Logger
) : Layer {
  data class Options(
    var cycle: Boolean = true,
    var overlappingResourceProvide: Boolean = true,
    var provideAfterRequire: Boolean = true,
    var requireWithoutDepToProvider: Boolean = true,

    var keyObject: Boolean = false,
    var inputObject: Boolean = false,
    var outputObject: Boolean = false,

    var throwErrors: Boolean = true,
    var throwWarnings: Boolean = false
  )

  var options = Options()
  private val stack = mutableSetOf<TaskKey>()


  override fun requireTopDownStart(key: TaskKey, input: In) {
    if(stack.contains(key)) {
      error("""Cyclic dependency. Cause:
        |requirement of task
        |  $key
        |from requirements
        |  ${stack.joinToString(" -> ")}
        |""".trimMargin())
    }
    stack.add(key)

    if(options.keyObject) {
      validateKey(key)
    }
    if(options.inputObject) {
      validateInput(input, key)
    }
  }

  override fun requireTopDownEnd(key: TaskKey) {
    stack.remove(key)
  }

  override fun <I : In, O : Out> validatePreWrite(key: TaskKey, data: TaskData<I, O>, txn: StoreReadTxn) {
    for(provideDep in data.resourceProvides) {
      val path = provideDep.key
      val provider = txn.providerOf(path)
      if(provider != null && provider != key) {
        // Overlapping provider tasks for resource `path`.
        error("""Overlapping provider tasks for resource. Cause:
          |resource
          |  $path
          |was provided by task
          |  $key
          |and task
          |  $provider
          |""".trimMargin())
      }
    }
  }

  override fun <I : In, O : Out> validatePostWrite(key: TaskKey, data: TaskData<I, O>, txn: StoreReadTxn) {
    for(requireDep in data.resourceRequires) {
      val path = requireDep.key
      val provider = txn.providerOf(path)
      when {
        provider == null -> {
          // No generator for `file`.
        }
        key == provider -> {
          // Required `file` provided by itself (`key`).
        }
        !txn.hasTransitiveTaskReq(key, provider) -> {
          // Resource `path` is required by task `key`, and resource `path` is provided by task `provider`, thus task `key` must (transitively) require task `provider`.
          error("""Hidden dependency. Cause:
            |task
            |  $key
            |requires resource
            |  $path
            |provided by task
            |  $provider
            |without a (transitive) task requirement on it
            |""".trimMargin())
        }
      }
    }

    for(provideDep in data.resourceProvides) {
      val path = provideDep.key
      val requiredByApps = txn.requireesOf(path)
      for(requiredBy in requiredByApps) {
        // Resource 'path' is provided by task 'key', and resource is required by task 'requiredBy', thus task 'requiredBy' must (transitively) require task 'key'.
        when {
          key == requiredBy -> {
            // Required `file` provided by itself (`task`).
          }
          !txn.hasTransitiveTaskReq(requiredBy, key) -> {
            error("""Hidden dependency. Cause:
              |resource
              |  $path
              |was provided by task
              |  $key
              |after being previously required by task
              |  $requiredBy
              |""".trimMargin())
          }
        }
      }
    }

    if(options.outputObject) {
      validateOutput(data.output, key)
    }
  }

  private fun validateKey(key: TaskKey) {
    val errors = validateObject(key.key, true)
    if(errors.isNotEmpty()) {
      val errorsStr = errors
        .mapIndexed { i, msg -> "$i) $msg" }
        .joinToString("\n\n")
      val message = """Task key:
        |  $key
        |failed one or more validation checks:
        |
        |$errorsStr
      """.trimMargin()
      warn(message)
    }
  }

  private fun validateInput(input: In, key: TaskKey) {
    val errors = validateObject(input, false)
    if(errors.isNotEmpty()) {
      val errorsStr = errors
        .mapIndexed { i, msg -> "$i) $msg" }
        .joinToString("\n\n")
      val message = """Input:
        |  $input
        |of task with key:
        |  $key
        |failed one or more validation checks:
        |
        |$errorsStr
      """.trimMargin()
      warn(message)
    }
  }

  private fun <O : Out> validateOutput(output: O, key: TaskKey) {
    val errors =
      if(output is OutTransientEquatableImpl<*, *>) {
        validateObject(output.getEquatableValue(), false)
      } else {
        validateObject(output, false)
      }
    if(errors.isNotEmpty()) {
      val errorsStr = errors
        .mapIndexed { i, msg -> "$i) $msg" }
        .joinToString("\n\n")
      val message = """Output:
        |  $output
        |of task with key:
        |  $key
        |failed one or more validation checks:
        |
        |$errorsStr
      """.trimMargin()
      warn(message)
    }
  }

  private fun validateObject(obj: Serializable?, checkSerializeRoundtrip: Boolean): List<String> {
    val errors = mutableListOf<String>()
    if(obj == null) {
      return errors
    }
    val serializedBeforeCalls = serialize(obj)
    val serializedBeforeCallsAgain = serialize(obj)

    // Check equality and hashCode after serialization because they may change the object's internal state.
    // Check self equality.
    if(obj != obj) {
      errors.add("""Not equal to itself.
        |Possible cause: incorrect equals implementation.""".trimMargin())
    }

    // Check self hash.
    run {
      val hash1 = obj.hashCode()
      val hash2 = obj.hashCode()
      if(hash1 != hash2) {
        errors.add("""Produced different hash codes.
          |  Possible cause: incorrect hashCode implementation.
          |  Hashes:
          |    $hash1
          |  vs
          |    $hash2""".trimMargin())
      }
    }

    // Check serialized output.
    val serializedAfterCalls = serialize(obj)
    val serializedAfterCallsAgain = serialize(obj)
    if(!Arrays.equals(serializedBeforeCalls, serializedBeforeCallsAgain)) {
      errors.add("""Serialized representation is different when serialized twice.
        |  Possible cause: incorrect serialization implementation.
        |  Serialized bytes:
        |    $serializedBeforeCalls
        |  vs
        |    $serializedAfterCalls""".trimMargin())
    } else if(!Arrays.equals(serializedBeforeCalls, serializedAfterCalls)) {
      errors.add("""Serialized representation is different when serialized twice, with calls to equals and hashCode in between.
        |  Possible cause: incorrect serialization implementation, possibly by using a non-transient hashCode cache.
        |  Serialized bytes:
        |    $serializedBeforeCalls
        |  vs
        |    $serializedAfterCalls""".trimMargin())
    } else if(!Arrays.equals(serializedAfterCalls, serializedAfterCallsAgain)) {
      errors.add("""Serialized representation is different when serialized twice, after calls to equals and hashcode.
        |  Possible cause: incorrect serialization implementation.
        |  Serialized bytes:
        |    $serializedAfterCalls
        |  vs
        |    $serializedAfterCallsAgain""".trimMargin())
    }

    if(checkSerializeRoundtrip) {
      // Check serialize-deserialize roundtrip.
      // Equality.
      val objDeserializedBeforeCalls = deserialize<Serializable>(serializedBeforeCalls)
      val objDeserializedAfterCalls = deserialize<Serializable>(serializedAfterCalls)
      if(obj != objDeserializedBeforeCalls || objDeserializedBeforeCalls != obj) {
        errors.add("""Not equal to itself after deserialization.
        |  Possible cause: incorrect serialization or equals implementation.
        |  Objects:
        |    $obj
        |  vs
        |    $objDeserializedBeforeCalls""".trimMargin())
      } else if(obj != objDeserializedAfterCalls || objDeserializedAfterCalls != obj) {
        errors.add("""Not equal to itself after deserialization, when serialized with calls to equals and hashCode in between.
        |  Possible cause: incorrect serialization or equals implementation, possibly by using a non-transient hashCode cache.
        |  Objects:
        |    $obj
        |  vs
        |    $objDeserializedAfterCalls""".trimMargin())
      }
      // Hash code.
      run {
        val beforeHash1 = obj.hashCode()
        val beforeHash2 = objDeserializedBeforeCalls.hashCode()
        if(beforeHash1 != beforeHash2) {
          errors.add("""Produced different hash codes after deserialization.
          |  Possible cause: incorrect serialization or hashCode implementation.
          |  Hashes:
          |    $beforeHash1
          |  vs
          |    $beforeHash2""".trimMargin())
        } else {
          val afterHash1 = obj.hashCode()
          val afterHash2 = objDeserializedAfterCalls.hashCode()
          if(afterHash1 != afterHash2) {
            errors.add("""Produced different hash codes after deserialization, when serialized with calls to equals and hashCode in between.
            |  Possible cause: incorrect serialization or hashCode implementation.
            |  Hashes:
            |    $afterHash1
            |  vs
            |    $afterHash2""".trimMargin())
          } else {
          }
        }
      }

      // Check serialize-deserialize-serialize roundtrip.
      val serializedBeforeCallsTwice = serialize(objDeserializedBeforeCalls)
      val serializedAfterCallsTwice = serialize(objDeserializedAfterCalls)
      if(!Arrays.equals(serializedBeforeCalls, serializedBeforeCallsTwice)) {
        errors.add("""Serialized representation is different after round-trip serialization.
        |  Possible cause: incorrect serialization implementation.
        |  Serialized bytes:
        |    $serializedBeforeCalls
        |  vs
        |    $serializedBeforeCallsTwice""".trimMargin())
      } else if(!Arrays.equals(serializedAfterCalls, serializedAfterCallsTwice)) {
        errors.add("""Serialized representation is different after round-trip serialization, with calls to equals and hashCode in between.
        |  Possible cause: incorrect serialization implementation, possibly by using a non-transient hashCode cache.
        |  Serialized bytes:
        |    $serializedBeforeCalls
        |  vs
        |    $serializedBeforeCallsTwice""".trimMargin())
      }
    }

    return errors
  }

  @Throws(IOException::class)
  private fun serialize(obj: Serializable): ByteArray {
    ByteArrayOutputStream().use { outputStream ->
      ObjectOutputStream(outputStream).use { objectOutputStream ->
        objectOutputStream.writeObject(obj)
        objectOutputStream.flush()
        return outputStream.toByteArray()
      }
    }
  }

  @Throws(ClassNotFoundException::class, IOException::class)
  private fun <T : Serializable> deserialize(bytes: ByteArray): T {
    ByteArrayInputStream(bytes).use { inputStream ->
      ObjectInputStream(inputStream).use { objectInputStream ->
        @Suppress("UNCHECKED_CAST")
        return objectInputStream.readObject() as T
      }
    }
  }


  private fun error(message: String, exception: Exception? = null) {
    if(options.throwErrors) {
      throw ValidationException(message, exception)
    } else {
      logger.error(message, exception)
    }
  }

  private fun warn(message: String, exception: Exception? = null) {
    if(options.throwWarnings) {
      throw ValidationException(message, exception)
    } else {
      logger.warn(message, exception)
    }
  }


  override fun toString() = "ValidationLayer"
}

class ValidationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
