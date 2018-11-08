package org.unipop.configuration;

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
