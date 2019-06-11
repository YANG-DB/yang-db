package com.kayhut.fuse.model.asgQuery;

/*-
 * #%L
 * AsgQueryVisitor.java - fuse-model - kayhut - 2,016
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
