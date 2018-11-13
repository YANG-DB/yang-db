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
