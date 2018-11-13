package com.kayhut.fuse.epb.plan.query;

/*-
 * #%L
 * fuse-dv-epb
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

import com.kayhut.fuse.dispatcher.query.QueryTransformer;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgQueryVisitor;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import javaslang.collection.Stream;

import java.util.*;
import java.util.function.Predicate;

public class AsgUnionSplitQueryTransformer implements QueryTransformer<AsgQuery, Iterable<AsgQuery>> {
    private QueryTransformer<AsgQuery, AsgQuery> queryTransformer;
    //region QueryTransformer Implementation

    public AsgUnionSplitQueryTransformer(QueryTransformer<AsgQuery, AsgQuery> queryTransformer) {
        this.queryTransformer = queryTransformer;
    }

    @Override
    public Iterable<AsgQuery> transform(AsgQuery query) {
        return Stream.ofAll(new PermutationVisitor(Collections.emptyMap()).visit(query.getStart()))
                .map(permutation -> AsgQueryUtil.transform(
                        query.getStart(),
                        asgEBase -> AsgEBase.Builder.get().withEBase(skipPermutationStops(asgEBase, permutation).geteBase()).build(),
                        asgEBase -> true,
                        AsgEBase::getB,
                        asgEBase -> skipPermutationStops(asgEBase, permutation).getNext()))
                .map(permutationQueryStart -> AsgQuery.AsgQueryBuilder.anAsgQuery()
                        .withName(query.getName())
                        .withOnt(query.getOnt())
                        .withStart((AsgEBase<Start>) (AsgEBase<?>) permutationQueryStart)
                        .withElements(new ArrayList<>(AsgQueryUtil.elements(permutationQueryStart)))
                        .withParams(query.getParameters())
                        .build())
                .map(q -> queryTransformer.transform(q))
                .toJavaList();
    }
    //endregion

    //region private methods
    private static AsgEBase<? extends EBase> skipPermutationStops(AsgEBase<? extends EBase> asgEBase, Map<Integer, Integer> permutation) {
        AsgEBase<? extends EBase> currentElement = asgEBase;
        while (permutation.containsKey(currentElement.geteNum())) {
            for (AsgEBase<? extends EBase> childEBase : currentElement.getNext()) {
                if (childEBase.geteNum() == permutation.get(currentElement.geteNum())) {
                    currentElement = childEBase;
                    break;
                }
            }
        }
        return currentElement;
    }
    //endregion

    //region PermutationVisitor
    public static class PermutationVisitor extends AsgQueryVisitor<Set<Map<Integer, Integer>>> {
        //region Constructors
        public PermutationVisitor(Map<Integer, Integer> currentPermutation) {
            super(
                    asgEBase -> leafPredicate.test(asgEBase),
                    asgEBase -> Collections.singleton(currentPermutation),
                    asgEBase -> true,
                    asgEBase -> Collections.emptyList(),
                    AsgEBase::getNext,
                    asgEBase -> Collections.emptySet(),
                    asgEBase -> someQuantPredicate.test(asgEBase.getParents().get(0)) ?
                            new PermutationVisitor(new MapBuilder<>(currentPermutation)
                                    .put(asgEBase.getParents().get(0).geteNum(), asgEBase.geteNum()).get())
                                    .visit(asgEBase) :
                            new PermutationVisitor(currentPermutation).visit(asgEBase),
                    PermutationVisitor::consolidatePermutations,
                    PermutationVisitor::consolidatePermutations);
        }
        //endregion

        //region Private Methods
        private static Set<Map<Integer, Integer>> consolidatePermutations(Set<Map<Integer, Integer>> permutations1, Set<Map<Integer, Integer>> permutations2) {
            if (permutations1 == null && permutations2 != null) {
                return permutations2;
            }

            if (permutations2 == null && permutations1 != null) {
                return permutations1;
            }

            if (permutations1 == null && permutations2 == null) {
                return Collections.emptySet();
            }

            Set<Map<Integer, Integer>> consolidatedPermutations = new HashSet<>();

            for (Map<Integer, Integer> permutation1 : permutations1) {
                for (Map<Integer, Integer> permutation2 : permutations2) {

                    boolean areDisjoint = true;
                    for (Integer permutation2Key : permutation2.keySet()) {
                        if (permutation1.keySet().contains(permutation2Key)) {
                            areDisjoint = false;
                            break;
                        }
                    }

                    if (areDisjoint) {
                        consolidatedPermutations.add(new MapBuilder<>(permutation1).putAll(permutation2).get());
                    } else {
                        consolidatedPermutations.add(permutation1);
                        consolidatedPermutations.add(permutation2);
                    }
                }
            }

            return consolidatedPermutations;
        }
        //endregion
    }
    //endregion

    //region Fields
    private static Predicate<AsgEBase<? extends EBase>> someQuantPredicate = asgEBase ->
            asgEBase.geteBase().getClass().equals(Quant1.class) &&
                    ((Quant1) asgEBase.geteBase()).getqType().equals(QuantType.some);

    private static Predicate<AsgEBase<? extends EBase>> leafPredicate = asgEBase -> asgEBase.getNext().isEmpty();
    //endregion
}
