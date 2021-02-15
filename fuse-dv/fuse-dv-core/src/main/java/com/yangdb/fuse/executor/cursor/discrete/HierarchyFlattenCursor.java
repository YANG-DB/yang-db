package com.yangdb.fuse.executor.cursor.discrete;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.opencsv.CSVWriter;
import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.executor.cursor.TraversalCursorContext;
import com.yangdb.fuse.model.results.*;
import com.yangdb.fuse.model.transport.cursor.CreateHierarchyFlattenCursorRequest;
import javaslang.collection.Stream;
import javaslang.control.Option;

import java.io.StringWriter;
import java.util.*;

public class HierarchyFlattenCursor implements Cursor<TraversalCursorContext> {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new HierarchyFlattenCursor(
                    new PathsTraversalCursor((TraversalCursorContext)context),
                    (CreateHierarchyFlattenCursorRequest)context.getCursorRequest());
        }
        //endregion
    }
    //endregion

    //region Constructors
    public HierarchyFlattenCursor(PathsTraversalCursor innerCursor, CreateHierarchyFlattenCursorRequest cursorRequest) {
        this.innerCursor = innerCursor;
    }
    //endregion

    //region Cursor Implementation
    @Override
    public QueryResultBase getNextResults(int numResults) {
        Map<String, Set<String>> childMap = new HashMap<>();
        List<String> roots = new ArrayList<>();
        Set<String> allVertices = new HashSet<>();

        AssignmentsQueryResult<Entity,Relationship> nextResults;
        do{
            nextResults = this.innerCursor.getNextResults(numResults);
            for (Assignment<Entity,Relationship> assignment : nextResults.getAssignments()) {
                Entity child = Stream.ofAll(assignment.getEntities()).find(e -> e.geteTag().contains("Child")).get();
                Option<Entity> parent = Stream.ofAll(assignment.getEntities()).find(e -> e.geteTag().contains("Parent"));

                allVertices.add(child.geteID());
                if(!parent.isEmpty()){
                    Set<String> children = childMap.computeIfAbsent(parent.get().geteID(), p -> new HashSet<>());
                    children.add(child.geteID());
                    allVertices.add(parent.get().geteID());
                }else{
                    roots.add(child.geteID());
                }
            }
        }
        while(nextResults.getSize() > 0);

        Set<String> handledVertices = new HashSet<>();

        List<HierarchyPath> paths = new ArrayList<>();
        Stack<String> nodeStack = new Stack<>();
        for (String root : roots) {
            paths.addAll(visitNode(root, nodeStack, handledVertices, childMap));
        }

        if(handledVertices.size() != allVertices.size()){
            throw new IllegalArgumentException("Hierarchy contains cycle, cannot flatten");
        }

        CsvQueryResult.Builder builder = CsvQueryResult.Builder.instance();

        for (HierarchyPath path : paths) {
            StringWriter writer = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(writer, separator, quotechar,"");
            csvWriter.writeNext(new String[] {path.getParent(), path.getChild(), Integer.toString(path.getDist())});
            builder.withLine(writer.getBuffer().toString());
        }
        return builder.build();
    }
    //endregion

    //region Private Methods
    private Collection<? extends HierarchyPath> visitNode(String node, Stack<String> nodeStack, Set<String> handledVertices,  Map<String, Set<String>> childMap) {
        List<HierarchyPath> paths = new ArrayList<>();

        if(!handledVertices.contains(node)){
            paths.add(new HierarchyPath(node, node, 0));
        }

        handledVertices.add(node);

        for(int i = 0; i < nodeStack.size(); i++){
            String parent = nodeStack.get(i);
            int dist = nodeStack.size() - i;
            paths.add(new HierarchyPath(parent, node, dist));
            paths.add(new HierarchyPath(node, parent, -1 * dist));
        }

        nodeStack.push(node);

        Set<String> stackSet = new HashSet<>(nodeStack);
        if(stackSet.size() < nodeStack.size()){
            throw new IllegalArgumentException("Hierarchy contains cycle, cannot flatten");
        }

        for (String child : childMap.getOrDefault(node, new HashSet<>())){
            paths.addAll(visitNode(child, nodeStack, handledVertices, childMap));

        };

        nodeStack.pop();


        return paths;
    }

    @Override
    public TraversalCursorContext getContext() {
        return innerCursor.getContext();
    }

    //endregion

    //region Fields
    private PathsTraversalCursor innerCursor;
    private char separator = ',';
    private char quotechar = '"';
    //endregion

    //region HierarchyPath
    private class HierarchyPath{
        private String parent;
        private String child;
        private int dist;

        public HierarchyPath(String parent, String child, int dist) {
            this.parent = parent;
            this.child = child;
            this.dist = dist;
        }

        public String getParent() {
            return parent;
        }

        public void setParent(String parent) {
            this.parent = parent;
        }

        public String getChild() {
            return child;
        }

        public void setChild(String child) {
            this.child = child;
        }

        public int getDist() {
            return dist;
        }

        public void setDist(int dist) {
            this.dist = dist;
        }
    }
    //endregion
}
