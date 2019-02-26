package mb.pie.api.stamp.output;

import mb.pie.api.stamp.OutputStamp;
import mb.pie.api.stamp.OutputStamper;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Output stamper that always produces the same the  stamp[inconsequential stamp][InconsequentialStamp], effectively ignoring the output.
 */
public class InconsequentialOutputStamper implements OutputStamper {
    public final static InconsequentialOutputStamper instance = new InconsequentialOutputStamper();


    @Override public OutputStamp stamp(@Nullable Serializable output) {
        return InconsequentialStamp.instance;
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        return true;
    }

    @Override public int hashCode() {
        return 0;
    }

    @Override public String toString() {
        return "InconsequentialOutputStamper()";
    }
}
