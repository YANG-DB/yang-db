package com.yangdb.test.scenario;

/*-
 * #%L
 * fuse-domain-dragons-test
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

import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.yangdb.fuse.model.GlobalConstants;

/**
 * Created by moti on 06/04/2017.
 */
public class DragonScenarioConversionConstants {
    public static String SIDE_A_ID = GlobalConstants.EdgeSchema.SOURCE_ID;
    public static String SIDE_B_ID = GlobalConstants.EdgeSchema.DEST_ID;
    public static String DIRECTION_COLUMN = "dir";
    public static String OUT_DIRECTION = "out";
    public static String IN_DIRECTION = "in";


    public static String DRAGONS_FILE = "Dragons.csv";
    public static String PERSON_FILE = "People.csv";
    public static String HORSES_FILE = "Horses.csv";
    public static String KINGDOM_FILE = "Kingdom.csv";
    public static String GUILD_FILE = "Guild.csv";
    public static String PERSON_OWNS_HORSE_FILE = "OwnsHorses.csv";
    public static String PERSON_OWNS_DRAGON_FILE = "OwnsDragons.csv";
    public static String DRAGON_FIRES_AT_FILE = "FiresAt.csv";
    public static String DRAGON_FREEZES_FILE = "Freezes.csv";
    public static String PERSON_OFFSPRING_FILE = "Offspring.csv";
    public static String PERSON_KNOWS_FILE = "KnowsPerson.csv";
    public static String PERSON_MEMBER_OF_FILE = "MemberOfGuild.csv";
    public static String PERSON_SUBJECT_OF_FILE = "PersonSubjectOf.csv";
    public static String GUILD_REGISTERED_IN_FILE = "GuildRegisteredIn.csv";
    public static String HORSE_ORIGINATED_IN_FILE = "HorseOriginatedIn.csv";
    public static String DRAGON_ORIGINATED_IN_FILE = "DragonOriginatedIn.csv";


    public static CsvSchema DRAGON_FILE_SCHEMA = CsvSchema.builder().addColumn("id").addColumn("name").
                                                                    setColumnSeparator(',').build();
    public static CsvSchema PERSON_FILE_SCHEMA = CsvSchema.builder().addColumn("id").
                                                                    addColumn("first_name").
                                                                    addColumn("last_name").
                                                                    addColumn("gender").
                                                                    addColumn("height").
                                                                    addColumn("birth_date").
                                                                    addColumn("death_date").
                                                                    setColumnSeparator(',').build();
    public static CsvSchema HORSE_FILE_SCHEMA = CsvSchema.builder().addColumn("id").addColumn("name").
                                                                    addColumn("color").addColumn("weight").
                                                                    setColumnSeparator(',').build();
    public static CsvSchema KINGDOM_FILE_SCHEMA = CsvSchema.builder().addColumn("id").addColumn("name").
                                                                    setColumnSeparator(',').build();
    public static CsvSchema GUILD_FILE_SCHEMA = CsvSchema.builder().addColumn("id").addColumn("name").
                                                                    setColumnSeparator(',').build();

    public static CsvSchema PERSON_OWNS_HORSE_SCHEMA = CsvSchema.builder().addColumn("person_id").
                                                                            addColumn("horse_id").
                                                                            addColumn("since").
                                                                            addColumn("till").
                                                                            setColumnSeparator(',').
                                                                            build();
    public static CsvSchema PERSON_OWNS_HORSE_ELASTIC_SCHEMA = edgeBuilder().addColumn("since").
                                                                            addColumn("till").
                                                                            setColumnSeparator(',').
                                                                            build();

    public static CsvSchema PERSON_OWNS_DRAGON_SCHEMA = CsvSchema.builder().addColumn("person_id").
                                                                            addColumn("dragon_id").
                                                                            addColumn("since").
                                                                            addColumn("till").
                                                                            setColumnSeparator(',').
                                                                            build();
    public static CsvSchema PERSON_OWNS_DRAGON_ELASTIC_SCHEMA = edgeBuilder().addColumn("since").
                                                                            addColumn("till").
                                                                            setColumnSeparator(',').
                                                                            build();
    public static CsvSchema DRAGON_FIRES_AT_SCHEMA = CsvSchema.builder().addColumn("dragon1_id").
                                                                            addColumn("dragon2_id").
                                                                            addColumn("time").
                                                                            addColumn("tmp").
                                                                            setColumnSeparator(',').
                                                                            build();
    public static CsvSchema DRAGON_FIRES_AT_ELASTIC_SCHEMA = edgeBuilder().addColumn("time").
                                                                        setColumnSeparator(',').
                                                                        build();
    public static CsvSchema DRAGON_FREEZES_SCHEMA = CsvSchema.builder().addColumn("dragon1_id").
                                                                        addColumn("dragon2_id").
                                                                        addColumn("time").
                                                                        addColumn("duration").
                                                                        addColumn("tmp").
                                                                        setColumnSeparator(',').
                                                                        build();
    public static CsvSchema DRAGON_FREEZES_ELASTIC_SCHEMA = edgeBuilder().addColumn("time").
                                                                        addColumn("duration").
                                                                        setColumnSeparator(',').
                                                                        build();
    public static CsvSchema PERSON_OFFSPRING_SCHEMA = CsvSchema.builder().addColumn("parent_id").
                                                                        addColumn("child_id").
                                                                        setColumnSeparator(',').
                                                                        build();
    public static CsvSchema PERSON_OFFSPRING_ELASTIC_SCHEMA = edgeBuilder().setColumnSeparator(',').build();

    public static CsvSchema PERSON_KNOWS_SCHEMA = CsvSchema.builder().addColumn("person1_id").
                                                                        addColumn("person2_id").
                                                                        addColumn("since").
                                                                        setColumnSeparator(',').
                                                                        build();
    public static CsvSchema PERSON_KNOWS_ELASTIC_SCHEMA = edgeBuilder().addColumn("since").setColumnSeparator(',').build();

    public static CsvSchema PERSON_MEMBER_OF_SCHEMA = CsvSchema.builder().addColumn("person_id").
                                                                            addColumn("guild_id").
                                                                            addColumn("since").
                                                                            addColumn("till").
                                                                            setColumnSeparator(',').
                                                                            build();
    public static CsvSchema PERSON_MEMBER_OF_ELASTIC_SCHEMA = edgeBuilder().addColumn("since").
                                                                            addColumn("till").
                                                                            setColumnSeparator(',').
                                                                            build();

    public static CsvSchema PERSON_SUBJECT_OF_SCHEMA = CsvSchema.builder().addColumn("person_id").
                                                                        addColumn("kingdom_id").
                                                                        setColumnSeparator(',').
                                                                        build();
    public static CsvSchema PERSON_SUBJECT_OF_ELASTIC_SCHEMA = edgeBuilder().
                                                                setColumnSeparator(',').
                                                                build();
    public static CsvSchema GUILD_REGISTERED_IN_SCHEMA = CsvSchema.builder().addColumn("guild_id").
                                                                            addColumn("kingdom_id").
                                                                            setColumnSeparator(',').
                                                                            build();
    public static CsvSchema GUILD_REGISTERED_IN_ELASTIC_SCHEMA = edgeBuilder().
                                                                setColumnSeparator(',').
                                                                build();
    public static CsvSchema HORSE_ORIGINATED_IN_SCHEMA = CsvSchema.builder().addColumn("horse_id").
                                                                            addColumn("kingdom_id").
                                                                            setColumnSeparator(',').
                                                                            build();
    public static CsvSchema HORSE_ORIGINATED_IN_ELASTIC_SCHEMA = edgeBuilder().
                                                                setColumnSeparator(',').
                                                                build();

    public static CsvSchema DRAGON_ORIGINATED_IN_SCHEMA = CsvSchema.builder().addColumn("dragon_id").
                                                                                addColumn("kingdom_id").
                                                                                setColumnSeparator(',').
                                                                                build();
    public static CsvSchema DRAGON_ORIGINATED_IN_ELASTIC_SCHEMA = edgeBuilder().
                                                                setColumnSeparator(',').
                                                                build();

    private static CsvSchema.Builder edgeBuilder(){
        return CsvSchema.builder().addColumn(SIDE_A_ID).addColumn(SIDE_B_ID).addColumn(DIRECTION_COLUMN);
    }
}
