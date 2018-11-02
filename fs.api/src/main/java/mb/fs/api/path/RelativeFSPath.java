package mb.fs.api.path;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

public class RelativeFSPath implements Serializable {
    private static final long serialVersionUID = 1L;

    final ArrayList<String> segments;


    RelativeFSPath(ArrayList<String> segments) {
        this.segments = segments;
    }

    public RelativeFSPath(Collection<String> segments) {
        this.segments = new ArrayList<>(segments);
    }

    public RelativeFSPath(String... segments) {
        this.segments = (ArrayList<String>) Arrays.asList(segments);
    }


    public List<String> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    public @Nullable String getLeafSegment() {
        final int segmentsSize = segments.size();
        if(segmentsSize > 0) {
            return segments.get(segmentsSize - 1);
        }
        return null;
    }


    public @Nullable RelativeFSPath getParent() {
        final int segmentsSize = segments.size();
        if(segmentsSize > 0) {
            return new RelativeFSPath(segments.subList(0, segmentsSize - 1));
        }
        return null;
    }

    public RelativeFSPath getNormalized() throws FSPathNormalizationException {
        final ArrayList<String> newSegments = FSPathNormalizer.normalize(this.segments);
        return new RelativeFSPath(newSegments);
    }


    public RelativeFSPath appendSegment(String segment) {
        final ArrayList<String> newSegments = new ArrayList<>(this.segments.size() + 1);
        newSegments.addAll(this.segments);
        newSegments.add(segment);
        return new RelativeFSPath(newSegments);
    }

    public RelativeFSPath appendSegments(Collection<String> segments) {
        final ArrayList<String> newSegments = new ArrayList<>(this.segments.size() + segments.size());
        newSegments.addAll(this.segments);
        newSegments.addAll(segments);
        return new RelativeFSPath(newSegments);
    }

    public RelativeFSPath appendSegments(String... segments) {
        final ArrayList<String> newSegments = new ArrayList<>(this.segments.size() + segments.length);
        newSegments.addAll(this.segments);
        Collections.addAll(newSegments, segments);
        return new RelativeFSPath(newSegments);
    }

    public RelativeFSPath appendRelativePath(RelativeFSPath relativePath) {
        final ArrayList<String> newSegments = new ArrayList<>(this.segments.size() + relativePath.segments.size());
        newSegments.addAll(this.segments);
        newSegments.addAll(relativePath.segments);
        return new RelativeFSPath(newSegments);
    }


    public RelativeFSPath appendToLeafSegment(String str) {
        final ArrayList<String> newSegments = new ArrayList<>(this.segments);
        final int leafIndex = newSegments.size() - 1;
        String leafSegment = newSegments.get(leafIndex);
        leafSegment = leafSegment + str;
        newSegments.set(leafIndex, leafSegment);
        return new RelativeFSPath(newSegments);
    }

    public RelativeFSPath replaceLeafSegment(String segment) {
        final ArrayList<String> newSegments = new ArrayList<>(this.segments);
        newSegments.set(newSegments.size() - 1, segment);
        return new RelativeFSPath(newSegments);
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final RelativeFSPath relativePath = (RelativeFSPath) o;
        return segments.equals(relativePath.segments);
    }

    @Override public int hashCode() {
        return segments.hashCode();
    }

    @Override public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        boolean first = true;
        for(String segment : segments) {
            if(!first) {
                stringBuilder.append('/');
            } else {
                first = false;
            }
            stringBuilder.append(segment);
        }
        return stringBuilder.toString();
    }
}
