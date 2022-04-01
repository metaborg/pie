package mb.pie.lang.test.call.foreignTask.nullary;

import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.inject.Inject;

public class Nullary implements TaskDef<@NonNull None, @NonNull String> {
    @Inject public Nullary() { }

    @NonNull
    @Override
    public String getId() {
        return getClass().getName();
    }

    @NonNull
    @Override
    public String exec(@NonNull ExecContext context, @NonNull None input) {
        return "Amazing static string";
    }
}
