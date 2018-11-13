package com.kayhut.fuse.generator.util;

/*-
 * #%L
 * fuse-domain-gragons-datagen
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 15-May-17.
 */
public class CsvUtil {

    private CsvUtil() {
        throw new IllegalAccessError("Utility class");
    }

    public static void writeCSV(String filePath, List<String[]> records,
                                final char separator, final char quoteChar) throws IOException {
        FileWriter fileWriter = new FileWriter(filePath, true);
        try (CSVWriter csvWriter = new CSVWriter(fileWriter, separator, quoteChar)) {
            csvWriter.writeAll(records);
        }
    }

    public static void appendResult(String[] record, String filePath) {
        ArrayList<String[]> records = new ArrayList<>();
        records.add(record);
        try {
            CsvUtil.writeCSV(filePath, records, ',', '\"');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void appendResults(List<String[]> records, String filePath) {
        new File(filePath).getParentFile().mkdirs();

        try {
            CsvUtil.writeCSV(filePath, records, ',', '\"');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String[]> readCSV(String filePath, final char separator) {
        List<String[]> fileContents = new ArrayList<>();
        try {
            File file = new File(filePath);
            if (file.exists()) {
                CSVReader csvReader = new CSVReader(new FileReader(file), separator);
                String[] line;
                while ((line = csvReader.readNext()) != null) {
                    if (line.length > 0) {
                        fileContents.add(line);
                    }
                }
            } else {
                throw new FileNotFoundException(String.format("The specified file %sdoes not exist in %s", file.getName(), file.getAbsolutePath()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileContents;
    }
}
