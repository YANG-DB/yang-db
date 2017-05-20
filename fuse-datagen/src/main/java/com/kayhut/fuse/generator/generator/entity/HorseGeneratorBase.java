package com.kayhut.fuse.generator.generator.entity;

import com.kayhut.fuse.generator.configuration.HorseConfiguration;
import com.kayhut.fuse.generator.model.entity.Horse;

/**
 * Created by benishue on 19/05/2017.
 */
public class HorseGeneratorBase extends EntityGeneratorBase<HorseConfiguration, Horse> {

    public HorseGeneratorBase(HorseConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Horse generate() {
        return null;
    }
}
