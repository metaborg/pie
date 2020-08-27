package mb.pie.task.java;

import mb.resource.ResourceKeyString;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.resource.hierarchical.FilenameExtensionUtil;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.resource.hierarchical.walk.TrueResourceWalker;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static mb.pie.task.java.Util.qualifiedNameToRelativePath;
import static mb.pie.task.java.Util.relativePathToQualifiedName;

class JavaResourceManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
    private final ResourceService resourceService;

    private final ArrayList<HierarchicalResource> sourcePath;
    private final HierarchicalResource sourceFileOutputDir;
    private final HierarchicalResource classFileOutputDir;

    JavaResourceManager(
        StandardJavaFileManager fileManager,
        ResourceService resourceService,
        ArrayList<HierarchicalResource> sourcePath,
        HierarchicalResource sourceFileOutputDir,
        HierarchicalResource classFileOutputDir
    ) {
        super(fileManager);
        this.resourceService = resourceService;
        this.sourcePath = sourcePath;
        this.sourceFileOutputDir = sourceFileOutputDir;
        this.classFileOutputDir = classFileOutputDir;
    }

    @Override public boolean hasLocation(Location location) {
        if(location instanceof StandardLocation) {
            switch((StandardLocation)location) {
                case CLASS_OUTPUT:
                case SOURCE_OUTPUT:
                case SOURCE_PATH:
                    return true;
                default:
                    return super.hasLocation(location);
            }
        } else {
            try {
                getNonStandardResource(location);
                return true;
            } catch(ResourceRuntimeException e) {
                return super.hasLocation(location);
            }
        }
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
        final @Nullable List<HierarchicalResource> baseResources = getResources(location);
        if(baseResources == null) {
            return super.list(location, packageName, kinds, recurse);
        }
        final ArrayList<JavaFileObject> results = new ArrayList<>();
        for(HierarchicalResource baseResource : baseResources) {
            final HierarchicalResource resource = baseResource.appendRelativePath(qualifiedNameToRelativePath(packageName));
            if(!resource.exists() || !resource.isDirectory()) continue;
            final ExtensionsPathMatcher pathMatcher = new ExtensionsPathMatcher(kinds.stream().map(Util::kindToExtension).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
            final ResourceMatcher matcher = new PathResourceMatcher(pathMatcher);
            if(recurse) {
                results.addAll(resource.walk(new TrueResourceWalker(), matcher).map(JavaResource::new).collect(Collectors.toList()));
            } else {
                results.addAll(resource.list(matcher).map(JavaResource::new).collect(Collectors.toList()));
            }
        }
        return results;
    }

    @Override public @Nullable String inferBinaryName(Location location, JavaFileObject file) {
        if(location instanceof StandardLocation) {
            switch((StandardLocation)location) {
                case CLASS_OUTPUT:
                case SOURCE_OUTPUT:
                case SOURCE_PATH:
                    final @Nullable List<HierarchicalResource> baseResources = getResources(location);
                    if(baseResources == null) break;
                    for(HierarchicalResource baseResource : baseResources) {
                        final ResourcePath basePath = baseResource.getPath();
                        final ResourcePath path = resourceService.getResourcePath(ResourceKeyString.parse(file.getName()));
                        if(!path.startsWith(basePath)) continue;
                        return relativePathToQualifiedName(FilenameExtensionUtil.removeExtension(basePath.relativizeToString(path)));
                    }
                    return null;
                default:
                    break;
            }
        }
        return super.inferBinaryName(location, file);
    }

    @Override
    public @Nullable FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        final @Nullable List<HierarchicalResource> baseResources = getResources(location);
        if(baseResources == null) {
            return super.getFileForInput(location, packageName, relativeName);
        }
        for(HierarchicalResource baseResource : baseResources) {
            final HierarchicalResource resource = baseResource
                .appendRelativePath(qualifiedNameToRelativePath(packageName))
                .appendRelativePath(relativeName);
            if(resource.exists()) {
                return new JavaResource(resource);
            }
        }
        return null;
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, @Nullable FileObject sibling) throws IOException {
        final @Nullable HierarchicalResource baseResource = getResource(location);
        if(baseResource == null) {
            return super.getFileForOutput(location, packageName, relativeName, sibling);
        }
        final HierarchicalResource resource = baseResource
            .appendRelativePath(qualifiedNameToRelativePath(packageName))
            .appendRelativePath(relativeName);
        return new JavaResource(resource);
    }

    @Override
    public @Nullable JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
        final @Nullable List<HierarchicalResource> baseResources = getResources(location);
        if(baseResources == null) {
            return super.getJavaFileForInput(location, className, kind);
        }
        for(HierarchicalResource baseResource : baseResources) {
            final HierarchicalResource resource = baseResource
                .appendRelativePath(qualifiedNameToRelativePath(className))
                .appendToLeaf(kind.extension);
            if(resource.exists()) {
                return new JavaResource(resource, kind);
            }
        }
        return null;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, @Nullable FileObject sibling) throws IOException {
        final @Nullable HierarchicalResource baseResource = getResource(location);
        if(baseResource == null) {
            return super.getJavaFileForOutput(location, className, kind, sibling);
        }
        final HierarchicalResource resource = baseResource
            .appendRelativePath(qualifiedNameToRelativePath(className))
            .appendToLeaf(kind.extension);
        return new JavaResource(resource);
    }


    private @Nullable List<HierarchicalResource> getResources(Location location) {
        if(location instanceof StandardLocation) {
            switch((StandardLocation)location) {
                case CLASS_OUTPUT:
                    return Collections.singletonList(classFileOutputDir);
                case SOURCE_OUTPUT:
                    return Collections.singletonList(sourceFileOutputDir);
                case SOURCE_PATH:
                    return !sourcePath.isEmpty() ? sourcePath : null;
                default:
                    return null;
            }
        } else {
            return Collections.singletonList(getNonStandardResource(location));
        }
    }

    private @Nullable HierarchicalResource getResource(Location location) {
        if(location instanceof StandardLocation) {
            switch((StandardLocation)location) {
                case CLASS_OUTPUT:
                    return classFileOutputDir;
                case SOURCE_OUTPUT:
                    return sourceFileOutputDir;
                default:
                    return null;
            }
        } else {
            return getNonStandardResource(location);
        }
    }

    private HierarchicalResource getNonStandardResource(Location location) {
        return resourceService.getHierarchicalResource(ResourceKeyString.parse(location.getName()));
    }
}
