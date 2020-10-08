package mb.pie.task.archive;

import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class ArchiveToZip implements TaskDef<ArchiveToZip.Input, None> {
    public static class Input implements Serializable {
        private final ArrayList<ArchiveDirectory> archiveDirectories;
        private final ResourceKey outputZipFile;
        private final ArrayList<Supplier<?>> originTasks;

        public Input(
            ArrayList<ArchiveDirectory> archiveDirectories,
            ResourceKey outputZipFile,
            ArrayList<Supplier<?>> originTasks
        ) {
            this.archiveDirectories = archiveDirectories;
            this.outputZipFile = outputZipFile;
            this.originTasks = originTasks;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return archiveDirectories.equals(input.archiveDirectories) &&
                outputZipFile.equals(input.outputZipFile) &&
                originTasks.equals(input.originTasks);
        }

        @Override public int hashCode() {
            return Objects.hash(archiveDirectories, outputZipFile, originTasks);
        }

        @Override public String toString() {
            return "Input{" +
                "archiveDirectories=" + archiveDirectories +
                ", outputJarFile=" + outputZipFile +
                ", originTasks=" + originTasks +
                '}';
        }
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws IOException {
        for(final Supplier<?> originTask : input.originTasks) {
            context.require(originTask);
        }

        ArchiveCommon.archiveToZip(context, input.outputZipFile, input.archiveDirectories);

        return None.instance;
    }
}
