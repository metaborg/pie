package mb.pie.lang.test.call.foreignTask.threeParam;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.util.Tuple3;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.inject.Inject;
import java.util.ArrayList;

public class ThreeParam implements TaskDef<ThreeParam.@NonNull Input, @NonNull Boolean> {
    public static class Input extends Tuple3<@NonNull ArrayList<@NonNull Integer>, @NonNull Integer, @NonNull Integer> {
        public Input(@NonNull ArrayList<@NonNull Integer> f1, @NonNull Integer f2, @NonNull Integer f3) {
            super(f1, f2, f3);
        }
    }

    @Inject public ThreeParam() { }

    @Override
    @NonNull
    public String getId() {
        return getClass().getName();
    }

    @Override
    public @NonNull Boolean exec(@NonNull ExecContext context, @NonNull Input input) {
        return input.component1().size() == input.component2()
            && input.component1().stream().mapToInt(x -> x).sum() == input.component3();
    }
}
