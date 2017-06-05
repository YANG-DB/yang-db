package com.kayhut.test.etl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by moti on 6/5/2017.
 */
public class FileTransformer {
    private String inputFile;
    private String outputFile;
    private Transformer transformer;
    private List<String> fieldNames;

    public FileTransformer(String inputFile, String outputFile, Transformer transformer, List<String> fieldNames) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.transformer = transformer;
        this.fieldNames = fieldNames;
    }

    public void transform() throws IOException {
        CsvSchema.Builder builder = CsvSchema.builder();
        fieldNames.forEach(f -> builder.addColumn(f));
        CsvSchema schema = builder.build();

        CsvSchema newSchema = transformer.getNewSchema(schema);
        ObjectMapper mapper = new CsvMapper();

        ObjectWriter writer = mapper.writerFor(new TypeReference<Map<String, Object>>() {
        }).with(newSchema).without(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        MappingIterator<Map<String, String>> mappingIterator = mapper.readerFor(new TypeReference<Map<String,Object>>() {}).with(schema).readValues(new File(inputFile));
        try(OutputStream out = new FileOutputStream(outputFile)) {
            while (mappingIterator.hasNext()) {
                Map<String, String> document = mappingIterator.next();
                List<Map<String, String>> transformed = transformer.transform(document);
                for (Map<String, String> doc : transformed) {
                    writer.writeValue(out, doc);
                }
            }
        }
    }

    public static void main(String args[]) throws IOException {
        FileTransformer transformer = new FileTransformer("C:\\dataout\\fires-1900.csv",
                "C:\\dataout\\fires-1900-dup.csv",
                new DuplicateEdgeTransformer("id1","id2"),
                Arrays.asList("id", "id1","id2", "startDate","duration"));
        transformer.transform();
    }

}
