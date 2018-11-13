package com.kayhut.fuse.gta.strategy.utils;

/*-
 * #%L
 * fuse-dv-gta
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Roman on 29/05/2017.
 */
public class TraversalUtil {
    //region Public Static Methods
    public static <S extends Step> Optional<S> first(Traversal<?, ?> traversal, Predicate<Step> predicate) {
        return step(traversal, predicate, nextDirection, -1);
    }

    public static <S extends Step> Optional<S> first(Traversal<?, ?> traversal, Class<S> klass) {
        return step(traversal, classPredicateFunction.apply(klass), nextDirection, -1);
    }

    public static <S extends Step> Iterable<S> firstSteps(Traversal<?, ?> traversal, Predicate<Step> predicate) {
        return steps(traversal, predicate, falsePredicate, nextDirection, -1);
    }

    public static <S extends Step> Iterable<S> firstSteps(Traversal<?, ?> traversal, Class<S> klass) {
        return steps(traversal, classPredicateFunction.apply(klass), falsePredicate, nextDirection, -1);
    }

    public static <S extends Step> Iterable<S> firstConsecutiveSteps(Traversal<?, ?> traversal, Predicate<Step> predicate) {
        return steps(traversal, predicate, negatePredicateFunction.apply(predicate), nextDirection, -1);
    }

    public static <S extends Step> Iterable<S> firstConsecutiveSteps(Traversal<?, ?> traversal, Class<S> klass) {
        return steps(traversal, classPredicateFunction.apply(klass), negatePredicateFunction.apply(classPredicateFunction.apply(klass)), nextDirection, -1);
    }

    public static <S extends Step> Optional<S> next(Traversal<?, ?> traversal, Step step, Predicate<Step> predicate) {
        return step(traversal, predicate, nextDirection, indexOf(traversal, step));
    }

    public static <S extends Step> Optional<S> next(Traversal<?, ?> traversal, Step step, Class<S> klass) {
        return step(traversal, classPredicateFunction.apply(klass), nextDirection, indexOf(traversal, step));
    }

    public static <S extends Step> Iterable<S> nextSteps(Traversal<?, ?> traversal, Step step, Predicate<Step> predicate) {
        return steps(traversal, predicate, falsePredicate, nextDirection, indexOf(traversal, step));
    }

    public static <S extends Step> Iterable<S> nextSteps(Traversal<?, ?> traversal, Step step, Class<S> klass) {
        return steps(traversal, classPredicateFunction.apply(klass), falsePredicate, nextDirection, indexOf(traversal, step));
    }

    public static <S extends Step> Iterable<S> nextConsecutiveSteps(Traversal<?, ?> traversal, Step step, Predicate<Step> predicate) {
        return steps(traversal, predicate, negatePredicateFunction.apply(predicate), nextDirection, indexOf(traversal, step));
    }

    public static <S extends Step> Iterable<S> nextConsecutiveSteps(Traversal<?, ?> traversal, Step step, Class<S> klass) {
        return steps(traversal, classPredicateFunction.apply(klass), negatePredicateFunction.apply(classPredicateFunction.apply(klass)), nextDirection, indexOf(traversal, step));
    }

    public static <S extends Step> Optional<S> prev(Traversal<?, ?> traversal, Step step, Predicate<Step> predicate) {
        return step(traversal, predicate, prevDirection, indexOf(traversal, step));
    }

    public static <S extends Step> Optional<S> prev(Traversal<?, ?> traversal, Step step, Class<S> klass) {
        return step(traversal, classPredicateFunction.apply(klass), prevDirection, indexOf(traversal, step));
    }

    public static <S extends Step> Iterable<S> prevSteps(Traversal<?, ?> traversal, Step step, Predicate<Step> predicate) {
        return steps(traversal, predicate, falsePredicate, prevDirection, indexOf(traversal, step));
    }

    public static <S extends Step> Iterable<S> prevSteps(Traversal<?, ?> traversal, Step step, Class<S> klass) {
        return steps(traversal, classPredicateFunction.apply(klass), falsePredicate, prevDirection, indexOf(traversal, step));
    }

    public static <S extends Step> Iterable<S> prevConsecutiveSteps(Traversal<?, ?> traversal, Step step, Predicate<Step> predicate) {
        return steps(traversal, predicate, negatePredicateFunction.apply(predicate), prevDirection, indexOf(traversal, step));
    }

    public static <S extends Step> Iterable<S> prevConsecutiveSteps(Traversal<?, ?> traversal, Step step, Class<S> klass) {
        return steps(traversal, classPredicateFunction.apply(klass), negatePredicateFunction.apply(classPredicateFunction.apply(klass)), prevDirection, indexOf(traversal, step));
    }

    public static <S extends Step> Optional<S> last(Traversal<?, ?> traversal, Predicate<Step> predicate) {
        return step(traversal, predicate, prevDirection, traversal.asAdmin().getSteps().size());
    }

    public static <S extends Step> Optional<S> last(Traversal<?, ?> traversal, Class<?> klass) {
        return step(traversal, classPredicateFunction.apply(klass), prevDirection, traversal.asAdmin().getSteps().size());
    }

    public static <S extends Step> Iterable<S> lastSteps(Traversal<?, ?> traversal, Predicate<Step> predicate) {
        return steps(traversal, predicate, falsePredicate, prevDirection, traversal.asAdmin().getSteps().size());
    }

    public static <S extends Step> Iterable<S> lastSteps(Traversal<?, ?> traversal, Class<S> klass) {
        return steps(traversal, classPredicateFunction.apply(klass), falsePredicate, prevDirection, traversal.asAdmin().getSteps().size());
    }

    public static <S extends Step> Iterable<S> lastConsecutiveSteps(Traversal<?, ?> traversal, Predicate<Step> predicate) {
        return steps(traversal, predicate, negatePredicateFunction.apply(predicate), prevDirection, traversal.asAdmin().getSteps().size());
    }

    public static <S extends Step> Iterable<S> lastConsecutiveSteps(Traversal<?, ?> traversal, Class<S> klass) {
        return steps(traversal, classPredicateFunction.apply(klass), negatePredicateFunction.apply(classPredicateFunction.apply(klass)), prevDirection, traversal.asAdmin().getSteps().size());
    }

    public static <S extends Step> Optional<S> remove(Traversal<?, ?> traversal, S step) {
        traversal.asAdmin().removeStep(step);
        return Optional.of(step);
    }

    public static <S extends Step> Iterable<S> remove(Traversal<?, ?> traversal, Iterable<S> steps) {
        Stream.ofAll(steps).forEach(step -> traversal.asAdmin().removeStep(step));
        return steps;
    }

    public static boolean isBefore(Traversal<?, ?> traversal, Step step1, Step step2) {
        int indexOfStep1 = traversal.asAdmin().getSteps().indexOf(step1);
        int indexOfStep2 = traversal.asAdmin().getSteps().indexOf(step2);

        if (indexOfStep1 < 0) {
            return false;
        }

        return indexOfStep2 < 0 || indexOfStep1 < indexOfStep2;
    }

    public static boolean isAfter(Traversal<?, ?> traversal, Step step1, Step step2) {
        int indexOfStep1 = traversal.asAdmin().getSteps().indexOf(step1);
        int indexOfStep2 = traversal.asAdmin().getSteps().indexOf(step2);

        if (indexOfStep1 < 0 || indexOfStep2 < 0) {
            return false;
        }

        return indexOfStep1 > indexOfStep2;
    }
    //endregion

    //region Public $getters
    public static <S extends Step> S first$(Traversal<?, ?> traversal, Predicate<Step> predicate) {
        return TraversalUtil.<S>first(traversal, predicate).get();
    }

    public static <S extends Step> S first$(Traversal<?, ?> traversal, Class<S> klass) {
        return TraversalUtil.first(traversal, klass).get();
    }

    public static <S extends Step> S next$(Traversal<?, ?> traversal, Step step, Predicate<Step> predicate) {
        return TraversalUtil.<S>next(traversal, step, predicate).get();
    }

    public static <S extends Step> S next$(Traversal<?, ?> traversal, Step step, Class<S> klass) {
        return TraversalUtil.next(traversal, step, klass).get();
    }

    public static <S extends Step> S prev$(Traversal<?, ?> traversal, Step step, Predicate<Step> predicate) {
        return TraversalUtil.<S>prev(traversal, step, predicate).get();
    }

    public static <S extends Step> S prev$(Traversal<?, ?> traversal, Step step, Class<S> klass) {
        return TraversalUtil.prev(traversal, step, klass).get();
    }

    public static <S extends Step> S last$(Traversal<?, ?> traversal, Predicate<Step> predicate) {
        return TraversalUtil.<S>last(traversal, predicate).get();
    }

    public static <S extends Step> S last$(Traversal<?, ?> traversal, Class<S> klass) {
        return TraversalUtil.<S>last(traversal, klass).get();
    }
    //endregion

    //region Private Methods
    private static <S extends Step> Optional<S> step(
            Traversal<?, ?> traversal,
            Predicate<Step> stepPredicate,
            Function<Integer, Integer> direction,
            int startIndex) {

        for(int index = direction.apply(startIndex) ;
            index >= 0 && index < traversal.asAdmin().getSteps().size() ;
            index = direction.apply(index)) {
            Step step = traversal.asAdmin().getSteps().get(index);
            if (stepPredicate.test(step)) {
                return Optional.of((S)step);
            }
        }

        return Optional.empty();
    }

    private static <S extends Step> Iterable<S> steps(
            Traversal<?, ?> traversal,
            Predicate<Step> stepPredicate,
            Predicate<Step> breakPredicate,
            Function<Integer, Integer> direction,
            int startIndex) {
        List<S> steps = new ArrayList<>();

        for (int index = direction.apply(startIndex);
             index >= 0 && index < traversal.asAdmin().getSteps().size();
             index = direction.apply(index)) {
            Step step = traversal.asAdmin().getSteps().get(index);

            if (stepPredicate.test(step)) {
                steps.add((S) step);
            }

            if (breakPredicate.test(step)) {
                break;
            }
        }

        return steps;
    }

    private static int indexOf(Traversal<?, ?> traversal, Step step) {
        List<Step> steps = traversal.asAdmin().getSteps();
        for(int index = 0 ; index < steps.size() ; index++) {
            Step currentStep = steps.get(index);

            if (currentStep.getId().equals(step.getId())) {
                return index;
            }
        }

        return -1;
    }
    //endregion

    //region Static members
    private static Function<Integer, Integer> nextDirection = a -> a + 1;
    private static Function<Integer, Integer> prevDirection = a -> a - 1;

    private static Function<Class<?>, Predicate<Step>> classPredicateFunction =
            klass -> step -> klass.isAssignableFrom(step.getClass());

    private static Function<Predicate<Step>, Predicate<Step>> negatePredicateFunction =
            predicate -> step -> !predicate.test(step);

    private static Predicate<Step> truePredicate = step -> true;
    private static Predicate<Step> falsePredicate = step -> false;
    //endregion
}
