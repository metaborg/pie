package mb.pie.runtime.layer;

import mb.pie.api.Layer;
import mb.pie.api.Logger;
import mb.pie.api.OutTransientEquatable;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.ResourceRequireDep;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.TaskData;
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
    public class Options {
        public boolean cycle = true;
        public boolean overlappingResourceProvide = true;
        public boolean provideAfterRequire = true;
        public boolean requireWithoutDepToProvider = true;

        public boolean keyObject = false;
        public boolean inputObject = false;
        public boolean outputObject = false;

        public boolean throwErrors = true;
        public boolean throwWarnings = false;
    }

    private final Logger logger;
    private final HashSet<TaskKey> stack = new HashSet<>();
    public Options options = new Options();


    public ValidationLayer(Logger logger) {
        this.logger = logger;
    }


    @Override public void requireTopDownStart(TaskKey currentTask, Serializable input) {
        if(stack.contains(currentTask)) {
            // Cyclic dependency.
            final StringBuilder sb = new StringBuilder();
            sb.append("Cyclic dependency. Cause:\n");
            sb.append("requirement of task\n");
            sb.append("  " + currentTask + "\n");
            sb.append("from requirements\n");
            sb.append("  " + stack.stream().map(TaskKey::toString).collect(Collectors.joining(" -> ")));
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

    @Override public void requireTopDownEnd(TaskKey key) {
        stack.remove(key);
    }

    @Override
    public <I extends Serializable, O extends @Nullable Serializable> void validatePreWrite(TaskKey currentTask, TaskData<I, O> data, StoreReadTxn txn) {
        for(ResourceProvideDep provideDep : data.resourceProvides) {
            final ResourceKey resource = provideDep.key;
            final @Nullable TaskKey provider = txn.providerOf(resource);
            if(provider != null && !provider.equals(currentTask)) {
                // Overlapping provider tasks for resource.
                final StringBuilder sb = new StringBuilder();
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

    @Override
    public <I extends Serializable, O extends @Nullable Serializable> void validatePostWrite(TaskKey currentTask, TaskData<I, O> data, StoreReadTxn txn) {
        for(ResourceRequireDep requireDep : data.resourceRequires) {
            final ResourceKey resource = requireDep.key;
            final @Nullable TaskKey provider = txn.providerOf(resource);
            if(provider == null) {
                // No provider for resource.
            } else if(currentTask.equals(provider)) {
                // Required resource provided by current task.
            } else if(!BottomUpShared.hasTransitiveTaskReq(txn, currentTask, provider)) {
                // Resource is required by current task, and resource is provided by task `provider`, thus the current task must (transitively) require task `provider`.
                final StringBuilder sb = new StringBuilder();
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

        for(ResourceProvideDep provideDep : data.resourceProvides) {
            final ResourceKey resource = provideDep.key;
            final Set<TaskKey> requirees = txn.requireesOf(resource);
            for(TaskKey requiree : requirees) {
                if(currentTask.equals(requiree)) {
                    // Required resource provided by itself current task.
                } else if(!BottomUpShared.hasTransitiveTaskReq(txn, requiree, currentTask)) {
                    // Resource is provided by current task, and resource is required by task 'requiree', thus task 'requiree' must (transitively) require the current task.
                    final StringBuilder sb = new StringBuilder();
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

    private void validateKey(TaskKey key) {
        final List<String> errors = validateObject(key.key, true);
        if(!errors.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Task key:\n");
            sb.append("  " + key + "\n");
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
            sb.append("  " + key);
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
            sb.append("  " + key);
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
        final ArrayList<String> errors = new ArrayList<String>();
        if(obj == null) {
            return errors;
        }
        final byte[] serializedBeforeCalls = serialize(obj);
        final byte[] serializedBeforeCallsAgain = serialize(obj);

        // Check equality and hashCode after serialization because they may change the object's internal state.
        // Check self equality.
        if(!obj.equals(obj)) {
            errors.add("Not equal to itself.\n  Possible incorrect cause equals implementation.");
        }

        // Check self hash.
        {
            final int hash1 = obj.hashCode();
            final int hash2 = obj.hashCode();
            if(hash1 != hash2) {
                errors.add(
                    "Produced different hash codes.\n  Possible incorrect cause hashCode implementation.\n  Hashes:\n    " + hash1 + "\n  vs\n    " + hash2);
            }
        }

        // Check serialized output.
        final byte[] serializedAfterCalls = serialize(obj);
        final byte[] serializedAfterCallsAgain = serialize(obj);
        if(!Arrays.equals(serializedBeforeCalls, serializedBeforeCallsAgain)) {
            errors.add(
                "Serialized representation is different when serialized twice.\n  Possible incorrect cause serialization implementation.\n  Serialized bytes:\n    " + serializedBeforeCalls + "\n  vs\n    " + serializedAfterCalls);
        } else if(!Arrays.equals(serializedBeforeCalls, serializedAfterCalls)) {
            errors.add(
                "Serialized representation is different when serialized twice, with calls to equals and hashCode : between.\n  Possible incorrect cause serialization implementation, possibly by using a non-transient hashCode cache.\n  Serialized bytes:\n    " + serializedBeforeCalls + "\n  vs\n    " + serializedAfterCalls);
        } else if(!Arrays.equals(serializedAfterCalls, serializedAfterCallsAgain)) {
            errors.add(
                "Serialized representation is different when serialized twice, after calls to equals and hashcode.\n  Possible incorrect cause serialization implementation.\n  Serialized bytes:\n    " + serializedAfterCalls + "\n  vs\n    " + serializedAfterCallsAgain);
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
                    "Serialized representation is different after round-trip serialization.\n  Possible incorrect cause serialization implementation.\n  Serialized bytes:\n    " + serializedBeforeCalls + "\n  vs\n    " + serializedBeforeCallsTwice);
            } else if(!Arrays.equals(serializedAfterCalls, serializedAfterCallsTwice)) {
                errors.add(
                    "Serialized representation is different after round-trip serialization, with calls to equals and hashCode : between.\n  Possible incorrect cause serialization implementation, possibly by using a non-transient hashCode cache.\n  Serialized bytes:\n    " + serializedBeforeCalls + "\n  vs\n    " + serializedBeforeCallsTwice);
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

