package com.kayhut.fuse.unipop.controller.utils;

import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.Contains;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.elasticsearch.common.geo.ShapeRelation;
import org.geojson.GeoJsonObject;
import org.unipop.elastic.common.Geo;
import org.unipop.process.predicate.ExistsP;
import org.unipop.process.predicate.Text;

import java.util.Arrays;
import java.util.stream.StreamSupport;

import static org.apache.tinkerpop.gremlin.process.traversal.Contains.within;

/**
 * Created by User on 27/03/2017.
 */
/*public class HasContainersQueryTranslator {
    //region Constructor
    public HasContainersQueryTranslator() {
        this.shouldCache = true;
    }

    public HasContainersQueryTranslator(boolean shouldCache) {
        this.shouldCache = shouldCache;
    }
    //endregion

    //region Public Methods
    public void applyHasContainer(QueryBuilder queryBuilder, HasContainer hasContainer) {
        if (Graph.Hidden.isHidden(hasContainer.getKey())) {
            applyHiddenHasContainer(queryBuilder, hasContainer);
            return;
        }

        if (hasContainer.getPredicate() instanceof ExistsP) {
            queryBuilder.push().exists(hasContainer.getKey()).pop();
        }

        if (hasContainer.getBiPredicate() != null ) {
            if (hasContainer.getBiPredicate() instanceof Compare) {
                Compare compare = (Compare) hasContainer.getBiPredicate();
                switch (compare) {
                    case eq:
                        queryBuilder.push().term(hasContainer.getKey(), hasContainer.getValue()).cache(shouldCache).pop();
                        break;
                    case neq:
                        queryBuilder.bool().mustNot().push().term(hasContainer.getKey(), hasContainer.getValue()).cache(shouldCache).pop();
                        break;
                    case gt:
                        queryBuilder.push().range(hasContainer.getKey(), hasContainer.getKey())
                                .from(hasContainer.getValue())
                                .includeLower(false)
                                .cache(shouldCache)
                                .pop();
                        break;
                    case gte:
                        queryBuilder.push().range(hasContainer.getKey(), hasContainer.getKey())
                                .from(hasContainer.getValue())
                                .includeLower(true)
                                .cache(shouldCache)
                                .pop();
                        break;
                    case lt:
                        queryBuilder.push().range(hasContainer.getKey(), hasContainer.getKey())
                                .to(hasContainer.getValue())
                                .includeUpper(false)
                                .cache(shouldCache)
                                .pop();
                        break;
                    case lte:
                        queryBuilder.push().range(hasContainer.getKey(), hasContainer.getKey())
                                .to(hasContainer.getValue())
                                .includeUpper(true)
                                .cache(shouldCache)
                                .pop();
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("predicate not supported in has step: %s" + hasContainer.toString()));
                }
            } else if (hasContainer.getBiPredicate() instanceof Contains) {
                Contains contains = (Contains) hasContainer.getBiPredicate();
                switch (contains) {
                    case within:
                        if (hasContainer.getValue() != null) {
                            queryBuilder.push().terms(hasContainer.getKey(), hasContainer.getValue()).cache(shouldCache).pop();
                        } else {
                            queryBuilder.push().exists(hasContainer.getKey()).cache(shouldCache).pop();
                        }
                        break;
                    case without:
                        if (hasContainer.getValue() != null) {
                            queryBuilder.push().bool().mustNot().terms(hasContainer.getKey(), hasContainer.getValue()).cache(shouldCache).pop();
                        } else {
                            queryBuilder.push().bool().mustNot().exists(hasContainer.getKey()).cache(shouldCache).pop();
                        }
                        break;

                    default:
                        throw new IllegalArgumentException(String.format("predicate not supported in has step: %s" + hasContainer.toString()));
                }
            } else if (hasContainer.getBiPredicate() instanceof Geo) {
                Geo geo = (Geo) hasContainer.getBiPredicate();
                switch (geo) {
                    case WITHIN:
                        break;

                    case INTERSECTS:
                        if (hasContainer.getValue() instanceof Iterable) {
                            queryBuilder.bool().should();

                            for (GeoJsonObject json : (Iterable<GeoJsonObject>)hasContainer.getValue()) {
                                queryBuilder.push().geoShape(hasContainer.getKey())
                                        .shape(json)
                                        .relation(ShapeRelation.INTERSECTS)
                                        .cache(true).pop();
                            }
                        } else {
                            queryBuilder.push().geoShape(hasContainer.getKey())
                                    .shape((GeoJsonObject)hasContainer.getValue())
                                    .relation(ShapeRelation.INTERSECTS)
                                    .cache(true)
                                    .pop();
                        }

                        break;

                    case DISJOINT:
                        break;
                }
            } else if (hasContainer.getBiPredicate() instanceof Text.TextPredicate) {
                Text.TextPredicate text = (Text.TextPredicate)hasContainer.getBiPredicate();
                switch (text) {
                    case PREFIX:
                        if (hasContainer.getValue() instanceof Iterable) {
                            queryBuilder.bool().should();
                            for (Object prefix : (Iterable) hasContainer.getValue()) {
                                queryBuilder.push().prefix(hasContainer.getKey(), prefix.toString())
                                        .cache(shouldCache)
                                        .pop();
                            }

                        } else {
                            queryBuilder.push().prefix(hasContainer.getKey(), hasContainer.getValue().toString())
                                    .cache(shouldCache)
                                    .pop();
                        }
                        break;
                    case REGEXP:
                        if (hasContainer.getValue() instanceof Iterable) {
                            queryBuilder.push().bool().should();

                            for (Object regexp : (Iterable) hasContainer.getValue()) {
                                queryBuilder.push().regexp(hasContainer.getKey(), hasContainer.getValue().toString())
                                        .cache(shouldCache)
                                        .pop();
                            }
                            queryBuilder.pop();
                        } else {
                            queryBuilder.push().regexp(hasContainer.getKey(), hasContainer.getValue().toString())
                                    .cache(shouldCache)
                                    .pop();
                        }
                }
            }
        }
    }
    //endregion

    //region Private Methods
    private void applyHiddenHasContainer(QueryBuilder queryBuilder, HasContainer hasContainer) {
        String plainKey = Graph.Hidden.unHide(hasContainer.getKey());
        switch (plainKey) {
            case "id":
                if (hasContainer.getBiPredicate() != null) {
                    if (hasContainer.getBiPredicate() instanceof Compare) {
                        Compare compare = (Compare) hasContainer.getBiPredicate();
                        switch (compare) {
                            case eq:
                                queryBuilder.push().term("_id", hasContainer.getValue()).cache(shouldCache).pop();
                                break;

                            case neq:
                                queryBuilder.push().bool().mustNot().term("_id", hasContainer.getValue()).cache(shouldCache).pop();
                                break;

                            default:
                                throw new IllegalArgumentException(String.format("predicate not supported in has step: %s" + hasContainer.toString()));

                        }
                    } else if (hasContainer.getBiPredicate() instanceof Contains) {
                        Contains contains = (Contains) hasContainer.getBiPredicate();
                        switch (contains) {
                            case within:
                                if (hasContainer.getValue() != null) {
                                    queryBuilder.push().terms("_id", hasContainer.getValue()).cache(shouldCache).pop();
                                }
                                break;

                            case without:
                                if (hasContainer.getValue() != null) {
                                    queryBuilder.push().bool().mustNot().terms("_id", hasContainer.getValue()).cache(shouldCache).pop();
                                }

                            default:
                                throw new IllegalArgumentException(String.format("predicate not supported in has step: %s" + hasContainer.toString()));
                        }
                    }
                }
                break;

            case "label":
                if (hasContainer.getBiPredicate() != null) {
                    if (hasContainer.getBiPredicate() instanceof Compare) {
                        Compare compare = (Compare) hasContainer.getBiPredicate();
                        switch (compare) {
                            case eq:
                                queryBuilder.push().term("_type", hasContainer.getValue()).cache(shouldCache).pop();
                                break;

                            case neq:
                                queryBuilder.push().bool().mustNot().term("_type", hasContainer.getValue()).cache(shouldCache).pop();
                                break;

                            default:
                                throw new IllegalArgumentException(String.format("predicate not supported in has step: %s" + hasContainer.toString()));
                        }
                    } else if (hasContainer.getBiPredicate() instanceof Contains) {
                        Contains contains = (Contains) hasContainer.getBiPredicate();
                        switch (contains) {
                            case within:
                                if (hasContainer.getValue() != null) {
                                    queryBuilder.push().terms("_type", hasContainer.getValue()).cache(shouldCache).pop();
                                }
                                break;

                            case without:
                                if (hasContainer.getValue() != null) {
                                    queryBuilder.push().bool().mustNot().terms("_type", hasContainer.getValue()).cache(shouldCache).pop();
                                }
                                break;

                            default:
                                throw new IllegalArgumentException(String.format("predicate not supported in has step: %s" + hasContainer.toString()));
                        }
                    }
                }
                break;
        }
    }
    //endregion

    //region Fields
    private boolean shouldCache;
    //endregion
}
*/