package com.kayhut.fuse.unipop.controller.search.translation;

import java.util.Collections;

/**
 * Created by Roman on 18/05/2017.
 */
public class M1QueryTranslator extends CompositeQueryTranslator {
    //region Constructors
    public M1QueryTranslator() {
        super(
                new HiddenQueryTranslator(
                        new CompareQueryTranslator(true),
                        new ContainsQueryTranslator(),
                        new ExistsQueryTranslator(),
                        new TextQueryTranslator(),
                        new AndPQueryTranslator(
                                new CompareQueryTranslator(true),
                                new ContainsQueryTranslator(),
                                new ExistsQueryTranslator(),
                                new TextQueryTranslator()
                        ),
                        new OrPQueryTranslator(
                                new CompareQueryTranslator(false),
                                new ContainsQueryTranslator(),
                                new ExistsQueryTranslator(),
                                new TextQueryTranslator()
                        )
                )
        );
    }
    //endregion
}
