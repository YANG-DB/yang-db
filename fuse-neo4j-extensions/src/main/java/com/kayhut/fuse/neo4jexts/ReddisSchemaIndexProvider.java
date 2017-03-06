package com.kayhut.fuse.neo4jexts;

import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.kernel.api.index.*;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.api.index.sampling.IndexSamplingConfig;
import org.neo4j.kernel.impl.api.scan.LabelScanStoreProvider;
import org.neo4j.kernel.impl.storemigration.StoreMigrationParticipant;

import java.io.IOException;

/**
 * Created by User on 05/03/2017.
 */
public class ReddisSchemaIndexProvider extends SchemaIndexProvider {

    static int PRIORITY;
    static {
        PRIORITY = 2;
    }

    public ReddisSchemaIndexProvider(final Config config) {
        super(ReddisIndexProviderFactory.PROVIDER_DESCRIPTOR, PRIORITY);
    }

    /**
     * Used for initially populating a created index, using batch insertion.
     *
     * @param indexId
     * @param descriptor
     * @param config
     * @param samplingConfig
     */
    @Override
    public IndexPopulator getPopulator(long indexId, IndexDescriptor descriptor, IndexConfiguration config, IndexSamplingConfig samplingConfig) {
        return null;
    }

    /**
     * Used for updating an index once initial population has completed.
     *
     * @param indexId
     * @param config
     * @param samplingConfig
     */
    @Override
    public IndexAccessor getOnlineAccessor(long indexId, IndexConfiguration config, IndexSamplingConfig samplingConfig) throws IOException {
        return null;
    }

    /**
     * Returns a failure previously gotten from {@link IndexPopulator#markAsFailed(String)}
     * <p>
     * Implementations are expected to persist this failure
     *
     * @param indexId
     */
    @Override
    public String getPopulationFailure(long indexId) throws IllegalStateException {
        return null;
    }

    /**
     * Called during startup to find out which state an index is in. If {@link InternalIndexState#FAILED}
     * is returned then a further call to {@link #getPopulationFailure(long)} is expected and should return
     * the failure accepted by any call to {@link IndexPopulator#markAsFailed(String)} call at the time
     * of failure.
     *
     * @param indexId
     */
    @Override
    public InternalIndexState getInitialState(long indexId) {
        return null;
    }

    @Override
    public StoreMigrationParticipant storeMigrationParticipant(FileSystemAbstraction fs, PageCache pageCache, LabelScanStoreProvider labelScanStoreProvider) {
        return null;
    }
}
