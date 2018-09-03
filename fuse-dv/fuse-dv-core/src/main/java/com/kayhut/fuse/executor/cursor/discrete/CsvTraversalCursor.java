package com.kayhut.fuse.executor.cursor.discrete;

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;
import com.kayhut.fuse.model.results.*;
import com.kayhut.fuse.model.transport.cursor.CreateCsvCursorRequest;
import com.opencsv.CSVWriter;
import javaslang.collection.Stream;

import java.io.StringWriter;
import java.util.*;

public class CsvTraversalCursor implements Cursor {
    //region CursorFactory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new CsvTraversalCursor(
                    new PathsTraversalCursor((TraversalCursorContext)context),
                    (CreateCsvCursorRequest) context.getCursorRequest());
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
        AssignmentsQueryResult newResult = (AssignmentsQueryResult) this.cursor.getNextResults(numResults);

        Stream.ofAll(newResult.getAssignments()).forEach(res -> builder.withLine(convertToCsv(res)));
        if(csvCursorRequest.isWithHeaders()){
            addHeaders(csvCursorRequest, builder);
        }
        return builder.build();
    }
    //endregion

    //region Private Methods
    private void addHeaders(CreateCsvCursorRequest csvCursorRequest, CsvQueryResult.Builder builder) {
        if(csvCursorRequest.getCsvElements().length > 0){
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

    private String convertToCsv(Assignment assignment){
        if(csvCursorRequest.getCsvElements().length == 0){
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

    private String convertCsvElement(CreateCsvCursorRequest.CsvElement csvElement, Assignment assignment) {
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

    private String convertToCsvNoSelection(Assignment assignment) {
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
    private Cursor cursor;
    private CreateCsvCursorRequest csvCursorRequest;
    private char separator = ',';
    private char quotechar = '"';
    //endregion
}
