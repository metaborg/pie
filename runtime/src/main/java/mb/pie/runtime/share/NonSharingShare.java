package mb.pie.runtime.share;

import mb.pie.api.Share;
import mb.pie.api.TaskData;
import mb.pie.api.TaskKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Supplier;

public class NonSharingShare implements Share {
    @Override
    public TaskData<?, ?> share(TaskKey key, Supplier<TaskData<?, ?>> execFunc, @Nullable Supplier<TaskData<?, ?>> visitedFunc) {
        final @Nullable TaskData<?, ?> taskData;
        if(visitedFunc != null) {
            taskData = visitedFunc.get();
        } else {
            taskData = null;
        }

        if(taskData != null) {
            return taskData;
        } else {
            return execFunc.get();
        }
    }

    @Override public String toString() {
        return "NonSharingShare()";
    }
}
