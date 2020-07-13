package mb.pie.api.stamp.output;

import mb.pie.api.stamp.OutputStamp;
import mb.pie.api.stamp.OutputStamper;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Output stamper that copies outputs into a stamp, and compares these stamps by equality.
 */
public class EqualsOutputStamper implements OutputStamper {
    @Override public OutputStamp stamp(@Nullable Serializable output) {
        return new ValueOutputStamp<>(output, this);
    }

    @Override public boolean equals(@Nullable Object o) {
        return this == o || o != null && this.getClass() == o.getClass();
    }

    @Override public int hashCode() {
        return 0;
    }

    @Override public String toString() {
        return "EqualsOutputStamper()";
    }
}
