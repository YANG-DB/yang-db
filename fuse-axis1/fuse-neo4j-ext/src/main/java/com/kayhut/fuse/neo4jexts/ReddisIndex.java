package com.kayhut.fuse.neo4jexts;

import org.neo4j.collection.primitive.PrimitiveLongSet;
import org.neo4j.kernel.api.exceptions.index.IndexEntryConflictException;
import org.neo4j.kernel.api.index.*;
import org.neo4j.storageengine.api.schema.IndexSample;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by User on 05/03/2017.
 */
public class ReddisIndex extends IndexAccessor.Adapter implements IndexPopulator,IndexUpdater {

    private InternalIndexState state = InternalIndexState.POPULATING;

    /**
     * Remove all data in the index and paves the way for populating an index.
     */
    @Override
    public void create() throws IOException {
        throw new IOException("Trying to create com.kayhut.fuse.neo4jexts.ReddisIndex !!!");
    }

    /**
     * Called when initially populating an index over existing data. Guaranteed to be
     * called by the same thread every time. All data coming in here is guaranteed to not
     * have been added to this index previously, so no checks needs to be performed before applying it.
     * Implementations may verify constraints at this time, or defer them until the first verification
     * of {@link #verifyDeferredConstraints(PropertyAccessor)}.
     *
     * @param updates batch of node property updates that needs to be inserted. Node ids will be retrieved using
     *                {@link NodePropertyUpdate#getNodeId()} method and property values will be retrieved using
     *                {@link NodePropertyUpdate#getValueAfter()} method.
     */
    @Override
    public void add(Collection<NodePropertyUpdate> updates) throws IndexEntryConflictException, IOException {

    }

    /**
     * Verify constraints for all entries added so far.
     *
     * @param accessor
     */
    @Override
    public void verifyDeferredConstraints(PropertyAccessor accessor) throws IndexEntryConflictException, IOException {

    }

    /**
     * Return an updater for applying a set of changes to this index, generally this will be a set of changes from a
     * transaction.
     * <p>
     * Index population goes through the existing data in the graph and feeds relevant data to this populator.
     * Simultaneously as population progresses there might be incoming updates
     * from committing transactions, which needs to be applied as well. This populator will only receive updates
     * for nodes that it already has seen. Updates coming in here must be applied idempotently as the same data
     * may have been {@link #add(Collection) added previously}.
     * Updates can come in two different {@link NodePropertyUpdate#getUpdateMode() modes}.
     * <ol>
     * <li>{@link UpdateMode#ADDED} means that there's an added property to a node already seen by this
     * populator and so needs to be added. Note that this addition needs to be applied idempotently.
     * <li>{@link UpdateMode#CHANGED} means that there's a change to a property for a node already seen by
     * this populator and that this new change needs to be applied. Note that this change needs to be
     * applied idempotently.</li>
     * <li>{@link UpdateMode#REMOVED} means that a property already seen by this populator or even the node itself
     * has been removed and need to be removed from this index as well. Note that this removal needs to be
     * applied idempotently.</li>
     * </ol>
     *
     * @param accessor
     */
    @Override
    public IndexUpdater newPopulatingUpdater(PropertyAccessor accessor) throws IOException {
        return null;
    }

    /**
     * Close this populator and releases any resources related to it.
     * If {@code populationCompletedSuccessfully} is {@code true} then it must mark this index
     * as {@link InternalIndexState#ONLINE} so that future invocations of its parent
     * {@link SchemaIndexProvider#getInitialState(long)} also returns {@link InternalIndexState#ONLINE}.
     *
     * @param populationCompletedSuccessfully
     */
    @Override
    public void close(boolean populationCompletedSuccessfully) throws IOException {

    }

    /**
     * Called then a population failed. The failure string should be stored for future retrieval by
     * {@link SchemaIndexProvider#getPopulationFailure(long)}. Called before {@link #close(boolean)}
     * if there was a failure during population.
     *
     * @param failure the description of the failure.
     * @throws IOException if marking failed.
     */
    @Override
    public void markAsFailed(String failure) throws IOException {

    }

    /**
     * Add the given {@link NodePropertyUpdate update} to the sampler for this index.
     *
     * @param update update to include in sample
     */
    @Override
    public void includeSample(NodePropertyUpdate update) {

    }

    /**
     * Configure specific type of sampling that should be used during index population.
     * Depends from type of node scan that is used during index population
     *
     * @param onlineSampling should online (sampling based on index population and updates) be used
     */
    @Override
    public void configureSampling(boolean onlineSampling) {

    }

    @Override
    public IndexSample sampleResult() {
        return null;
    }

    @Override
    public void process(NodePropertyUpdate update) throws IOException, IndexEntryConflictException {

    }

    @Override
    public void remove(PrimitiveLongSet nodeIds) throws IOException {

    }
}
