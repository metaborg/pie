package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Definition of an executable task.
 * <p>
 * Inputs of type I must adhere to the following properties:
 * <ul>
 * <li>Implement {@link Serializable}</li>
 * <li>Implement {@link Object#equals(Object)} and {@link Object#hashCode()}</li>
 * <li>Must NOT be {@code null}</li>
 * <li>If the input is used as a key, it must also adhere to the properties of {@link #key(Serializable)} below</li>
 * </ul>
 * <p>
 * Furthermore, keys returned by the {@link #key(Serializable)} method, must adhere to the following properties:
 * <ul>
 * <li>Implement {@link Serializable}</li>
 * <li>Implement {@link Object#equals(Object)} and {@link Object#hashCode()}</li>
 * <li>Must NOT be {@code null}</li>
 * <li>
 * {@link Object#equals(Object)} and {@link Object#hashCode()} must return the same values after a serialization
 * roundtrip (e.g., serialize-deserialize)
 * </li>
 * <li>
 * The key's serialized bytes must be equal when the key's {@link Object#equals(Object)} method returns {@code true}
 * </li>
 * <p>
 * Finally, outputs of type {@code O} must adhere to the following properties:
 * <ul>
 * <li>Implement {@link Serializable}</li>
 * <li>Implement {@link Object#equals(Object)} and {@link Object#hashCode()}</li>
 * <p>
 * Failure to adhere to these properties will cause unsound incrementality.
 */
public interface TaskDef<I extends Serializable, O extends @Nullable Serializable> {
    /**
     * Gets the unique identifier of this task definition.
     *
     * @return Unique identifier of this task definition.
     */
    String getId();

    /**
     * Executes the task with given input, and returns its output.
     *
     * @param context Execution context for requiring tasks, and requiring/providing resources.
     * @param input   Input object.
     * @return Output object.
     * @throws Exception            When execution of the task fails unexpectedly.
     * @throws InterruptedException When execution of the task is cancelled or otherwise interrupted.
     */
    O exec(ExecContext context, I input) throws Exception;


    /**
     * Returns a key that uniquely identifies a task for given input. Defaults to entire input.
     *
     * @param input Input object.
     * @return Key for given input.
     */
    default Serializable key(I input) {
        return input;
    }

    /**
     * Returns the description of task for given {@code input}, with given {@code maxLength}. Defaults to {@code
     * "ID(input)"}.
     */
    default String desc(I input, int maxLength) {
        return this.getId() + '(' + StringUtil.toShortString(input.toString(), maxLength) + ')';
    }


    /**
     * Creates a task instance for this task definition with given {@code input}.
     */
    default Task<O> createTask(I input) {
        return new Task<>(this, input);
    }

    /**
     * Creates a serializable task instance for this task definition with given {@code input}.
     */
    default STask createSerializableTask(I input) {
        return new STask(this.getId(), input);
    }
}
