package com.kayhut.fuse.generator.data.generation.entity;

import com.github.javafaker.Faker;


/**
 * Created by benishue on 15-May-17.
 */

/**
 * @param <C> - Configuration Per Entity
 * @param <E> - Entity (e.g., Dragon, Person)
 */
public abstract class EntityGeneratorBase<C, E> {

    //region Ctrs
    public EntityGeneratorBase(C configuration) {
        this.faker = new Faker();
        this.configuration = configuration;
    }
    //endregion

    //region Abstract Methods
    public abstract E generate();
    //endregion

    //region Fields
    protected final Faker faker;
    protected final C configuration;
    //endregion

}
