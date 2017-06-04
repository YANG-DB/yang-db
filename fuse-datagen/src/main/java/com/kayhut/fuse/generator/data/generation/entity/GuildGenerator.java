package com.kayhut.fuse.generator.data.generation.entity;

import com.kayhut.fuse.generator.configuration.GuildConfiguration;
import com.kayhut.fuse.generator.model.entity.Guild;
import com.kayhut.fuse.generator.util.DateUtil;
import com.kayhut.fuse.generator.util.RandomUtil;

import java.util.Date;

/**
 * Created by benishue on 19/05/2017.
 */
public class GuildGenerator extends EntityGeneratorBase<GuildConfiguration, Guild> {

    public GuildGenerator(GuildConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Guild generate() {
        Date startDateOfStory = configuration.getStartDateOfStory();
        return Guild.Builder.get()
                .withDescription(faker.lorem().sentence(10, 5))
                .withEstablishDate(RandomUtil.randomDate(startDateOfStory, DateUtil.addYearsToDate(startDateOfStory, 5)))
                .withUrl(faker.internet().url())
                .withIconId(faker.internet().avatar())
                .build();
    }
}