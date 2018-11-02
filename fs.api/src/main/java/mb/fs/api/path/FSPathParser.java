package mb.fs.api.path;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FSPathParser {
    private final static Pattern absPattern = Pattern.compile("^([^/^:]+?):([^/]+?:)?/?([^:]*)");
    private final static Pattern relPattern = Pattern.compile("^([^/][^:]*)");


    public static @Nullable FSPath parseAbsolute(String pathStr) {
        final Matcher matcher = absPattern.matcher(pathStr);
        if(!matcher.matches()) {
            return null;
        }
        final String selectorRoot = matcher.group(1);
        final String selectorStr = matcher.group(2);
        final String[] selectors = selectorStr.split(":");
        final String segmentsStr = matcher.group(3);
        final String[] segments = segmentsStr.split("/");
        return FSPath.from(selectorRoot, selectors, segments);
    }

    public static @Nullable RelativeFSPath parseRelative(String pathStr) {
        final Matcher matcher = relPattern.matcher(pathStr);
        if(!matcher.matches()) {
            return null;
        }
        final String segmentsStr = matcher.group(1);
        final String[] segments = segmentsStr.split("/");
        return new RelativeFSPath(segments);
    }
}
