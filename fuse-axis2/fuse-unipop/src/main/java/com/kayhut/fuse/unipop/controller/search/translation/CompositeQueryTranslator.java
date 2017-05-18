package com.kayhut.fuse.unipop.controller.search.translation;

import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;

import java.util.Collections;

/**
 * Created by Roman on 18/05/2017.
 */
public class CompositeQueryTranslator implements PredicateQueryTranslator {
    public enum Mode {
        first,
        all
    }

    public static class Parent extends CompositeQueryTranslator {
        //region Constructors
        public Parent(String parentName) {
            super(null, Collections.emptyList());
            this.parentName = parentName;
        }
        //endregion

        //region Override Methods
        @Override
        public QueryBuilder translate(QueryBuilder queryBuilder, String key, P<?> predicate) {
            CompositeQueryTranslator parent = this.parent;
            while(parent != null) {
                if (parent.name.equals(this.parentName)) {
                    return parent.translate(queryBuilder, key, predicate);
                }
            }

            return queryBuilder;
        }
        //endregion

        //region Fields
        private String parentName;
        //endregion
    }

    //region Constructors
    public CompositeQueryTranslator(PredicateQueryTranslator...translators) {
        this(null, translators);
    }

    public CompositeQueryTranslator(String name, PredicateQueryTranslator...translators) {
        this(name, Stream.of(translators).toJavaList());
    }

    public CompositeQueryTranslator(Iterable<PredicateQueryTranslator> translators) {
        this(null, translators);
    }

    public CompositeQueryTranslator(String name, Iterable<PredicateQueryTranslator> translators) {
        this.name = name;
        this.translators = Stream.ofAll(translators).toJavaList();

        Stream.ofAll(this.translators)
                .filter(translator -> CompositeQueryTranslator.class.isAssignableFrom(translator.getClass()))
                .map(translator -> (CompositeQueryTranslator)translator)
                .forEach(compositeQueryTranslator -> compositeQueryTranslator.parent = this);
    }
    //endregion

    //region PredicateQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, String key, P<?> predicate) {
        for(PredicateQueryTranslator translator : this.translators) {
            queryBuilder = translator.translate(queryBuilder, key, predicate);
        }

        return queryBuilder;
    }
    //endregion

    //region Fields
    protected Mode mode;
    protected String name;
    protected CompositeQueryTranslator parent;
    protected Iterable<PredicateQueryTranslator> translators;
    //endregion
}
