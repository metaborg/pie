package mb.pie.bench.util;

import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.resource.WritableResource;
import mb.resource.hierarchical.HierarchicalResource;

import java.io.IOException;
import java.util.HashSet;

public class ChangeMaker {
    private final HierarchicalResource baseDirectory;
    private final HashSet<ResourceKey> changedResources = new HashSet<>();

    public ChangeMaker(HierarchicalResource baseDirectory) {
        this.baseDirectory = baseDirectory;
    }


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

    public void replaceFirst(String relativePath, String regex, String replacement) throws IOException {
        writeString(relativePath, readString(relativePath).replaceFirst(regex, replacement));
    }

    public void replaceFirst(WritableResource file, String regex, String replacement) throws IOException {
        writeString(file, readString(file).replaceFirst(regex, replacement));
    }


    public void markAsChanged(ResourceKey key) {
        changedResources.add(key);
    }

    public HashSet<ResourceKey> getChangedResources() {
        return changedResources;
    }

    public void reset() {
        changedResources.clear();
    }


    private HierarchicalResource getResource(String relativePath) {
        return baseDirectory.appendRelativePath(relativePath);
    }
}
