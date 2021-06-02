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

import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.executor.cursor.TraversalCursorContext;
import com.yangdb.fuse.model.results.*;
import com.yangdb.fuse.model.transport.cursor.CreateCsvCursorRequest;
import com.opencsv.CSVWriter;
import javaslang.collection.Stream;
import org.apache.commons.lang.ArrayUtils;

import java.io.StringWriter;
import java.util.*;

public class CsvTraversalCursor implements Cursor<TraversalCursorContext> {
    //region CursorFactory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new CsvTraversalCursor(
                    new PathsTraversalCursor((TraversalCursorContext)context),
                     context.getCursorRequest()!=null ? (CreateCsvCursorRequest) context.getCursorRequest() : new CreateCsvCursorRequest());
        }
        //endregion
    }
    //endregion

    //region Constructors
    public CsvTraversalCursor(Cursor cursor, CreateCsvCursorRequest csvCursorRequest) {
        this.cursor = cursor;
        this.csvCursorRequest = csvCursorRequest;
    }
    //endregion

    //region Cursor Implementation
    @Override
    public CsvQueryResult getNextResults(int numResults) {

        CsvQueryResult.Builder builder = CsvQueryResult.Builder.instance();
        AssignmentsQueryResult<Entity,Relationship> newResult = (AssignmentsQueryResult<Entity,Relationship>) this.cursor.getNextResults(numResults);

        Stream.ofAll(newResult.getAssignments()).forEach(res -> builder.withLine(convertToCsv(res)));
        if(csvCursorRequest.isWithHeaders()){
            addHeaders(csvCursorRequest, builder);
        }
        return builder.build();
    }

    @Override
    public TraversalCursorContext getContext() {
        return cursor.getContext();
    }
    //endregion

    //region Private Methods
    private void addHeaders(CreateCsvCursorRequest csvCursorRequest, CsvQueryResult.Builder builder) {
        if(!ArrayUtils.isEmpty(csvCursorRequest.getCsvElements())){
            String[] header = new String[csvCursorRequest.getCsvElements().length];
            for (int i = 0; i < csvCursorRequest.getCsvElements().length; i++) {
                CreateCsvCursorRequest.CsvElement currentElm = csvCursorRequest.getCsvElements()[i];
                if(currentElm.getElementType() == CreateCsvCursorRequest.ElementType.Entity){
                    header[i] = currentElm.getTag1() + "." + currentElm.getProperty();
                }else{
                    header[i] = currentElm.getTag1() + "-" + currentElm.getTag2() + "." + currentElm.getProperty();
                }
            }
            builder.withHeader(header);
        }
    }

    private String convertToCsv(Assignment<Entity,Relationship> assignment){
        if(ArrayUtils.isEmpty(csvCursorRequest.getCsvElements())){
            return convertToCsvNoSelection(assignment);
        }
        StringWriter writer = new StringWriter();

        CSVWriter csvWriter = new CSVWriter(writer, separator, quotechar,"");
        List<String> values = new ArrayList<>();
        for (CreateCsvCursorRequest.CsvElement csvElement : csvCursorRequest.getCsvElements()) {
            String value = convertCsvElement(csvElement, assignment);
            values.add(value);

        }
        csvWriter.writeNext(Stream.ofAll(values).toJavaArray(String.class));
        return writer.getBuffer().toString();
    }

    private String convertCsvElement(CreateCsvCursorRequest.CsvElement csvElement, Assignment<Entity,Relationship> assignment) {
        switch (csvElement.getElementType()) {
            case Entity:
                Optional<Entity> entity = Stream.ofAll(assignment.getEntities()).find(e -> e.geteTag().contains(csvElement.getTag1())).toJavaOptional();
                if(entity.isPresent()){
                    Optional<Property> property = Stream.ofAll(entity.get().getProperties()).find(p -> p.getpType().equals(csvElement.getProperty())).toJavaOptional();
                    if(property.isPresent()){
                        return property.get().getValue().toString();
                    }else{
                        switch (csvElement.getProperty()){
                            case "id":
                                return entity.get().geteID();
                            case "eType":
                                return entity.get().geteType();
                        }
                    }
                }
                break;
            case Rel:

                Optional<Relationship> relationship = Stream.ofAll(assignment.getRelationships()).find(r -> r.geteTag1().equals(csvElement.getTag1()) && r.geteTag2().equals(csvElement.getTag2())).toJavaOptional();
                if(relationship.isPresent()){
                    Optional<Property> property = Stream.ofAll(relationship.get().getProperties()).find(p -> p.getpType().equals(csvElement.getProperty())).toJavaOptional();
                    if(property.isPresent()){
                        return property.get().getValue().toString();
                    }else{
                        switch (csvElement.getProperty()){
                            case "eid1":
                                return relationship.get().geteID1();
                            case "eid2":
                                return relationship.get().geteID2();
                            case "rType":
                                return relationship.get().getrType();
                        }
                    }
                }
                break;
        }

        return null;
    }

    private String convertToCsvNoSelection(Assignment<Entity,Relationship> assignment) {
        Map<String, Entity> entityMap = new HashMap<>();
        StringWriter writer = new StringWriter();

        CSVWriter csvWriter = new CSVWriter(writer, separator, quotechar,"");
        Stream.ofAll(assignment.getEntities()).forEach(e -> Stream.ofAll(e.geteTag()).forEach(t -> entityMap.put(t, e)));

        List<String> sortedTags = Stream.ofAll(entityMap.keySet()).sorted(String::compareTo).toJavaList();
        List<String> values = new ArrayList<>();
        for (String tag : sortedTags) {
            Entity entity = entityMap.get(tag);
            values.add(entity.geteID());
            values.add(entity.geteType());
            convertProperties(entity.getProperties());
        }

        Stream<Relationship> sortedRelations = Stream.ofAll(assignment.getRelationships()).sorted((o1, o2) -> {
            int compareRes = o1.geteTag1().compareTo(o2.geteTag1());
            if (compareRes == 0) {
                return o1.geteTag2().compareTo(o2.geteTag2());
            }
            return compareRes;
        });

        for (Relationship relation : sortedRelations) {
            values.add(relation.getrID());
            values.add(relation.getrType());
            values.add(relation.geteTag1());
            values.add(relation.geteID1());
            values.add(relation.geteTag2());
            values.add(relation.geteID2());
            values.addAll(convertProperties( relation.getProperties()));
        }

        csvWriter.writeNext(Stream.ofAll(values).toJavaArray(String.class));

        return writer.getBuffer().toString();
    }

    private List<String> convertProperties(Collection<Property> properties2) {
        List<String> values = new ArrayList<>();
        List<Property> sortedProperties = Stream.ofAll(properties2).sorted(Comparator.comparing(Property::getpType)).toJavaList();
        for (Property property : sortedProperties) {
            values.add(property.getValue().toString());
        }
        return values;
    }
    //endregion

    //region Fields
    private Cursor<TraversalCursorContext> cursor;
    private CreateCsvCursorRequest csvCursorRequest;
    private char separator = ',';
    private char quotechar = '"';
    //endregion
}
