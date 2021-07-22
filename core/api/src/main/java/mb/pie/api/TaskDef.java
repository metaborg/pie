package mb.pie.api;

import java.io.Serializable;
import java.util.Set;

/**
 * Definition of an incremental task which takes objects of type {@code I} and produces objects of type {@code O} when
 * executed.
 *
 * Inputs of type {@code I} must adhere to the following properties:
 * <ul>
 * <li>Must implement {@link Serializable}</li>
 * <li>Must have identity. That is, must implement {@link Object#equals(Object)} and {@link Object#hashCode()}</li>
 * <li>Must NOT be {@code null}</li>
 * <li>
 * Must be immutable. That is, once passed as an input to a task, it must not change in a way that its identity changes.
 * This means that transient fields may still change, but no guarantee is given about these fields.
 * </li>
 * <li>If the input is used as a key, must also adhere to the properties of {@link #key(Serializable)} below</li>
 * </ul>
 *
 * Furthermore, keys returned by the {@link #key(Serializable)} method, must adhere to the following properties:
 * <ul>
 * <li>Must implement {@link Serializable}</li>
 * <li>Have identity: must implement {@link Object#equals(Object)} and {@link Object#hashCode()}</li>
 * <li>Must NOT be {@code null}</li>
 * <li>
 * Must be immutable. That is, once passed as an input to a task, it must not change in a way that its identity changes.
 * This means that transient fields may still change, but no guarantee is given about these fields.
 * </li>
 * {@link Object#equals(Object)} and {@link Object#hashCode()} must return the same values after a serialization
 * roundtrip (e.g., serialize-deserialize)
 * </li>
 * <li>
 * The key's serialized bytes must be equal when the key's {@link Object#equals(Object)} method returns {@code true}
 * </li>
 * </ul>
 *
 * Finally, outputs of type {@code O} must adhere to the following properties:
 * <ul>
 * <li>Must implement {@link Serializable}</li>
 * <li>Must have identity. That is, must implement {@link Object#equals(Object)} and {@link Object#hashCode()}</li>
 * <li>
 * Must be immutable. That is, once passed as an input to a task, it must not change in a way that its identity changes.
 * This means that transient fields may still change, but no guarantee is given about these fields.
 * </li>
 * </ul>
 *
 * Failure to adhere to these properties will cause unsound incrementality.
 *
 * @param <I> Type of input objects. Must be {@link Serializable} and have identity, and may NOT be {@code null}.
 * @param <O> Type of output objects. Must be {@link Serializable} and have identity, and may be {@code null}.
 */
public interface TaskDef<I extends Serializable, O extends Serializable> {
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
     * @return Output object. May be {@code null}.
     * @throws Exception When execution of the task fails unexpectedly.
     */
    O exec(ExecContext context, I input) throws Exception;

    /**
     * Gets whether task with given {@code input} should be executed when it is affected (and thus scheduled) in a
     * bottom-up build, based on the {@code tags} that were activated. If false, the task will not be executed and will
     * be deferred until the next bottom-up build, where it will be scheduled again. Defaults to {@code true}.
     *
     * In top-down builds, or in bottom-up builds where the task is directly required, this method is not used, and
     * tasks will not be deferred.
     *
     * @param input Input object of the task.
     * @param tags  Tags that were activated in the bottom-up build.
     * @return True to execute the task, false to defer it.
     */
    default boolean shouldExecWhenAffected(I input, Set<?> tags) {
        return true;
    }


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
     * Creates a {@link Task task instance} for this task definition with given {@code input}.
     */
    default Task<O> createTask(I input) {
        return new Task<>(this, input);
    }

    /**
     * Creates an {@link Supplier incremental supplier} for this task definition with given {@code input}. An
     * incremental supplier is {@link Serializable} and as such can be used as an input or output of a task.
     */
    default STask<O> createSupplier(I input) {
        return new STask<>(this, input);
    }

    /**
     * Creates an {@link Function incremental function} for this task definition. An incremental function is {@link
     * Serializable} and as such can be used as an input or output of a task.
     */
    default Function<I, O> createFunction() {
        return new STaskDef<>(this);
    }
}
