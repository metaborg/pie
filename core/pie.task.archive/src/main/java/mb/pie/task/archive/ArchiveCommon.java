package mb.pie.task.archive;

import mb.pie.api.ExecContext;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.pie.task.archive.Common.ExecContextProvider;
import mb.pie.task.archive.Common.ExecContextRequirer;
import mb.pie.task.archive.Common.NoopProvider;
import mb.pie.task.archive.Common.Provider;
import mb.pie.task.archive.Common.Requirer;
import mb.pie.task.archive.Common.ResourceServiceRequirer;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.WritableResource;
import mb.resource.hierarchical.HierarchicalResource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ArchiveCommon {
    public static WritableResource archiveToZip(
        ExecContext context,
        ResourceKey zipFile,
        Iterable<ArchiveDirectory> archiveDirectories
    ) throws IOException {
        return archiveToZip(context.getWritableResource(zipFile), archiveDirectories, new ExecContextRequirer(context), new ExecContextProvider(context));
    }

    public static WritableResource archiveToZip(
        ResourceService resourceService,
        WritableResource zipFile,
        Iterable<ArchiveDirectory> archiveDirectories
    ) throws IOException {
        return archiveToZip(zipFile, archiveDirectories, new ResourceServiceRequirer(resourceService), new NoopProvider());
    }

    private static WritableResource archiveToZip(
        WritableResource zipFile,
        Iterable<ArchiveDirectory> archiveDirectories,
        Requirer requirer,
        Provider provider
    ) throws IOException {
        return archive(
            zipFile,
            archiveDirectories,
            (resource) -> {
                try {
                    return new ZipOutputStream(resource.openWriteBuffered());
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            },
            (relativePath) -> false,
            ZipEntry::new,
            requirer,
            provider
        );
    }


    public static WritableResource archiveToJar(
        ExecContext context,
        ResourceKey jarFile,
        Iterable<ArchiveDirectory> archiveDirectories,
        Manifest manifest
    ) throws IOException {
        return archiveToJar(context.getWritableResource(jarFile), archiveDirectories, manifest, new ExecContextRequirer(context), new ExecContextProvider(context));
    }

    public static WritableResource archiveToJar(
        ResourceService resourceService,
        WritableResource jarFile,
        Iterable<ArchiveDirectory> archiveDirectories,
        Manifest manifest
    ) throws IOException {
        return archiveToJar(jarFile, archiveDirectories, manifest, new ResourceServiceRequirer(resourceService), new NoopProvider());
    }

    private static WritableResource archiveToJar(
        WritableResource jarFile,
        Iterable<ArchiveDirectory> archiveDirectories,
        Manifest manifest,
        Requirer requirer,
        Provider provider
    ) throws IOException {
        return archive(
            jarFile,
            archiveDirectories,
            (resource) -> {
                try {
                    return new JarOutputStream(resource.openWriteBuffered(), manifest);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            },
            (relativePath) -> {
                // Skip 'META-INF/MANIFEST.MF' files, since this is file added by passing the manifest into
                // JarOutputStream's constructor. Adding this file again here would create a duplicate file,
                // which results in an exception.
                return relativePath.endsWith("META-INF/MANIFEST.MF");
            },
            JarEntry::new,
            requirer,
            provider
        );
    }


    private static WritableResource archive(
        WritableResource archiveFile,
        Iterable<ArchiveDirectory> archiveDirectories,
        Function<WritableResource, ZipOutputStream> outputStreamFunction,
        Predicate<String> pathSkipPredicate,
        Function<String, ZipEntry> entryFunction,
        Requirer requirer,
        Provider provider
    ) throws IOException {
        try(final ZipOutputStream archiveOutputStream = outputStreamFunction.apply(archiveFile)) {
            for(ArchiveDirectory archiveDirectory : archiveDirectories) {
                final HierarchicalResource root = requirer.require(archiveDirectory.directory, ResourceStampers.modifiedDirRec(archiveDirectory.walker, archiveDirectory.matcher));
                try(final Stream<? extends HierarchicalResource> stream = root.walk(archiveDirectory.walker, archiveDirectory.matcher)) {
                    stream.forEach(resource -> {
                        // Files.walk returns absolute paths, so we need to relativize them.
                        final String relativePath = root.getPath().relativize(resource.getPath());
                        // Skip empty relative paths, indicating the root directory, as it would be stored as '/' in the
                        // Zip file, which is not allowed.
                        if(relativePath.isEmpty() || pathSkipPredicate.test(relativePath)) {
                            return;
                        }
                        try {
                            final boolean isDirectory = resource.isDirectory();
                            final String name = createZipName(relativePath, isDirectory);
                            final ZipEntry entry = entryFunction.apply(name);
                            entry.setTime(resource.getLastModifiedTime().toEpochMilli());
                            archiveOutputStream.putNextEntry(entry);
                            if(!isDirectory) {
                                try(final BufferedInputStream inputStream = resource.openReadBuffered()) {
                                    IoCommon.copy(inputStream, archiveOutputStream, new byte[1024 * 4]);
                                }
                            }
                            archiveOutputStream.closeEntry();
                        } catch(IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
                }
            }
            archiveOutputStream.flush();
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
        provider.provide(archiveFile);
        return archiveFile;
    }

    private static String createZipName(String relativePath, boolean isDirectory) {
        // Zip (and JAR) files are required to use '/' as a path separator.
        String name = relativePath.replace("\\", "/");
        if(isDirectory && !name.endsWith("/")) {
            // Zip (and JAR) files require directories to end with '/'.
            name = name + "/";
        }
        return name;
    }
}
