package mb.pie.api.serde;

import org.checkerframework.checker.nullness.qual.NonNull;

public class SerializeRuntimeException extends RuntimeException {
    public SerializeRuntimeException(Throwable cause) {
        super(cause);
    }

    @Override public @NonNull Throwable getCause() {
        return super.getCause();
    }
}
