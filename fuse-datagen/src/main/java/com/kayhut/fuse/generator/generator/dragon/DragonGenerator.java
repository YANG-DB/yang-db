package com.kayhut.fuse.generator.generator.dragon;

import com.github.javafaker.Faker;
import com.kayhut.fuse.generator.model.entity.Dragon;
import org.apache.commons.configuration.Configuration;


/**
 * Created by benishue on 15-May-17.
 */
public class DragonGenerator {

    public DragonGenerator(DragonConfiguration dragonConfiguration) {
        faker = new Faker();
        this.minDragonPower = dragonConfiguration.getMinPower();
        this.maxDragonPower = dragonConfiguration.getMaxPower();
    }

    public Dragon generateDragon(){
        return Dragon.DragonBuilder.aDragon().withName(faker.gameOfThrones().dragon())
                .withPower(faker.number().numberBetween(this.minDragonPower, this.maxDragonPower))
                .build();
    }

    private Faker faker;
    private int maxDragonPower;
    private int minDragonPower;

}
