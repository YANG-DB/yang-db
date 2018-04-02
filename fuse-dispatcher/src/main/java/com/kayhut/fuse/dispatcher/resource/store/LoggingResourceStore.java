package com.kayhut.fuse.dispatcher.resource.store;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Created by roman.margolis on 29/11/2017.
 */
public class LoggingResourceStore implements ResourceStore {
    public static final String injectionName = "LoggingResourceStore.inner";

    //region Constructors
    @Inject
    public LoggingResourceStore(
            @Named(injectionName) ResourceStore innerResourceStore,
            Descriptor<QueryResource> queryResourceDescriptor,
            Descriptor<CursorResource> cursorResourceDescriptor,
            Descriptor<PageResource> pageResourceDescriptor) {

        this.logger = LoggerFactory.getLogger(innerResourceStore.getClass());
        this.innerResourceStore = innerResourceStore;
        this.queryResourceDescriptor = queryResourceDescriptor;
        this.cursorResourceDescriptor = cursorResourceDescriptor;
        this.pageResourceDescriptor = pageResourceDescriptor;
    }
    //endregion

    //region ResourceStore Implementation
    @Override
    public Iterable<QueryResource> getQueryResources() {
        return this.innerResourceStore.getQueryResources();
    }

    @Override
    public Optional<QueryResource> getQueryResource(String queryId) {
        return this.innerResourceStore.getQueryResource(queryId);
    }

    @Override
    public Optional<CursorResource> getCursorResource(String queryId, String cursorId) {
        return this.innerResourceStore.getCursorResource(queryId, cursorId);
    }

    @Override
    public Optional<PageResource> getPageResource(String queryId, String cursorId, String pageId) {
        return this.innerResourceStore.getPageResource(queryId, cursorId, pageId);
    }

    @Override
    public void addQueryResource(QueryResource queryResource) {
        this.innerResourceStore.addQueryResource(queryResource);
        this.logger.info("QueryResource was added: {}", this.queryResourceDescriptor.describe(queryResource));
    }

    @Override
    public void deleteQueryResource(String queryId) {
        Optional<QueryResource> queryResourceToDelete = this.innerResourceStore.getQueryResource(queryId);
        this.innerResourceStore.deleteQueryResource(queryId);

        if (queryResourceToDelete.isPresent()) {
            this.logger.debug("QueryResource was deleted: {}", this.queryResourceDescriptor.describe(queryResourceToDelete.get()));
        }
    }

    @Override
    public void addCursorResource(String queryId, CursorResource cursorResource) {
        Optional<QueryResource> queryResource = this.innerResourceStore.getQueryResource(queryId);
        this.innerResourceStore.addCursorResource(queryId, cursorResource);

        if (queryResource.isPresent()) {
            this.logger.debug("CursorResource was added: {} {}",
                    this.queryResourceDescriptor.describe(queryResource.get()),
                    this.cursorResourceDescriptor.describe(cursorResource));
        }
    }

    @Override
    public void deleteCursorResource(String queryId, String cursorId) {
        Optional<QueryResource> queryResource = this.innerResourceStore.getQueryResource(queryId);
        Optional<CursorResource> cursorResourceToDelete = queryResource.flatMap(queryResource1 -> queryResource1.getCursorResource(cursorId));

        if (queryResource.isPresent() && cursorResourceToDelete.isPresent()) {
            this.logger.debug("CursorResource was deleted: {} {}",
                    this.queryResourceDescriptor.describe(queryResource.get()),
                    this.cursorResourceDescriptor.describe(cursorResourceToDelete.get()));
        }
    }

    @Override
    public void addPageResource(String queryId, String cursorId, PageResource pageResource) {
        Optional<QueryResource> queryResource = this.innerResourceStore.getQueryResource(queryId);
        Optional<CursorResource> cursorResource = queryResource.flatMap(queryResource1 -> queryResource1.getCursorResource(cursorId));

        this.innerResourceStore.addPageResource(queryId, cursorId, pageResource);

        if (queryResource.isPresent() && cursorResource.isPresent()) {
            if (pageResource.isAvailable()) {
                this.logger.debug("PageResource is available: {} {} {}",
                        this.queryResourceDescriptor.describe(queryResource.get()),
                        this.cursorResourceDescriptor.describe(cursorResource.get()),
                        this.pageResourceDescriptor.describe(pageResource));
            } else {
                this.logger.debug("PageResource was added: {} {} {}",
                        this.queryResourceDescriptor.describe(queryResource.get()),
                        this.cursorResourceDescriptor.describe(cursorResource.get()),
                        this.pageResourceDescriptor.describe(pageResource));
            }
        }
    }

    @Override
    public void deletePageResource(String queryId, String cursorId, String pageId) {
        Optional<QueryResource> queryResource = this.innerResourceStore.getQueryResource(queryId);
        Optional<CursorResource> cursorResource = queryResource.flatMap(queryResource1 -> queryResource1.getCursorResource(cursorId));
        Optional<PageResource> pageResourceToDelete = cursorResource.flatMap(cursorResource1 -> cursorResource1.getPageResource(pageId));

        this.innerResourceStore.deletePageResource(queryId, cursorId, pageId);

        if (queryResource.isPresent() && cursorResource.isPresent() && pageResourceToDelete.isPresent()) {
            this.logger.debug("PageResource was deleted: {} {} {}",
                    this.queryResourceDescriptor.describe(queryResource.get()),
                    this.cursorResourceDescriptor.describe(cursorResource.get()),
                    this.pageResourceDescriptor.describe(pageResourceToDelete.get()));
        }
    }
    //endregion

    //region Fields
    private Logger logger;

    private ResourceStore innerResourceStore;

    private Descriptor<QueryResource> queryResourceDescriptor;
    private Descriptor<CursorResource> cursorResourceDescriptor;
    private Descriptor<PageResource> pageResourceDescriptor;
    //endregion
}
