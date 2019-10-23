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



import com.yangdb.fuse.generator.configuration.GuildConfiguration;
import com.yangdb.fuse.generator.model.entity.Guild;
import com.yangdb.fuse.generator.util.DateUtil;
import com.yangdb.fuse.generator.util.RandomUtil;

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
