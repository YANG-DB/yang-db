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
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.results.*;
import com.yangdb.fuse.model.transport.cursor.CreateCsvCursorRequest;
import javaslang.collection.Stream;
import org.apache.commons.lang.ArrayUtils;

import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.transport.cursor.CreateCsvCursorRequest.ElementType.Prop;

public class CsvTraversalCursor implements Cursor<TraversalCursorContext> {

    public static final String EID_1 = "eid1";
    public static final String EID_2 = "eid2";
    public static final String R_TYPE = "rType";
    public static final String E_TYPE = "eType";
    public static final String ID = "id";

    //region CursorFactory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new CsvTraversalCursor(context.getQueryResource().getAsgQuery().getProjectedFields(),
                    new PathsTraversalCursor((TraversalCursorContext) context),
                    context.getCursorRequest() != null ? (CreateCsvCursorRequest) context.getCursorRequest() : new CreateCsvCursorRequest());
        }
        //endregion
    }
    //endregion

    //region Constructors
    public CsvTraversalCursor(Map<String, List<AsgEBase<EBase>>> projection, Cursor cursor, CreateCsvCursorRequest csvCursorRequest) {
        this.projection = projection;
        this.cursor = cursor;
        this.csvCursorRequest = csvCursorRequest;
    }
    //endregion

    //region Cursor Implementation
    @Override
    public CsvQueryResult getNextResults(int numResults) {

        CsvQueryResult.Builder builder = CsvQueryResult.Builder.instance();
        AssignmentsQueryResult<Entity, Relationship> newResult = (AssignmentsQueryResult<Entity, Relationship>) this.cursor.getNextResults(numResults);

        if (ArrayUtils.isEmpty(csvCursorRequest.getCsvElements())) {
            populateHeaderColumns(newResult);
        }

        Stream.ofAll(newResult.getAssignments()).forEach(res -> builder.withLine(convertToCsvProjectedSelection(res)));
        if (csvCursorRequest.isWithHeaders()) {
            addHeaders(csvCursorRequest, builder);
        }
        return builder.build();
    }

    private void populateHeaderColumns(AssignmentsQueryResult<Entity, Relationship> newResult) {
        //populate csv element
        CreateCsvCursorRequest.CsvElement[] elements = projection.entrySet().stream()
                .map(e -> new CreateCsvCursorRequest.CsvElement(e.getKey(), e.getValue().get(0).geteBase()))
                .toArray(CreateCsvCursorRequest.CsvElement[]::new);
        //set columns
        csvCursorRequest.setCsvElements(elements);

        if (!newResult.getAssignments().isEmpty()) {
            Assignment<Entity, Relationship> assignment = newResult.getAssignments().get(0);
            List<CreateCsvCursorRequest.CsvElement> rows = new ArrayList<>();
            for (CreateCsvCursorRequest.CsvElement element : elements) {
                String tag = element.getTag1();
                rows.add(element);
                switch (element.getElementType()) {
                    case Entity:
                        assignment.getEntityByTag(tag).ifPresent(e -> rows.addAll(Stream.ofAll(e.getProperties()).sorted(Comparator.comparing(Property::getpType))
                                .map(p->new CreateCsvCursorRequest.CsvElement(element.getTag1(),p.getpType(), Prop)).toJavaList()));
                        break;
                    case Rel:
                        assignment.getRelationByTag(tag).ifPresent(relation -> rows.addAll(Stream.ofAll(relation.getProperties()).sorted(Comparator.comparing(Property::getpType))
                                .map(p->new CreateCsvCursorRequest.CsvElement(element.getTag1(),p.getpType(), Prop)).toJavaList()));
                        break;
                }
            }
            //set columns with updated properties
            csvCursorRequest.setCsvElements(rows.toArray(new CreateCsvCursorRequest.CsvElement[0]));
        }
    }


    @Override
    public TraversalCursorContext getContext() {
        return cursor.getContext();
    }
    //endregion

    //region Private Methods
    private void addHeaders(CreateCsvCursorRequest csvCursorRequest, CsvQueryResult.Builder builder) {
        if (!ArrayUtils.isEmpty(csvCursorRequest.getCsvElements())) {
            String[] header = new String[csvCursorRequest.getCsvElements().length];
            for (int i = 0; i < csvCursorRequest.getCsvElements().length; i++) {
                CreateCsvCursorRequest.CsvElement currentElm = csvCursorRequest.getCsvElements()[i];
                header[i] = currentElm.getTag1() + "." + currentElm.getProperty();
            }
            builder.withHeader(header);
        } else {
            List<String> headers = projection.entrySet().stream()
                    .map(e -> new CreateCsvCursorRequest.CsvElement(e.getKey(), e.getValue().get(0).geteBase()))
                    .map(e -> String.format("%s:%s", e.getTag1(), e.getProperty()))
                    .collect(Collectors.toList());

            builder.withHeader(headers.toArray(new String[0]));
        }
    }

    private String convertToCsv(Assignment<Entity, Relationship> assignment) {
        if (ArrayUtils.isEmpty(csvCursorRequest.getCsvElements())) {
            return convertToCsvNoSelection(assignment);
        }
        StringWriter writer = new StringWriter();

        CSVWriter csvWriter = new CSVWriter(writer, separator, quotechar, "");
        List<String> values = new ArrayList<>();
        for (CreateCsvCursorRequest.CsvElement csvElement : csvCursorRequest.getCsvElements()) {
            String value = convertCsvElement(csvElement, assignment);
            values.add(value);

        }
        csvWriter.writeNext(Stream.ofAll(values).toJavaArray(String.class));
        return writer.getBuffer().toString();
    }

    private String convertCsvElement(CreateCsvCursorRequest.CsvElement csvElement, Assignment<Entity, Relationship> assignment) {
        switch (csvElement.getElementType()) {
            case Entity:
                Optional<Entity> entity = Stream.ofAll(assignment.getEntities()).find(e -> e.geteTag().contains(csvElement.getTag1())).toJavaOptional();
                if (entity.isPresent()) {
                    Optional<Property> property = Stream.ofAll(entity.get().getProperties()).find(p -> p.getpType().equals(csvElement.getProperty())).toJavaOptional();
                    if (property.isPresent()) {
                        return property.get().getValue().toString();
                    } else {
                        switch (csvElement.getProperty()) {
                            case ID:
                                return entity.get().geteID();
                            case E_TYPE:
                                return entity.get().geteType();
                        }
                    }
                }
                break;
            case Rel:

                Optional<Relationship> relationship = Stream.ofAll(assignment.getRelationships())
                        .find(r -> r.geteTag1().equals(csvElement.getTag1()) && r.geteTag2().equals(csvElement.getTag2())).toJavaOptional();
                if (relationship.isPresent()) {
                    Optional<Property> property = Stream.ofAll(relationship.get().getProperties()).find(p -> p.getpType().equals(csvElement.getProperty())).toJavaOptional();
                    if (property.isPresent()) {
                        return property.get().getValue().toString();
                    } else {
                        switch (csvElement.getProperty()) {
                            case EID_1:
                                return relationship.get().geteID1();
                            case EID_2:
                                return relationship.get().geteID2();
                            case R_TYPE:
                                return relationship.get().getrType();
                        }
                    }
                }
                break;
        }

        return null;
    }

    private String convertToCsvNoSelection(Assignment<Entity, Relationship> assignment) {
        Map<String, Entity> entityMap = new HashMap<>();
        StringWriter writer = new StringWriter();

        CSVWriter csvWriter = new CSVWriter(writer, separator, quotechar, "");
        Stream.ofAll(assignment.getEntities()).forEach(e -> Stream.ofAll(e.geteTag()).forEach(t -> entityMap.put(t, e)));

        List<String> sortedTags = Stream.ofAll(entityMap.keySet()).sorted(String::compareTo).toJavaList();
        List<String> values = new ArrayList<>();
        for (String tag : sortedTags) {
            Entity entity = entityMap.get(tag);
            values.add(entity.geteID());
            values.add(entity.geteType());
            convertProperties(entity.getProperties());
        }

        Stream<Relationship> sortedRelations = Stream.ofAll(assignment.getRelationships()).sorted(Comparator.comparing(Relationship::getrID));

        for (Relationship relation : sortedRelations) {
            values.add(relation.getrID());
            values.add(relation.getrType());
            values.add(relation.geteTag1());
            values.add(relation.geteID1());
            values.add(relation.geteTag2());
            values.add(relation.geteID2());
            values.addAll(convertProperties(relation.getProperties()));
        }

        csvWriter.writeNext(Stream.ofAll(values).toJavaArray(String.class));

        return writer.getBuffer().toString();
    }

    private String convertToCsvProjectedSelection(Assignment<Entity, Relationship> assignment) {
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer, separator, quotechar, "");
        List<String> values = new ArrayList<>();
        for (CreateCsvCursorRequest.CsvElement element : csvCursorRequest.getCsvElements()) {
            String tag = element.getTag1();
            switch (element.getElementType()) {
                case Entity:
                    assignment.getEntityByTag(tag).ifPresent(e -> {
                        values.add(e.geteID());
                        values.addAll(convertProperties(e.getProperties()));
                    });
                    break;
                case Rel:
                    assignment.getRelationByTag(tag).ifPresent(relation -> {
                        values.add(relation.getrID());
                        values.addAll(convertProperties(relation.getProperties()));
                    });
                    break;
            }
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

    private Map<String, List<AsgEBase<EBase>>> projection;
    //region Fields
    private Cursor<TraversalCursorContext> cursor;
    private CreateCsvCursorRequest csvCursorRequest;
    private char separator = ',';
    private char quotechar = '"';
    //endregion
}
