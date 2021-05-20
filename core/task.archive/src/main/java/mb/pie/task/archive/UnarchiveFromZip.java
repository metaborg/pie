package mb.pie.task.archive;

import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.path.string.PathStringMatcher;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;

public class UnarchiveFromZip implements TaskDef<UnarchiveFromZip.Input, ResourcePath> {
    public static class Input implements Serializable {
        private final ResourceKey inputZipFile;
        private final ResourcePath outputDirectory;
        private final PathStringMatcher matcher;
        private final @Nullable Supplier<?> originTask;

        public Input(ResourceKey inputZipFile, ResourcePath outputDirectory, PathStringMatcher matcher, @Nullable Supplier<?> originTask) {
            this.inputZipFile = inputZipFile;
            this.outputDirectory = outputDirectory;
            this.matcher = matcher;
            this.originTask = originTask;
        }

        public Input(ResourceKey inputZipFile, ResourcePath outputDirectory, @Nullable Supplier<?> originTask) {
            this(inputZipFile, outputDirectory, PathStringMatcher.ofTrue(), originTask);
        }

        public Input(ResourceKey inputZipFile, ResourcePath outputDirectory, PathStringMatcher matcher) {
            this(inputZipFile, outputDirectory, matcher, null);
        }

        public Input(ResourceKey inputZipFile, ResourcePath outputDirectory) {
            this(inputZipFile, outputDirectory, PathStringMatcher.ofTrue());
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(!inputZipFile.equals(input.inputZipFile)) return false;
            if(!outputDirectory.equals(input.outputDirectory)) return false;
            if(!matcher.equals(input.matcher)) return false;
            return originTask != null ? originTask.equals(input.originTask) : input.originTask == null;
        }

        @Override public int hashCode() {
            int result = inputZipFile.hashCode();
            result = 31 * result + outputDirectory.hashCode();
            result = 31 * result + matcher.hashCode();
            result = 31 * result + (originTask != null ? originTask.hashCode() : 0);
            return result;
        }

        @Override public String toString() {
            return "Input{" +
                "inputZipFile=" + inputZipFile +
                ", outputDirectory=" + outputDirectory +
                ", matcher=" + matcher +
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

        UnarchiveCommon.unarchiveZip(context, input.inputZipFile, input.outputDirectory, input.matcher);

        return input.outputDirectory;
    }
}
