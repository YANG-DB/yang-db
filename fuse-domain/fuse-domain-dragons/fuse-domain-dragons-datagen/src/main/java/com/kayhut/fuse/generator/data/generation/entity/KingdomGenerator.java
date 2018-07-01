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

    private final int MIN_FUND = 10000;
    private final int MAX_FUND = 9999999;
    private final int INDEPENDENCE_DAY_INTERVAL = 15;

    public KingdomGenerator(KingdomConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Kingdom generate() {
        Date startDateOfStory = configuration.getStartDateOfStory();
        return Kingdom.Builder.get()
                .withKing(String.format("King %s %s", faker.name().firstName(), faker.name().lastName()))
                .withQueen(String.format("Queen %s %s", faker.name().firstName(), faker.name().lastName()))
                .withFunds(RandomUtil.uniform(MIN_FUND, MAX_FUND))
                .withIndependenceDay(RandomUtil.randomDate(startDateOfStory, DateUtil.addYearsToDate(startDateOfStory,
                        INDEPENDENCE_DAY_INTERVAL)))
                .build();
    }
}