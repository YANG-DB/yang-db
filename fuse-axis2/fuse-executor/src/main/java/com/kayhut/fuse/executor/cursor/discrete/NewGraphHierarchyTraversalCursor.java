package com.kayhut.fuse.executor.cursor.discrete;

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;
import com.kayhut.fuse.model.results.Assignment;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.model.results.QueryResultBase;
import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Element;

import java.util.*;

public class NewGraphHierarchyTraversalCursor implements Cursor {
    //region Constructors
    public NewGraphHierarchyTraversalCursor(TraversalCursorContext context, Iterable<String> countTags) {
        this.countTags = Stream.ofAll(countTags).toJavaSet();
        this.distinctIds = new HashSet<>();
    }
    //endregion

    //region Cursor Implementation
    @Override
    public QueryResultBase getNextResults(int numResults) {
        Map<String, Map<Element, String>> idElementLabelMap = new HashMap<>();

        try {
            while(this.distinctIds.size() < numResults) {
                Path path = context.getTraversal().next();
                List<Object> pathObjects = path.objects();
                List<Set<String>> pathLabels = path.labels();
                for (int objectIndex = 0; objectIndex < path.objects().size(); objectIndex++) {
                    Element element = (Element) pathObjects.get(objectIndex);
                    String pathLabel = pathLabels.get(objectIndex).iterator().next();

                    if (this.countTags.contains(pathLabel)) {
                        this.distinctIds.add(element.id().toString());
                    }

                    Map<Element, String> elementLabelMap = idElementLabelMap.computeIfAbsent(element.id().toString(), id -> new HashMap<>());
                    elementLabelMap.putIfAbsent(element, pathLabel);
                }
            }
        } catch (NoSuchElementException ex) {

        }

        return null;
    }
    //endregion

    //region Fields
    private TraversalCursorContext context;
    private Set<String> countTags;
    private Set<String> distinctIds;
    //endregion
}
