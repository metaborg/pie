package mb.pie.api.serde;

import org.checkerframework.checker.nullness.qual.NonNull;

public class DeserializeRuntimeException extends RuntimeException {
    public DeserializeRuntimeException(Throwable cause) {
        super(cause);
    }

    @Override public @NonNull Throwable getCause() {
        return super.getCause();
    }
}
