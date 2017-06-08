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

import static com.kayhut.test.scenario.ETLUtils.splitFileToChunks;

/**
 * Created by Roman on 07/06/2017.
 */
public class TimePartitionFile {
    public static void mainDragons(String[] args) {
        splitFileToChunks("C:\\demo_data_6June2017\\personsRelations_OWNS_DRAGON-out.csv", "C:\\demo_data_6June2017\\own_dragons_chunks",
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.id", CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn("endDate", CsvSchema.ColumnType.STRING)
                        .addColumn("direction", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.type", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.type", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.name", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.name", CsvSchema.ColumnType.STRING)
                        .build(),
                "startDate");
    }

    public static void mainOwnHorses(String[] args) {
        splitFileToChunks("C:\\demo_data_6June2017\\personsRelations_OWNS_HORSE-out.csv", "C:\\demo_data_6June2017\\own_horses_chunks",
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.id", CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn("endDate", CsvSchema.ColumnType.STRING)
                        .addColumn("direction", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.type", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.type", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.name", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.name", CsvSchema.ColumnType.STRING)
                        .build(),
                "startDate");
    }

    public static void mainFreeze(String[] args) {
        splitFileToChunks("E:\\fuse_data\\edges\\dragonsRelations_FREEZES-out.csv", "E:\\fuse_data\\edges\\dragonsRelations_FREEZES_chunks",
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.id", CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn("endDate", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.type", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.type", CsvSchema.ColumnType.STRING)
                        .addColumn("direction", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.name", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.name", CsvSchema.ColumnType.STRING)
                        .build(),
                "startDate");
    }

    public static void mainMemberOf(String[] args) {
        splitFileToChunks("C:\\demo_data_6June2017\\guildsRelations_MEMBER_OF_GUILD-out.csv", "C:\\demo_data_6June2017\\member_chunks",
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.id", CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn("endDate", CsvSchema.ColumnType.STRING)
                        .addColumn("direction", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.type", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.type", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.firstName", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.name", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.name", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.firstName", CsvSchema.ColumnType.STRING)
                        .build(),
                "startDate");
    }

    public static void mainKnows(String[] args) {
        splitFileToChunks("C:\\demo_data_6June2017\\personsRelations_KNOWS-out.csv", "C:\\demo_data_6June2017\\knows_chunks",
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.id", CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn("direction", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.type", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.type", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.firstName", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.firstName", CsvSchema.ColumnType.STRING)
                        .build(),
                "startDate");
    }

    public static void mainOriginatedDragon(String[] args) {
        splitFileToChunks("C:\\demo_data_6June2017\\kingdomsRelations_ORIGINATED_DRAGON-out.csv", "C:\\demo_data_6June2017\\dragon_originated_chunks",
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.id", CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn("direction", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.type", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.type", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.name", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.name", CsvSchema.ColumnType.STRING)
                        .build(),
                "startDate");

    }

    public static void mainOriginatedHorse(String[] args) {
        splitFileToChunks("C:\\demo_data_6June2017\\kingdomsRelations_ORIGINATED_HORSE-out.csv", "C:\\demo_data_6June2017\\horse_originated_chunks",
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.id", CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn("direction", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.type", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.type", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.name", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.name", CsvSchema.ColumnType.STRING)
                        .build(),
                "startDate");
    }


    public static void mainSubjectOf(String[] args) {
        splitFileToChunks("C:\\demo_data_6June2017\\kingdomsRelations_SUBJECT_OF_PERSON-out.csv", "C:\\demo_data_6June2017\\subject_chunks",
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.id", CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn("direction", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.type", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.type", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.firstName", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.firstName", CsvSchema.ColumnType.STRING)
                        .build(),
                "startDate");
    }

    public static void mainRegisteredIn(String[] args) {
        splitFileToChunks("C:\\demo_data_6June2017\\kingdomsRelations_REGISTERED_GUILD-out.csv", "C:\\demo_data_6June2017\\registered_chunks",
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.id", CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn("direction", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.type", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.type", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.name", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.name", CsvSchema.ColumnType.STRING)
                        .build(),
                "startDate");
    }
    public static void main(String[] args) {
        mainRegisteredIn(args);
    }
}
