package mb.pie.task.archive;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.fs.FSResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Collections;

class TestBase {
    final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
    final FSResource rootDirectory = new FSResource(fileSystem.getPath("/"));
    final ArchiveToZip archiveToZip = new ArchiveToZip();
    final ArchiveToJar archiveToJar = new ArchiveToJar();
    final UnarchiveFromZip unarchiveFromZip = new UnarchiveFromZip();
    final UnarchiveFromJar unarchiveFromJar = new UnarchiveFromJar();
    final MapTaskDefs taskDefs = new MapTaskDefs(archiveToZip, archiveToJar, unarchiveFromZip, unarchiveFromJar);
    final Pie pie = new PieBuilderImpl().withTaskDefs(taskDefs).build();

    FSResource createDir(FSResource parent, String relativePath) throws IOException {
        return parent.appendRelativePath(relativePath).createDirectory(true);
    }

    FSResource createFile(FSResource parent, String text, String relativePath) throws IOException {
        final FSResource resource = parent.appendRelativePath(relativePath);
        resource.createParents();
        resource.writeString(text, StandardCharsets.UTF_8);
        return resource;
    }

    @SafeVarargs final <T> ArrayList<T> createList(T... items) {
        final ArrayList<T> list = new ArrayList<>();
        Collections.addAll(list, items);
        return list;
    }
}
