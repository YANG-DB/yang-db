package org.unipop.configuration;

/*-
 * #%L
 * UniGraphConfiguration.java - unipop-core - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import java.util.function.Consumer;

/**
 * Created by Roman on 14/05/2017.
 */
public class UniGraphConfiguration extends BaseConfiguration {
    //region Constructors
    public UniGraphConfiguration() {}

    public UniGraphConfiguration(final Configuration configuration) {
        configuration.getKeys().forEachRemaining(key -> addProperty(key, configuration.getProperty(key)));
    }
    //endregion

    //region Properties
    public int getBulkMax() {
        return super.getInt(BULK_MAX);
    }

    public void setBulkMax(int value) {
        super.addProperty(BULK_MAX, value);
    }

    public int getBulkMin() {
        return super.getInt(BULK_MIN);
    }

    public void setBulkMin(int value) {
        super.addProperty(BULK_MIN, value);
    }

    public long getBulkDecayInterval() {
        return super.getLong(BULK_DECAY_INTERVAL);
    }

    public void setBulkDecayInterval(long value) {
        super.addProperty(BULK_DECAY_INTERVAL, value);
    }

    public int getBulkStart() {
        return super.getInt(BULK_START);
    }

    public void setBulkStart(int value) {
        super.addProperty(BULK_START, value);
    }

    public int getBulkMultiplier() {
        return super.getInt(BULK_MULTIPLIER);
    }

    public void setBulkMultiplier(int value) {
        super.addProperty(BULK_MULTIPLIER, value);
    }
    //endregion

    //region Consts
    public static final String BULK_MAX = "bulk.max";
    public static final String BULK_MIN = "bulk.min";
    public static final String BULK_DECAY_INTERVAL = "bulk.decayInterval";
    public static final String BULK_START = "bulk.start";
    public static final String BULK_MULTIPLIER = "bulk.multiplier";
    //endregion

}
