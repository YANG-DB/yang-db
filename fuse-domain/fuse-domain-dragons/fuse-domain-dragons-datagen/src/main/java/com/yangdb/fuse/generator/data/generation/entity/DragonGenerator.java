package com.yangdb.fuse.generator.data.generation.entity;

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



import com.yangdb.fuse.generator.configuration.DragonConfiguration;
import com.yangdb.fuse.generator.data.generation.other.PropertiesGenerator;
import com.yangdb.fuse.generator.model.entity.Dragon;

import java.util.Date;

/**
 * Created by benishue on 19/05/2017.
 */
public class DragonGenerator extends EntityGeneratorBase<DragonConfiguration, Dragon> {

    public DragonGenerator(DragonConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Dragon generate() {
        return Dragon.Builder.get()
                .withName(faker.name().firstName() + " " + faker.gameOfThrones().dragon())
                .withBirthDate(faker.date().between(new Date( -46376431374L),new Date( -14819522574L)))
                .withPower(faker.number()
                        .numberBetween(configuration.getMinPower(), configuration.getMaxPower()))
                .withGender(PropertiesGenerator.generateGender())
                .withColor(PropertiesGenerator.generateColor())
                .build();
    }
}
