package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Definition of an executable task.
 * <p>
 * Inputs of type I must adhere to the following properties:
 * * Implement [Serializable].
 * * Implement [equals][Object.equals] and [hashCode][Object.hashCode].
 * * Must NOT be `null`.
 * * If the input is used as a key, it must also adhere to key's properties.
 * <p>
 * Furthermore, keys returned by the {@link #key(Serializable)} method, must adhere to the following properties:
 * * Implement [Serializable].
 * * Implement [equals][Object.equals] and [hashCode][Object.hashCode].
 * * Must NOT be `null`.
 * * [Equals][Object.equals] and [hashCode][Object.hashCode] must return the same values after a serialization roundtrip (e.g., serialize-deserialize).
 * * The key's serialized bytes must be equal when the key's [equals][Object.equals] method returns true.
 * <p>
 * Finally, outputs of type O must adhere to the following properties:
 * * Implement [Serializable].
 * * Implement [equals][Object.equals] and [hashCode][Object.hashCode].
 * <p>
 * Failure to adhere to these properties will cause unsound incrementality.
 */
public interface TaskDef<I extends Serializable, O extends @Nullable Serializable> {
    /**
     * Unique identifier of the task definition.
     */
    String getId();

    /**
     * Executes the task with given input, and returns its output.
     *
     * @throws Exception            when execution of the task fails unexpectedly.
     * @throws InterruptedException when execution of the task is cancelled or otherwise interrupted.
     */
    O exec(ExecContext context, I input) throws Exception;


    /**
     * Returns a key that uniquely identifies the task for given input. Defaults to entire input.
     */
    default Serializable key(I input) {
        return input;
    }

    /**
     * Returns the description of task for given [input], with given [maximum length][maxLength]. Defaults to ID(input).
     */
    default String desc(I input, int maxLength) {
        return this.getId() + '(' + StringUtil.toShortString(input.toString(), maxLength) + ')';
    }


    /**
     * Creates a task instance with given [input] for this task definition.
     */
    default Task<O> createTask(I input) {
        return new Task<>(this, input);
    }

    /**
     * Creates a serializable task instance with given [input] for this task definition.
     */
    default STask createSerializableTask(I input) {
        return new STask(this.getId(), input);
    }
}
