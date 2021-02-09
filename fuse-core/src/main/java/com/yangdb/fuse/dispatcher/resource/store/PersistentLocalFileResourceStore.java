package com.yangdb.fuse.dispatcher.resource.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.dispatcher.resource.CursorResource;
import com.yangdb.fuse.dispatcher.resource.PageResource;
import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.dispatcher.utils.FileUtils;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import javaslang.collection.Stream;
import javaslang.control.Option;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.StorageType._stored;

public class PersistentLocalFileResourceStore extends InMemoryResourceStore {
    private static final ObjectMapper mapper = new ObjectMapper();
    public static final String QUERY_INFO = "queryInfo_";

    public PersistentLocalFileResourceStore() {
        super();
//        init in-mem store using the persistent file system store
        loadFromStorage().forEach(super::addQueryResource);
    }

    /**
     * loads data from file system storage
     *
     * @return
     */
    private Collection<? extends QueryResource> loadFromStorage() {
        File dir = null;
        try {
            dir = FileUtils.getOrCreateFile(GlobalConstants.FileSystemConfigs.QUERY_STORE_NAME_DEFUALT_VALUE, System.getProperty("user.dir"), true);
            return Stream
                    .of(dir.listFiles() == null ? new File[0] : dir.listFiles())
                        .map(this::loadFromStorage)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                    .toJavaList();
        } catch (Throwable throwable) {
            throw new FuseError.FuseErrorException("Failed writing file for new Query ", throwable.getCause());
        }
    }

    /**
     * load single query resource (including all sub resources)
     * - including cursor resource
     * - including traversal context resource
     * - including page resource
     * - including data pages
     *
     * @param queryDir
     * @return
     */
    private Optional<QueryResource> loadFromStorage(File queryDir)  {
        Option<File> queryFile = Stream.of(queryDir.listFiles() == null ? new File[0] : queryDir.listFiles())
                .find(file -> FilenameUtils.getBaseName(file.getName()).equals(QUERY_INFO + queryDir.getName()));
        if(queryFile.isDefined()) {
            try {
                return Optional.of(mapper.readValue(queryFile.get(),QueryResource.class));
            } catch (IOException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public Collection<QueryResource> getQueryResources() {
        return super.getQueryResources();
    }


    @Override
    public Collection<QueryResource> getQueryResources(Predicate<String> predicate) {
        return super.getQueryResources(predicate);
    }

    @Override
    public Optional<QueryResource> getQueryResource(String queryId) {
        return super.getQueryResource(queryId);
    }

    @Override
    public Optional<CursorResource> getCursorResource(String queryId, String cursorId) {
        return super.getCursorResource(queryId, cursorId);
    }

    @Override
    public Optional<PageResource> getPageResource(String queryId, String cursorId, String pageId) {
        return super.getPageResource(queryId, cursorId, pageId);
    }

    @Override
    public synchronized boolean addQueryResource(QueryResource queryResource) {
        super.addQueryResource(queryResource);
        // persist to local file store
        File dir = null;
        try {
            dir = FileUtils.getOrCreateFile(GlobalConstants.FileSystemConfigs.QUERY_STORE_NAME_DEFUALT_VALUE, System.getProperty("user.dir"), true);
            String id = queryResource.getQueryMetadata().getId();
            boolean exists = Stream.of(dir.listFiles() == null ? new File[0] : dir.listFiles())
                    .exists(file -> FilenameUtils.getBaseName(file.getName()).equals(id));
            //a query already exists with this specific name
            if (exists) {
                throw new FuseError.FuseErrorException("a query already exists with this specific name [" + id + "]", new IllegalArgumentException());
            }
            //create folder with the query name under queries folder
            File queryDir = FileUtils.getOrCreateFile(id, dir.getAbsolutePath(), true);
            if (queryDir.exists()) {
                //store query resource into a file
                Path path = Paths.get(queryDir.getAbsolutePath() + "/" + QUERY_INFO + id + ".json");
                try {
                    Files.write(path, mapper.writeValueAsBytes(queryResource));
                } catch (IOException e) {
                    throw new FuseError.FuseErrorException("Failed writing file for new Ontology [" + QUERY_INFO + id + "] ", e.getCause());
                }
            }
        } catch (IOException e) {
            throw new FuseError.FuseErrorException("Failed writing file for new Query ", e.getCause());
        }

        return true;
    }

    @Override
    public synchronized boolean deleteQueryResource(String queryId) {
        super.deleteQueryResource(queryId);
        // todo remove query resources from local store
        return false;
    }

    @Override
    public boolean addCursorResource(String queryId, CursorResource cursorResource) {
        super.addCursorResource(queryId, cursorResource);
        //todo add CursorResource to local store
        return false;
    }

    @Override
    public boolean deleteCursorResource(String queryId, String cursorId) {
        super.deleteCursorResource(queryId, cursorId);
        //todo remove CursorResource from local store
        return false;
    }

    @Override
    public boolean addPageResource(String queryId, String cursorId, PageResource pageResource) {
        super.addPageResource(queryId, cursorId, pageResource);
        //todo add PageResource to local store
        return false;
    }

    @Override
    public boolean deletePageResource(String queryId, String cursorId, String pageId) {
        return false;
    }

    @Override
    public boolean test(CreateQueryRequest.StorageType type) {
        return type.equals(_stored);
    }


}
