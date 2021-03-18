package mb.pie.task.archive;

import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.path.string.PathStringMatcher;
import mb.resource.hierarchical.match.path.string.TruePathStringMatcher;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;

public class UnarchiveFromJar implements TaskDef<UnarchiveFromJar.Input, ResourcePath> {
    public static class Input implements Serializable {
        private final ResourceKey inputJarFile;
        private final ResourcePath outputDirectory;
        private final PathStringMatcher matcher;
        private final boolean unarchiveManifest;
        private final boolean verifySignaturesIfSigned;
        private final @Nullable Supplier<?> originTask;

        public Input(ResourceKey inputJarFile, ResourcePath outputDirectory, PathStringMatcher matcher, boolean unarchiveManifest, boolean verifySignaturesIfSigned, @Nullable Supplier<?> originTask) {
            this.inputJarFile = inputJarFile;
            this.outputDirectory = outputDirectory;
            this.matcher = matcher;
            this.unarchiveManifest = unarchiveManifest;
            this.verifySignaturesIfSigned = verifySignaturesIfSigned;
            this.originTask = originTask;
        }

        public Input(ResourceKey inputJarFile, ResourcePath outputDirectory, PathStringMatcher matcher, boolean unarchiveManifest, boolean verifySignaturesIfSigned) {
            this(inputJarFile, outputDirectory, matcher, unarchiveManifest, verifySignaturesIfSigned, null);
        }

        public Input(ResourceKey inputJarFile, ResourcePath outputDirectory, PathStringMatcher matcher, boolean unarchiveManifest, @Nullable Supplier<?> originTask) {
            this(inputJarFile, outputDirectory, matcher, unarchiveManifest, true, originTask);
        }

        public Input(ResourceKey inputJarFile, ResourcePath outputDirectory, PathStringMatcher matcher, boolean unarchiveManifest) {
            this(inputJarFile, outputDirectory, matcher, unarchiveManifest, true, null);
        }

        public Input(ResourceKey inputJarFile, ResourcePath outputDirectory, PathStringMatcher matcher, @Nullable Supplier<?> originTask) {
            this(inputJarFile, outputDirectory, matcher, true, true, originTask);
        }

        public Input(ResourceKey inputJarFile, ResourcePath outputDirectory, PathStringMatcher matcher) {
            this(inputJarFile, outputDirectory, matcher, null);
        }

        public Input(ResourceKey inputJarFile, ResourcePath outputDirectory) {
            this(inputJarFile, outputDirectory, new TruePathStringMatcher());
        }


        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(unarchiveManifest != input.unarchiveManifest) return false;
            if(verifySignaturesIfSigned != input.verifySignaturesIfSigned) return false;
            if(!inputJarFile.equals(input.inputJarFile)) return false;
            if(!outputDirectory.equals(input.outputDirectory)) return false;
            if(!matcher.equals(input.matcher)) return false;
            return originTask != null ? originTask.equals(input.originTask) : input.originTask == null;
        }

        @Override public int hashCode() {
            int result = inputJarFile.hashCode();
            result = 31 * result + outputDirectory.hashCode();
            result = 31 * result + matcher.hashCode();
            result = 31 * result + (unarchiveManifest ? 1 : 0);
            result = 31 * result + (verifySignaturesIfSigned ? 1 : 0);
            result = 31 * result + (originTask != null ? originTask.hashCode() : 0);
            return result;
        }

        @Override public String toString() {
            return "Input{" +
                "inputJarFile=" + inputJarFile +
                ", outputDirectory=" + outputDirectory +
                ", matcher=" + matcher +
                ", unarchiveManifest=" + unarchiveManifest +
                ", verifySignaturesIfSigned=" + verifySignaturesIfSigned +
                ", originTask=" + originTask +
                '}';
        }
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public ResourcePath exec(ExecContext context, Input input) throws IOException {
        if(input.originTask != null) {
            context.require(input.originTask);
        }

        UnarchiveCommon.unarchiveJar(context, input.inputJarFile, input.outputDirectory, input.matcher, input.unarchiveManifest, input.verifySignaturesIfSigned);

        return input.outputDirectory;
    }
}
