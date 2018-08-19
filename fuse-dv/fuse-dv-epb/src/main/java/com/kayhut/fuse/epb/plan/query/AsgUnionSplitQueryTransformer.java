package com.kayhut.fuse.epb.plan.query;

import com.kayhut.fuse.dispatcher.query.QueryTransformer;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgQueryVisitor;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import javaslang.collection.Stream;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class AsgUnionSplitQueryTransformer implements QueryTransformer<AsgQuery, Iterable<AsgQuery>> {
    //region QueryTransformer Implementation
    @Override
    public Iterable<AsgQuery> transform(AsgQuery query) {
         List<AsgEBase<Quant1>> someQuants =
                 AsgQueryUtil.elements(query, asgEBase -> asgEBase.geteBase().getClass().equals(Quant1.class) &&
                ((Quant1)asgEBase.geteBase()).getqType().equals(QuantType.some));

         return null;
    }
    //endregion

    public static class PermutationVisitor extends AsgQueryVisitor<Set<Map<Integer, Integer>>> {

        public PermutationVisitor(Map<Integer, Integer> currentPermutation) {
            super(
                    asgEBase -> leafPredicate.test(asgEBase),
                    asgEBase -> true,
                    asgEBase -> Collections.emptyList(),
                    AsgEBase::getNext,
                    asgEBase -> Collections.emptySet(),
                    asgEBase -> someQuantPredicate.test(asgEBase.getParents().get(0)) ?
                            new PermutationVisitor(new MapBuilder<>(currentPermutation)
                            .put(asgEBase.getParents().get(0).geteNum(), asgEBase.geteNum()).get())
                            .visit(asgEBase) :
                            new PermutationVisitor(currentPermutation).visit(asgEBase),
                    asgEBase -> Collections.singleton(currentPermutation),
                    AsgUnionSplitQueryTransformer::consolidatePermutations);
        }
    }

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

        for(Map<Integer, Integer> permutation1 : permutations1) {
            for(Map<Integer, Integer> permutation2 : permutations2) {

                boolean areDisjoint = true;
                for(Integer permutation2Key : permutation2.keySet()) {
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

    //region Fields
    private static Predicate<AsgEBase<? extends EBase>> someQuantPredicate = asgEBase ->
            asgEBase.geteBase().getClass().equals(Quant1.class) &&
                    ((Quant1)asgEBase.geteBase()).getqType().equals(QuantType.some);

    private static Predicate<AsgEBase<? extends EBase>> leafPredicate = asgEBase -> asgEBase.getNext().isEmpty();
    //endregion
}
