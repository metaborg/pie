package mb.pie.util;

import mb.pie.api.None;
import mb.resource.fs.FSPath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public class Util {
    public static <T, R> Function<T, R> throwingLambda(ThrowingFunction<T, R> lambda) {
        return argument -> {
            try {
                return lambda.compute(argument);
            } catch (Exception e) {
                throw new WrapperException(e);
            }
        };
    }

    public static <T1, T2, T3> Tuple3<T1, T2, T3> tuple(T1 component1, T2 component2, T3 component3) {
        return new Tuple3<>(component1, component2, component3);
    }

    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> tuple(T1 component1, T2 component2, T3 component3, T4 component4) {
        return new Tuple4<>(component1, component2, component3, component4);
    }

    public static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> tuple(T1 component1, T2 component2, T3 component3, T4 component4, T5 component5) {
        return new Tuple5<>(component1, component2, component3, component4, component5);
    }

    public static None writeString(FSPath input, String s) throws IOException {
        Path path = input.getJavaPath();
        Files.createDirectories(path.getParent());
        Files.write(path, s.getBytes());
        return None.instance;
    }

    @Nullable
    public static String readToString(FSPath path) {
        try {
            return new String(Files.readAllBytes(path.getJavaPath()));
        } catch (IOException e) {
            return null;
        }
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R> {
        R compute(T argument) throws Exception;
    }

    public static class WrapperException extends Error {
        private final Exception wrappedException;

        public WrapperException(Exception wrappedException) {
            this.wrappedException = wrappedException;
        }

        public Exception getWrappedException() {
            return wrappedException;
        }
    }
}
