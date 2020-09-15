package mb.pie.task.java;

import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.WritableResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.AnyResourceMatcher;
import mb.resource.hierarchical.match.DirectoryResourceMatcher;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.TrueResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionPathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.resource.hierarchical.walk.TrueResourceWalker;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;

public class CreateJar implements TaskDef<CreateJar.Input, ResourceKey> {
    public static class Input implements Serializable {
        private final @Nullable ResourceKey manifestFile;
        private final ArrayList<ArchiveDirectory> archiveDirectories;
        private final ResourceKey outputJarFile;
        private final ArrayList<Supplier<?>> originTasks;

        public Input(@Nullable ResourceKey manifestFile, ArrayList<ArchiveDirectory> archiveDirectories, ResourceKey outputJarFile, ArrayList<Supplier<?>> originTasks) {
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

    public static class ArchiveDirectory implements Serializable {
        private final ResourcePath directory;
        private final ResourceWalker walker;
        private final ResourceMatcher matcher;

        public ArchiveDirectory(ResourcePath directory, ResourceWalker walker, ResourceMatcher matcher) {
            this.directory = directory;
            this.matcher = matcher;
            this.walker = walker;
        }

        public static ArchiveDirectory ofDirectory(ResourcePath directory) {
            return new ArchiveDirectory(directory, new TrueResourceWalker(), new TrueResourceMatcher());
        }

        public static ArchiveDirectory ofClassFilesInDirectory(ResourcePath directory) {
            return new ArchiveDirectory(directory, new TrueResourceWalker(), new AnyResourceMatcher(new DirectoryResourceMatcher(), new PathResourceMatcher(new ExtensionPathMatcher("class"))));
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final ArchiveDirectory that = (ArchiveDirectory)o;
            return directory.equals(that.directory) &&
                walker.equals(that.walker) &&
                matcher.equals(that.matcher);
        }

        @Override public int hashCode() {
            return Objects.hash(directory, walker, matcher);
        }

        @Override public String toString() {
            return "ArchiveDirectory{" +
                "directory=" + directory +
                ", walker=" + walker +
                ", matcher=" + matcher +
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

        try {
            final WritableResource jarFile = context.getWritableResource(input.outputJarFile);
            try(final JarOutputStream jarOutputStream = new JarOutputStream(jarFile.openWriteBuffered(), manifest)) {
                for(ArchiveDirectory archiveDirectory : input.archiveDirectories) {
                    final HierarchicalResource root = context.require(archiveDirectory.directory, ResourceStampers.modifiedDirRec(archiveDirectory.walker, archiveDirectory.matcher));
                    try(final Stream<? extends HierarchicalResource> stream = root.walk(archiveDirectory.walker, archiveDirectory.matcher)) {
                        stream.forEach(resource -> {
                            // Files.walk returns absolute paths, so we need to relativize them.
                            final String relativePath = root.getPath().relativize(resource.getPath());
                            if(relativePath.endsWith("META-INF/MANIFEST.MF") || relativePath.isEmpty()) {
                                // Skip 'META-INF/MANIFEST.MF' files, since this is file added by passing the manifest into
                                // JarOutputStream's constructor. Adding this file again here would create a duplicate file,
                                // which results in an exception.
                                // Also skip empty relative paths, indicating the root directory, as it would be stored
                                // as '/' in the JAR (Zip) file, which is not allowed.
                                return;
                            }
                            try {
                                final boolean isDirectory = resource.isDirectory();
                                final String name = createJarName(relativePath, isDirectory);
                                final JarEntry entry = new JarEntry(name);
                                entry.setTime(resource.getLastModifiedTime().toEpochMilli());
                                jarOutputStream.putNextEntry(entry);
                                if(!isDirectory) {
                                    try(final BufferedInputStream inputStream = resource.openReadBuffered()) {
                                        copy(inputStream, jarOutputStream, new byte[1024 * 4]);
                                    }
                                }
                                jarOutputStream.closeEntry();
                            } catch(IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
                    }
                }
                jarOutputStream.flush();
            }
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }

        return input.outputJarFile;
    }

    private static String createJarName(String relativePath, boolean isDirectory) {
        // JAR (Zip) files are required to use '/' as a path separator.
        String name = relativePath.replace("\\", "/");
        if(isDirectory && !name.endsWith("/")) {
            // JAR (Zip) files require directories to end with '/'.
            name = name + "/";
        }
        return name;
    }

    private static void copy(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        int n;
        while(-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
    }
}
