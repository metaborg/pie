package mb.pie.task.java.jdk;

import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;

import javax.tools.JavaFileManager;
import javax.tools.StandardJavaFileManager;
import java.util.ArrayList;

@FunctionalInterface
public interface FileManagerFactory {
    JavaFileManager create(
        StandardJavaFileManager fileManager,
        JavaFileObjectFactory javaFileObjectFactory,
        ResourceService resourceService,
        ArrayList<HierarchicalResource> sourcePath,
        HierarchicalResource sourceFileOutputDir,
        HierarchicalResource classFileOutputDir
    );
}
