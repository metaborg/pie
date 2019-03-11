package mb.pie.runtime.layer

import mb.pie.api.*
import mb.pie.runtime.exec.BottomUpShared.hasTransitiveTaskReq
import java.io.*
import java.util.*

public class ValidationLayer : Layer {
  public class Options {
    public var cycle: Boolean = true;
    public var overlappingResourceProvide: Boolean = true;
    public var provideAfterRequire: Boolean = true;
    public var requireWithoutDepToProvider: Boolean = true;

    public var keyObject: Boolean = false;
    public var inputObject: Boolean = false;
    public var outputObject: Boolean = false;

    public var throwErrors: Boolean = true;
    public var throwWarnings: Boolean = false
  }

  private val logger: Logger;
  private val stack: HashSet<TaskKey> = HashSet<TaskKey>();
  public var options: Options = Options();


  public constructor(logger: Logger) {
    this.logger = logger
  }


  override fun requireTopDownStart(currentTask: TaskKey, input: In) {
    if(stack.contains(currentTask)) {
      // Cyclic dependency.
      val sb: StringBuilder = StringBuilder();
      sb.append("Cyclic dependency. Cause:\n");
      sb.append("requirement of task\n");
      sb.append("  " + currentTask + "\n");
      sb.append("from requirements\n");
      sb.append("  " + stack.joinToString(" -> "));
      error(sb.toString());
    }
    stack.add(currentTask);

    if(options.keyObject) {
      validateKey(currentTask);
    }
    if(options.inputObject) {
      validateInput(input, currentTask);
    }
  }

  override fun requireTopDownEnd(key: TaskKey) {
    stack.remove(key);
  }

  override fun <I : In, O : Out> validatePreWrite(currentTask: TaskKey, data: TaskData<I, O>, txn: StoreReadTxn) {
    for(provideDep: ResourceProvideDep in data.resourceProvides) {
      val resource: ResourceKey = provideDep.key;
      val provider: TaskKey? = txn.providerOf(resource);
      if(provider != null && !provider.equals(currentTask)) {
        // Overlapping provider tasks for resource.
        val sb: StringBuilder = StringBuilder();
        sb.append("Overlapping provider tasks for resource. Cause:\n");
        sb.append("resource\n");
        sb.append("  " + resource + "\n");
        sb.append("was provided by task\n");
        sb.append("  " + currentTask + "\n");
        sb.append("and task\n");
        sb.append("  " + provider);
        error(sb.toString());
      }
    }
  }

  override fun <I : In, O : Out> validatePostWrite(currentTask: TaskKey, data: TaskData<I, O>, txn: StoreReadTxn) {
    for(requireDep: ResourceRequireDep in data.resourceRequires) {
      val resource: ResourceKey = requireDep.key;
      val provider: TaskKey? = txn.providerOf(resource);
      if(provider == null) {
        // No provider for resource.
      } else if(currentTask.equals(provider)) {
        // Required resource provided by current task.
      } else if(!hasTransitiveTaskReq(txn, currentTask, provider)) {
        // Resource is required by current task, and resource is provided by task `provider`, thus the current task must (transitively) require task `provider`.
        val sb: StringBuilder = StringBuilder();
        sb.append("Hidden dependency. Cause:\n");
        sb.append("task\n");
        sb.append("  " + currentTask + "\n");
        sb.append("requires resource\n");
        sb.append("  " + resource + "\n");
        sb.append("provided by task\n");
        sb.append("  " + provider + "\n");
        sb.append("without a (transitive) task requirement on it");
        error(sb.toString());
      }
    }

    for(provideDep: ResourceProvideDep in data.resourceProvides) {
      val resource: ResourceKey = provideDep.key;
      val requirees: MutableSet<TaskKey> = txn.requireesOf(resource);
      for(requiree: TaskKey in requirees) {
        if(currentTask.equals(requiree)) {
          // Required resource provided by itself current task.
        } else if(!hasTransitiveTaskReq(txn, requiree, currentTask)) {
          // Resource is provided by current task, and resource is required by task 'requiree', thus task 'requiree' must (transitively) require the current task.
          val sb: StringBuilder = StringBuilder();
          sb.append("Hidden dependency. Cause:\n");
          sb.append("resource\n");
          sb.append("  " + resource + "\n");
          sb.append("was provided by task\n");
          sb.append("  " + currentTask + "\n");
          sb.append("after being previously required by task\n");
          sb.append("  " + requiree);
          error(sb.toString());
        }
      }
    }

    if(options.outputObject) {
      validateOutput(data.output, currentTask);
    }
  }

  private fun validateKey(key: TaskKey) {
    val errors: List<String> = validateObject(key.key, true);
    if(!errors.isEmpty()) {
      val sb: StringBuilder = StringBuilder();
      sb.append("Task key:\n");
      sb.append("  " + key + "\n");
      sb.append("failed one or more validation checks:\n");
      sb.append("\n");
      var first: Boolean = true;
      for(error: String in errors) {
        if(!first) sb.append("\n\n");
        first = false;
        sb.append("* " + error);
      }
      warn(sb.toString());
    }
  }

  private fun validateInput(input: In, key: TaskKey) {
    val errors: List<String> = validateObject(input, false);
    if(!errors.isEmpty()) {
      val sb: StringBuilder = StringBuilder();
      sb.append("Input:\n");
      sb.append("  " + input + "\n");
      sb.append("of task with key\n");
      sb.append("  " + key);
      sb.append("failed one or more validation checks:\n");
      sb.append("\n");
      var first: Boolean = true;
      for(error: String in errors) {
        if(!first) sb.append("\n\n");
        first = false;
        sb.append("* " + error);
      }
      warn(sb.toString());
    }
  }

  private fun validateOutput(output: Out, key: TaskKey) {
    val errors: List<String>;
    if(output is OutTransientEquatable<*, *>) {
      errors = validateObject(output.getEquatableValue(), false);
    } else {
      errors = validateObject(output, false);
    }
    if(!errors.isEmpty()) {
      val sb: StringBuilder = StringBuilder();
      sb.append("Output:\n");
      sb.append("  " + output + "\n");
      sb.append("of task with key\n");
      sb.append("  " + key);
      sb.append("failed one or more validation checks:\n");
      sb.append("\n");
      var first: Boolean = true;
      for(error: String in errors) {
        if(!first) sb.append("\n\n");
        first = false;
        sb.append("* " + error);
      }
      warn(sb.toString());
    }
  }

  private fun validateObject(obj: Serializable?, checkSerializeRoundtrip: Boolean): List<String> {
    val errors: MutableList<String> = ArrayList<String>();
    if(obj == null) {
      return errors;
    }
    val serializedBeforeCalls: ByteArray = serialize(obj);
    val serializedBeforeCallsAgain: ByteArray = serialize(obj);

    // Check equality and hashCode after serialization because they may change the object's internal state.
    // Check self equality.
    if(!obj.equals(obj)) {
      errors.add("Not equal to itself.\n  Possible cause: incorrect equals implementation.");
    }

    // Check self hash.
    run {
      val hash1: Int = obj.hashCode();
      val hash2: Int = obj.hashCode();
      if(hash1 != hash2) {
        errors.add("Produced different hash codes.\n  Possible cause: incorrect hashCode implementation.\n  Hashes:\n    " + hash1 + "\n  vs\n    " + hash2);
      }
    }

    // Check serialized output.
    val serializedAfterCalls: ByteArray = serialize(obj);
    val serializedAfterCallsAgain: ByteArray = serialize(obj);
    if(!Arrays.equals(serializedBeforeCalls, serializedBeforeCallsAgain)) {
      errors.add("Serialized representation is different when serialized twice.\n  Possible cause: incorrect serialization implementation.\n  Serialized bytes:\n    " + serializedBeforeCalls + "\n  vs\n    " + serializedAfterCalls);
    } else if(!Arrays.equals(serializedBeforeCalls, serializedAfterCalls)) {
      errors.add("Serialized representation is different when serialized twice, with calls to equals and hashCode in between.\n  Possible cause: incorrect serialization implementation, possibly by using a non-transient hashCode cache.\n  Serialized bytes:\n    " + serializedBeforeCalls + "\n  vs\n    " + serializedAfterCalls);
    } else if(!Arrays.equals(serializedAfterCalls, serializedAfterCallsAgain)) {
      errors.add("Serialized representation is different when serialized twice, after calls to equals and hashcode.\n  Possible cause: incorrect serialization implementation.\n  Serialized bytes:\n    " + serializedAfterCalls + "\n  vs\n    " + serializedAfterCallsAgain);
    }

    if(checkSerializeRoundtrip) {
      // Check serialize-deserialize roundtrip.
      // Equality.
      val objDeserializedBeforeCalls: Serializable = deserialize<Serializable>(serializedBeforeCalls);
      val objDeserializedAfterCalls: Serializable = deserialize<Serializable>(serializedAfterCalls);
      if(!obj.equals(objDeserializedBeforeCalls) || !objDeserializedBeforeCalls.equals(obj)) {
        errors.add("Not equal to itself after deserialization.\n  Possible cause: incorrect serialization or equals implementation.\n  Objects:\n    " + obj + "\n  vs\n    " + objDeserializedBeforeCalls);
      } else if(!obj.equals(objDeserializedAfterCalls) || !objDeserializedAfterCalls.equals(obj)) {
        errors.add("Not equal to itself after deserialization, when serialized with calls to equals and hashCode in between.\n  Possible cause: incorrect serialization or equals implementation, possibly by using a non-transient hashCode cache.\n  Objects:\n    " + obj + "\n  vs\n    " + objDeserializedAfterCalls);
      }
      // Hash code.
      run {
        val beforeHash1: Int = obj.hashCode();
        val beforeHash2: Int = objDeserializedBeforeCalls.hashCode();
        if(beforeHash1 != beforeHash2) {
          errors.add("Produced different hash codes after deserialization.\n  Possible cause: incorrect serialization or hashCode implementation.\n  Hashes:\n    " + beforeHash1 + "\n  vs\n    " + beforeHash2);
        } else {
          val afterHash1: Int = obj.hashCode();
          val afterHash2: Int = objDeserializedAfterCalls.hashCode();
          if(afterHash1 != afterHash2) {
            errors.add("Produced different hash codes after deserialization, when serialized with calls to equals and hashCode in between.\n  Possible cause: incorrect serialization or hashCode implementation.\n  Hashes:\n    " + afterHash1 + "\n  vs\n    " + afterHash2);
          } else {
          }
        }
      }

      // Check serialize-deserialize-serialize roundtrip.
      val serializedBeforeCallsTwice: ByteArray = serialize(objDeserializedBeforeCalls);
      val serializedAfterCallsTwice: ByteArray = serialize(objDeserializedAfterCalls);
      if(!Arrays.equals(serializedBeforeCalls, serializedBeforeCallsTwice)) {
        errors.add("Serialized representation is different after round-trip serialization.\n  Possible cause: incorrect serialization implementation.\n  Serialized bytes:\n    " + serializedBeforeCalls + "\n  vs\n    " + serializedBeforeCallsTwice);
      } else if(!Arrays.equals(serializedAfterCalls, serializedAfterCallsTwice)) {
        errors.add("Serialized representation is different after round-trip serialization, with calls to equals and hashCode in between.\n  Possible cause: incorrect serialization implementation, possibly by using a non-transient hashCode cache.\n  Serialized bytes:\n    " + serializedBeforeCalls + "\n  vs\n    " + serializedBeforeCallsTwice);
      }
    }

    return errors;
  }

  @Throws(IOException::class)
  private fun serialize(obj: Serializable): ByteArray {
    ByteArrayOutputStream().use { outputStream ->
      ObjectOutputStream(outputStream).use { objectOutputStream ->
        objectOutputStream.writeObject(obj);
        objectOutputStream.flush();
      }
      outputStream.flush();
      return outputStream.toByteArray();
    }
  }

  @Throws(ClassNotFoundException::class, IOException::class)
  private fun <T : Serializable> deserialize(bytes: ByteArray): T {
    ByteArrayInputStream(bytes).use { inputStream ->
      ObjectInputStream(inputStream).use { objectInputStream ->
        return objectInputStream.readObject() as T;
      }
    }
  }


  private fun error(message: String) {
    if(options.throwErrors) {
      throw ValidationException(message);
    } else {
      logger.error(message, null);
    }
  }

  private fun warn(message: String) {
    if(options.throwWarnings) {
      throw ValidationException(message);
    } else {
      logger.warn(message, null);
    }
  }


  override fun toString(): String {
    return "ValidationLayer()";
  }
}

public class ValidationException : RuntimeException {
  public constructor(message: String, cause: Throwable? = null) : super(message, cause);
}
