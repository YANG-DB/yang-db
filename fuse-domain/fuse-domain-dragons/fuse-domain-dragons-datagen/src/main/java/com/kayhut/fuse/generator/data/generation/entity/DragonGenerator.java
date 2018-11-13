package com.kayhut.fuse.generator.data.generation.entity;

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

import com.kayhut.fuse.generator.configuration.DragonConfiguration;
import com.kayhut.fuse.generator.data.generation.other.PropertiesGenerator;
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
        return Dragon.Builder.get().withName(faker.name().firstName() + " " + faker.gameOfThrones().dragon())
                .withPower(faker.number()
                        .numberBetween(configuration.getMinPower(), configuration.getMaxPower()))
                .withGender(PropertiesGenerator.generateGender())
                .withColor(PropertiesGenerator.generateColor())
                .build();
    }
}
