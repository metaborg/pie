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

class JavaResource implements JavaFileObject {
    private final HierarchicalResource resource;
    private final Kind kind;


    JavaResource(HierarchicalResource resource, Kind kind) {
        this.resource = resource;
        this.kind = kind;
    }

    JavaResource(HierarchicalResource resource) {
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
        resource.createParents();
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
