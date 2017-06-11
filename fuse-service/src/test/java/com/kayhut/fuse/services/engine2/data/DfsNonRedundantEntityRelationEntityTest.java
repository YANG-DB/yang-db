package com.kayhut.fuse.services.engine2.data;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.unipop.controller.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.jooby.test.JoobyRule;
import org.junit.ClassRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roman on 22/05/2017.
 */
public class DfsNonRedundantEntityRelationEntityTest extends EntityRelationEntityTest {
    @ClassRule
    public static JoobyRule createApp() {
        return new JoobyRule(new FuseApp(new DefaultAppUrlSupplier("/fuse"), new DefaultAppUrlSupplier("/fuse"))
                .conf("application.engine2.dev.conf", "m1.dfs.non_redundant"));
    }

    @Override
    protected TraversalConstraint getExpectedEdgeTraversalConstraint(
            String relationType,
            Direction direction,
            String relProperty,
            P relPropertyPredicate,
            String entityBId,
            Iterable<String> entityBTypes) {

        List<Traversal> traversals = new ArrayList<>();
        traversals.add(__.has(T.label, relationType));
        traversals.add(__.has(GlobalConstants.HasKeys.DIRECTION, direction));

        if (relProperty != null) {
            traversals.add(__.has(relProperty, relPropertyPredicate));
        }

        return Constraint.by(__.and(Stream.ofAll(traversals).toJavaArray(Traversal.class)));
    }
}
