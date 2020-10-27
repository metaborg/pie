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
    // Trial set-up

    private @Nullable FileSystem fileSystem;
    private @Nullable HierarchicalResource temporaryDirectory;

    public TemporaryDirectoryState setupTrial() throws IOException {
        if(useDiskTemporaryDirectory) {
            temporaryDirectory = FSResource.createTemporaryDirectory("pie.bench.spoofax3");
        } else {
            fileSystem = Jimfs.newFileSystem();
            temporaryDirectory = new FSResource(fileSystem.getPath(""));
        }
        return this;
    }


    // Invocation hot-path (during measurement)

    @SuppressWarnings({"ConstantConditions", "NullableProblems"})
    public HierarchicalResource getTemporaryDirectory() {
        return temporaryDirectory;
    }


    // Trial tear-down

    public void tearDown() throws IOException {
        if(temporaryDirectory == null) {
            throw new IllegalStateException("tearDown was called without first calling setup");
        }
        temporaryDirectory.delete(true);
        temporaryDirectory = null;
        if(fileSystem != null) {
            fileSystem.close();
        }
        fileSystem = null;
    }


    // Parameters

    @Param("true") public boolean useDiskTemporaryDirectory;
}
