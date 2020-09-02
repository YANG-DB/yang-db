package com.yangdb.fuse.model.asgQuery;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

/*-
 *
 * AsgQueryUtil.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.yangdb.fuse.model.Tagged;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.entity.EndPattern;
import com.yangdb.fuse.model.query.optional.OptionalComp;
import com.yangdb.fuse.model.query.properties.*;
import com.yangdb.fuse.model.query.properties.constraint.NamedParameter;
import com.yangdb.fuse.model.query.properties.constraint.ParameterizedConstraint;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantBase;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import javax.management.relation.Relation;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.Tagged.isSeq;
import static com.yangdb.fuse.model.Tagged.tagSeq;
import static java.util.stream.Collectors.toSet;

/**
 * Created by Roman on 15/05/2017.
 */
public class AsgQueryUtil {
    //region Public Methods
    public static List<EProp> getParameterizedConstraintEProps(AsgQuery query) {
        return getEprops(query).stream()
                .filter(prop -> prop.getCon() != null)
                .filter(prop -> ParameterizedConstraint.class.isAssignableFrom(prop.getCon().getClass()))
                .collect(Collectors.toList());
    }

    public static Optional<EProp> getParameterizedConstraint(AsgEBase<? extends EBase> eBase, NamedParameter namedParameter) {
        return getEprops(eBase).stream()
                .filter(prop -> prop.getCon() != null)
                .filter(prop -> ParameterizedConstraint.class.isAssignableFrom(prop.getCon().getClass()))
                .filter(prop -> ((ParameterizedConstraint) prop.getCon()).getParameter().getName().equals(namedParameter.getName()))
                .findFirst();
    }

    public static int maxEntityNum(AsgQuery query) {
        return Stream.ofAll(AsgQueryUtil.eNums(query,
                asgEBase -> !QuantBase.class.isAssignableFrom(asgEBase.geteBase().getClass())
                        && !BaseProp.class.isAssignableFrom(asgEBase.geteBase().getClass())
                        && !BasePropGroup.class.isAssignableFrom(asgEBase.geteBase().getClass())))
                .max().getOrElse(0);
    }

    public static int max(AsgQuery query) {
        return Stream.ofAll(AsgQueryUtil.eNums(query)).max().get();
    }

    public static int maxQuantNum(AsgQuery query) {
        return Stream.ofAll(AsgQueryUtil.eNums(query,
                asgEBase -> QuantBase.class.isAssignableFrom(asgEBase.geteBase().getClass()))).max().getOrElse(0);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> get(AsgEBase<T> asgEBase, int eNum) {
        return element(asgEBase, emptyIterableFunction, AsgEBase::getNext, p -> p.geteBase().geteNum() == eNum, truePredicate);
    }

    /**
     * get last Descendant - skip Props & Prop groups
     *
     * @param asgEBase
     * @param excludes
     * @param <T>
     * @return
     */
    public static <T extends EBase> AsgEBase<EBase> lastDescendant(AsgEBase<T> asgEBase, List<Class> excludes) {
        return element(asgEBase, emptyIterableFunction,
                element -> element.getNext().stream()
                        .filter(p -> !excludes.stream().filter(e -> e.isAssignableFrom(p.geteBase().getClass())).findAny().isPresent())
                        .collect(Collectors.toList()),
                e -> !e.hasNext(),
                truePredicate).get();
    }


    public static <T extends EBase> Optional<? extends BaseProp> getByPropTag(AsgEBase<T> asgEBase, String pTag) {
        return java.util.stream.Stream
                .concat(getEprops(asgEBase).stream(), getRelProps(asgEBase).stream())
                .filter(p -> p.getpTag()!=null)
                .filter(p -> p.getpTag().equals(pTag))
                .findFirst();
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getByTag(AsgEBase<T> asgEBase, String eTag) {
        return element(asgEBase, emptyIterableFunction, AsgEBase::getNext,
                p -> (EEntityBase.class.isAssignableFrom(p.geteBase().getClass()) &&
                        ((EEntityBase) p.geteBase()).geteTag() != null &&
                        ((EEntityBase) p.geteBase()).geteTag().equals(eTag))
                        ||
                        (Rel.class.isAssignableFrom(p.geteBase().getClass()) &&
                                ((Rel) p.geteBase()).getWrapper() != null &&
                                ((Rel) p.geteBase()).getWrapper().equals(eTag))
                , truePredicate);
    }

    public static <T extends EBase, S extends EBase> Map<String, List<AsgEBase<EBase>>> groupByTags(AsgEBase<T> asgEBase) {
        List<AsgEBase<EBase>> elements = elements(asgEBase,
                emptyIterableFunction,
                AsgEBase::getNext,
                p -> (EEntityBase.class.isAssignableFrom(p.geteBase().getClass()) && ((EEntityBase) p.geteBase()).geteTag() != null)
                        || (Rel.class.isAssignableFrom(p.geteBase().getClass()) && ((Rel) p.geteBase()).getWrapper() != null),
                truePredicate,
                Collections.emptyList());

        return elements
                .stream()
                .collect(Collectors.groupingBy(o->((Tagged) o.geteBase()).geteTag()));

    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> ancestor(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return element(
                asgEBase,
                emptyIterableFunction,
                AsgEBase::getParents,
                asgEBase1 -> predicate.test(asgEBase1) && notThisPredicateFunction.apply(asgEBase).test(asgEBase1),
                truePredicate);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> ancestor(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate, Predicate<AsgEBase> traversalPredicate) {
        return element(
                asgEBase,
                emptyIterableFunction,
                AsgEBase::getParents,
                asgEBase1 -> predicate.test(asgEBase1) && notThisPredicateFunction.apply(asgEBase).test(asgEBase1),
                traversalPredicate);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> ancestorRoot(AsgEBase<T> asgEBase) {
        return element(
                asgEBase,
                emptyIterableFunction,
                AsgEBase::getParents,
                asgEBase1 -> asgEBase1.getParents().isEmpty(),
                truePredicate);
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> ancestors(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return elements(
                asgEBase,
                emptyIterableFunction,
                AsgEBase::getParents,
                asgEBase1 -> predicate.test(asgEBase1) && notThisPredicateFunction.apply(asgEBase).test(asgEBase1),
                truePredicate,
                Collections.emptyList());
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> ancestor(AsgEBase<T> asgEBase, Class<?> klass) {
        return ancestor(asgEBase, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> ancestors(AsgEBase<T> asgEBase, Class<?> klass) {
        return ancestors(asgEBase, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> ancestor(AsgEBase<T> asgEBase, int eNum) {
        return ancestor(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> nextDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return element(
                asgEBase,
                emptyIterableFunction,
                AsgEBase::getNext,
                asgEBase1 -> predicate.test(asgEBase1) && notThisPredicateFunction.apply(asgEBase).test(asgEBase1),
                truePredicate);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> nextDescendant(AsgEBase<T> asgEBase, Class<?> klass) {
        return nextDescendant(asgEBase, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> nextDescendant(AsgEBase<T> asgEBase, int eNum) {
        return nextDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> nextAdjacentDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return element(
                asgEBase,
                emptyIterableFunction,
                AsgEBase::getNext,
                asgEBase1 -> predicate.test(asgEBase1) && notThisPredicateFunction.apply(asgEBase).test(asgEBase1),
                adjacentDfsPredicate.apply(asgEBase));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> adjacentAncestor(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return element(
                asgEBase,
                emptyIterableFunction,
                AsgEBase::getParents,
                asgEBase1 -> predicate.test(asgEBase1) && notThisPredicateFunction.apply(asgEBase).test(asgEBase1),
                adjacentDfsPredicate.apply(asgEBase));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> nextAdjacentDescendant(AsgEBase<T> asgEBase, Class<?> klass) {
        return nextAdjacentDescendant(asgEBase, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> adjacentAncestor(AsgEBase<T> asgEBase, Class<?> klass) {
        return adjacentAncestor(asgEBase, classPredicateFunction.apply(klass));
    }

    /**
     * A leaf is:
     * a node that has no next element OR has no parents and next descendant is not a quant
     *
     * @param asgEBase
     * @param <T>
     * @return
     */
    public static <T extends EBase> Optional<Boolean> isLeaf(AsgEBase<T> asgEBase) {
        if (!EEntityBase.class.isAssignableFrom(asgEBase.geteBase().getClass()))
            return Optional.empty();
        return Optional.of(!asgEBase.hasNext() || (isFirst(asgEBase) && !nextDescendant(asgEBase, QuantBase.class).isPresent()));
    }

    public static <T extends EBase> boolean isFirst(AsgEBase<T> asgEBase) {
        return ((asgEBase.geteBase().getClass().equals(Start.class)) ||
                asgEBase.getParents().isEmpty() ||
                asgEBase.getParents().get(0).geteBase().getClass().equals(Start.class));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> nextAdjacentDescendant(AsgEBase<T> asgEBase, Class<?> klass, int hopes) {
        int count = 0;
        Optional<AsgEBase<S>> element = nextAdjacentDescendant(asgEBase, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                notThisPredicateFunction.apply(asgEBase).test(asgEBase1));
        while (!element.isPresent() && count < hopes && !asgEBase.getNext().isEmpty()) {
            AsgEBase<? extends EBase> next = asgEBase.getNext().get(0);
            element = nextAdjacentDescendant(next, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                    notThisPredicateFunction.apply(next).test(asgEBase1));
            count++;
        }
        return element;
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> nextAdjacentDescendant(AsgEBase<T> asgEBase, int eNum) {
        return nextAdjacentDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> nextDescendants(AsgEBase<T> asgEBase, Predicate<AsgEBase> elementPredicate, Predicate<AsgEBase> dfsPredicate) {
        return elements(
                asgEBase,
                emptyIterableFunction,
                AsgEBase::getNext,
                asgEBase1 -> elementPredicate.test(asgEBase1) && notThisPredicateFunction.apply(asgEBase).test(asgEBase1),
                dfsPredicate,
                Collections.emptyList());
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> nextDescendants(AsgEBase<T> asgEBase, Class<?> klass) {
        return nextDescendants(asgEBase, classPredicateFunction.apply(klass), truePredicate);
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> nextAdjacentDescendants(AsgEBase<T> asgEBase, Predicate<AsgEBase> elementPredicate) {
        return nextDescendants(asgEBase, elementPredicate, adjacentDfsPredicate.apply(asgEBase));
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> nextDescendantsSingleHop(AsgEBase<T> asgEBase, Class<?> klass) {
        return nextDescendants(asgEBase,
                (asgEBase1 -> classPredicateFunction.apply(klass).test(asgEBase1) && asgEBase1 != asgEBase),
                (asgEBase1 -> asgEBase1 == asgEBase || !classPredicateFunction.apply(klass).test(asgEBase1)));
    }


    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> nextAdjacentDescendants(AsgEBase<T> asgEBase, Class<?> klass) {
        return nextDescendants(asgEBase, classPredicateFunction.apply(klass), adjacentDfsPredicate.apply(asgEBase));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> bDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return element(
                asgEBase,
                AsgEBase::getB,
                emptyIterableFunction,
                asgEBase1 -> predicate.test(asgEBase1) && notThisPredicateFunction.apply(asgEBase).test(asgEBase1),
                truePredicate);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> bDescendant(AsgEBase<T> asgEBase, Class<?> klass) {
        return bDescendant(asgEBase, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> bDescendant(AsgEBase<T> asgEBase, int eNum) {
        return bDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> bDescendants(AsgEBase<T> asgEBase, Predicate<AsgEBase> elementPredicate, Predicate<AsgEBase> dfsPredicate) {
        return elements(
                asgEBase,
                AsgEBase::getB,
                emptyIterableFunction,
                asgEBase1 -> elementPredicate.test(asgEBase1) && notThisPredicateFunction.apply(asgEBase).test(asgEBase1),
                dfsPredicate,
                Collections.emptyList());
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> bDescendants(AsgEBase<T> asgEBase, Class<?> klass) {
        return bDescendants(asgEBase, classPredicateFunction.apply(klass), truePredicate);
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> bDescendants(AsgEBase<T> asgEBase, int eNum) {
        return bDescendants(asgEBase, enumPredicateFunction.apply(eNum), truePredicate);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> bAdjacentDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return element(
                asgEBase,
                AsgEBase::getB,
                asgEbase1 -> Collections.emptyList(),
                asgEBase1 -> predicate.test(asgEBase1) && notThisPredicateFunction.apply(asgEBase).test(asgEBase1),
                adjacentDfsPredicate.apply(asgEBase));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> bAdjacentDescendant(AsgEBase<T> asgEBase, Class<?> klass) {
        return bAdjacentDescendant(asgEBase, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> bAdjacentDescendant(AsgEBase<T> asgEBase, int eNum) {
        return bAdjacentDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> bAdjacentDescendants(AsgEBase<T> asgEBase, Predicate<AsgEBase> elementPredicates) {
        return bDescendants(asgEBase, elementPredicates, adjacentDfsPredicate.apply(asgEBase));
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> bAdjacentDescendants(AsgEBase<T> asgEBase, Class<?> klass) {
        return bDescendants(asgEBase, classPredicateFunction.apply(klass), adjacentDfsPredicate.apply(asgEBase));
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> bAdjacentDescendants(AsgEBase<T> asgEBase, int eNum) {
        return bDescendants(asgEBase, enumPredicateFunction.apply(eNum), adjacentDfsPredicate.apply(asgEBase));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> descendantBDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return element(
                asgEBase,
                AsgEBase::getB,
                AsgEBase::getNext,
                asgEBase1 -> predicate.test(asgEBase1) && notThisPredicateFunction.apply(asgEBase).test(asgEBase1),
                truePredicate);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> descendantBDescendant(AsgEBase<T> asgEBase, Class<?> klass) {
        return descendantBDescendant(asgEBase, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> descendantBDescendant(AsgEBase<T> asgEBase, int eNum) {
        return descendantBDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> descendantBDescendants(AsgEBase<T> asgEBase, Predicate<AsgEBase> elementPredicate, Predicate<AsgEBase> dfsPredicate) {
        return elements(
                asgEBase,
                AsgEBase::getB,
                AsgEBase::getNext,
                asgEBase1 -> elementPredicate.test(asgEBase1) && notThisPredicateFunction.apply(asgEBase).test(asgEBase1),
                dfsPredicate,
                Collections.emptyList());
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> ancestorBDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return element(
                asgEBase,
                AsgEBase::getB,
                AsgEBase::getParents,
                asgEBase1 -> predicate.test(asgEBase1) && notThisPredicateFunction.apply(asgEBase).test(asgEBase1),
                truePredicate);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> ancestorBDescendant(AsgEBase<T> asgEBase, Class<?> klass) {
        return ancestorBDescendant(asgEBase, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> ancestorBDescendant(AsgEBase<T> asgEBase, int eNum) {
        return ancestorBDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase> Optional<AsgEBase<T>> element(AsgQuery query, Predicate<AsgEBase> predicate) {
        return element(query.getStart(), AsgEBase::getB, AsgEBase::getNext, predicate, truePredicate);
    }

    public static <T extends EBase> Optional<AsgEBase<T>> element(AsgQuery query, Class<T> klass) {
        return element(query, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase> Optional<AsgEBase<T>> element(AsgQuery query, int eNum) {
        return element(query, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase> AsgEBase<T> element$(AsgQuery query, Predicate<AsgEBase> predicate) {
        return AsgQueryUtil.<T>element(query, predicate).get();
    }

    public static <T extends EBase> AsgEBase<T> element$(AsgQuery query, Class<T> klass) {
        return AsgQueryUtil.<T>element(query, klass).get();
    }

    public static <T extends EBase> AsgEBase<T> element$(AsgQuery query, int eNum) {
        return AsgQueryUtil.<T>element(query, eNum).get();
    }

    public static <T extends EBase> List<AsgEBase<T>> elements(AsgEBase<? extends EBase> eBase, Predicate<AsgEBase> elementPredicate) {
        return elements(eBase, AsgEBase::getB, AsgEBase::getNext, elementPredicate, truePredicate, Collections.emptyList());
    }

    public static <T extends EBase> List<AsgEBase<T>> elements(AsgQuery query, Predicate<AsgEBase> elementPredicate) {
        return elements(query.getStart(), elementPredicate);
    }

    public static <T extends EBase> List<AsgEBase<T>> elements(AsgQuery query, Class<T> klass) {
        return elements(query, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase> List<AsgEBase<T>> elements(AsgEBase<? extends EBase> eBase, Class<T> klass) {
        return elements(eBase, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase> List<AsgEBase<T>> elements(AsgQuery query, int eNum) {
        return elements(query, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase> List<AsgEBase<T>> elements(AsgEBase<? extends EBase> eBase, int eNum) {
        return elements(eBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase> List<AsgEBase<T>> elements(AsgQuery query) {
        return elements(query, truePredicate);
    }

    public static <T extends EBase> List<AsgEBase<T>> elements(AsgEBase<? extends EBase> eBase) {
        return elements(eBase, truePredicate);
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> pathToNextDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return path(asgEBase, AsgEBase::getNext, predicate, Collections.emptyList());
    }

    /**
     * direct Descendant distance may only be 1 or 2 steps away and include only Quant and any klass types
     *
     * @param asgEBase
     * @param klass
     * @param <T>
     * @return
     */
    public static <T extends EBase> List<AsgEBase<? extends EBase>> pathToDirectDescendant(AsgEBase<T> asgEBase, Class<?> klass) {
        if (!asgEBase.hasNext())
            return Collections.emptyList();

        List<AsgEBase<? extends EBase>> path = AsgQueryUtil.pathToNextDescendant(asgEBase, EProp.class);
        //direct Descendant distance may only be 1 or 2 (quant) steps away
        if (path.size() > 3)
            return Collections.emptyList();

        //skip first node which is the source
        if (path.subList(1, path.size())
                .stream().filter(p -> !Arrays.asList(klass, Quant1.class).contains(p.geteBase().getClass()))
                .findAny().isPresent())
            return Collections.emptyList();

        return path;
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> pathToNextDescendant(AsgEBase<T> asgEBase, Class<?> klass) {
        return pathToNextDescendant(asgEBase, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> pathToNextDescendant(AsgEBase<T> asgEBase, int eNum) {
        return pathToNextDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> pathToBDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return path(asgEBase, AsgEBase::getB, predicate, Collections.emptyList());
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> pathToBDescendant(AsgEBase<T> asgEBase, Class<?> klass) {
        return pathToBDescendant(asgEBase, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> pathToBDescendant(AsgEBase<T> asgEBase, int eNum) {
        return pathToBDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> pathToAncestor(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate, List<AsgEBase<? extends EBase>> path) {
        return path(asgEBase, AsgEBase::getParents, predicate, path);
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> pathToAncestor(AsgEBase<T> asgEBase, Class<?> klass) {
        return pathToAncestor(asgEBase, classPredicateFunction.apply(klass), Collections.emptyList());
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> pathToAncestor(AsgEBase<T> asgEBase, int eNum) {
        return pathToAncestor(asgEBase, enumPredicateFunction.apply(eNum), Collections.emptyList());
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<? extends EBase>> path(AsgEBase<T> sourceAsgEBase, AsgEBase<S> destinationAsgEBase) {
        List<AsgEBase<? extends EBase>> path = pathToNextDescendant(sourceAsgEBase, destinationAsgEBase.geteNum());
        if (path.isEmpty()) {
            path = pathToAncestor(sourceAsgEBase, destinationAsgEBase.geteNum());
        }

        return path;
    }

    public static <T extends EBase, S extends EBase, Z extends EBase> Optional<AsgEBase<Z>> findFirstInPath(AsgEBase<T> sourceAsgEBase, AsgEBase<S> destinationAsgEBase, Predicate<AsgEBase> predicate) {
        List<AsgEBase<? extends EBase>> path = pathToNextDescendant(sourceAsgEBase, destinationAsgEBase.geteNum());
        if (path.isEmpty()) {
            path = pathToAncestor(sourceAsgEBase, destinationAsgEBase.geteNum());
        }
        Optional<AsgEBase<? extends EBase>> first = path.stream().filter(p -> predicate.test(p)).findFirst();
        if (first.isPresent())
            return Optional.of((AsgEBase<Z>) first.get());

        return Optional.empty();
    }

    public static List<AsgEBase<? extends EBase>> path(AsgQuery query, int sourceEnum, int destinationEnum) {
        Optional<AsgEBase<EBase>> sourceAsgEBase = element(query, sourceEnum);
        if (!sourceAsgEBase.isPresent()) {
            return Collections.emptyList();
        }

        List<AsgEBase<? extends EBase>> path = pathToNextDescendant(sourceAsgEBase.get(), destinationEnum);
        if (path.isEmpty()) {
            path = pathToAncestor(sourceAsgEBase.get(), destinationEnum);

            if (path.isEmpty()) {
                path = pathToBDescendant(sourceAsgEBase.get(), destinationEnum);
            }
        }

        return path;
    }

    public static List<Integer> eNums(AsgQuery query) {
        return values(query.getStart(), AsgEBase::getB, AsgEBase::getNext, AsgEBase::geteNum,
                truePredicate, truePredicate, Collections.emptyList());
    }

    public static List<Integer> eNums(AsgQuery query, Predicate<AsgEBase> elementPredicate) {
        return values(query.getStart(), AsgEBase::getB, AsgEBase::getNext, AsgEBase::geteNum,
                elementPredicate, truePredicate, Collections.emptyList());
    }

    public static List<String> eTags(AsgQuery query) {
        return eTags(query.getStart());
    }

    public static <T extends EBase> List<String> eTags(AsgEBase<T> element) {
        return values(element,
                AsgEBase::getB,
                AsgEBase::getNext,
                asgEBase -> ((Tagged) asgEBase.geteBase()).geteTag(),
                asgEBase -> asgEBase.geteBase() instanceof Tagged,
                truePredicate, Collections.emptyList());
    }

    public static AsgEBase<Rel> reverse(AsgEBase<Rel> relAsgEBase) {
        Rel reversedRel = new Rel();
        reversedRel.seteNum(relAsgEBase.geteNum());
        reversedRel.setrType(relAsgEBase.geteBase().getrType());
        reversedRel.setDir(relAsgEBase.geteBase().getDir() == Rel.Direction.L ? Rel.Direction.R : Rel.Direction.L);

        return AsgEBase.Builder.<Rel>get().withEBase(reversedRel).build();
    }

    /**
     * flatten query graph to assignments pathes
     * @param query
     * @return
     */
    public static List<List<AsgEBase<? extends EBase>>> flattenQuery(AsgQuery query) {
        List<AsgEBase<EBase>> ends = elements(query.getStart(), emptyIterableFunction,  AsgEBase::getNext, truePredicate, truePredicate, Collections.emptyList())
                .stream()
                .filter(e->!e.hasNext()).collect(Collectors.toList());
        List<List<AsgEBase<? extends EBase>>> pathes = ends.stream().map(e -> path(query.getStart(), e)).collect(Collectors.toList());
        return pathes;

    }

    /**
     * print list of pathes
     * @param pathes
     * @return
     */
    public static List<String> patterns(List<List<AsgEBase<? extends EBase>>> pathes) {
        return pathes.stream().map(p->pattern(p)).collect(Collectors.toList());
    }

    public static String pattern(AsgQuery query) {
        List<AsgEBase<? extends EBase>> elements = elements(query).stream().collect(Collectors.toList());
        return pattern(elements);
    }

    public static String pattern(List<AsgEBase<? extends EBase>> elements) {
        StringJoiner joiner = new StringJoiner(":", "", "");
        elements.forEach(e -> {
            if (e.geteBase() instanceof EEntityBase)
                joiner.add(EEntityBase.class.getSimpleName());
            else if (e.geteBase() instanceof Rel)
                joiner.add(Relation.class.getSimpleName());
            else if (e.geteBase() instanceof EPropGroup)
                joiner.add(EPropGroup.class.getSimpleName());
            else if (e.geteBase() instanceof RelPropGroup)
                joiner.add(RelPropGroup.class.getSimpleName());
            else
                joiner.add(e.geteBase().getClass().getSimpleName());
        });
        return joiner.toString();
    }

    public static <T extends EBase> AsgEBase<T> deepClone(
            AsgEBase<T> asgEBase,
            Predicate<AsgEBase<? extends EBase>> nextPredicate,
            Predicate<AsgEBase<? extends EBase>> bPredicate) {
        AsgEBase.Builder<T> eBaseBuilder = AsgEBase.Builder.get();
        eBaseBuilder.withEBase(asgEBase.geteBase());
        Stream.ofAll(asgEBase.getNext()).filter(nextPredicate).map(elm -> deepClone(elm, nextPredicate, bPredicate)).forEach(eBaseBuilder::withNext);
        Stream.ofAll(asgEBase.getB()).filter(bPredicate).map(elm -> deepClone(elm, nextPredicate, bPredicate)).forEach(elm -> eBaseBuilder.withB(elm));
        return eBaseBuilder.build();
    }

    public static <T extends EBase> AsgEBase<T> deepCloneWithEnums(
            AtomicInteger counter,
            AsgEBase<T> asgEBase,
            Predicate<AsgEBase<? extends EBase>> nextPredicate,
            Predicate<AsgEBase<? extends EBase>> bPredicate) {
        return deepCloneWithEnums(counter, asgEBase, nextPredicate, bPredicate, false);
    }

    public static <T extends EBase> AsgEBase<T> deepCloneWithEnums(
            AtomicInteger counter,
            AsgEBase<T> asgEBase,
            Predicate<AsgEBase<? extends EBase>> nextPredicate,
            Predicate<AsgEBase<? extends EBase>> bPredicate,
            boolean cloneTags) {

        AsgEBase.Builder<T> eBaseBuilder = AsgEBase.Builder.get();
        T clone;
        //quant base has a special sequence counter
        if (QuantBase.class.isAssignableFrom(asgEBase.geteBase().getClass())) {
            clone = (T) asgEBase.geteBase().clone(counter.incrementAndGet());
        } else if (BasePropGroup.class.isAssignableFrom(asgEBase.geteBase().getClass())) {
            clone = (T) asgEBase.geteBase().clone(counter.incrementAndGet());
/*
        } else if (EndPattern.class.isAssignableFrom(asgEBase.geteBase().getClass())) {
            clone = (T) ((EndPattern) asgEBase.geteBase()).getEndEntity().clone(counter.incrementAndGet());
*/
        } else {
            clone = (T) asgEBase.geteBase().clone(counter.incrementAndGet());
        }

        // this code will cause creation of new tags that will not assiciated with former tags that was cloned from and therefore will not be found
        if (clone instanceof Tagged &&
                ((Tagged) clone).geteTag() != null) {

            if (cloneTags) {
                ((Tagged) clone).seteTag(tagSeq(((Tagged) clone).geteTag()));
            }

            if (isSeq((Tagged) clone)) {
                Tagged.setSeq(clone.geteNum(), (Tagged) clone);
            }
        }

        eBaseBuilder.withEBase(clone);

        List<AsgEBase<? extends EBase>> next = Stream.ofAll(asgEBase.getNext()).filter(nextPredicate).toJavaList();
        for (int i = 0; i < next.size(); i++) {
            eBaseBuilder.withNext(deepCloneWithEnums(counter, next.get(i), nextPredicate, bPredicate, cloneTags));
        }
        List<AsgEBase<? extends EBase>> bNext = Stream.ofAll(asgEBase.getB()).filter(bPredicate).toJavaList();

        for (int i = 0; i < bNext.size(); i++) {
            eBaseBuilder.withB(deepCloneWithEnums(counter, next.get(i), nextPredicate, bPredicate, cloneTags));
        }
        return eBaseBuilder.build();
    }

    public static <T extends EBase> AsgEBase<T> deepCloneWithEnums(
            int[] max,
            AsgEBase<T> asgEBase,
            Predicate<AsgEBase<? extends EBase>> nextPredicate,
            Predicate<AsgEBase<? extends EBase>> bPredicate) {
        return deepCloneWithEnums(max, asgEBase, nextPredicate, bPredicate, false);
    }

    public static <T extends EBase> AsgEBase<T> deepCloneWithEnums(
            int[] max,
            AsgEBase<T> asgEBase,
            Predicate<AsgEBase<? extends EBase>> nextPredicate,
            Predicate<AsgEBase<? extends EBase>> bPredicate,
            boolean cloneTags) {
        AsgEBase.Builder<T> eBaseBuilder = AsgEBase.Builder.get();
        T clone;
        //quant base has a special sequence counter
        if (QuantBase.class.isAssignableFrom(asgEBase.geteBase().getClass())) {
            clone = (T) asgEBase.geteBase().clone(max[0] * 100);
        } else if (BasePropGroup.class.isAssignableFrom(asgEBase.geteBase().getClass())) {
            clone = (T) asgEBase.geteBase().clone(max[0] * 100);
        } else if (EndPattern.class.isAssignableFrom(asgEBase.geteBase().getClass())) {
            clone = (T) ((EndPattern) asgEBase.geteBase()).getEndEntity().clone(++max[0]);
        } else {
            clone = (T) asgEBase.geteBase().clone(++max[0]);
        }
        //update etag
        //this will cause creation of new tags that will not be associated with former tags that was cloned from and therefore will not be found
        if ((clone instanceof Tagged) && (((Tagged) clone).geteTag() != null) && cloneTags) {
            if (isSeq((Tagged) clone) ) {
                Tagged.setSeq(clone.geteNum(), (Tagged) clone);
            } else {
                //clone tag according to running sequence
                ((Tagged) clone).seteTag(String.format("%s:%d",((Tagged) clone).geteTag(),clone.geteNum()));
            }
        }

        eBaseBuilder.withEBase(clone);

        List<AsgEBase<? extends EBase>> next = Stream.ofAll(asgEBase.getNext()).filter(nextPredicate).toJavaList();
        for (int i = 0; i < next.size(); i++) {
            eBaseBuilder.withNext(deepCloneWithEnums(max, next.get(i), nextPredicate, bPredicate, cloneTags));
        }
        List<AsgEBase<? extends EBase>> bNext = Stream.ofAll(asgEBase.getB()).filter(bPredicate).toJavaList();

        for (int i = 0; i < bNext.size(); i++) {
            eBaseBuilder.withB(deepCloneWithEnums(max, next.get(i), nextPredicate, bPredicate, cloneTags));
        }
        return eBaseBuilder.build();
    }

    public static OptionalStrippedQuery stripOptionals(AsgQuery query) {
        List<AsgEBase<OptionalComp>> optionals = AsgQueryUtil.elements(query.getStart(),
                AsgEBase::getB,
                AsgEBase::getNext,
                e -> e.geteBase() instanceof OptionalComp
                , e -> !(e.geteBase() instanceof OptionalComp),
                new ArrayList<>());

        AsgEBase<Start> clonedStart = AsgQueryUtil.deepClone(query.getStart(), e -> !(e.geteBase() instanceof OptionalComp), b -> true);

        List elements = elements(clonedStart);
        AsgQuery clonedMainQuery = AsgQuery.AsgQueryBuilder.anAsgQuery().withStart(clonedStart)
                .withOrigin(query.getOrigin())
                .withName(query.getName()).withOnt(query.getOnt()).withElements(elements).build();
        OptionalStrippedQuery.Builder builder = OptionalStrippedQuery.Builder.get();
        builder.withMainQuery(clonedMainQuery);
        for (AsgEBase<OptionalComp> optionalElement : optionals) {
            AsgEBase<? extends EBase> clonedOptional = AsgQueryUtil.deepClone(optionalElement.getNext().get(0), e -> true, e -> true);
            AsgEBase<EBase> optionalParent = AsgQueryUtil.ancestor(optionalElement, EEntityBase.class).get();
            AsgEBase clonedParent = AsgEBase.Builder.get().withEBase(optionalParent.geteBase()).withNext(clonedOptional).build();
            AsgEBase<Start> startAsgEBase = AsgEBase.Builder.get().withEBase(new Start(0, clonedParent.geteNum())).withNext(clonedParent).build();
            AsgQuery optionalQuery = AsgQuery.AsgQueryBuilder.anAsgQuery().withOnt(query.getOnt()).withName(query.getName()).withStart(startAsgEBase).withElements(new ArrayList<>(AsgQueryUtil.elements(startAsgEBase))).build();
            builder.withOptionalQuery(optionalElement, optionalQuery);

        }
        return builder.build();
    }

    /**
     * add a new quant element after the source entity
     *
     * @param quantAsg
     * @param source
     * @return
     */
    public static void addAsNext(AsgEBase<Quant1> quantAsg, AsgEBase source) {
        List<AsgEBase<? extends EBase>> next = new ArrayList<>(source.getNext());
        quantAsg.addNext(next);
        quantAsg.setParent(Arrays.asList(source));
        source.setNext(Arrays.asList(quantAsg));
    }

    public static void replace(AsgEBase source, AsgEBase target) {
        List<AsgEBase<? extends EBase>> next = new ArrayList<>(source.getNext());
        target.setNext(next);
    }

    /**
     * replace the targets parent with the new given one
     * @param newParent
     * @param oldParent
     */
    public static void replaceParents(AsgEBase newParent,AsgEBase oldParent) {
        //assuming single parent strategy
        List<AsgEBase<? extends EBase>> next = new ArrayList<>(oldParent.getNext());
        next.forEach(oldParent::removeNextChild);
        next.forEach(newParent::addNextChild);

    }


    public static int count(AsgEBase<? extends EBase> asgEBase, Class<? extends EBase> aClass) {
        return elements(asgEBase, classPredicateFunction.apply(aClass)).size();
    }

    public static List<AsgEBase<? extends EBase>> mergePath(List<AsgEBase<? extends EBase>>... paths) {
        Set<AsgEBase<? extends EBase>> collect = Arrays.asList(paths).stream()
                .flatMap(p -> p.stream())
                .collect(toSet());
        return collect.stream()
                .sorted(Comparator.comparing(AsgEBase::geteNum))
                .collect(Collectors.toList());

    }

    /**
     * completely remove path elements from query
     *
     * @param query
     * @param source
     * @param target
     * @return
     */
    public static AsgQuery removeStep(AsgQuery query, AsgEBase<ETyped> source, AsgEBase<ETyped> target) {
        List<AsgEBase<? extends EBase>> parents = target.getParents();
        //remove target
        remove(query, target);
        //remove source (if exists a quant inside - remove only when quent is empty beside target)
        parents.stream()
                .filter(p -> p.geteBase() instanceof QuantBase)
                .filter(p -> !((QuantBase)p.geteBase()).hasNext())
                .forEach( p-> remove(query, p));
        //remove rel
        parents.stream()
                .filter(p -> !(p.geteBase() instanceof QuantBase))
                .forEach(p -> remove(query, p));
        //dont remove source
        return query;
    }



    /**
     * removes all elements from query
     * @param query
     * @param element
     * @return
     */
    public static AsgQuery remove(AsgQuery query, AsgEBase<? extends EBase> ... element) {
        for (AsgEBase<? extends EBase> asgEBase : element) {
           _remove(query, asgEBase);
        }
        return query;
    }

    private static void  _remove(AsgQuery query, AsgEBase<? extends EBase> element) {
        List<AsgEBase<? extends EBase>> newParents = new ArrayList<>(element.getParents());
        newParents.forEach(p -> p.removeNextChild(element));
        newParents.forEach(p -> p.addNextChild(element.getNext()));
        //clean element
        element.setParent(new ArrayList<>());
        element.setNext(new ArrayList<>());
        //clean query from element
        query.getElements().remove(element);
    }

    /**
     * REPLACE all tagged elements with tag name starting with 'statringTagValue' with functional value placer
     * @param query
     * @param statringTagValue
     * @param function
     * @return
     */
    public static AsgQuery replaceTagsStartingWith(AsgQuery query,String statringTagValue,Function<EBase,String> function) {
        elements(query.getStart(), emptyIterableFunction, AsgEBase::getNext,
                p -> (EEntityBase.class.isAssignableFrom(p.geteBase().getClass()) &&
                        ((EEntityBase) p.geteBase()).geteTag() != null &&
                        ((EEntityBase) p.geteBase()).geteTag().startsWith(statringTagValue))
                        ||
                        (Rel.class.isAssignableFrom(p.geteBase().getClass()) &&
                                ((Rel) p.geteBase()).getWrapper() != null &&
                                ((Rel) p.geteBase()).getWrapper().startsWith(statringTagValue))
                , truePredicate,Collections.emptyList())
                .forEach(e->((Tagged)e.geteBase()).seteTag(function.apply(e.geteBase())));
        return query;
    }

    public static class OptionalStrippedQuery {
        private AsgQuery mainQuery;
        private List<Tuple2<AsgEBase<OptionalComp>, AsgQuery>> optionalQueries;

        public AsgQuery getMainQuery() {
            return mainQuery;
        }

        public List<Tuple2<AsgEBase<OptionalComp>, AsgQuery>> getOptionalQueries() {
            return optionalQueries;
        }

        public OptionalStrippedQuery(AsgQuery mainQuery, List<Tuple2<AsgEBase<OptionalComp>, AsgQuery>> optionalQueries) {
            this.mainQuery = mainQuery;
            this.optionalQueries = optionalQueries;
        }

        public static final class Builder {
            private AsgQuery mainQuery;
            private List<Tuple2<AsgEBase<OptionalComp>, AsgQuery>> optionalQueries = new ArrayList<>();

            public static Builder get() {
                return new Builder();
            }

            public Builder withMainQuery(AsgQuery mainQuery) {
                this.mainQuery = mainQuery;
                return this;
            }

            public Builder withOptionalQuery(AsgEBase<OptionalComp> optionalComp, AsgQuery optionalQuery) {
                this.optionalQueries.add(new Tuple2<>(optionalComp, optionalQuery));
                return this;
            }

            public OptionalStrippedQuery build() {
                return new OptionalStrippedQuery(mainQuery, optionalQueries);
            }


        }
    }

    public static List<EProp> getEprops(AsgQuery query) {
        List<EProp> eProps = Stream.ofAll(AsgQueryUtil.elements(query, EProp.class))
                .map(AsgEBase::geteBase).toJavaList();

        List<EPropGroup> ePropsGroup = Stream.ofAll(AsgQueryUtil.elements(query, EPropGroup.class))
                .map(AsgEBase::geteBase).toJavaList();
        List<EProp> eProps2 = Stream.ofAll(ePropsGroup).flatMap(EPropGroup::getProps).toJavaList();

        return java.util.stream.Stream.concat(eProps.stream(), eProps2.stream()).collect(Collectors.toList());
    }

    public static List<EProp> getEprops(AsgEBase<? extends EBase> eBase) {
        List<EProp> eProps = Stream.ofAll(AsgQueryUtil.elements(eBase, EProp.class))
                .map(AsgEBase::geteBase).toJavaList();

        List<EPropGroup> ePropsGroup = Stream.ofAll(AsgQueryUtil.elements(eBase, EPropGroup.class))
                .map(AsgEBase::geteBase).toJavaList();
        List<EProp> eProps2 = Stream.ofAll(ePropsGroup).flatMap(EPropGroup::getProps).toJavaList();

        return java.util.stream.Stream.concat(eProps.stream(), eProps2.stream()).collect(Collectors.toList());
    }

    public static List<RelProp> getRelProps(AsgQuery query) {
        List<RelProp> relProps = Stream.ofAll(AsgQueryUtil.elements(query, RelProp.class))
                .map(AsgEBase::geteBase).toJavaList();
        List<RelPropGroup> relPropsGroup = Stream.ofAll(AsgQueryUtil.elements(query, RelPropGroup.class))
                .map(AsgEBase::geteBase).toJavaList();
        List<RelProp> relProps2 = Stream.ofAll(relPropsGroup).flatMap(RelPropGroup::getProps).toJavaList();

        return java.util.stream.Stream.concat(relProps.stream(), relProps2.stream()).collect(Collectors.toList());
    }

    public static List<RelProp> getRelProps(AsgEBase<? extends EBase> eBase) {
        List<RelProp> relProps = Stream.ofAll(AsgQueryUtil.elements(eBase, RelProp.class))
                .map(AsgEBase::geteBase).toJavaList();
        List<RelPropGroup> relPropsGroup = Stream.ofAll(AsgQueryUtil.elements(eBase, RelPropGroup.class))
                .map(AsgEBase::geteBase).toJavaList();
        List<RelProp> relProps2 = Stream.ofAll(relPropsGroup).flatMap(RelPropGroup::getProps).toJavaList();

        return java.util.stream.Stream.concat(relProps.stream(), relProps2.stream()).collect(Collectors.toList());
    }

    public static <T extends EBase> AsgEBase<T> transform(
            AsgEBase<? extends EBase> asgEBase,
            Function<AsgEBase<? extends EBase>, AsgEBase<T>> transformFunction,
            Predicate<AsgEBase<? extends EBase>> dfsPredicate,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> vElementProvider,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> hElementProvider) {
        return visit(
                asgEBase,
                truePredicate,
                transformFunction,
                dfsPredicate,
                vElementProvider,
                hElementProvider,
                asgEBase1 -> transform(asgEBase1, transformFunction, dfsPredicate, vElementProvider, hElementProvider),
                asgEBase1 -> transform(asgEBase1, transformFunction, dfsPredicate, vElementProvider, hElementProvider),
                AsgEBase::below,
                AsgEBase::next);
    }

    public static <T> T visit(
            AsgEBase<? extends EBase> asgEBase,
            Predicate<AsgEBase> elementPredicate,
            Function<AsgEBase<? extends EBase>, T> elementValueFunction,
            Predicate<AsgEBase<? extends EBase>> dfsPredicate,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> vElementProvider,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> hElementProvider,
            Function<AsgEBase<? extends EBase>, T> vElementInvocation,
            Function<AsgEBase<? extends EBase>, T> hElementInvocation,
            BiFunction<T, T, T> vElementConsolidate,
            BiFunction<T, T, T> hElementConsolidate) {
        T currentValue = null;

        if (elementPredicate.test(asgEBase)) {
            currentValue = elementValueFunction.apply(asgEBase);
        }

        if (dfsPredicate.test(asgEBase)) {
            for (AsgEBase<? extends EBase> elementAsgEBase : vElementProvider.apply(asgEBase)) {
                currentValue = vElementConsolidate.apply(currentValue, vElementInvocation.apply(elementAsgEBase));
            }

            for (AsgEBase<? extends EBase> elementAsgEBase : hElementProvider.apply(asgEBase)) {
                currentValue = hElementConsolidate.apply(currentValue, hElementInvocation.apply(elementAsgEBase));
            }
        }

        return currentValue;
    }
    //endregion

    //region Private Methods
    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> element(
            AsgEBase<T> asgEBase,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> vElementProvider,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> hElementProvider,
            Predicate<AsgEBase> predicate,
            Predicate<AsgEBase> dfsPredicate) {

        if (predicate.test(asgEBase)) {
            return Optional.of((AsgEBase<S>) asgEBase);
        }

        if (dfsPredicate.test(asgEBase)) {
            for (AsgEBase<? extends EBase> elementAsgEBase : vElementProvider.apply(asgEBase)) {
                Optional<AsgEBase<EBase>> recResult = element(elementAsgEBase, hElementProvider, vElementProvider, predicate, dfsPredicate);
                if (recResult.isPresent()) {
                    return Optional.of((AsgEBase<S>) recResult.get());
                }
            }
        }

        if (dfsPredicate.test(asgEBase)) {
            for (AsgEBase<? extends EBase> elementAsgEBase : hElementProvider.apply(asgEBase)) {
                Optional<AsgEBase<EBase>> recResult = element(elementAsgEBase, hElementProvider, vElementProvider, predicate, dfsPredicate);
                if (recResult.isPresent()) {
                    return Optional.of((AsgEBase<S>) recResult.get());
                }
            }
        }

        return Optional.empty();
    }

    private static <T extends EBase> List<AsgEBase<T>> elements(
            AsgEBase<? extends EBase> asgEBase,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> vElementProvider,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> hElementProvider,
            Predicate<AsgEBase> elementPredicate,
            Predicate<AsgEBase> dfsPredicate,
            List<AsgEBase<T>> elements) {

        return values(
                asgEBase,
                vElementProvider,
                hElementProvider,
                asgEBase1 -> (AsgEBase<T>) asgEBase1,
                elementPredicate,
                dfsPredicate,
                elements);
    }

    private static <T> List<T> values(
            AsgEBase<? extends EBase> asgEBase,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> vElementProvider,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> hElementProvider,
            Function<AsgEBase<? extends EBase>, T> valueFunction,
            Predicate<AsgEBase> elementPredicate,
            Predicate<AsgEBase> dfsPredicate,
            List<T> values) {

        List<T> newValues = values;

        if (elementPredicate.test(asgEBase)) {
            newValues = new ArrayList<>(values);
            newValues.add(valueFunction.apply(asgEBase));
        }

        if (dfsPredicate.test(asgEBase)) {
            for (AsgEBase<? extends EBase> elementAsgEBase : vElementProvider.apply(asgEBase)) {
                newValues = values(elementAsgEBase, vElementProvider, hElementProvider, valueFunction, elementPredicate, dfsPredicate, newValues);
            }
        }

        if (dfsPredicate.test(asgEBase)) {
            for (AsgEBase<? extends EBase> elementAsgEBase : hElementProvider.apply(asgEBase)) {
                newValues = values(elementAsgEBase, vElementProvider, hElementProvider, valueFunction, elementPredicate, dfsPredicate, newValues);
            }
        }

        return newValues;
    }


    public static  <T extends EBase> Optional<AsgEBase<T>> calculateNextAncestor(AsgEBase<? extends EBase> eProp, Class<T> clazz,List<Class<? extends EBase>> allowedInPath) {
        final List<AsgEBase<? extends EBase>> path = AsgQueryUtil.pathToAncestor(eProp, clazz);
        if(path.isEmpty()) return Optional.empty();
        //go over internal path elements make sure they all comply with allowedInPath restriction
        if(path.stream().limit(path.size() - 1).skip(1).anyMatch(e -> !allowedInPath.contains(e.geteBase().getClass())))
            return Optional.empty();
        return Optional.of((AsgEBase<T>) path.get(path.size()-1));

    }

    public static List<AsgEBase<? extends EBase>> path(
            AsgEBase<? extends EBase> asgEBase,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> elementProvider,
            Predicate<AsgEBase> predicate,
            List<AsgEBase<? extends EBase>> path) {

        List<AsgEBase<? extends EBase>> newPath = new ArrayList<>(path);
        newPath.add(asgEBase);
        if (predicate.test(asgEBase)) {
            return newPath;
        }

        for (AsgEBase<? extends EBase> elementAsgEBase : elementProvider.apply(asgEBase)) {
            newPath = path(elementAsgEBase, elementProvider, predicate, newPath);
            if (predicate.test(newPath.get(newPath.size() - 1))) {
                return newPath;
            }
        }

        return path;
    }

    private static Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> emptyIterableFunction =
            (asgEBase -> Collections.emptyList());

    private static Function<Class<?>, Predicate<AsgEBase>> classPredicateFunction =
            (klass) -> (asgEBase -> klass.isAssignableFrom(asgEBase.geteBase().getClass()));

    private static Function<AsgEBase, Predicate<AsgEBase>> notThisPredicateFunction =
            (asgEBase) -> (asgEBase1 -> asgEBase1 != asgEBase);

    private static Function<Integer, Predicate<AsgEBase>> enumPredicateFunction =
            (eNum) -> (asgEBase -> asgEBase.geteNum() == eNum);

    private static Predicate<AsgEBase> truePredicate = (asgEBase -> true);
    private static Predicate<AsgEBase> falsePredicate = (asgEBase -> false);

    private static Function<AsgEBase, Predicate<AsgEBase>> adjacentDfsPredicate = (asgEBase -> (asgEBase1 -> asgEBase == asgEBase1));
}
