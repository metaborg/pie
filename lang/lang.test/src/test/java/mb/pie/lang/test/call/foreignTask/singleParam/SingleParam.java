package mb.pie.lang.test.call.foreignTask.singleParam;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.inject.Inject;

public class SingleParam implements TaskDef<@NonNull Integer, @NonNull Boolean> {
    @Inject public SingleParam() { }

    @Override
    @NonNull
    public String getId() {
        return getClass().getName();
    }

    @Override
    public @NonNull Boolean exec(@NonNull ExecContext context, @NonNull Integer input) {
        return input > 6;
    }
}
