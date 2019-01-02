package com.kayhut.fuse.assembly.knowledge.cdr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kayhut.fuse.assembly.knowledge.domain.*;
import com.opencsv.CSVReader;
import org.elasticsearch.client.Client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.kayhut.fuse.assembly.knowledge.domain.RelationBuilder.*;
import static com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder.*;

/**
 * load cdr file into the DB
 */
public abstract class DataLoader {
    /**
     * load CDR data csv
     * Phone A, Phone B, Type , Date Time, Duration (Sec),LAC ,CID,? , Lat ,Long
     *
     * Relation
     * (a:Phone)-[:Type]->(b:Phone)
     * (a:Phone)-[:Location]->(l:Location)
     *
     * Vertices
     * -Phone
     *     id: String
     *
     * - Location
     *     lat: float
     *     long: float
     *
     * Edges
     *  - Tagged
     *       time: DateTime
     *  - Type
     *       name: String
     *       time: DateTime
     *       duration: long
     *
     *
     *
     * @param client
     * @param ctx
     * @param file
     * @return
     */

    public static long load(Client client, KnowledgeWriterContext ctx, String file) throws JsonProcessingException {
        List<String[]> strings = readCSV(file, ',');

        strings.forEach(line -> {
            //phone1
            final EntityBuilder e1 = ctx.e().cat("phone").ctx("cdr");
            ValueBuilder v0 = ctx.v().field("phone").value(line[0]).bdt("phone");
            e1.value(v0);

            //phone2
            final EntityBuilder e2 = ctx.e().cat("phone").ctx("cdr");
            ValueBuilder v1 = ctx.v().field("phone").value(line[1]).bdt("phone");
            e2.value(v1);

            //phone1->phone2 [type]
            final RelationBuilder rel = ctx.rel().cat("type").ctx("cdr");
            RvalueBuilder v2 = ctx.r().field("name").value(line[2]).ctx("cdr").bdt("type");
            //cast to date
            RvalueBuilder v3 = ctx.r().field("time").value(line[3]).ctx("cdr").bdt("time");
            //cast to long
            RvalueBuilder v4 = ctx.r().field("duration").value(line[4]).ctx("cdr").bdt("time");
            rel.value(v2);
            rel.value(v3);
            rel.value(v4);

            //bind
            rel.sideA(e1).sideB(e2);
            e1.rel(rel,"out");

            final EntityBuilder e4 = ctx.e().cat("location").ctx("cdr");
            //cast to float
            ValueBuilder v8 = ctx.v().field("lat").value(line[7]).ctx("cdr").bdt("geo");
            //cast to float
            ValueBuilder v9 = ctx.v().field("long").value(line[8]).ctx("cdr").bdt("geo");
            e4.value(v8);
            e4.value(v9);


            //phone1->location [type]
            final RelationBuilder location = ctx.rel().cat("location").ctx("cdr");
            RvalueBuilder loc_v3 = ctx.r().field("time").value(line[3]).ctx("cdr").bdt("time");
            //cast to date
            location.value(loc_v3);
            //bind
            location.sideA(e1).sideB(e4);
            e1.rel(location,"out");
        });

        int counter = 0;
        counter += commit(ctx, REL_INDEX, ctx.getRelations());
        counter += commit(ctx, REL_INDEX, ctx.getrValues());
        counter += commit(ctx, INDEX, ctx.getEntities());
        counter += commit(ctx, INDEX, ctx.geteValues());
        return counter;
    }

    public static List<String[]> readCSV(String filePath, final char separator) {
        List<String[]> fileContents = new ArrayList<>();
        try {
            File file = new File(Thread.currentThread().getContextClassLoader().getResource(filePath).getFile());
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