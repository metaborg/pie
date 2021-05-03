package mb.pie.task.java.ecj;

import mb.pie.task.java.FileManagerFactory;
import mb.pie.task.java.JavaFileObjectFactory;
import mb.pie.task.java.JavaResourceManager;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;

import javax.tools.JavaFileManager;
import javax.tools.StandardJavaFileManager;
import java.util.ArrayList;

public class EclipseFileManagerFactory implements FileManagerFactory {
    @Override
    public JavaFileManager create(
        StandardJavaFileManager fileManager,
        JavaFileObjectFactory javaFileObjectFactory,
        ResourceService resourceService,
        ArrayList<HierarchicalResource> sourcePath,
        HierarchicalResource sourceFileOutputDir,
        HierarchicalResource classFileOutputDir
    ) {
        return new JavaResourceManager(fileManager, javaFileObjectFactory, resourceService, sourcePath, sourceFileOutputDir, classFileOutputDir);
    }
}
