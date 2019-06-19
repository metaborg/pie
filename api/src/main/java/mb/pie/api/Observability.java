package mb.pie.api;

import java.io.Serializable;

public enum Observability implements Serializable {
    Observed,
    RootObserved,
    Detached;

    public boolean isObservable() {
        return this == RootObserved || this == Observed;
    }

    public boolean isUnobservable() {
        return this == Detached;
    }
}
