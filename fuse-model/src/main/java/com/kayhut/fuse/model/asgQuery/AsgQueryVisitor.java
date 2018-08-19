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
public class AsgQueryVisitor<TOut> {
    //region Constructors
    public AsgQueryVisitor(
            Predicate<AsgEBase> elementPredicate,
            Predicate<AsgEBase> dfsPredicate,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> vElementProvider,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> hElementProvider,
            Function<AsgEBase<? extends EBase>, TOut> vElementInvocation,
            Function<AsgEBase<? extends EBase>, TOut> hElementInvocation,
            Function<AsgEBase<? extends EBase>, TOut> elementValueFunction,
            BiFunction<TOut, TOut, TOut> elementConsolidate) {

        this.elementPredicate = elementPredicate;
        this.dfsPredicate = dfsPredicate;
        this.vElementProvider = vElementProvider;
        this.hElementProvider = hElementProvider;
        this.vElementInvocation = vElementInvocation;
        this.hElementInvocation = hElementInvocation;
        this.elementValueFunction = elementValueFunction;
        this.elementConsolidate = elementConsolidate;
    }
    //endregion

    public TOut visit(AsgEBase<? extends EBase> asgEBase) {
        TOut currentValue = null;

        if (elementPredicate.test(asgEBase)) {
            currentValue = this.elementValueFunction.apply(asgEBase);
        }

        if (dfsPredicate.test(asgEBase)) {
            for (AsgEBase<? extends EBase> elementAsgEBase : vElementProvider.apply(asgEBase)) {
                currentValue = this.elementConsolidate.apply(currentValue, this.vElementInvocation.apply(elementAsgEBase));
            }

            for (AsgEBase<? extends EBase> elementAsgEBase : hElementProvider.apply(asgEBase)) {
                currentValue = this.elementConsolidate.apply(currentValue, this.hElementInvocation.apply(elementAsgEBase));
            }
        }

        return currentValue;
    }

    //region Fields
    protected Predicate<AsgEBase> elementPredicate;
    protected Predicate<AsgEBase> dfsPredicate;

    protected Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> vElementProvider;
    protected Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> hElementProvider;

    protected Function<AsgEBase<? extends EBase>, TOut> vElementInvocation;
    protected Function<AsgEBase<? extends EBase>, TOut> hElementInvocation;

    protected Function<AsgEBase<? extends EBase>, TOut> elementValueFunction;
    protected BiFunction<TOut, TOut, TOut> elementConsolidate;
    //endregion
}
