package com.kayhut.fuse.unipop.controller.search.translation;

/**
 * Created by Roman on 18/05/2017.
 */
public class M1QueryTranslator extends CompositeQueryTranslator {
    //region Constructors
    public M1QueryTranslator() {
        super(
                new HiddenQueryTranslator(
                        new CompareQueryTranslator(),
                        new ContainsQueryTranslator(),
                        new ExistsQueryTranslator(),
                        new TextQueryTranslator()
                )
        );
    }
    //endregion
}
