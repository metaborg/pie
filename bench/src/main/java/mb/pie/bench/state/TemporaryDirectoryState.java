package mb.pie.bench.state;

import com.google.common.jimfs.Jimfs;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.nio.file.FileSystem;

@State(Scope.Thread)
public class TemporaryDirectoryState {
    // Invocation

    private @Nullable FileSystem fileSystem;
    private @Nullable HierarchicalResource temporaryDirectory;

    public TemporaryDirectoryState setupInvocation() throws IOException {
        if(fileSystem != null && temporaryDirectory != null) {
            throw new IllegalStateException("setupInvocation was called before tearDownInvocation");
        }
        if(useDiskTemporaryDirectory) {
            temporaryDirectory = FSResource.createTemporaryDirectory("pie.bench.spoofax3");
        } else {
            fileSystem = Jimfs.newFileSystem();
            temporaryDirectory = new FSResource(fileSystem.getPath(""));
        }
        return this;
    }

    @SuppressWarnings({"ConstantConditions", "NullableProblems"})
    public HierarchicalResource getTemporaryDirectory() {
        return temporaryDirectory;
    }

    public void tearDownInvocation() throws IOException {
        if(temporaryDirectory == null) {
            throw new IllegalStateException("tearDownInvocation was called before setupInvocation");
        }
        temporaryDirectory.delete(true);
        temporaryDirectory = null;
        if(fileSystem != null) {
            fileSystem.close();
        }
        fileSystem = null;
    }


    // Parameters

    /*@Param("true")*/ public boolean useDiskTemporaryDirectory = true;
}
