package mb.pie.serde.fst;

import mb.pie.api.serde.DeserializeRuntimeException;
import mb.pie.api.serde.Serde;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FstSerde implements Serde {
    private final FSTConfiguration configuration;


    public FstSerde(FSTConfiguration configuration) {
        this.configuration = configuration;
    }

    public FstSerde() {
        this(FSTConfiguration.createDefaultConfiguration());
    }


    @Override public <T> void serialize(T obj, OutputStream outputStream) {
        try(final FSTObjectOutput output = new FSTObjectOutput(outputStream, configuration)) {
            output.writeObject(obj, obj.getClass());
            output.flush();
        } catch(IOException e) {
            throw new DeserializeRuntimeException(e);
        }
    }

    @Override public <T> T deserialize(Class<T> type, InputStream inputStream) {
        try(final FSTObjectInput input = new FSTObjectInput(inputStream, configuration)) {
            @SuppressWarnings("unchecked") final T obj = (T)input.readObject(type);
            return obj;
        } catch(Exception e) {
            throw new DeserializeRuntimeException(e);
        }
    }


    @Override public <T> void serializeNullable(@Nullable T obj, Class<T> type, OutputStream outputStream) {
        try(final FSTObjectOutput output = new FSTObjectOutput(outputStream, configuration)) {
            if(obj != null) {
                output.writeObject(obj, obj.getClass());
            } else {
                output.writeObject(obj);
            }
            output.flush();
        } catch(IOException e) {
            throw new DeserializeRuntimeException(e);
        }
    }

    @Override public <T> @Nullable T deserializeNullable(Class<T> type, InputStream inputStream) {
        try(final FSTObjectInput input = new FSTObjectInput(inputStream, configuration)) {
            @SuppressWarnings("unchecked") final @Nullable T obj = (T)input.readObject(type);
            return obj;
        } catch(Exception e) {
            throw new DeserializeRuntimeException(e);
        }
    }


    @Override public void serializeTypeAndObject(@Nullable Object obj, OutputStream outputStream) {
        try(final FSTObjectOutput output = new FSTObjectOutput(outputStream, configuration)) {
            output.writeObject(obj);
            output.flush();
        } catch(IOException e) {
            throw new DeserializeRuntimeException(e);
        }
    }

    @Override
    public @Nullable Object deserializeTypeAndObject(@Nullable ClassLoader classLoader, InputStream inputStream) {
        try(final FSTObjectInput input = new FSTObjectInput(inputStream, configuration)) {
            return input.readObject();
        } catch(Exception e) {
            throw new DeserializeRuntimeException(e);
        }
    }
}
