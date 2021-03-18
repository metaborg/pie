package mb.pie.task.archive;

import mb.pie.api.ExecContext;
import mb.pie.task.archive.Common.ExecContextProvider;
import mb.pie.task.archive.Common.NoopProvider;
import mb.pie.task.archive.Common.Provider;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.path.string.PathStringMatcher;
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
        ResourcePath outputDirectory,
        PathStringMatcher matcher
    ) throws IOException {
        unarchiveZip(context.require(zipFile), context.getHierarchicalResource(outputDirectory), matcher, new ExecContextProvider(context));
    }

    public static void unarchiveZip(
        ExecContext context,
        ResourceKey zipFile,
        ResourcePath outputDirectory
    ) throws IOException {
        unarchiveZip(context.require(zipFile), context.getHierarchicalResource(outputDirectory), PathStringMatcher.ofTrue(), new ExecContextProvider(context));
    }

    public static void unarchiveZip(
        ReadableResource zipFile,
        HierarchicalResource outputDirectory,
        PathStringMatcher matcher
    ) throws IOException {
        unarchiveZip(zipFile, outputDirectory, matcher, new NoopProvider());
    }

    public static void unarchiveZip(
        ReadableResource zipFile,
        HierarchicalResource outputDirectory
    ) throws IOException {
        unarchiveZip(zipFile, outputDirectory, PathStringMatcher.ofTrue(), new NoopProvider());
    }

    private static void unarchiveZip(
        ReadableResource zipFile,
        HierarchicalResource outputDirectory,
        PathStringMatcher matcher,
        Provider provider
    ) throws IOException {
        unarchive(
            zipFile,
            outputDirectory,
            matcher,
            (resource) -> {
                try {
                    return new ZipInputStream(resource.openReadBuffered());
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            },
            provider
        );
    }


    public static @Nullable Manifest unarchiveJar(
        ExecContext context,
        ResourceKey jarFile,
        ResourcePath outputDirectory,
        PathStringMatcher matcher,
        boolean unarchiveManifest,
        boolean verifySignaturesIfSigned
    ) throws IOException {
        return unarchiveJar(context.require(jarFile), context.getHierarchicalResource(outputDirectory), matcher, unarchiveManifest, verifySignaturesIfSigned, new ExecContextProvider(context));
    }

    public static @Nullable Manifest unarchiveJar(
        ExecContext context,
        ResourceKey jarFile,
        ResourcePath outputDirectory,
        boolean unarchiveManifest,
        boolean verifySignaturesIfSigned
    ) throws IOException {
        return unarchiveJar(context.require(jarFile), context.getHierarchicalResource(outputDirectory), PathStringMatcher.ofTrue(), unarchiveManifest, verifySignaturesIfSigned, new ExecContextProvider(context));
    }

    public static @Nullable Manifest unarchiveJar(
        ReadableResource jarFile,
        HierarchicalResource outputDirectory,
        PathStringMatcher matcher,
        boolean unarchiveManifest,
        boolean verifySignaturesIfSigned
    ) throws IOException {
        return unarchiveJar(jarFile, outputDirectory, matcher, unarchiveManifest, verifySignaturesIfSigned, new NoopProvider());
    }

    public static @Nullable Manifest unarchiveJar(
        ReadableResource jarFile,
        HierarchicalResource outputDirectory,
        boolean unarchiveManifest,
        boolean verifySignaturesIfSigned
    ) throws IOException {
        return unarchiveJar(jarFile, outputDirectory, PathStringMatcher.ofTrue(), unarchiveManifest, verifySignaturesIfSigned, new NoopProvider());
    }

    private static @Nullable Manifest unarchiveJar(
        ReadableResource jarFile,
        HierarchicalResource outputDirectory,
        PathStringMatcher matcher,
        boolean unarchiveManifest,
        boolean verifySignaturesIfSigned,
        Provider provider
    ) throws IOException {
        // Use an atomic reference to allow writing in closure.
        final AtomicReference<@Nullable Manifest> manifestRef = new AtomicReference<>();
        unarchive(
            jarFile,
            outputDirectory,
            matcher,
            (resource) -> {
                try {
                    final JarInputStream jarInputStream = new JarInputStream(resource.openReadBuffered(), verifySignaturesIfSigned);
                    manifestRef.set(jarInputStream.getManifest());
                    return jarInputStream;
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            },
            provider
        );
        final @Nullable Manifest manifest = manifestRef.get();
        if(unarchiveManifest && manifest != null) {
            final HierarchicalResource manifestFile = outputDirectory.appendRelativePath(JarFile.MANIFEST_NAME).getNormalized();
            try(final BufferedOutputStream outputStream = manifestFile.ensureFileExists().openWriteBuffered()) {
                manifest.write(outputStream);
                outputStream.flush();
            }
            provider.provide(manifestFile);
        }
        return manifest;
    }


    private static void unarchive(
        ReadableResource archiveFile,
        HierarchicalResource outputDirectory,
        PathStringMatcher matcher,
        Function<ReadableResource, ZipInputStream> inputStreamFunction,
        Provider provider
    ) throws IOException {
        // Normalize output directory to ensure that unpacked files have the output directory as prefix.
        outputDirectory = outputDirectory.getNormalized();

        try(final ZipInputStream archiveInputStream = inputStreamFunction.apply(archiveFile)) {
            @Nullable ZipEntry entry;
            while((entry = archiveInputStream.getNextEntry()) != null) {
                try {
                    final String name = entry.getName();
                    if(name.isEmpty() || name.equals("/") || !matcher.matches(name)) {
                        continue; // Skip empty, root, and non-matching paths.
                    }
                    final HierarchicalResource target = outputDirectory.appendRelativePath(name).getNormalized();
                    if(!target.startsWith(outputDirectory)) {
                        throw new IOException("Cannot unarchive entry '" + name + "' from archive '" + archiveFile + "', resulting path ' " + target + "' is not in the unpack directory '" + outputDirectory + "'");
                    }
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
                        provider.provide(target);
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
