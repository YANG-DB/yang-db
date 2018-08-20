package com.kayhut.fuse.model.asgQuery;

import com.kayhut.fuse.model.query.EBase;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Roman on 8/18/2018.
 */
public class AsgQueryVisitor<T> {
    //region Constructors
    public AsgQueryVisitor(
            Predicate<AsgEBase> elementPredicate,
            Function<AsgEBase<? extends EBase>, T> elementValueFunction,
            Predicate<AsgEBase<? extends EBase>> dfsPredicate,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> vElementProvider,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> hElementProvider,
            Function<AsgEBase<? extends EBase>, T> vElementInvocation,
            Function<AsgEBase<? extends EBase>, T> hElementInvocation,
            BiFunction<T, T, T> vElementConsolidate,
            BiFunction<T, T, T> hElementConsolidate) {

        this.elementPredicate = elementPredicate;
        this.elementValueFunction = elementValueFunction;

        this.dfsPredicate = dfsPredicate;

        this.vElementProvider = vElementProvider;
        this.hElementProvider = hElementProvider;

        this.vElementInvocation = vElementInvocation;
        this.hElementInvocation = hElementInvocation;

        this.vElementConsolidate = vElementConsolidate;
        this.hElementConsolidate = hElementConsolidate;
    }
    //endregion

    public T visit(AsgEBase<? extends EBase> asgEBase) {
        return AsgQueryUtil.visit(
                asgEBase,
                this.elementPredicate,
                this.elementValueFunction,
                this.dfsPredicate,
                this.vElementProvider,
                this.hElementProvider,
                this.vElementInvocation,
                this.hElementInvocation,
                this.vElementConsolidate,
                this.hElementConsolidate);
    }

    //region Fields
    protected Predicate<AsgEBase> elementPredicate;
    protected Predicate<AsgEBase<? extends EBase>> dfsPredicate;

    protected Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> vElementProvider;
    protected Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> hElementProvider;

    protected Function<AsgEBase<? extends EBase>, T> vElementInvocation;
    protected Function<AsgEBase<? extends EBase>, T> hElementInvocation;

    protected Function<AsgEBase<? extends EBase>, T> elementValueFunction;
    protected BiFunction<T, T, T> vElementConsolidate;
    protected BiFunction<T, T, T> hElementConsolidate;
    //endregion
}
