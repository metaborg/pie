package mb.pie.api.stamp.output;

import mb.pie.api.OutTransientEquatable;
import mb.pie.api.stamp.OutputStamp;
import mb.pie.api.stamp.OutputStamper;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class ValueOutputStamp<V extends @Nullable Serializable> implements OutputStamp {
    private final V value;
    private final OutputStamper stamper;


    public ValueOutputStamp(V value, OutputStamper stamper) {
        this.value = value;
        this.stamper = stamper;
    }


    @Override public OutputStamper getStamper() {
        return stamper;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ValueOutputStamp<?> that = (ValueOutputStamp<?>)o;
        if(value instanceof OutTransientEquatable<?, ?> && that.value instanceof OutTransientEquatable<?, ?>) {
            // TODO: should OutTransientEquatable not have a special equality implementation that compares the equatable value?
            final @Nullable Serializable valueEq = ((OutTransientEquatable<?, ?>)value).getEquatableValue();
            final @Nullable Serializable thatEq = ((OutTransientEquatable<?, ?>)that.value).getEquatableValue();
            if(!Objects.equals(valueEq, thatEq)) {
                return false;
            }
        } else if(!Objects.equals(value, that.value)) {
            return false;
        }
        return stamper.equals(that.stamper);
    }

    @Override public int hashCode() {
        int result;
        //noinspection ConstantConditions
        if(value == null) {
            result = 0;
        } else if(value instanceof OutTransientEquatable<?, ?>) {
            // TODO: should OutTransientEquatable not have a special hashCode implementation that hashes the equatable @Nullable value?
            final @Nullable Serializable valueEq = ((OutTransientEquatable<?, ?>)value).getEquatableValue();
            //noinspection ConstantConditions
            result = valueEq != null ? valueEq.hashCode() : 0;
        } else {
            result = value.hashCode();
        }
        result = 31 * result + stamper.hashCode();
        return result;
    }

    @Override public String toString() {
        return "ValueOutputStamp(value=" + value + ", stamper=" + stamper + ')';
    }
}
