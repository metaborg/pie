package mb.pie.api.stamp.output;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Common task output stampers.
 */
public class OutputStampers {
    public static EqualsOutputStamper equals() {
        return new EqualsOutputStamper();
    }

    public static FuncEqualsOutputStamper funcEquals(Function<@Nullable Serializable, @Nullable Serializable> func) {
        return new FuncEqualsOutputStamper(func);
    }

    public static InconsequentialOutputStamper inconsequential() {
        return InconsequentialOutputStamper.instance;
    }
}
