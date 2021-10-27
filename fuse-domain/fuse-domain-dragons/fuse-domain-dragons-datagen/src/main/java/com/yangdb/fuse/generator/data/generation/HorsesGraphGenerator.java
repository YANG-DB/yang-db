package com.yangdb.fuse.generator.data.generation;

/*-
 * #%L
 * fuse-domain-dragons-datagen
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



import com.yangdb.fuse.generator.configuration.HorseConfiguration;
import com.yangdb.fuse.generator.data.generation.entity.HorseGenerator;
import com.yangdb.fuse.generator.model.entity.EntityBase;
import com.yangdb.fuse.generator.model.entity.Horse;
import com.yangdb.fuse.generator.util.CsvUtil;
import javaslang.collection.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.yangdb.fuse.generator.data.generation.graph.GraphGeneratorBase.BUFFER;

/**
 * Created by benishue on 05/06/2017.
 */
public class HorsesGraphGenerator {

    public static final String[] HORSE_HEADER = {"id", "name", "color", "weight", "maxSpeed", "distance"};
    private final Logger logger = LoggerFactory.getLogger(HorsesGraphGenerator.class);

    public HorsesGraphGenerator(final HorseConfiguration configuration) {
        this.horseConf =configuration;
    }

    public List<String> generateHorsesGraph() {
        List<Horse> horses = generateHorses();
        return Stream.ofAll(horses).map(EntityBase::getId).toJavaList();
    }

    public List<Horse> generateHorses() {
        List<Horse> horses = new ArrayList<>();
        List<String[]> horsesRecords = new ArrayList<>();
        horsesRecords.add(0, HORSE_HEADER);
        try {
            HorseGenerator generator = new HorseGenerator(horseConf);
            int elements = horseConf.getNumberOfNodes();

            for (int i = 0; i < elements; i++) {
                Horse horse = generator.generate();
                horse.setId(Integer.toString(i));
                horses.add(horse);
                horsesRecords.add(horse.getRecord());
                if(elements % BUFFER == 0)
                    logger.info("writing to file ... "+ BUFFER +" elements");
            }
            //Write graph
            CsvUtil.appendResults(horsesRecords, horseConf.getEntitiesFilePath());

        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
        return horses;
    }

    //region Fields
    private final HorseConfiguration horseConf;
    //endregion

}
