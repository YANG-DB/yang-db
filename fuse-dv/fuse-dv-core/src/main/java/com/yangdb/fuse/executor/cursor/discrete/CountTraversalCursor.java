package com.yangdb.fuse.executor.cursor.discrete;

import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.executor.cursor.TraversalCursorContext;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.results.AssignmentCount;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.transport.cursor.CreateCsvCursorRequest;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.results.AssignmentsQueryResult.Builder.instance;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

public class CountTraversalCursor extends PathsTraversalCursor {
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new CountTraversalCursor((TraversalCursorContext)context);
        }
        //endregion
    }

    public CountTraversalCursor(TraversalCursorContext context) {
        super(context);
    }

    @Override
    public AssignmentsQueryResult getNextResults(int numResults) {
        return super.getNextResults(numResults);
    }

    protected AssignmentsQueryResult toQuery(int numResults) {
        AssignmentsQueryResult.Builder builder = instance();
        final Query pattern = getContext().getQueryResource().getQuery();
        builder.withPattern(pattern);
        Map<String,AtomicLong> labelsCount = new HashMap<>();
        //build assignments
        while (getContext().getTraversal().hasNext()) {
            (getContext().getTraversal().next(numResults)).forEach(path -> {
                Map<String, Long> collect = path.objects().stream().map(e -> (Element) e)
                        .collect(groupingBy(Element::label, Collectors.counting()));

                collect.forEach((key, value) -> {
                    if (labelsCount.containsKey(key))
                        labelsCount.get(key).addAndGet(value);
                    else
                        labelsCount.put(key, new AtomicLong(value));
                });
            });
        }
        builder.withAssignment(new AssignmentCount(labelsCount));
        return builder.build();
    }
}
