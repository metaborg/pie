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
    // Parameters

    @Param("true") public boolean useDiskTemporaryDirectory;


    // Trial

    private @Nullable FileSystem fileSystem;
    private @Nullable HierarchicalResource temporaryDirectory;

    public HierarchicalResource setupTrial() throws IOException {
        if(fileSystem != null && temporaryDirectory != null) {
            throw new IllegalStateException("setupTrial was called before tearDownTrial");
        }
        if(useDiskTemporaryDirectory) {
            temporaryDirectory = FSResource.temporaryDirectory().appendRelativePath("pie.bench");
            temporaryDirectory.delete(true);
            temporaryDirectory.ensureDirectoryExists();
        } else {
            fileSystem = Jimfs.newFileSystem();
            temporaryDirectory = new FSResource(fileSystem.getPath("/pie.bench"));
        }
        return temporaryDirectory;
    }

    public void tearDownTrial() throws IOException {
        if(temporaryDirectory == null) {
            throw new IllegalStateException("tearDownTrial was called before setupTrial");
        }
        temporaryDirectory.delete(true);
        temporaryDirectory = null;
        if(fileSystem != null) {
            fileSystem.close();
        }
        fileSystem = null;
    }


    // Invocation

    public HierarchicalResource setupInvocation() throws IOException {
        if(temporaryDirectory == null) {
            throw new IllegalStateException("setupInvocation was called before setupTrial");
        }
        temporaryDirectory.delete(true);
        temporaryDirectory.ensureDirectoryExists();
        return temporaryDirectory;
    }

    public void tearDownInvocation() throws IOException {
        if(temporaryDirectory == null) {
            throw new IllegalStateException("tearDownInvocation was called before setupTrial");
        }
        temporaryDirectory.delete(true);
    }
}
