package com.yangdb.fuse.asg.strategy.type;

/*-
 * #%L
 * fuse-asg
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



import com.yangdb.fuse.asg.strategy.AsgStrategy;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.Tagged;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.RelPattern;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.EndPattern;
import com.yangdb.fuse.model.query.properties.BaseProp;
import com.yangdb.fuse.model.query.properties.BasePropGroup;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantBase;
import com.yangdb.fuse.model.query.quant.QuantType;
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
        AtomicInteger counter = new AtomicInteger(Stream.ofAll(AsgQueryUtil.eNums(query)).max().get());
        Stream.ofAll(AsgQueryUtil.elements(query, RelPattern.class))
                .forEach(relPattern -> {
                    //get end Pattern entity - should exist according to the validation
                    final Optional<AsgEBase<EndPattern>> endPattern = AsgQueryUtil.nextDescendant(relPattern, EndPattern.class);
                    //get parent element of type Entity
                    Optional<AsgEBase<EEntityBase>> parent = AsgQueryUtil.ancestor(relPattern, EEntityBase.class);
                    //if not present something is wrong with the query  - the validator should inform this
                    if (parent.isPresent() && endPattern.isPresent()) {
                        //check is there is a Or type quant in between rel & parent
                        List<AsgEBase<? extends EBase>> path = AsgQueryUtil.path(parent.get(), relPattern);
                        Optional<AsgEBase<Quant1>> quant = path.stream().filter(p -> p.geteBase() instanceof QuantBase).map(q -> (AsgEBase<Quant1>) q).findFirst();

                        //no quant present - create one for the inner or
                        if (!quant.isPresent()) {
                            // add quant of 'Or' type after the rel
                            Quant1 newQuant = new Quant1(counter.incrementAndGet(), QuantType.some);
                            AsgEBase<Quant1> quantAsg = new AsgEBase<>(newQuant);
                            addRelPattern(counter, query, quantAsg, relPattern, endPattern.get());
                            //remove pattern
                            parent.get().removeNextChild(relPattern);
                            parent.get().addNext(quantAsg);
                        } else if (quant.get().geteBase().getqType().equals(QuantType.all)) {
                            // quant of type all exist -> replace with an OR quant and add condition to all union parts
                            Quant1 newQuant = new Quant1(counter.incrementAndGet(), QuantType.some);
                            AsgEBase<Quant1> quantAsg = new AsgEBase<>(newQuant);
                            addRelPattern(counter, query, quantAsg, relPattern, endPattern.get());
                            //remove pattern
                            quant.get().removeNextChild(relPattern);
                            //add or quant to containing And quant
                            quant.get().addNext(quantAsg);
                        } else {
                            // quant of type some exist -> add the inner or patterns after the quant
                            addRelPattern(counter, query, quant.get(), relPattern,endPattern.get());
                            //remove pattern
                            parent.get().removeNextChild(relPattern);
                        }
                    }
                });

        //replace all EndPatterns with its internal real entity
        AsgQueryUtil.elements(query, EndPattern.class)
                .forEach(p-> ((AsgEBase) p).seteBase(p.geteBase().getEndEntity()));

    }

    /**
     * add a number of steps according to the given range in the rel pattern
     *
     * @param query
     * @param quantAsg
     * @param relPattern
     */
    private void addRelPattern(AtomicInteger counter, AsgQuery query, AsgEBase<Quant1> quantAsg, AsgEBase<RelPattern> relPattern, AsgEBase<EndPattern> endPattern) {
        Range range = relPattern.geteBase().getLength();
        //duplicate the rel pattern according to the range, range should already be validated by the validator
        LongStream.rangeClosed(range.getLower() , range.getUpper())
                //this is the Root some quant all pattern premutations will be added to...
                .forEach(value -> {
                    //if value == 0  remove the RelPattern entirely
                    if(value==0) {
                        //todo remove RelPattern & EndPattern
                         //final AsgEBase<? extends EBase> simplifiedPath = AsgQueryUtil.remove(query,relPattern,endPattern);
                         // quantAsg.addNext(simplifiedPath);

                    } else {
                        final AsgEBase<? extends EBase> relConcretePattern = addPath(counter, value, relPattern, endPattern);
                        //add the path after the end pattern section
                        if (endPattern.hasNext()) {
                            final AsgEBase<? extends EBase> nextAfterEndPattern = endPattern.getNext().get(0);
                            final AsgEBase<? extends EBase> afterEndPattern = AsgQueryUtil.deepCloneWithEnums(counter, nextAfterEndPattern, e -> true, e -> true);
                            //get last Descendant of same type of end pattern
                            final List<AsgEBase<EBase>> endElements = AsgQueryUtil.nextDescendants(relConcretePattern, EndPattern.class);
                            endElements.get(endElements.size() - 1).addNext(afterEndPattern);
                        }
                        quantAsg.addNext(relConcretePattern);
                    }
                });
    }

    /**
     * rel pattern premutation generator
     * @param value
     * @param relPattern
     * @param endPattern
     * @return
     */
    private AsgEBase<? extends EBase> addPath(AtomicInteger counter,long value, AsgEBase<RelPattern> relPattern, AsgEBase<EndPattern> endPattern) {
        final AtomicReference<AsgEBase<? extends EBase>> current = new AtomicReference<>();
        LongStream.rangeClosed(1, value)
                .forEach(step -> {
                    AsgEBase<? extends EBase> node = buildStep(counter,relPattern, endPattern);
                    if (current.get() == null) {
                        current.set(node);
                    } else {
                        //the build step returns a cloned pattern  of [rel:Rel]-....->(endPattern:Entity)->...
                        //
                        if(!(current.get().geteBase() instanceof QuantBase)) {
                            final AsgEBase<Quant1> quant = new AsgEBase<>(new Quant1(counter.incrementAndGet(), QuantType.all));
                            AsgQueryUtil.addAsNext(quant,current.get());
                            current.set(quant);
                        } else {
                            //knowing that the rel pattern has a shape of a line not a tree get the last Descendant
                            current.set(AsgQueryUtil.nextDescendant(current.get(),EndPattern.class).get());
                        }
                        current.get().addNext(AsgQueryUtil.ancestorRoot(node).get());
                        current.set(node);
                    }
                });
        //get first node in the path to add to the containing quant
        return AsgQueryUtil.ancestorRoot(current.get()).get();
    }


    /**
     * build a new complete rel->pattern step cloned from existing step
     * @param relPattern
     * @param endPattern
     * @return
     */
    private AsgEBase<? extends EBase> buildStep(AtomicInteger counter,AsgEBase<RelPattern> relPattern, AsgEBase<EndPattern> endPattern) {

        RelPattern pattern = relPattern.geteBase();
        List<AsgEBase<? extends EBase>> belowList = new ArrayList<>(relPattern.getB());

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

        // a valid rel patten must have a single next element
        final AsgEBase<? extends EBase> nextAfterRel = relPattern.getNext().get(0);
        final AsgEBase<? extends EBase> clonedPath = clonePath(counter, nextAfterRel, endPattern);

        //add new path to new rel
        relAsg.addNext(clonedPath);
        return clonedPath;
    }


    public static AsgEBase<? extends EBase> clonePath(AtomicInteger counter, AsgEBase<? extends EBase> from, AsgEBase<EndPattern> to) {
        return AsgQueryUtil.deepCloneWithEnums(counter,
                from,
                e -> {
                    if (e.geteBase() instanceof BaseProp || e.geteBase() instanceof BasePropGroup) {
                        List<AsgEBase<? extends EBase>> list = AsgQueryUtil.pathToNextDescendant(from, to.geteNum());
                        list.remove(to);
                        return list.contains(e.getParents().get(0));
                    }
                    return AsgQueryUtil.pathToNextDescendant(from, to.geteNum()).contains(e);
                }, e -> true);
    }
    //endregion

}
