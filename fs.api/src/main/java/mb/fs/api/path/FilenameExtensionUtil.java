package mb.fs.api.path;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

public class FilenameExtensionUtil {
    public static @Nullable String extension(String filename) {
        final int i = filename.lastIndexOf('.');
        if(i > 0) {
            return filename.substring(i + 1);
        }
        return null;
    }

    public static String replaceExtension(String filename, String extension) {
        final int i = filename.lastIndexOf('.');
        if(i < 0) {
            return filename;
        }
        final String leafNoExtension = filename.substring(0, i);
        return leafNoExtension + "." + extension;
    }

    public static String appendExtension(String filename, String extension) {
        return filename + "." + extension;
    }

    public static String applyToExtension(String filename, Function<String, String> func) {
        final int i = filename.lastIndexOf('.');
        if(i < 0) {
            return filename;
        }
        final String extension = filename.substring(i + 1);
        final String newExtension = func.apply(extension);
        final String leafNoExtension = filename.substring(0, i);
        return leafNoExtension + "." + newExtension;
    }
}
