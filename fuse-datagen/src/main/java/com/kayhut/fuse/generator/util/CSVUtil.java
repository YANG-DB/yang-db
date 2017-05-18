package com.kayhut.fuse.generator.util;

import au.com.bytecode.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 15-May-17.
 */
public class CSVUtil {

    public static void writeCSV(String filePath,  List<String[]> records, char separator, char quoteChar) throws IOException {
        CSVWriter csvWriter = null;
        try {
            FileWriter fileWriter = new FileWriter(filePath, true);
            //using custom delimiter and quote character
            csvWriter = new CSVWriter(fileWriter, separator, quoteChar);
            csvWriter.writeAll(records);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            csvWriter.close();
        }
    }

    public static void appendResult(String[] record, String filePath) {
        ArrayList<String[]> records = new ArrayList<>();
        records.add(record);
        try {
            CSVUtil.writeCSV(filePath, records, ',' ,'\"');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
