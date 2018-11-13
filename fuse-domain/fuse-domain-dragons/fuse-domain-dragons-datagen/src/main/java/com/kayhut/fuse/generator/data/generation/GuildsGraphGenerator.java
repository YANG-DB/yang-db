package com.kayhut.fuse.generator.data.generation;

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

import com.kayhut.fuse.generator.configuration.GuildConfiguration;
import com.kayhut.fuse.generator.configuration.PersonConfiguration;
import com.kayhut.fuse.generator.data.generation.entity.GuildGenerator;
import com.kayhut.fuse.generator.model.entity.EntityBase;
import com.kayhut.fuse.generator.model.entity.Guild;
import com.kayhut.fuse.generator.model.enums.EntityType;
import com.kayhut.fuse.generator.model.enums.RelationType;
import com.kayhut.fuse.generator.model.relation.MemberOf;
import com.kayhut.fuse.generator.model.relation.RelationBase;
import com.kayhut.fuse.generator.util.CsvUtil;
import com.kayhut.fuse.generator.util.RandomUtil;
import javaslang.collection.Stream;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by benishue on 05/06/2017.
 */
public class GuildsGraphGenerator {

    private final Logger logger = LoggerFactory.getLogger(GuildsGraphGenerator.class);
    //Not all of the population is member of guild
    private final double NOT_ASSIGNED_TO_GUILD_RATIO = 0.025;
    //The shape parameter in  Exponential distribution
    private final double LAMBDA_EXP_DIST = 0.5;

    public GuildsGraphGenerator(final Configuration configuration) {
        this.guildConf = new GuildConfiguration(configuration);
        this.personConf = new PersonConfiguration(configuration);
    }

    public List<String> generateGuildsGraph() {
        List<Guild> guilds = generateGuilds();
        return Stream.ofAll(guilds).map(EntityBase::getId).toJavaList();
    }

    public List<Guild> generateGuilds() {
        List<Guild> guildsList = new ArrayList<>();
        List<String[]> guildsRecords = new ArrayList<>();
        try {
            GuildGenerator generator = new GuildGenerator(guildConf);
            int guildsSize = guildConf.getNumberOfNodes();
            String[] guildsNames = guildConf.getGuilds();

            for (int i = 0; i < guildsSize; i++) {
                Guild guild = generator.generate();
                guild.setName(guildsNames[i]);
                guild.setId(Integer.toString(i));
                guildsList.add(guild);
                guildsRecords.add(guild.getRecord());
            }
            //Write graph
            CsvUtil.appendResults(guildsRecords, guildConf.getEntitiesFilePath());

        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
        return guildsList;
    }

    /**
     * @param guildsIdList
     * @param personsIdList
     * @return Map <Guild Id, List of Persons Ids>
     */
    public Map<String, List<String>> attachPersonsToGuilds(List<String> guildsIdList,
                                                                    List<String> personsIdList) {
        Map<String, List<String>> guildToPersonsSet = new HashMap<>();

        int maxGuildMembership = personConf.getMaxGuildMembership();
        int membersPopulationSize = Math.toIntExact(Math.round(personsIdList.size() * (1 - NOT_ASSIGNED_TO_GUILD_RATIO)));
        //One person can be belong to several guilds <Guild Id, List of persons Ids>

        //We are creating an Exp distribution of guild size summed up to the guilds number
        double[] expDistArray = RandomUtil.getExpDistArray(guildsIdList.size(), 1.0 - NOT_ASSIGNED_TO_GUILD_RATIO, LAMBDA_EXP_DIST);
        List<Double> guildsMembersDist = Arrays.stream(expDistArray).boxed().collect(Collectors.toList());

        List<String> shuffledPersonsIds = IntStream.rangeClosed(0, membersPopulationSize)
                .mapToObj(Integer::toString).collect(Collectors.toList());
        //Adding the same persons to several guilds - without changing the ratio of each guild
        for (int k = 0; k < maxGuildMembership; k++) {
            int startIndex = 0;
            for (int i = 0; i < guildsMembersDist.size(); i++) {
                String guildId =  guildsIdList.get(i);
                int guildMembersSize = Math.toIntExact(Math.round(guildsMembersDist.get(i) * membersPopulationSize));
                for (int j = startIndex; j < membersPopulationSize; j++) {
                    String personId = shuffledPersonsIds.get(j);

                    if (guildToPersonsSet.get(guildId) == null) {
                        guildToPersonsSet.put(guildId, new ArrayList<>(Arrays.asList(personId)));
                    } else {
                        List<String> personsInGuild = guildToPersonsSet.get(guildId);
                        if (!personsInGuild.contains(personId)) { //avoiding duplicate members in a Guild
                            guildToPersonsSet.get(guildId).add(personId);
                        }
                    }
                    if (j == startIndex + guildMembersSize)
                        break;
                }
                startIndex += guildMembersSize + 1;
            }
            Collections.shuffle(shuffledPersonsIds);
        }
        printPersonsToGuild(guildToPersonsSet, EntityType.GUILD);
        return guildToPersonsSet;
    }

    private void printPersonsToGuild(Map<String, List<String>> personsToGuild, EntityType entityType) {
        List<String[]> p2gRecords = new ArrayList<>();

        for (Map.Entry<String, List<String>> p2g : personsToGuild.entrySet()) {
            String guildId = p2g.getKey();
            List<String> personsIds = p2g.getValue();
            for (String personId : personsIds) {
                String edgeId = guildId + "_" + personId;
                Date since = RandomUtil.randomDate(guildConf.getStartDateOfStory(), guildConf.getEndDateOfStory());
                Date till = RandomUtil.randomDate(since, guildConf.getEndDateOfStory());
                RelationBase personMemberOfGuildsRel = new MemberOf(edgeId, guildId, personId, since, till);
                p2gRecords.add(personMemberOfGuildsRel.getRecord());
            }
        }
        //Write graph
        String memberOfRelationsFile = String.format("%s_%s_%s.csv",
                guildConf.getRelationsFilePath().replace(".csv", ""),
                RelationType.MEMBER_OF,
                entityType);
        CsvUtil.appendResults(p2gRecords, memberOfRelationsFile);
    }

    //region Fields
    private final GuildConfiguration guildConf;
    private final PersonConfiguration personConf;
    //endregion

}
