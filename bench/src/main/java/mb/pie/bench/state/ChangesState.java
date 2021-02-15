package mb.pie.bench.state;

import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.resource.WritableResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.core.platform.LoggerComponent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;

@State(Scope.Thread)
public class ChangesState {
    private mb.log.api.@Nullable Logger logger;
    private @Nullable HierarchicalResource baseDirectory;
    private final LinkedHashSet<ResourceKey> changedResources = new LinkedHashSet<>();


    // Invocation set-up

    public ChangesState setupInvocation(LoggerComponent loggerComponent, HierarchicalResource baseDirectory) {
        if(logger != null || this.baseDirectory != null) {
            throw new IllegalStateException("setupInvocation was called before tearDownInvocation");
        }
        logger = loggerComponent.getLoggerFactory().create(ChangesState.class);
        logger.trace("ChangesState.setupInvocation");
        this.baseDirectory = baseDirectory;
        reset();
        return this;
    }


    // Invocation hot-path (during measurement)

    public String readString(String relativePath) throws IOException {
        return readString(getResource(relativePath));
    }

    public String readString(ReadableResource file) throws IOException {
        return file.readString();
    }


    public void writeString(String relativePath, String text) throws IOException {
        writeString(getResource(relativePath), text);
    }

    public void writeString(WritableResource file, String text) throws IOException {
        file.writeString(text);
        markAsChanged(file.getKey());
    }


    public void replaceAll(String relativePath, String regex, String replacement) throws IOException {
        writeString(relativePath, readString(relativePath).replaceAll(regex, replacement));
    }

    public void replaceAll(WritableResource file, String regex, String replacement) throws IOException {
        writeString(file, readString(file).replaceAll(regex, replacement));
    }

    public void replaceAllLiteral(String relativePath, String target, String replacement) throws IOException {
        writeString(relativePath, readString(relativePath).replace(target, replacement));
    }

    public void replaceAllLiteral(WritableResource file, String target, String replacement) throws IOException {
        writeString(file, readString(file).replace(target, replacement));
    }

    public void replaceFirst(String relativePath, String regex, String replacement) throws IOException {
        writeString(relativePath, readString(relativePath).replaceFirst(regex, replacement));
    }

    public void replaceFirst(WritableResource file, String regex, String replacement) throws IOException {
        writeString(file, readString(file).replaceFirst(regex, replacement));
    }

    public void replaceFirstLiteral(String relativePath, String target, String replacement) throws IOException {
        writeString(relativePath, readString(relativePath).replaceFirst(Pattern.quote(target), replacement));
    }

    public void replaceFirstLiteral(WritableResource file, String target, String replacement) throws IOException {
        writeString(file, readString(file).replaceFirst(Pattern.quote(target), replacement));
    }


    public void markAsChanged(ResourceKey key) {
        changedResources.add(key);
    }


    public LinkedHashSet<ResourceKey> getChangedResources() {
        return changedResources;
    }


    public void reset() {
        changedResources.clear();
    }


    // Invocation tear-down

    public void tearDownInvocation() {
        if(logger == null || baseDirectory == null) {
            throw new IllegalStateException("tearDownInvocation was called before setupInvocation");
        }
        reset();
        baseDirectory = null;
        logger.trace("ChangesState.tearDownInvocation");
        logger = null;
    }


    // Helper methods

    @SuppressWarnings("ConstantConditions") private HierarchicalResource getResource(String relativePath) {
        return baseDirectory.appendRelativePath(relativePath).getNormalized();
    }
}
