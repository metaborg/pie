package mb.pie.lang.test.call.foreignTask.generic;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.util.Tuple3;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;

public class Generic<A extends Integer, B extends ArrayList<A>> implements TaskDef<Generic.@NonNull Input<A, B>, @NonNull Integer> {
    public static class Input<A extends Serializable, B extends Serializable> extends Tuple3<@NonNull ArrayList<@NonNull A>, @NonNull B, @NonNull A> {
        public Input(@NonNull ArrayList<@NonNull A> f1, @NonNull B f2, @NonNull A f3) {
            super(f1, f2, f3);
        }
    }

    @Inject public Generic() { }

    @Override
    @NonNull
    public String getId() {
        return getClass().getName();
    }

    @Override
    public @NonNull Integer exec(@NonNull ExecContext context, @NonNull Input<A, B> input) {
        return input.component1().stream().mapToInt(x -> x).sum() + input.component2().size() + input.component3().intValue();
    }
}
