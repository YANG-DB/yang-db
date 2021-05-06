package com.yangdb.fuse.dispatcher.resource.store;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2021 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
    public static final String CURSOR_INFO = "cursorInfo_";
    public static final String PAGE_INFO = "pageInfo_";

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
        try {
            File dir = FileUtils.getOrCreateFile(GlobalConstants.FileSystemConfigs.QUERY_STORE_NAME_DEFUALT_VALUE, System.getProperty("user.dir"), true);
            return Stream
                    .of(dir.listFiles() == null ? new File[0] : dir.listFiles())
                    .map(this::loadFromStorage)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toJavaList();
        } catch (Throwable throwable) {
            throw new FuseError.FuseErrorException("Failed loading files store for Query resource info ", throwable.getCause());
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
    private Optional<QueryResource> loadFromStorage(File queryDir) {
        Option<File> queryFile = Stream.of(queryDir.listFiles() == null ? new File[0] : queryDir.listFiles())
                .find(file -> FilenameUtils.getBaseName(file.getName()).equals(QUERY_INFO + queryDir.getName()));
        if (Files.exists(queryFile.get().toPath())) {
            try {
//                BufferedReader reader = Files.newBufferedReader(queryFile.get().toPath());
                return Optional.of(QueryResource.deserialize(mapper, Files.readAllBytes(queryFile.get().toPath())));
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
        Optional<CursorResource> resource = super.getCursorResource(queryId, cursorId);
        if (!resource.isPresent()) {
            try {
                File cursorDir = getCursorDir(queryId, cursorId);
                //store cursor data file
                Path path = Paths.get(cursorDir.getAbsolutePath() + "/" + CURSOR_INFO + cursorId + ".json");
                try {
                    if (Files.exists(path)) {
                        CursorResource cursorResource = CursorResource.deserialize(mapper, Files.readAllBytes(path));
                        //add from storage to memory
                        super.getQueryResource(queryId).get().addCursorResource(cursorId, cursorResource);
                        resource = Optional.of(cursorResource);
                    }
                } catch (IOException e) {
                    throw new FuseError.FuseErrorException("Failed reading file for CursorResource Info [" + CURSOR_INFO + cursorId + "] ", e.getCause());
                }
            } catch (IOException e) {
                throw new FuseError.FuseErrorException("Failed reading file for CursorResource Info [" + CURSOR_INFO + cursorId + "] ", e.getCause());
                //cursor may be missing
            }
        }
        return resource;
    }

    @Override
    public Optional<PageResource> getPageResource(String queryId, String cursorId, String pageId) {
        Optional<PageResource> pageResource = super.getPageResource(queryId, cursorId, pageId);
        if (!pageResource.isPresent()) {
            try {
                File pageDir = getPageDir(queryId, cursorId);
                //store cursor data file
                Path path = Paths.get(pageDir.getAbsolutePath() + "/" + PAGE_INFO + pageId + ".json");
                try {
                    if (Files.exists(path)) {
                        PageResource page = PageResource.deserialize(mapper, Files.readAllBytes(path));
                        //add from storage to memory
                        super.getQueryResource(queryId).get().getCursorResource(cursorId).get().addPageResource(pageId, page);
                        pageResource = Optional.of(page);
                    }
                } catch (IOException e) {
                    throw new FuseError.FuseErrorException("Failed reading file for CursorResource Info [" + CURSOR_INFO + cursorId + "] ", e.getCause());
                }
            } catch (IOException e) {
                throw new FuseError.FuseErrorException("Failed reading file for PageResource Info [" + PAGE_INFO + pageId + "] ", e.getCause());
            }
        }
        return pageResource;
    }

    @Override
    public synchronized boolean addQueryResource(QueryResource queryResource) {
        super.addQueryResource(queryResource);
        // persist to local file store
        try {
            File dir = getQueryDir();
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
                    Files.write(path, QueryResource.serialize(mapper, queryResource));
                } catch (IOException e) {
                    throw new FuseError.FuseErrorException("Failed writing file for new query resource [" + QUERY_INFO + id + "] ", e.getCause());
                }
            }
        } catch (IOException e) {
            throw new FuseError.FuseErrorException("Failed writing file for new query resource ", e.getCause());
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
        //add CursorResource to local store
        try {
            File queryDir = getQueryDir(queryId);

            if (!queryDir.exists()) {
                throw new FuseError.FuseErrorException("a query folder doesnt exists with this specific name [" + queryId + "]", new IllegalArgumentException());
            }
            //create folder named cursor name
            File cursorDir = getCursorDir(queryId);

            boolean exists = Stream.of(cursorDir.listFiles() == null ? new File[0] : cursorDir.listFiles())
                    .exists(file -> FilenameUtils.getBaseName(file.getName()).equals(cursorResource.getCursorId()));
            //a cursor already exists with this specific name
            if (exists) {
                throw new FuseError.FuseErrorException("a cursor already exists with this specific name [" + queryId + "]", new IllegalArgumentException());
            }
            //store cursor data file
            File cursorFileLocation = getCursorDir(queryId, cursorResource.getCursorId());
            Path path = Paths.get(cursorFileLocation.getAbsolutePath() + "/" + CURSOR_INFO + cursorResource.getCursorId() + ".json");
            try {
                Files.write(path, CursorResource.serialize(mapper, cursorResource));
            } catch (IOException e) {
                throw new FuseError.FuseErrorException("Failed writing file for new CursorResource Info [" + CURSOR_INFO + cursorResource.getCursorId() + "] ", e.getCause());
            }

        } catch (IOException e) {
            throw new FuseError.FuseErrorException("Failed writing file for new CursorResource Info ", e.getCause());
        }

        return true;
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

        //add PageResource to local store
        try {
            File queryDir = getQueryDir(queryId);

            if (!queryDir.exists()) {
                throw new FuseError.FuseErrorException("a query folder doesnt exists with this specific name [" + queryId + "]", new IllegalArgumentException());
            }

            File cursorDir = getCursorDir(queryId);

            if (!cursorDir.exists()) {
                throw new FuseError.FuseErrorException("a cursor folder doesnt exists with this specific query name [" + queryId + "]", new IllegalArgumentException());
            }
            //create folder named page
            File pageDir = getPageDir(queryId, cursorId);
            boolean exists = Stream.of(pageDir.listFiles() == null ? new File[0] : pageDir.listFiles())
                    .exists(file -> FilenameUtils.getBaseName(file.getName()).equals(pageResource.getPageId()));
            //a page already exists with this specific name
            if (exists) {
                throw new FuseError.FuseErrorException("a page already exists with this specific name [" + pageResource.getPageId() + "]", new IllegalArgumentException());
            }
            //store page data file
            try {
                Path path = Paths.get(pageDir.getAbsolutePath() + "/" + PAGE_INFO + pageResource.getPageId() + ".json");
                Files.write(path, PageResource.serialize(mapper, pageResource));
            } catch (IOException e) {
                throw new FuseError.FuseErrorException("Failed writing file for new PageResource Info [" + PAGE_INFO + pageResource.getPageId() + "] ", e.getCause());
            }

        } catch (IOException e) {
            throw new FuseError.FuseErrorException("Failed writing file for new PageResource Info ", e.getCause());
        }
        return true;
    }

    @Override
    public boolean deletePageResource(String queryId, String cursorId, String pageId) {
        super.deletePageResource(queryId, cursorId, pageId);
        //todo remove PageResource from local store
        return false;
    }

    @Override
    public boolean test(CreateQueryRequest.StorageType type) {
        return type.equals(_stored);
    }

    //    utility functions
//    -----------------------------------------------------------------------------------------------------------------------------------------
    private File getQueryDir() throws IOException {
        return FileUtils.getOrCreateFile(GlobalConstants.FileSystemConfigs.QUERY_STORE_NAME_DEFUALT_VALUE, System.getProperty("user.dir"), true);
    }

    private File getQueryDir(String queryId) throws IOException {
        return FileUtils.getOrCreateFile(GlobalConstants.FileSystemConfigs.QUERY_STORE_NAME_DEFUALT_VALUE + "/" + queryId, System.getProperty("user.dir"), true);
    }

    private File getCursorDir(String queryId) throws IOException {
        return FileUtils.getOrCreateFile(GlobalConstants.FileSystemConfigs.CURSOR_STORE_NAME_DEFUALT_VALUE, getQueryDir(queryId).getAbsolutePath(), true);
    }

    private File getCursorDir(String queryId, String cursorId) throws IOException {
        return FileUtils.getOrCreateFile(GlobalConstants.FileSystemConfigs.CURSOR_STORE_NAME_DEFUALT_VALUE + "/" + cursorId, getQueryDir(queryId).getAbsolutePath(), true);
    }

    private File getPageDir(String queryId, String cursorId) throws IOException {
        return FileUtils.getOrCreateFile(GlobalConstants.FileSystemConfigs.PAGE_STORE_NAME_DEFUALT_VALUE, getCursorDir(queryId, cursorId).getAbsolutePath(), true);
    }

    private File getPageDir(String queryId, String cursorId, String pageId) throws IOException {
        return FileUtils.getOrCreateFile(GlobalConstants.FileSystemConfigs.PAGE_STORE_NAME_DEFUALT_VALUE + "/" + pageId, getCursorDir(queryId, cursorId).getAbsolutePath(), true);
    }


}
