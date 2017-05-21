package com.kayhut.fuse.generator.data.generation.entity;

import com.kayhut.fuse.generator.configuration.KingdomConfiguration;
import com.kayhut.fuse.generator.model.entity.Kingdom;
import com.kayhut.fuse.generator.util.DateUtil;
import com.kayhut.fuse.generator.util.RandomUtil;

import java.util.Date;

/**
 * Created by benishue on 19/05/2017.
 */
public class KingdomGenerator extends EntityGeneratorBase<KingdomConfiguration, Kingdom> {

    public KingdomGenerator(KingdomConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Kingdom generate() {
        Date startDateOfStory = configuration.getStartDateOfStory();
        return Kingdom.KingdomBuilder.aKingdom()
                .withKing(String.format("King %s %s", faker.name().firstName(), faker.name().lastName()))
                .withQueen(String.format("Queen %s %s", faker.name().firstName(), faker.name().lastName()))
                .withFunds(RandomUtil.uniform(10000, 9999999))
                .withIndependenceDay(RandomUtil.randomDate(startDateOfStory, DateUtil.addYearsToDate(startDateOfStory, 15)))
                .build();
    }
}