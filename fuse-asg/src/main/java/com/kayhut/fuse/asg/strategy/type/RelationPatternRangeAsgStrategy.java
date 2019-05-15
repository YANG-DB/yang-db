package com.kayhut.fuse.asg.strategy.type;

/*-
 * #%L
 * fuse-asg
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

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.model.Range;
import com.kayhut.fuse.model.Tagged;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.RelPattern;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantBase;
import com.kayhut.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * expand given relation range pattern into an Or quant with all premutations of requested path length
 * Example:
 * (:E)-[:R | 1..3]->(:E) would be transformed into:
 * <p>
 * (:E)-Quant[OR]
 * -[:R]->(:E)
 * -[:R]->(:E)-[:R]->(:E)
 * -[:R]->(:E)-[:R]->(:E)-[:R]->(:E)
 */
public class RelationPatternRangeAsgStrategy implements AsgStrategy {

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        counter = new AtomicInteger(Stream.ofAll(AsgQueryUtil.eNums(query)).max().get());
        Stream.ofAll(AsgQueryUtil.elements(query, RelPattern.class))
                .forEach(relPattern -> {
                    //get parent element of type Entity
                    Optional<AsgEBase<EBase>> parent = AsgQueryUtil.ancestor(relPattern, EEntityBase.class);
                    //if not present something is wrong with the query  - the validator should inform this
                    if (parent.isPresent()) {
                        //check is there is a Or type quant in between rel & parent
                        List<AsgEBase<? extends EBase>> path = AsgQueryUtil.path(parent.get(), relPattern);
                        Optional<AsgEBase<Quant1>> quant = path.stream().filter(p -> p.geteBase() instanceof QuantBase).map(q -> (AsgEBase<Quant1>) q).findFirst();
                        if (!quant.isPresent()) {
                            // add quant of 'Or' type after the rel
                            Quant1 newQuant = new Quant1(counter.incrementAndGet(), QuantType.some);
                            AsgEBase<Quant1> quantAsg = new AsgEBase<>(newQuant);
                            addRelPattern(query, quantAsg, relPattern);
                            //remove pattern
                            parent.get().removeNextChild(relPattern);
                            parent.get().addNext(quantAsg);
                        } else if (quant.get().geteBase().getqType().equals(QuantType.all)) {
                            // if quant of type all exist -> replace with an OR quant and add condition to all union parts
                            Quant1 newQuant = new Quant1(counter.incrementAndGet(), QuantType.some);
                            AsgEBase<Quant1> quantAsg = new AsgEBase<>(newQuant);
                            addRelPattern(query, quantAsg, relPattern);
                            //remove pattern
                            quant.get().removeNextChild(relPattern);
                            //add or quant to containing And quant
                            quant.get().addNext(quantAsg);
                        } else {
                            //add the or patterns right after the quant
                            addRelPattern(query, quant.get(), relPattern);
                            //remove pattern
                            parent.get().removeNextChild(relPattern);
                        }
                    }
                });

    }

    /**
     * add a number of steps according to the given range in the rel pattern
     *
     * @param query
     * @param quantAsg
     * @param relPattern
     */
    private void addRelPattern(AsgQuery query, AsgEBase<Quant1> quantAsg, AsgEBase<RelPattern> relPattern) {
        Range range = relPattern.geteBase().getLength();
        AsgEBase<? extends EBase> next = relPattern.getNext().get(0);
        //detach from original next
        List<AsgEBase<? extends EBase>> nextList = new ArrayList<>(next.getNext());
        nextList.forEach(next::removeNextChild);
        //detach from original rel pattern
        List<AsgEBase<? extends EBase>> belowList = new ArrayList<>(relPattern.getB());
        belowList.forEach(relPattern::removeBChild);

        LongStream.rangeClosed(range.getLower() > 0 ? range.getLower() : 1, range.getUpper())
                .forEach(value -> {
                    quantAsg.addNext(addPath(value, relPattern, belowList, next, nextList));
                });
    }

    private AsgEBase addPath(long value, AsgEBase<RelPattern> relPattern, List<AsgEBase<? extends EBase>> belowList, AsgEBase<? extends EBase> next, List<AsgEBase<? extends EBase>> nextNextList) {
        final AtomicReference<AsgEBase<? extends EBase>> current = new AtomicReference<>();
        LongStream.rangeClosed(1, value)
                .forEach(step -> {
                    AsgEBase<? extends EBase> node = buildStep(relPattern, belowList, next, nextNextList);
                    if (current.get() == null) {
                        current.set(node);
                    } else {
                        //if not a quant -> make it one...
                        if(!(current.get().geteBase() instanceof QuantBase)) {
                            final AsgEBase<Quant1> quant = new AsgEBase<>(new Quant1(counter.incrementAndGet(), QuantType.all));
                            AsgQueryUtil.addAsNext(quant,current.get());
                            current.set(quant);
                        } else {
                            // find last EType entity in given pattern and add the quant to it...
/*
                            final AsgEBase<ETyped> lastEType = AsgQueryUtil.elements(node, ETyped.class).stream().sorted(Comparator.comparingInt(AsgEBase::geteNum)).collect(Collectors.toList()).get(0);
                            AsgEBase<EBase> quant = AsgQueryUtil.nextDescendant(lastEType, QuantBase.class).get();
                            current.set(quant);
*/
                        }
                        current.get().addNext(AsgQueryUtil.ancestorRoot(node).get());
                        current.set(node);
                    }
                });
        //get first node in the path to add to the containing quant
        return AsgQueryUtil.ancestorRoot(current.get()).get();
    }

    /**
     * build a new complete rel->Entity step cloned from existing step
     *
     * @param relPattern
     * @param belowList
     * @param next
     * @param nextNextList
     * @return
     */
    private AsgEBase<? extends EBase> buildStep(AsgEBase<RelPattern> relPattern,
                                                List<AsgEBase<? extends EBase>> belowList,
                                                AsgEBase<? extends EBase> next,
                                                List<AsgEBase<? extends EBase>> nextNextList) {
        RelPattern pattern = relPattern.geteBase();
        //duplicate rel
        AsgEBase<Rel> relAsg = new AsgEBase<>(new Rel(counter.incrementAndGet(), pattern.getrType(), pattern.getDir(),
                pattern.getWrapper() != null ? pattern.getWrapper() + "_" + counter.get() : null, 0, pattern.getB()));
        //clone below rel constraints and reset parent with tag (should be wrapper) and eunm
        List<AsgEBase<? extends EBase>> belowCollect = belowList.stream()
                .map(AsgEBase::clone)
                .peek(e -> {
                    e.geteBase().seteNum(counter.incrementAndGet());
                    e.setParent(new ArrayList<>(Arrays.asList(relAsg)));
                    //set new tag for the newly created element
                    if ((e instanceof Tagged) && ((Tagged) e).geteTag() != null) {
                        ((Tagged) e).seteTag(((Tagged) e).geteTag() + "_" + counter.get());
                    }
                }).collect(Collectors.toList());

        relAsg.below(belowCollect);
        //duplicate element next after rel
        EBase eBaseNext = next.geteBase().clone();
        eBaseNext.seteNum(counter.incrementAndGet());
        //set new tag for the newly created element
        if ((eBaseNext instanceof Tagged) && ((Tagged) eBaseNext).geteTag() != null) {
            ((Tagged) eBaseNext).seteTag(((Tagged) eBaseNext).geteTag() + "_" + counter.get());
        }

        //clone next with new enum
        AsgEBase<? extends EBase> nextAsg = new AsgEBase(eBaseNext);
        nextAsg.geteBase().seteNum(counter.incrementAndGet());

        //if no elements next - conclude
        if (nextNextList.isEmpty()) {
            relAsg.addNext(nextAsg);
            return nextAsg;
        }

        //if next is not a quant -> add a quant
        Optional<AsgEBase<? extends EBase>> nextQuanAsg = Optional.empty();
        if(!(eBaseNext instanceof QuantBase)) {
            nextQuanAsg = Optional.of(new AsgEBase<>(new Quant1(counter.incrementAndGet(),QuantType.all)));
            nextQuanAsg.get().addNext(nextAsg);
        }

        //clone paths for next's next list
        final List<AsgEBase<? extends EBase>> clonedNextNextList = nextNextList.stream()
                .map(p -> AsgQueryUtil.deepCloneWithEnums(counter, p, e -> true, e -> true))
                .collect(Collectors.toList());

        //add next list to quant/next element
        nextAsg.nextList(clonedNextNextList);
        //add quant/next element itself to the rel
        if(nextQuanAsg.isPresent()) {
            relAsg.addNext(nextQuanAsg.get());
        } else {
            relAsg.addNext(nextAsg);
        }
        return nextAsg;
    }

    private List<AsgEBase<? extends EBase>> deepClone(List<AsgEBase<? extends EBase>> next) {
        return null;
    }

    //endregion

    private AtomicInteger counter;
}
