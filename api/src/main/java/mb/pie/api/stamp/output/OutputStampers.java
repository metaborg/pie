package mb.pie.api.stamp.output;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Common task output stampers.
 */
public class OutputStampers {
    EqualsOutputStamper getEquals() {
        return new EqualsOutputStamper();
    }

    FuncEqualsOutputStamper getFuncEquals(Function<@Nullable Serializable, @Nullable Serializable> func) {
        return new FuncEqualsOutputStamper(func);
    }

    InconsequentialOutputStamper getInconsequential() {
        return InconsequentialOutputStamper.instance;
    }
}
