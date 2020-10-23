package mb.pie.task.archive;

import mb.pie.api.ExecContext;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnarchiveCommon {
    public static void unarchiveZip(
        ExecContext context,
        ResourceKey zipFile,
        ResourcePath outputDirectory
    ) throws IOException {
        unarchive(
            context,
            zipFile,
            outputDirectory,
            (resource) -> {
                try {
                    return new ZipInputStream(resource.openReadBuffered());
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        );
    }

    public static @Nullable Manifest unarchiveJar(
        ExecContext context,
        ResourceKey jarFile,
        ResourcePath outputDirectory,
        boolean unarchiveManifest,
        boolean verifySignaturesIfSigned
    ) throws IOException {
        // Use an atomic reference to allow writing in closure.
        final AtomicReference<@Nullable Manifest> manifestRef = new AtomicReference<>();
        unarchive(
            context,
            jarFile,
            outputDirectory,
            (resource) -> {
                try {
                    final JarInputStream jarInputStream = new JarInputStream(resource.openReadBuffered(), verifySignaturesIfSigned);
                    manifestRef.set(jarInputStream.getManifest());
                    return jarInputStream;
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        );
        final @Nullable Manifest manifest = manifestRef.get();
        if(unarchiveManifest && manifest != null) {
            final HierarchicalResource manifestFile = context.getHierarchicalResource(outputDirectory.appendRelativePath(JarFile.MANIFEST_NAME).getNormalized());
            try(final BufferedOutputStream outputStream = manifestFile.ensureFileExists().openWriteBuffered()) {
                manifest.write(outputStream);
                outputStream.flush();
            }
            context.provide(manifestFile);
        }
        return manifest;
    }

    private static void unarchive(
        ExecContext context,
        ResourceKey archiveFile,
        ResourcePath outputDirectory,
        Function<ReadableResource, ZipInputStream> inputStreamFunction
    ) throws IOException {
        // Normalize output directory to ensure that unpacked files have the output directory as prefix.
        outputDirectory = outputDirectory.getNormalized();

        try(final ZipInputStream archiveInputStream = inputStreamFunction.apply(context.require(archiveFile))) {
            ZipEntry entry;
            while((entry = archiveInputStream.getNextEntry()) != null) {
                try {
                    final String name = entry.getName();
                    if(name.isEmpty() || name.equals("/")) {
                        continue; // Skip empty or root paths.
                    }
                    final ResourcePath targetPath = outputDirectory.appendRelativePath(name).getNormalized();
                    if(!targetPath.startsWith(outputDirectory)) {
                        throw new IOException("Cannot unarchive entry '" + name + "' from archive '" + archiveFile + "', resulting path ' " + targetPath + "' is not in the unpack directory '" + outputDirectory + "'");
                    }
                    final HierarchicalResource target = context.getHierarchicalResource(targetPath);
                    if(entry.isDirectory()) {
                        target.ensureDirectoryExists();
                        // Do not provide directory to prevent overlapping provided resources.
                    } else {
                        target.ensureFileExists();
                        target.setLastModifiedTime(entry.getLastModifiedTime().toInstant());
                        try(final BufferedOutputStream outputStream = target.openWriteBuffered()) {
                            IoCommon.copy(archiveInputStream, outputStream, new byte[1024 * 4]);
                            outputStream.flush();
                        }
                        context.provide(target);
                    }
                } finally {
                    archiveInputStream.closeEntry();
                }
            }
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
    }
}
