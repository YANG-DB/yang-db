package com.yangdb.fuse.dispatcher.utils;

import com.yangdb.fuse.model.Tagged;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.RelPattern;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.*;

import java.util.*;

import static com.yangdb.fuse.model.query.Rel.Direction.R;

public abstract class GraphApiUtils {
    /**
     * generate findPath graph query between two concrete vertices id's
     *
     * @param ontology
     * @param sourceId
     * @param targetId
     * @param relationType
     * @param maxHops
     * @return
     */
    public static Query findPathQuery(String ontology, String sourceEntity, String sourceId, String targetEntity, String targetId, String relationType, int maxHops) {
        return Query.Builder.instance().withName(UUID.randomUUID().toString())
                .withOnt(ontology)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new EConcrete(1, sourceEntity + "_" + sourceId, sourceEntity, sourceId, sourceEntity + "_" + sourceId, 2),
                        new RelPattern(2, relationType, new com.yangdb.fuse.model.Range(1, maxHops), R, null, 3, 0),
                        new TypedEndPattern<>(new EConcrete(3, Tagged.tagSeq(targetEntity), targetEntity, targetId, targetEntity + "_" + targetId, 0))
                )).build();
    }

    /**
     * generate findPath graph query between two concrete vertices id's
     *
     * @param ontology
     * @param sourceId
     * @param relationType
     * @param maxHops
     * @return
     */
    public static Query findPathQuery(String ontology, String sourceEntity, String sourceId, String targetEntity, String relationType, int maxHops) {
        return Query.Builder.instance().withName(UUID.randomUUID().toString())
                .withOnt(ontology)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new EConcrete(1, sourceEntity + "_" + sourceId, sourceEntity, sourceId, sourceEntity + "_" + sourceId, 2),
                        new RelPattern(2, relationType, new com.yangdb.fuse.model.Range(1, maxHops), R, null, 3, 0),
                        new TypedEndPattern<>(new ETyped(3, Tagged.tagSeq(targetEntity), targetEntity, 0))
                )).build();
    }

    /**
     * generate findPath graph query between two concrete vertices id's
     *
     * @param ontology
     * @param relationType
     * @param maxHops
     * @return
     */
    public static Query findPathQuery(String ontology, String sourceEntity, String targetEntity, String relationType, int maxHops) {
        return Query.Builder.instance().withName(UUID.randomUUID().toString())
                .withOnt(ontology)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, sourceEntity, sourceEntity, 2),
                        new RelPattern(2, relationType, new com.yangdb.fuse.model.Range(1, maxHops), R, null, 3, 0),
                        new TypedEndPattern<>(new ETyped(3, Tagged.tagSeq(targetEntity), targetEntity, 0))
                )).build();
    }

    /**
     * generate findPath graph query between two concrete vertices id's
     *
     * @param ontology
     * @param relationType
     * @param maxHops
     * @return
     */
    public static Query findPathQuery(String ontology, String sourceEntity, String relationType, int maxHops) {
        return Query.Builder.instance().withName(UUID.randomUUID().toString())
                .withOnt(ontology)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, sourceEntity, sourceEntity, 2),
                        new RelPattern(2, relationType, new com.yangdb.fuse.model.Range(1, maxHops), R, null, 3, 0),
                        new UnTypedEndPattern<>(new EUntyped(3, Tagged.tagSeq("AnyOf"), -1, 0))
                )).build();
    }

    /**
     * generate findPath graph query between two concrete vertices id's
     *
     * @param ontology
     * @param relationType
     * @param maxHops
     * @return
     */
    public static Query findPathQuery(String ontology, String sourceEntity, String[] targetEntities, String relationType, int maxHops) {
        return Query.Builder.instance().withName(UUID.randomUUID().toString())
                .withOnt(ontology)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, sourceEntity, sourceEntity, 2),
                        new RelPattern(2, relationType, new com.yangdb.fuse.model.Range(1, maxHops), R, null, 3, 0),
                        new UnTypedEndPattern<>(new EUntyped(3, Tagged.tagSeq("AnyOf"), Arrays.asList(targetEntities),Collections.emptyList(),-1, 0))
                )).build();
    }

}
