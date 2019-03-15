package mb.pie.api.stamp.output;

import mb.pie.api.stamp.OutputStamp;
import mb.pie.api.stamp.OutputStamper;
import org.checkerframework.checker.nullness.qual.Nullable;

public class InconsequentialStamp implements OutputStamp {
    public final static InconsequentialStamp instance = new InconsequentialStamp();


    @Override public OutputStamper getStamper() {
        return InconsequentialOutputStamper.instance;
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
        return "InconsequentialStamp()";
    }
}
