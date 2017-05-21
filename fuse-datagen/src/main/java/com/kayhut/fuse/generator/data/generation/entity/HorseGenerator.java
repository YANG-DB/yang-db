package com.kayhut.fuse.generator.data.generation.entity;

import com.kayhut.fuse.generator.configuration.HorseConfiguration;
import com.kayhut.fuse.generator.data.generation.other.PropertiesGenerator;
import com.kayhut.fuse.generator.model.entity.Horse;
import com.kayhut.fuse.generator.util.RandomUtil;

/**
 * Created by benishue on 19/05/2017.
 */
public class HorseGenerator extends EntityGeneratorBase<HorseConfiguration, Horse> {

    public HorseGenerator(HorseConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Horse generate() {
        long weight = Math.round(RandomUtil.randomGaussianNumber(configuration.getWeightMean(), configuration.getWeightSD()));

        return Horse.HorseBuilder.aHorse().withName(faker.cat().name().concat( " " + faker.gameOfThrones().character()))
                .withMaxSpeed(faker.number().numberBetween(configuration.getMinSpeed(), configuration.getMaxSpeed()))
                .withColor(PropertiesGenerator.generateColor())
                .withWeight((int) weight)
                .withMaxDistance(Math.toIntExact(faker.number().numberBetween(Math.round(configuration.getMaxDistance() * 0.1), configuration.getMaxDistance())))
                .build();
    }
}
