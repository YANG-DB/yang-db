package com.kayhut.test.scenario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.kayhut.test.etl.ChunkPartitioner;
import com.kayhut.test.etl.DateFieldPartitioner;
import com.kayhut.test.etl.Partitioner;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Roman on 07/06/2017.
 */
public class TimePartitionFile {
    public static void main(String[] args) {
        String filePath = "E:\\fuse_data\\edgesAfterEtl\\dragonsRelations_FIRES-out.csv";
        String destFolder = "E:\\fuse_data\\edgesAfterEtl\\dragonsRelations_chunks";

        new File(destFolder).mkdirs();

        ObjectReader reader = new CsvMapper().reader(
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.id", CsvSchema.ColumnType.STRING)
                        .addColumn("timestamp", CsvSchema.ColumnType.NUMBER)
                        .addColumn("temperature", CsvSchema.ColumnType.NUMBER)
                        .addColumn("entityB.type", CsvSchema.ColumnType.STRING)
                        .addColumn("direction", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.color", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.name", CsvSchema.ColumnType.STRING)
                        .build()
        ).forType(new TypeReference<Map<String, String>>() {
        });

        Partitioner partitioner = new DateFieldPartitioner("timestamp", "%s", "yyyy-MM-dd HH:mm:ss.SSS", "yyyyMM");

        Map<String, List<String>> bufferedPartitions = new HashMap<>();
        int maxBufferedLines = 100000;

        int numLines = 0;
        int totalNumLinesScanned = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<String, String> fire = reader.readValue(line);
                String partitionKey = partitioner.getPartition(fire);

                List<String> bufferedPartition = bufferedPartitions.get(partitionKey);
                if (bufferedPartition == null) {
                    bufferedPartition = new ArrayList<>();
                    bufferedPartitions.put(partitionKey, bufferedPartition);
                }

                bufferedPartition.add(line);
                numLines++;

                if (numLines == maxBufferedLines) {
                    flushBufferedPartitions(bufferedPartitions, filePath, destFolder);
                    totalNumLinesScanned += numLines;
                    numLines = 0;
                    bufferedPartitions.clear();

                    System.out.println("total # lines: " + totalNumLinesScanned);
                }
            }

            flushBufferedPartitions(bufferedPartitions, filePath, destFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void flushBufferedPartitions(Map<String, List<String>> bufferedPartitions, String filePath, String destFolder) throws IOException {
        String fileName = FilenameUtils.removeExtension(FilenameUtils.getName(filePath));
        String fileExtension = FilenameUtils.getExtension(filePath);

        for(Map.Entry<String, List<String>> entry : bufferedPartitions.entrySet()) {
            String partitionFileName = getPartitionFileName(destFolder, fileName, entry.getKey(), fileExtension);
            try (BufferedWriter wr = new BufferedWriter(new FileWriter(partitionFileName, true))) {
                for (String line : entry.getValue()) {
                    wr.write(line + System.lineSeparator());
                }
            }
        }
    }

    private static String getPartitionFileName(String destFolder, String fileName, String partitionKey, String fileExtension) {
        return Paths.get(destFolder, fileName + "." + partitionKey + "." + fileExtension).toString();
    }
}
