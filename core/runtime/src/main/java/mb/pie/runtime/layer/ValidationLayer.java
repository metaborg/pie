package mb.pie.runtime.layer;

import mb.pie.api.Layer;
import mb.pie.api.Logger;
import mb.pie.api.OutTransientEquatable;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.ResourceRequireDep;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.Task;
import mb.pie.api.TaskData;
import mb.pie.api.TaskDefs;
import mb.pie.api.TaskKey;
import mb.pie.runtime.exec.BottomUpShared;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"StringConcatenationInsideStringBufferAppend", "StatementWithEmptyBody", "StringBufferReplaceableByString"})
public class ValidationLayer implements Layer {
    public static class ValidationOptions {
        public final boolean cycle;
        public final boolean overlappingResourceProvide;
        public final boolean provideAfterRequire;
        public final boolean requireWithoutDepToProvider;

        public final boolean checkSelfEquals;
        public final boolean checkSelfHashCode;
        public final boolean checkSerialization;

        public final boolean checkKeyObjects;
        public final boolean checkInputObjects;
        public final boolean checkOutputObjects;

        public final boolean throwErrors;
        public final boolean throwWarnings;

        public final int shortStringLength;

        public ValidationOptions(
            boolean cycle,
            boolean overlappingResourceProvide,
            boolean provideAfterRequire,
            boolean requireWithoutDepToProvider,
            boolean checkSelfEquals,
            boolean checkSelfHashCode,
            boolean checkSerialization,
            boolean checkKeyObjects,
            boolean checkInputObjects,
            boolean checkOutputObjects,
            boolean throwErrors,
            boolean throwWarnings,
            int shortStringLength
        ) {
            this.cycle = cycle;
            this.overlappingResourceProvide = overlappingResourceProvide;
            this.provideAfterRequire = provideAfterRequire;
            this.requireWithoutDepToProvider = requireWithoutDepToProvider;
            this.checkSelfEquals = checkSelfEquals;
            this.checkSelfHashCode = checkSelfHashCode;
            this.checkSerialization = checkSerialization;
            this.checkKeyObjects = checkKeyObjects;
            this.checkInputObjects = checkInputObjects;
            this.checkOutputObjects = checkOutputObjects;
            this.throwErrors = throwErrors;
            this.throwWarnings = throwWarnings;
            this.shortStringLength = shortStringLength;
        }

        public static ValidationOptions normal() {
            return new ValidationOptions(
                true,
                true,
                true,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                true,
                false,
                1024
            );
        }

        public static ValidationOptions all() {
            return new ValidationOptions(
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                4096
            );
        }

        public static ValidationOptions all_except_serialization() {
            return new ValidationOptions(
                true,
                true,
                true,
                true,
                true,
                true,
                false,
                true,
                true,
                true,
                true,
                true,
                4096
            );
        }
    }

    private final ValidationOptions options;
    private final TaskDefs taskDefs;
    private final Logger logger;
    private final HashSet<TaskKey> stack = new HashSet<>();


    public ValidationLayer(ValidationOptions options, TaskDefs taskDefs, Logger logger) {
        this.options = options;
        this.taskDefs = taskDefs;
        this.logger = logger;
    }

    public ValidationLayer(TaskDefs taskDefs, Logger logger) {
        this(ValidationOptions.normal(), taskDefs, logger);
    }


    @Override public void requireTopDownStart(TaskKey currentTask, Serializable input) {
        final String taskDefId = currentTask.id;
        if(!taskDefs.exists(taskDefId)) {
            throw new RuntimeException(
                "Required task '" + currentTask.toShortString(
                    100) + "', but the ID of its task definition '" + taskDefId + "' has not been registered with the task definition collection");
        }

        if(stack.contains(currentTask)) {
            // Cyclic dependency.
            final StringBuilder sb = new StringBuilder();
            sb.append("Cyclic dependency. Cause:\n");
            sb.append("requirement of task\n");
            sb.append("  " + currentTask.toShortString(options.shortStringLength) + "\n");
            sb.append("from requirements\n");
            sb.append("  " + stack.stream().map((k) -> k.toShortString(options.shortStringLength)).collect(Collectors.joining(" -> ")));
            error(sb.toString());
        }
        stack.add(currentTask);

        if(options.checkKeyObjects) {
            validateKey(currentTask);
        }
        if(options.checkInputObjects) {
            validateInput(input, currentTask);
        }
    }

    @Override public void requireTopDownEnd(TaskKey key) {
        stack.remove(key);
    }

    @Override public void validateVisited(TaskKey currentTaskKey, Task<?> currentTask, TaskData visitedData) {
        if(!visitedData.input.equals(currentTask.input)) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Visited task with same key was required with different input in same session. Cause:\n");
            sb.append("task with key\n");
            sb.append("  " + currentTaskKey.toShortString(options.shortStringLength) + "\n");
            sb.append("was already visited with input\n");
            sb.append("  " + visitedData.input + "\n");
            sb.append("while now required with input\n");
            sb.append("  " + currentTask.input);
            error(sb.toString());
        }
    }

    @Override public void validatePreWrite(TaskKey currentTaskKey, TaskData dataToStore, StoreReadTxn txn) {
        for(ResourceProvideDep provideDep : dataToStore.resourceProvides) {
            final ResourceKey resource = provideDep.key;
            final @Nullable TaskKey provider = txn.providerOf(resource);
            if(provider != null && !provider.equals(currentTaskKey)) {
                // Overlapping provider tasks for resource.
                final StringBuilder sb = new StringBuilder();
                sb.append("Overlapping provider tasks for resource. Cause:\n");
                sb.append("resource\n");
                sb.append("  " + resource + "\n");
                sb.append("was provided by task\n");
                sb.append("  " + currentTaskKey.toShortString(options.shortStringLength) + "\n");
                sb.append("and task\n");
                sb.append("  " + provider.toShortString(options.shortStringLength));
                error(sb.toString());
            }
        }
    }

    @Override public void validatePostWrite(TaskKey currentTaskKey, TaskData dataToStore, StoreReadTxn txn) {
        for(ResourceRequireDep requireDep : dataToStore.resourceRequires) {
            final ResourceKey resource = requireDep.key;
            final @Nullable TaskKey provider = txn.providerOf(resource);
            if(provider == null) {
                // No provider for resource.
            } else if(currentTaskKey.equals(provider)) {
                // Required resource provided by current task.
            } else if(!BottomUpShared.hasTransitiveTaskReq(txn, currentTaskKey, provider)) {
                // Resource is required by current task, and resource is provided by task `provider`, thus the current task must (transitively) require task `provider`.
                final StringBuilder sb = new StringBuilder();
                sb.append("Hidden dependency. Cause:\n");
                sb.append("task\n");
                sb.append("  " + currentTaskKey.toShortString(options.shortStringLength) + "\n");
                sb.append("requires resource\n");
                sb.append("  " + resource + "\n");
                sb.append("provided by task\n");
                sb.append("  " + provider.toShortString(options.shortStringLength) + "\n");
                sb.append("without a (transitive) task requirement on it");
                error(sb.toString());
            }
        }

        for(ResourceProvideDep provideDep : dataToStore.resourceProvides) {
            final ResourceKey resource = provideDep.key;
            final Set<TaskKey> requirees = txn.requireesOf(resource);
            for(TaskKey requiree : requirees) {
                if(currentTaskKey.equals(requiree)) {
                    // Required resource provided by current task.
                } else if(!BottomUpShared.hasTransitiveTaskReq(txn, requiree, currentTaskKey)) {
                    // Resource is provided by current task, and resource is required by task `requiree`, thus task `requiree` must (transitively) require the current task.
                    final StringBuilder sb = new StringBuilder();
                    sb.append("Hidden dependency. Cause:\n");
                    sb.append("resource\n");
                    sb.append("  " + resource + "\n");
                    sb.append("was provided by task\n");
                    sb.append("  " + currentTaskKey.toShortString(options.shortStringLength) + "\n");
                    sb.append("after being previously required by task\n");
                    sb.append("  " + requiree.toShortString(options.shortStringLength));
                    error(sb.toString());
                }
            }
        }

        if(options.checkOutputObjects) {
            validateOutput(dataToStore.output, currentTaskKey);
        }
    }

    private void validateKey(TaskKey key) {
        final List<String> errors = validateObject(key.key, true);
        if(!errors.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Task key:\n");
            sb.append("  " + key.toShortString(options.shortStringLength) + "\n");
            sb.append("failed one or more validation checks:\n");
            sb.append("\n");
            boolean first = true;
            for(String error : errors) {
                if(!first) sb.append("\n\n");
                first = false;
                sb.append("* " + error);
            }
            warn(sb.toString());
        }
    }

    private void validateInput(Serializable input, TaskKey key) {
        final List<String> errors = validateObject(input, false);
        if(!errors.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Input:\n");
            sb.append("  " + input + "\n");
            sb.append("of task with key\n");
            sb.append("  " + key.toShortString(options.shortStringLength));
            sb.append("failed one or more validation checks:\n");
            sb.append("\n");
            boolean first = true;
            for(String error : errors) {
                if(!first) sb.append("\n\n");
                first = false;
                sb.append("* " + error);
            }
            warn(sb.toString());
        }
    }

    private void validateOutput(@Nullable Serializable output, TaskKey key) {
        final List<String> errors;
        if(output instanceof OutTransientEquatable<?, ?>) {
            final OutTransientEquatable<?, ?> outTransEq = (OutTransientEquatable<?, ?>) output;
            errors = validateObject(outTransEq.getEquatableValue(), false);
        } else {
            errors = validateObject(output, false);
        }
        if(!errors.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Output:\n");
            sb.append("  " + output + "\n");
            sb.append("of task with key\n");
            sb.append("  " + key.toShortString(options.shortStringLength));
            sb.append("failed one or more validation checks:\n");
            sb.append("\n");
            boolean first = true;
            for(String error : errors) {
                if(!first) sb.append("\n\n");
                first = false;
                sb.append("* " + error);
            }
            warn(sb.toString());
        }
    }

    @SuppressWarnings("EqualsWithItself")
    private List<String> validateObject(@Nullable Serializable obj, boolean checkSerializeRoundtrip) {
        final ArrayList<String> errors = new ArrayList<>();
        if(obj == null) {
            return errors;
        }

        byte[] serializedBeforeCalls = new byte[0];
        byte[] serializedBeforeCallsAgain = new byte[0];
        if(options.checkSerialization) {
            serializedBeforeCalls = serialize(obj);
            serializedBeforeCallsAgain = serialize(obj);
        }

        // Check equality and hashCode after serialization because they may change the object's internal state.
        if(options.checkSelfEquals) {
            if(!obj.equals(obj)) {
                final String serializeNote;
                if(options.checkSerialization) {
                    serializeNote = " Note: equals was called after serializing the object. Make sure that it does not compare internal state that changes after being serialized.";
                } else {
                    serializeNote = "";
                }
                errors.add("Not equal to itself.\n  Possible incorrect equals implementation." + serializeNote);
            }
        }
        if(options.checkSelfHashCode) {
            final int hash1 = obj.hashCode();
            final int hash2 = obj.hashCode();
            if(hash1 != hash2) {
                final String serializeNote;
                if(options.checkSerialization) {
                    serializeNote = " Note: hashCode was called after serializing the object. Make sure that it does not hash internal state that changes after being serialized.";
                } else {
                    serializeNote = "";
                }
                errors.add("Hash code not equal to hash code of itself.\n  Possible incorrect hashCode implementation." + serializeNote + "\n  Hashes:\n    " + hash1 + "\n  vs\n    " + hash2);
            }
        }

        if(!options.checkSerialization) {
            return errors;
        }

        // Check serialized output.
        final byte[] serializedAfterCalls = serialize(obj);
        final byte[] serializedAfterCallsAgain = serialize(obj);
        if(!Arrays.equals(serializedBeforeCalls, serializedBeforeCallsAgain)) {
            errors.add(
                "Serialized representation is different when serialized twice.\n  Possible incorrect cause serialization implementation.\n  Serialized bytes:\n    " + Arrays.toString(serializedBeforeCalls) + "\n  vs\n    " + Arrays.toString(serializedAfterCalls));
        } else if(!Arrays.equals(serializedBeforeCalls, serializedAfterCalls)) {
            errors.add(
                "Serialized representation is different when serialized twice, with calls to equals and hashCode : between.\n  Possible incorrect cause serialization implementation, possibly by using a non-transient hashCode cache.\n  Serialized bytes:\n    " + Arrays.toString(serializedBeforeCalls) + "\n  vs\n    " + Arrays.toString(serializedAfterCalls));
        } else if(!Arrays.equals(serializedAfterCalls, serializedAfterCallsAgain)) {
            errors.add(
                "Serialized representation is different when serialized twice, after calls to equals and hashcode.\n  Possible incorrect cause serialization implementation.\n  Serialized bytes:\n    " + Arrays.toString(serializedAfterCalls) + "\n  vs\n    " + Arrays.toString(serializedAfterCallsAgain));
        }

        if(checkSerializeRoundtrip) {
            // Check serialize-deserialize roundtrip.
            // Equality.
            final Serializable objDeserializedBeforeCalls = deserialize(serializedBeforeCalls);
            final Serializable objDeserializedAfterCalls = deserialize(serializedAfterCalls);
            if(!obj.equals(objDeserializedBeforeCalls) || !objDeserializedBeforeCalls.equals(obj)) {
                errors.add(
                    "Not equal to itself after deserialization.\n  Possible incorrect cause serialization or equals implementation.\n  Objects:\n    " + obj + "\n  vs\n    " + objDeserializedBeforeCalls);
            } else if(!obj.equals(objDeserializedAfterCalls) || !objDeserializedAfterCalls.equals(obj)) {
                errors.add(
                    "Not equal to itself after deserialization, when serialized with calls to equals and hashCode : between.\n  Possible incorrect cause serialization or equals implementation, possibly by using a non-transient hashCode cache.\n  Objects:\n    " + obj + "\n  vs\n    " + objDeserializedAfterCalls);
            }
            // Hash code.
            {
                final int beforeHash1 = obj.hashCode();
                final int beforeHash2 = objDeserializedBeforeCalls.hashCode();
                if(beforeHash1 != beforeHash2) {
                    errors.add(
                        "Produced different hash codes after deserialization.\n  Possible incorrect cause serialization or hashCode implementation.\n  Hashes:\n    " + beforeHash1 + "\n  vs\n    " + beforeHash2);
                } else {
                    final int afterHash1 = obj.hashCode();
                    final int afterHash2 = objDeserializedAfterCalls.hashCode();
                    if(afterHash1 != afterHash2) {
                        errors.add(
                            "Produced different hash codes after deserialization, when serialized with calls to equals and hashCode : between.\n  Possible incorrect cause serialization or hashCode implementation.\n  Hashes:\n    " + afterHash1 + "\n  vs\n    " + afterHash2);
                    } else {
                    }
                }
            }

            // Check serialize-deserialize-serialize roundtrip.
            final byte[] serializedBeforeCallsTwice = serialize(objDeserializedBeforeCalls);
            final byte[] serializedAfterCallsTwice = serialize(objDeserializedAfterCalls);
            if(!Arrays.equals(serializedBeforeCalls, serializedBeforeCallsTwice)) {
                errors.add(
                    "Serialized representation is different after round-trip serialization.\n  Possible incorrect cause serialization implementation.\n  Serialized bytes:\n    " + Arrays.toString(serializedBeforeCalls) + "\n  vs\n    " + Arrays.toString(serializedBeforeCallsTwice));
            } else if(!Arrays.equals(serializedAfterCalls, serializedAfterCallsTwice)) {
                errors.add(
                    "Serialized representation is different after round-trip serialization, with calls to equals and hashCode : between.\n  Possible incorrect cause serialization implementation, possibly by using a non-transient hashCode cache.\n  Serialized bytes:\n    " + Arrays.toString(serializedBeforeCalls) + "\n  vs\n    " + Arrays.toString(serializedBeforeCallsTwice));
            }
        }

        return errors;
    }

    private byte[] serialize(Serializable obj) {
        try(
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)
        ) {
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            outputStream.flush();
            return outputStream.toByteArray();
        } catch(IOException e) {
            throw new ValidationException("Serialization of " + obj + " in validation failed unexpectedly", e);
        }
    }

    private <T extends Serializable> T deserialize(byte[] bytes) {
        try(
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)
        ) {
            @SuppressWarnings("unchecked") final T obj = (T) objectInputStream.readObject();
            return obj;
        } catch(IOException | ClassNotFoundException e) {
            throw new ValidationException("Deserialization in validation failed unexpectedly", e);
        }
    }


    private void error(String message) {
        if(options.throwErrors) {
            throw new ValidationException(message);
        } else {
            logger.error(message, null);
        }
    }

    private void warn(String message) {
        if(options.throwWarnings) {
            throw new ValidationException(message);
        } else {
            logger.warn(message, null);
        }
    }


    @Override public String toString() {
        return "ValidationLayer()";
    }
}

