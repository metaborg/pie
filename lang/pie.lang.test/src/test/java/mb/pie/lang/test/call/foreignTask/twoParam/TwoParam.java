package mb.pie.lang.test.call.foreignTask.twoParam;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.util.Tuple2;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.inject.Inject;

public class TwoParam implements TaskDef<TwoParam.@NonNull Input, @NonNull String> {
    public static class Input extends Tuple2<@NonNull Boolean, @NonNull String> {
        public Input(@NonNull Boolean bool, @NonNull String str) {
            super(bool, str);
        }
    }

    @Inject public TwoParam() { }

    @Override
    @NonNull
    public String getId() {
        return getClass().getName();
    }

    @Override
    public @NonNull String exec(@NonNull ExecContext context, @NonNull Input input) {
        return input.component2() + input.component1();
    }
}
