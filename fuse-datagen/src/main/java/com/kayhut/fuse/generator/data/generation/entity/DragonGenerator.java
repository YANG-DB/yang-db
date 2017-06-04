package com.kayhut.fuse.generator.data.generation.entity;

import com.kayhut.fuse.generator.configuration.DragonConfiguration;
import com.kayhut.fuse.generator.model.entity.Dragon;

/**
 * Created by benishue on 19/05/2017.
 */
public class DragonGenerator extends EntityGeneratorBase<DragonConfiguration, Dragon> {

    public DragonGenerator(DragonConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Dragon generate() {
        return Dragon.Builder.get().withName(faker.gameOfThrones().dragon())
                .withPower(faker.number()
                        .numberBetween(configuration.getMinPower(), configuration.getMaxPower()))
                .build();
    }
}
