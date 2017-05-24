package com.kayhut.fuse.unipop.controller.search.appender;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.kayhut.fuse.unipop.controller.context.PromiseVertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.CollectionUtil;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalPredicateByKeyProvider;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartition;
import javaslang.collection.Stream;
import javaslang.control.Option;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.Contains;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.util.AndP;
import org.apache.tinkerpop.gremlin.process.traversal.util.OrP;
import org.apache.tinkerpop.gremlin.structure.T;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Elad on 4/26/2017.
 */
public class PromiseEdgeIndexAppender implements SearchAppender<PromiseVertexControllerContext> {

    @Override
    public boolean append(SearchBuilder searchBuilder, PromiseVertexControllerContext context) {

        TraversalValuesByKeyProvider labelsProvider = new TraversalValuesByKeyProvider();

        Set<String> labels = labelsProvider.getValueByKey(context.getEdgeConstraint().get().getTraversal(), T.label.getAccessor());

        for (String label :
                labels) {

            Optional<GraphEdgeSchema> edgeSchema = context.getSchema().getEdgeSchema(label);

            if (edgeSchema.isPresent()) {

                IndexPartition indexPartition = edgeSchema.get().getIndexPartition();

                if (indexPartition instanceof TimeSeriesIndexPartition) {

                    TimeSeriesIndexPartition tsIndexPartition = (TimeSeriesIndexPartition) indexPartition;

                    TraversalPredicateByKeyProvider visitor = new TraversalPredicateByKeyProvider();

                    Set<P> predicates = visitor.getPredicateByKey(context.getEdgeConstraint().get().getTraversal(), tsIndexPartition.getTimeField());

                    if(predicates.size() == 0) {
                        //if there are no constraints, add all indices
                        Stream.ofAll(tsIndexPartition.getIndices()).forEach(index -> searchBuilder.getIndices().add(index));

                    } else {
                        searchBuilder.getIndices().addAll(Stream.ofAll(tsIndexPartition.getIndices()).filter(index -> isIndexRelevant(index, predicates, tsIndexPartition)).toJavaList());
                    }
                } else {
                    searchBuilder.getIndices().addAll(getEdgeSchemasIndices(edgeSchema.get().getIndexPartition()));
                }

            }
        }

        return true;

    }

    private boolean isIndexRelevant(String index, Set<P> predicates, TimeSeriesIndexPartition tsIndexPartition) {

        for (P predicate : predicates) {
            if(predicate instanceof AndP) {
                return Stream.ofAll(((AndP) predicate).getPredicates()).find(p -> !isIndexRelevant(index, Stream.of((P) p).toJavaSet(), tsIndexPartition)).isEmpty();
            } else if (predicate instanceof OrP) {
                return Stream.ofAll(((OrP) predicate).getPredicates()).find(p -> isIndexRelevant(index, Stream.of((P) p).toJavaSet(), tsIndexPartition)).toJavaList().size() > 0;
            } else {
                return testSimplePredicate(predicate, index, tsIndexPartition);
            }
        }

        return false;
    }

    private boolean testSimplePredicate(P predicate, String index, TimeSeriesIndexPartition tsIndexPartition) {

        if (predicate.getBiPredicate().equals(Compare.eq)) {

            return index.equals(getIndexName(predicate.getValue(), tsIndexPartition));

        } else if (predicate.getBiPredicate().equals(Compare.gt) ||
                   predicate.getBiPredicate().equals(Compare.gte)) {

            String indexName = getIndexName(predicate.getValue(), tsIndexPartition);

            // assuming the indexes are returned by order
            List<String> list = Stream.ofAll(tsIndexPartition.getIndices()).toJavaList();
            return  list.indexOf(indexName) <= list.indexOf(index);

        } else if (predicate.getBiPredicate().equals(Compare.lt) ||
                   predicate.getBiPredicate().equals(Compare.lte)) {

            String indexName = getIndexName(predicate.getValue(), tsIndexPartition);

            // assuming the indexes are returned by order
            List<String> list = Stream.ofAll(tsIndexPartition.getIndices()).toJavaList();
            return  list.indexOf(indexName) >= list.indexOf(index);

        } else if (predicate.getBiPredicate().equals(Compare.neq)) {
            //all indices are relevant
            return true;
        } else if (predicate.getBiPredicate().equals(Contains.within)) {
            return Stream.ofAll(CollectionUtil.listFromObjectValue(predicate.getValue())).map(date -> getIndexName(date, tsIndexPartition)).toJavaList().contains(index);
        } else if (predicate.getBiPredicate().equals(Contains.without)) {
            //all indices are relevant ?
            return true;
        } else {
            //default ??
            return true;
        }
    }

    private String getIndexName(Object value, TimeSeriesIndexPartition indexPartition) {

        String formattedDate = new SimpleDateFormat(indexPartition.getDateFormat()).format(value);

        return String.format(indexPartition.getIndexFormat(), formattedDate);
    }

    private List<String> getEdgeSchemasIndices(IndexPartition indexPartition) {
        return Lists.newArrayList(indexPartition.getIndices());
    }
}
