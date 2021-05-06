package mb.pie.task.archive;

import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ArchiveToJar implements TaskDef<ArchiveToJar.Input, ResourceKey> {
    public static class Input implements Serializable {
        private final @Nullable ResourceKey manifestFile;
        private final ArrayList<ArchiveDirectory> archiveDirectories;
        private final ResourceKey outputJarFile;
        private final ArrayList<Supplier<?>> originTasks;

        public Input(
            @Nullable ResourceKey manifestFile,
            ArrayList<ArchiveDirectory> archiveDirectories,
            ResourceKey outputJarFile,
            ArrayList<Supplier<?>> originTasks
        ) {
            this.manifestFile = manifestFile;
            this.archiveDirectories = archiveDirectories;
            this.outputJarFile = outputJarFile;
            this.originTasks = originTasks;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return Objects.equals(manifestFile, input.manifestFile) &&
                archiveDirectories.equals(input.archiveDirectories) &&
                outputJarFile.equals(input.outputJarFile) &&
                originTasks.equals(input.originTasks);
        }

        @Override public int hashCode() {
            return Objects.hash(manifestFile, archiveDirectories, outputJarFile, originTasks);
        }

        @Override public String toString() {
            return "Input{" +
                "manifestFile=" + manifestFile +
                ", archiveDirectories=" + archiveDirectories +
                ", outputJarFile=" + outputJarFile +
                ", originTasks=" + originTasks +
                '}';
        }
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public ResourceKey exec(ExecContext context, Input input) throws IOException {
        for(final Supplier<?> originTask : input.originTasks) {
            context.require(originTask);
        }

        final Manifest manifest;
        if(input.manifestFile != null) {
            try(final BufferedInputStream inputStream = context.require(input.manifestFile).openReadBuffered()) {
                manifest = new Manifest(inputStream);
            }
        } else {
            manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        }

        ArchiveCommon.archiveToJar(context, input.outputJarFile, input.archiveDirectories, manifest);

        return input.outputJarFile;
    }
}
