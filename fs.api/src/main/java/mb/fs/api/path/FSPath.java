package mb.fs.api.path;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

public class FSPath implements Serializable {
    private static final long serialVersionUID = 1L;

    final String selectorRoot;
    final List<String> selectors;
    final List<String> segments;


    public static FSPath fromSelector(String selectorRoot) {
        return new FSPath(selectorRoot);
    }


    public static FSPath fromSelectors(String selectorRoot, List<String> selectors) {
        return new FSPath(selectorRoot, selectors);
    }

    public static FSPath fromSelectors(String selectorRoot, Collection<String> selectors) {
        return new FSPath(selectorRoot, new ArrayList<>(selectors));
    }

    public static FSPath fromSelectors(String selectorRoot, String... selectors) {
        return new FSPath(selectorRoot, Arrays.asList(selectors));
    }


    public static FSPath from(String selectorRoot, List<String> segments) {
        return new FSPath(selectorRoot, new ArrayList<>(), segments);
    }

    public static FSPath from(String selectorRoot, Collection<String> segments) {
        return new FSPath(selectorRoot, new ArrayList<>(), new ArrayList<>(segments));
    }

    public static FSPath from(String selectorRoot, String... segments) {
        return new FSPath(selectorRoot, new ArrayList<>(), Arrays.asList(segments));
    }


    public static FSPath from(String selectorRoot, List<String> selectors, List<String> segments) {
        return new FSPath(selectorRoot, selectors, segments);
    }

    public static FSPath from(String selectorRoot, List<String> selectors, Collection<String> segments) {
        return new FSPath(selectorRoot, selectors, new ArrayList<>(segments));
    }

    public static FSPath from(String selectorRoot, List<String> selectors, String... segments) {
        return new FSPath(selectorRoot, selectors, Arrays.asList(segments));
    }


    public static FSPath from(String selectorRoot, Collection<String> selectors, List<String> segments) {
        return new FSPath(selectorRoot, new ArrayList<>(selectors), segments);
    }

    public static FSPath from(String selectorRoot, Collection<String> selectors, Collection<String> segments) {
        return new FSPath(selectorRoot, new ArrayList<>(selectors), new ArrayList<>(segments));
    }

    public static FSPath from(String selectorRoot, Collection<String> selectors, String... segments) {
        return new FSPath(selectorRoot, new ArrayList<>(selectors), Arrays.asList(segments));
    }


    public static FSPath from(String selectorRoot, String[] selectors, List<String> segments) {
        return new FSPath(selectorRoot, Arrays.asList(selectors), segments);
    }

    public static FSPath from(String selectorRoot, String[] selectors, Collection<String> segments) {
        return new FSPath(selectorRoot, Arrays.asList(selectors), new ArrayList<>(segments));
    }

    public static FSPath from(String selectorRoot, String[] selectors, String... segments) {
        return new FSPath(selectorRoot, Arrays.asList(selectors), Arrays.asList(segments));
    }


    FSPath(String selectorRoot, List<String> selectors, List<String> segments) {
        this.selectorRoot = selectorRoot;
        this.selectors = selectors;
        this.segments = segments;
    }

    FSPath(String selectorRoot, List<String> selectors) {
        this.selectorRoot = selectorRoot;
        this.selectors = selectors;
        this.segments = new ArrayList<>();
    }

    FSPath(String selectorRoot) {
        this.selectorRoot = selectorRoot;
        this.selectors = new ArrayList<>();
        this.segments = new ArrayList<>();
    }


    public String getSelectorRoot() {
        return selectorRoot;
    }

    public List<String> getSelectors() {
        return Collections.unmodifiableList(selectors);
    }

    public @Nullable String getSelectorLeaf() {
        final int selectorsSize = selectors.size();
        if(selectorsSize > 0) {
            return selectors.get(selectorsSize - 1);
        }
        return null;
    }

    public @Nullable FSPath getSelectorParent() {
        final int selectorsSize = selectors.size();
        if(selectorsSize > 1) {
            return new FSPath(selectorRoot, selectors.subList(0, selectorsSize - 1), this.segments);
        }
        return null;
    }


    public List<String> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    public @Nullable String getSegmentLeaf() {
        final int segmentsSize = segments.size();
        if(segmentsSize > 0) {
            return segments.get(segmentsSize - 1);
        }
        return null;
    }

    public @Nullable FSPath getSegmentParent() {
        final int segmentsSize = segments.size();
        if(segmentsSize > 0) {
            return new FSPath(selectorRoot, selectors, segments.subList(0, segmentsSize - 1));
        }
        return null;
    }


    public @Nullable FSPath getParent() {
        final int segmentsSize = segments.size();
        if(segmentsSize > 0) {
            return new FSPath(selectorRoot, selectors, segments.subList(0, segmentsSize - 1));
        }
        final int selectorsSize = selectors.size();
        if(selectorsSize > 0) {
            return new FSPath(selectorRoot, selectors.subList(0, selectorsSize - 1));
        }
        return null;
    }

    public FSPath getNormalized() throws FSPathNormalizationException {
        final ArrayList<String> newSegments = FSPathNormalizer.normalize(this.segments);
        return new FSPath(selectorRoot, this.selectors, newSegments);
    }

    public @Nullable RelativeFSPath getRelativeTo(FSPath other) {
        if(!this.selectors.equals(other.selectors)) {
            return null;
        }
        final int segmentsSize = this.segments.size();
        if(segmentsSize > other.segments.size()) {
            return null;
        }
        for(int i = 0; i < segmentsSize; i++) {
            if(!this.segments.get(i).equals(other.segments.get(i))) {
                return null;
            }
        }
        return new RelativeFSPath(other.segments.subList(segmentsSize, other.segments.size()));
    }

    public FSPath getRoot() {
        return new FSPath(selectorRoot, this.selectors);
    }


    public FSPath appendSelector(String selector) {
        final ArrayList<String> newSelectors = new ArrayList<>(this.selectors.size() + 1);
        newSelectors.addAll(this.selectors);
        newSelectors.add(selector);
        return new FSPath(selectorRoot, newSelectors, this.segments);
    }

    public FSPath appendSelectors(Collection<String> selectors) {
        final ArrayList<String> newSelectors = new ArrayList<>(this.selectors.size() + selectors.size());
        newSelectors.addAll(this.selectors);
        newSelectors.addAll(selectors);
        return new FSPath(selectorRoot, newSelectors, this.segments);
    }

    public FSPath appendSelectors(String... selectors) {
        final ArrayList<String> newSelectors = new ArrayList<>(this.selectors.size() + selectors.length);
        newSelectors.addAll(this.selectors);
        Collections.addAll(newSelectors, selectors);
        return new FSPath(selectorRoot, newSelectors, this.segments);
    }


    public FSPath appendSegment(String segment) {
        final ArrayList<String> newSegments = new ArrayList<>(this.segments.size() + 1);
        newSegments.addAll(this.segments);
        newSegments.add(segment);
        return new FSPath(selectorRoot, this.selectors, newSegments);
    }

    public FSPath appendSegments(Collection<String> segments) {
        final ArrayList<String> newSegments = new ArrayList<>(this.segments.size() + segments.size());
        newSegments.addAll(this.segments);
        newSegments.addAll(segments);
        return new FSPath(selectorRoot, this.selectors, newSegments);
    }

    public FSPath appendSegments(Iterable<String> segments) {
        final ArrayList<String> newSegments = new ArrayList<>(this.segments.size());
        newSegments.addAll(this.segments);
        segments.forEach(newSegments::add);
        return new FSPath(selectorRoot, this.selectors, newSegments);
    }

    public FSPath appendSegments(String... segments) {
        final ArrayList<String> newSegments = new ArrayList<>(this.segments.size() + segments.length);
        newSegments.addAll(this.segments);
        Collections.addAll(newSegments, segments);
        return new FSPath(selectorRoot, this.selectors, newSegments);
    }

    public FSPath appendRelativePath(RelativeFSPath relativePath) {
        final ArrayList<String> newSegments = new ArrayList<>(this.segments.size() + relativePath.segments.size());
        newSegments.addAll(this.segments);
        newSegments.addAll(relativePath.segments);
        return new FSPath(selectorRoot, this.selectors, newSegments);
    }


    public FSPath appendToLeafSegment(String str) {
        final ArrayList<String> newSegments = new ArrayList<>(this.segments);
        final int leafIndex = newSegments.size() - 1;
        String leafSegment = newSegments.get(leafIndex);
        leafSegment = leafSegment + str;
        newSegments.set(leafIndex, leafSegment);
        return new FSPath(selectorRoot, this.selectors, newSegments);
    }

    public FSPath replaceLeafSegment(String segment) {
        final ArrayList<String> newSegments = new ArrayList<>(this.segments);
        newSegments.set(newSegments.size() - 1, segment);
        return new FSPath(selectorRoot, this.selectors, newSegments);
    }

    public FSPath applyToLeafSegment(Function<String, String> func) {
        final ArrayList<String> newSegments = new ArrayList<>(this.segments);
        final int leafIndex = newSegments.size() - 1;
        final String leafSegment = newSegments.get(leafIndex);
        newSegments.set(newSegments.size() - 1, func.apply(leafSegment));
        return new FSPath(selectorRoot, this.selectors, newSegments);
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final FSPath path = (FSPath) o;
        if(!selectorRoot.equals(path.selectorRoot)) return false;
        if(!selectors.equals(path.selectors)) return false;
        return segments.equals(path.segments);
    }

    @Override public int hashCode() {
        int result = selectorRoot.hashCode();
        result = 31 * result + selectors.hashCode();
        result = 31 * result + segments.hashCode();
        return result;
    }

    @Override public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(selectorRoot);
        stringBuilder.append(':');
        for(String selector : selectors) {
            stringBuilder.append(selector);
            stringBuilder.append(':');
        }
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
