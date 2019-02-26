package mb.pie.api.stamp.output;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Common task output stampers.
 */
public class OutputStampers {
    public static EqualsOutputStamper getEquals() {
        return new EqualsOutputStamper();
    }

    public static FuncEqualsOutputStamper getFuncEquals(Function<@Nullable Serializable, @Nullable Serializable> func) {
        return new FuncEqualsOutputStamper(func);
    }

    public static InconsequentialOutputStamper getInconsequential() {
        return InconsequentialOutputStamper.instance;
    }
}
