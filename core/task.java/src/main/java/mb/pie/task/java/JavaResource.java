package mb.pie.task.java;

import mb.resource.hierarchical.HierarchicalResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.Objects;

public class JavaResource implements JavaFileObject {
    public static class Factory implements JavaFileObjectFactory {
        @Override public JavaFileObject create(HierarchicalResource resource) {
            return new JavaResource(resource);
        }

        @Override public JavaFileObject create(HierarchicalResource resource, Kind kind) {
            return new JavaResource(resource, kind);
        }
    }


    protected final HierarchicalResource resource;
    protected final Kind kind;


    public JavaResource(HierarchicalResource resource, Kind kind) {
        this.resource = resource;
        this.kind = kind;
    }

    public JavaResource(HierarchicalResource resource) {
        this(resource, Util.kindOfResource(resource));
    }


    @Override public Kind getKind() {
        return kind;
    }

    @Override public boolean isNameCompatible(String simpleName, Kind kind) {
        if(!this.kind.equals(kind)) return false;
        // Taken from SimpleJavaFileObject.
        final String baseName = simpleName + kind.extension;
        return (baseName.equals(getName()) || getName().endsWith("/" + baseName));
    }

    @Override public @Nullable NestingKind getNestingKind() {
        return null;
    }

    @Override public @Nullable Modifier getAccessLevel() {
        return null;
    }


    @Override public URI toUri() {
        return resource.getPath().asResourceKeyString().toUri();
    }

    @Override public String getName() {
        return resource.getPath().asString();
    }

    @Override public InputStream openInputStream() throws IOException {
        return resource.openRead();
    }

    @Override public OutputStream openOutputStream() throws IOException {
        resource.createParents();
        return resource.openWrite();
    }

    @Override public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        return new InputStreamReader(openInputStream());
    }

    @Override public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return resource.readString();
    }

    @Override public Writer openWriter() throws IOException {
        return new OutputStreamWriter(openOutputStream());
    }

    @Override public long getLastModified() {
        try {
            return resource.getLastModifiedTime().toEpochMilli();
        } catch(IOException e) {
            return 0;
        }
    }

    @Override public boolean delete() {
        try {
            resource.delete();
        } catch(IOException e) {
            return false;
        }
        return true;
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final JavaResource that = (JavaResource)o;
        return resource.equals(that.resource) && kind == that.kind;
    }

    @Override public int hashCode() {
        return Objects.hash(resource, kind);
    }

    @Override public String toString() {
        return "JavaResource{" +
            "resource=" + resource +
            ", kind=" + kind +
            '}';
    }
}
