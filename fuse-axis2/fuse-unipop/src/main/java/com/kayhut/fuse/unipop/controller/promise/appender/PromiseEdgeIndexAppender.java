package com.kayhut.fuse.unipop.controller.promise.appender;

import com.google.common.collect.Lists;
import com.kayhut.fuse.unipop.controller.common.appender.SearchAppender;
import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.CollectionUtil;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalPredicateByKeyProvider;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.Contains;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.util.AndP;
import org.apache.tinkerpop.gremlin.process.traversal.util.OrP;
import org.apache.tinkerpop.gremlin.structure.T;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Elad on 4/26/2017.
 */
public class PromiseEdgeIndexAppender implements SearchAppender<VertexControllerContext> {

    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {

        TraversalValuesByKeyProvider labelsProvider = new TraversalValuesByKeyProvider();

        Set<String> labels = labelsProvider.getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor());

        for (String label :
                labels) {

            Optional<GraphEdgeSchema> edgeSchema = context.getSchemaProvider().getEdgeSchema(label);

            if (edgeSchema.isPresent()) {

                IndexPartitions indexPartitions = edgeSchema.get().getIndexPartitions().get();

                if (indexPartitions instanceof TimeSeriesIndexPartitions) {

                    TimeSeriesIndexPartitions tsIndexPartition = (TimeSeriesIndexPartitions) indexPartitions;

                    TraversalPredicateByKeyProvider visitor = new TraversalPredicateByKeyProvider();

                    Set<P> predicates = visitor.getPredicateByKey(context.getConstraint().get().getTraversal(), tsIndexPartition.getTimeField());

                    if(predicates.size() == 0) {
                        //if there are no constraints, add all indices
                        searchBuilder.getIndices().addAll(Stream.ofAll(indexPartitions.partitions()).flatMap(IndexPartitions.Partition::indices).toJavaSet());

                    } else {
                        //ass only indices satisfying the constraints
                        searchBuilder.getIndices().addAll(Stream.ofAll(indexPartitions.partitions())
                            .flatMap(IndexPartitions.Partition::indices)
                                .filter(index -> isIndexRelevant(index, predicates, tsIndexPartition))
                                .toJavaSet());
                    }
                } else {
                    //index partition is static, add all indices
                    searchBuilder.getIndices().addAll(Stream.ofAll(indexPartitions.partitions()).flatMap(IndexPartitions.Partition::indices).toJavaSet());
                }

            }
        }

        return true;

    }

    private boolean isIndexRelevant(String index, Set<P> predicates, TimeSeriesIndexPartitions tsIndexPartition) {

        for (P predicate : predicates) {
            if(predicate instanceof AndP) {
                return Stream.ofAll(((AndP) predicate).getPredicates()).find(p -> !isIndexRelevant(index, Stream.of((P) p).toJavaSet(), tsIndexPartition)).isEmpty();
            } else if (predicate instanceof OrP) {
                return Stream.ofAll(((OrP) predicate).getPredicates()).find(p -> isIndexRelevant(index, Stream.of((P) p).toJavaSet(), tsIndexPartition)).toJavaList().size() > 0;
            } else {
                return testSimplePredicate(predicate, index, tsIndexPartition);
            }
        }

        return true;
    }

    private boolean testSimplePredicate(P predicate, String index, TimeSeriesIndexPartitions tsIndexPartition) {

        if (predicate.getBiPredicate().equals(Compare.eq)) {

            return index.equals(getIndexName(predicate.getValue(), tsIndexPartition));

        } else if (predicate.getBiPredicate().equals(Compare.gt) ||
                   predicate.getBiPredicate().equals(Compare.gte)) {

            String indexName = getIndexName(predicate.getValue(), tsIndexPartition);

            // assuming the indexes are returned by order
            List<String> list = Stream.ofAll(tsIndexPartition.partitions()).flatMap(IndexPartitions.Partition::indices).toJavaList();

            return  list.indexOf(indexName) <= list.indexOf(index);

        } else if (predicate.getBiPredicate().equals(Compare.lt) ||
                   predicate.getBiPredicate().equals(Compare.lte)) {

            String indexName = getIndexName(predicate.getValue(), tsIndexPartition);

            // assuming the indexes are returned by order
            List<String> list = Stream.ofAll(tsIndexPartition.partitions()).flatMap(IndexPartitions.Partition::indices).toJavaList();
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

    private String getIndexName(Object value, TimeSeriesIndexPartitions indexPartition) {

        String formattedDate = new SimpleDateFormat(indexPartition.getDateFormat()).format(value);

        return String.format(indexPartition.getIndexFormat(), formattedDate);
    }

}
