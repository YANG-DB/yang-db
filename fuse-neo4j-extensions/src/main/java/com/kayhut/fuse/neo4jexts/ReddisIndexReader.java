package com.kayhut.fuse.neo4jexts;

import org.neo4j.collection.primitive.PrimitiveLongIterator;
import org.neo4j.storageengine.api.schema.IndexReader;
import org.neo4j.storageengine.api.schema.IndexSampler;

/**
 * Created by User on 05/03/2017.
 */
public class ReddisIndexReader implements IndexReader {
    /**
     * Searches this index for a certain value.
     *
     * @param value property value to search for.
     * @return ids of matching nodes.
     */
    @Override
    public PrimitiveLongIterator seek(Object value) {
        return null;
    }

    /**
     * Searches this index for numerics values between {@code lower} and {@code upper}.
     *
     * @param lower lower numeric bound of search (inclusive).
     * @param upper upper numeric bound of search (inclusive).
     * @return ids of matching nodes.
     */
    @Override
    public PrimitiveLongIterator rangeSeekByNumberInclusive(Number lower, Number upper) {
        return null;
    }

    /**
     * Searches this index for string values between {@code lower} and {@code upper}.
     *
     * @param lower        lower numeric bound of search.
     * @param includeLower whether or not lower bound is inclusive.
     * @param upper        upper numeric bound of search.
     * @param includeUpper whether or not upper bound is inclusive.
     * @return ids of matching nodes.
     */
    @Override
    public PrimitiveLongIterator rangeSeekByString(String lower, boolean includeLower, String upper, boolean includeUpper) {
        return null;
    }

    /**
     * Searches this index for string values starting with {@code prefix}.
     *
     * @param prefix prefix that matching strings must start with.
     * @return ids of matching nodes.
     */
    @Override
    public PrimitiveLongIterator rangeSeekByPrefix(String prefix) {
        return null;
    }

    /**
     * Scans this index returning all nodes.
     *
     * @return node ids in index.
     */
    @Override
    public PrimitiveLongIterator scan() {
        return null;
    }

    /**
     * Searches this index for string values containing the exact search string.
     *
     * @param exactTerm the exact string to search for in the index
     * @return ids of matching nodes.
     */
    @Override
    public PrimitiveLongIterator containsString(String exactTerm) {
        return null;
    }

    /**
     * Searches this index for string values ending with the suffix search string.
     *
     * @param suffix the string to search for in the index
     * @return ids of matching nodes.
     */
    @Override
    public PrimitiveLongIterator endsWith(String suffix) {
        return null;
    }

    /**
     * @param nodeId        node if to match.
     * @param propertyValue property value to match.
     * @return number of index entries for the given {@code nodeId} and {@code propertyValue}.
     */
    @Override
    public long countIndexedNodes(long nodeId, Object propertyValue) {
        return 0;
    }

    @Override
    public IndexSampler createSampler() {
        return null;
    }

    @Override
    public void close() {

    }
}
