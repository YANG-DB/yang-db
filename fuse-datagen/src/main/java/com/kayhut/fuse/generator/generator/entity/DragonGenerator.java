package com.kayhut.fuse.generator.generator.entity;

import com.kayhut.fuse.generator.configuration.DragonConfiguration;
import com.kayhut.fuse.generator.model.entity.Dragon;

/**
 * Created by benishue on 19/05/2017.
 */
public class DragonGenerator extends EntityGenerator<DragonConfiguration, Dragon> {

    public DragonGenerator(DragonConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Dragon generate() {
        return Dragon.DragonBuilder.aDragon().withName(faker.gameOfThrones().dragon())
                .withPower(faker.number()
                        .numberBetween(configuration.getMinPower(), configuration.getMaxPower()))
                .build();
    }
}
