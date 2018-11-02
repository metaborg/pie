package mb.fs.api.path;

import java.util.ArrayList;
import java.util.Collection;

public class FSPathNormalizer {
    public static ArrayList<String> normalize(Collection<String> segments) throws FSPathNormalizationException {
        final ArrayList<String> newSegments = new ArrayList<>(segments.size());
        for(String segment : segments) {
            if(segment.equals("..")) {
                final int newSegmentsSize = newSegments.size();
                if(newSegmentsSize == 0) {
                    throw new FSPathNormalizationException(segments);
                } else {
                    newSegments.remove(newSegmentsSize - 1);
                }
            } else if(!segment.equals(".")) {
                newSegments.add(segment);
            }
        }
        return newSegments;
    }
}
