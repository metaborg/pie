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

public class UnarchiveFromJar implements TaskDef<UnarchiveFromJar.Input, None> {
    public static class Input implements Serializable {
        private final ResourceKey inputJarFile;
        private final ResourcePath outputDirectory;
        private final boolean unarchiveManifest;
        private final boolean verifySignaturesIfSigned;
        private final @Nullable Supplier<?> originTask;

        public Input(ResourceKey inputJarFile, ResourcePath outputDirectory, boolean unarchiveManifest, boolean verifySignaturesIfSigned, @Nullable Supplier<?> originTask) {
            this.inputJarFile = inputJarFile;
            this.outputDirectory = outputDirectory;
            this.unarchiveManifest = unarchiveManifest;
            this.verifySignaturesIfSigned = verifySignaturesIfSigned;
            this.originTask = originTask;
        }

        public Input(ResourceKey inputJarFile, ResourcePath outputDirectory, boolean unarchiveManifest, boolean verifySignaturesIfSigned) {
            this(inputJarFile, outputDirectory, unarchiveManifest, verifySignaturesIfSigned, null);
        }

        public Input(ResourceKey inputJarFile, ResourcePath outputDirectory, boolean unarchiveManifest, @Nullable Supplier<?> originTask) {
            this(inputJarFile, outputDirectory, unarchiveManifest, true, originTask);
        }

        public Input(ResourceKey inputJarFile, ResourcePath outputDirectory, boolean unarchiveManifest) {
            this(inputJarFile, outputDirectory, unarchiveManifest, true, null);
        }

        public Input(ResourceKey inputJarFile, ResourcePath outputDirectory, @Nullable Supplier<?> originTask) {
            this(inputJarFile, outputDirectory, true, true, originTask);
        }

        public Input(ResourceKey inputJarFile, ResourcePath outputDirectory) {
            this(inputJarFile, outputDirectory, true, true, null);
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return unarchiveManifest == input.unarchiveManifest &&
                verifySignaturesIfSigned == input.verifySignaturesIfSigned &&
                inputJarFile.equals(input.inputJarFile) &&
                outputDirectory.equals(input.outputDirectory) &&
                Objects.equals(originTask, input.originTask);
        }

        @Override public int hashCode() {
            return Objects.hash(inputJarFile, outputDirectory, unarchiveManifest, verifySignaturesIfSigned, originTask);
        }

        @Override public String toString() {
            return "Input{" +
                "inputJarFile=" + inputJarFile +
                ", outputDirectory=" + outputDirectory +
                ", unarchiveManifest=" + unarchiveManifest +
                ", verifySignaturesIfSigned=" + verifySignaturesIfSigned +
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

        UnarchiveCommon.unarchiveJar(context, input.inputJarFile, input.outputDirectory, input.unarchiveManifest, input.verifySignaturesIfSigned);

        return None.instance;
    }
}
