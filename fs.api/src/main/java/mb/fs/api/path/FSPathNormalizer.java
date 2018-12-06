package mb.fs.api.path;

import java.util.ArrayList;

public class FSPathNormalizer {
    public static ArrayList<String> normalize(Iterable<String> segments, int segmentsSize) throws FSPathNormalizationException {
        final ArrayList<String> newSegments = new ArrayList<>(segmentsSize);
        for(String segment : segments) {
            if(segment.equals("..")) {
                final int newSegmentsSize = newSegments.size();
                if(newSegmentsSize == 0) {
                    throw new FSPathNormalizationException();
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
