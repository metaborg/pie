package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * {@link TaskDef} implementation using anonymous functions.
 */
public class LambdaTaskDef<I extends Serializable, @Nullable O extends Serializable> implements TaskDef<I, O> {
    private final String id;

    private final BiFunction<ExecContext, I, O> execFunc;
    private final @Nullable Function<I, Serializable> keyFunc;
    private final @Nullable BiFunction<I, Integer, String> descFunc;

    public LambdaTaskDef(String id, BiFunction<ExecContext, I, O> execFunc,
                         @Nullable Function<I, Serializable> keyFunc, @Nullable BiFunction<I, Integer, String> descFunc) {
        this.id = id;
        this.execFunc = execFunc;
        this.keyFunc = keyFunc;
        this.descFunc = descFunc;
    }

    public String getId() {
        return this.id;
    }

    public O exec(ExecContext context, I input) {
        return execFunc.apply(context, input);
    }

    public Serializable key(I input) {
        if(keyFunc != null) {
            return keyFunc.apply(input);
        } else {
            return TaskDef.super.key(input);
        }
    }

    public String desc(I input, int maxLength) {
        if(descFunc != null) {
            return descFunc.apply(input, maxLength);
        } else {
            return TaskDef.super.desc(input, maxLength);
        }
    }
}
