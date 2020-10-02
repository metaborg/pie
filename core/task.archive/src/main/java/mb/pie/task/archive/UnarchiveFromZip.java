package mb.pie.task.archive;

import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public class UnarchiveFromZip implements TaskDef<UnarchiveFromZip.Input, None> {
    public static class Input implements Serializable {
        private final ResourceKey inputZipFile;
        private final ResourcePath outputDirectory;
        private final @Nullable Supplier<?> originTask;

        public Input(ResourceKey inputZipFile, ResourcePath outputDirectory, @Nullable Supplier<?> originTask) {
            this.inputZipFile = inputZipFile;
            this.outputDirectory = outputDirectory;
            this.originTask = originTask;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return inputZipFile.equals(input.inputZipFile) &&
                outputDirectory.equals(input.outputDirectory) &&
                Objects.equals(originTask, input.originTask);
        }

        @Override public int hashCode() {
            return Objects.hash(inputZipFile, outputDirectory, originTask);
        }

        @Override public String toString() {
            return "Input{" +
                "inputZipFile=" + inputZipFile +
                ", outputDirectory=" + outputDirectory +
                ", originTask=" + originTask +
                '}';
        }
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws IOException {
        if(input.originTask != null) {
            context.require(input.originTask);
        }

        UnarchiveCommon.unarchiveZip(context, input.inputZipFile, input.outputDirectory);

        return None.instance;
    }
}
