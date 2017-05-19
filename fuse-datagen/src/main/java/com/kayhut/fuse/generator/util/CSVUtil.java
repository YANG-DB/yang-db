package com.kayhut.fuse.generator.util;

import au.com.bytecode.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 15-May-17.
 */
public class CSVUtil {

    private CSVUtil() {
        throw new IllegalAccessError("Utility class");
    }

    public static void writeCSV(String filePath,  List<String[]> records,
                                char separator, char quoteChar) throws IOException {
        FileWriter fileWriter = new FileWriter(filePath, true);
        //using custom delimiter and quote character
        try (CSVWriter csvWriter = new CSVWriter(fileWriter, separator, quoteChar)) {
            csvWriter.writeAll(records);
        }
    }

    public static void appendResult(String[] record, String filePath) throws IOException {
        ArrayList<String[]> records = new ArrayList<>();
        records.add(record);
        CSVUtil.writeCSV(filePath, records, ',' ,'\"');
    }
}
