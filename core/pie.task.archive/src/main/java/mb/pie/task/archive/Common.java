package mb.pie.task.archive;

import mb.pie.api.ExecContext;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.ResourceService;
import mb.resource.WritableResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;

import java.io.IOException;

class Common {
    interface Requirer {
        void require(ReadableResource resource) throws IOException;

        HierarchicalResource require(ResourcePath resource, ResourceStamper<HierarchicalResource> stamper) throws IOException;
    }

    static class ExecContextRequirer implements Requirer {
        private final ExecContext context;

        ExecContextRequirer(ExecContext context) {
            this.context = context;
        }

        @Override
        public void require(ReadableResource resource) throws IOException {
            context.require(resource);
        }

        @Override
        public HierarchicalResource require(ResourcePath resource, ResourceStamper<HierarchicalResource> stamper) throws IOException {
            return context.require(resource, stamper);
        }
    }

    static class ResourceServiceRequirer implements Requirer {
        private final ResourceService resourceService;

        ResourceServiceRequirer(ResourceService resourceService) {
            this.resourceService = resourceService;
        }

        @Override
        public void require(ReadableResource resource) {}

        @Override
        public HierarchicalResource require(ResourcePath resource, ResourceStamper<HierarchicalResource> stamper) throws IOException {
            return resourceService.getHierarchicalResource(resource);
        }
    }


    interface Provider {
        void provide(WritableResource resource) throws IOException;
    }

    static class ExecContextProvider implements Provider {
        private final ExecContext context;

        ExecContextProvider(ExecContext context) {
            this.context = context;
        }

        @Override
        public void provide(WritableResource resource) throws IOException {
            context.provide(resource);
        }
    }

    static class NoopProvider implements Provider {
        @Override
        public void provide(WritableResource resource) {}
    }
}
