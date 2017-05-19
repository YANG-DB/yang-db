package com.kayhut.fuse.generator.generator.entity;

import com.kayhut.fuse.generator.configuration.KingdomConfiguration;
import com.kayhut.fuse.generator.model.entity.Kingdom;

/**
 * Created by benishue on 19/05/2017.
 */
public class KingdomGenerator extends EntityGenerator<KingdomConfiguration, Kingdom> {

    public KingdomGenerator(KingdomConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Kingdom generate() {
        return null;
    }
}
