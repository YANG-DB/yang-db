package com.kayhut.fuse.model.execution.plan.descriptors;

/*-
 * #%L
 * AsgQueryDescriptor.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.*;
import com.kayhut.fuse.model.query.quant.QuantBase;

import javax.management.relation.Relation;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by roman.margolis on 28/11/2017.
 */
public class AsgQueryDescriptor implements Descriptor<AsgQuery> {
    //region Descriptor Implementation
    @Override
    public String describe(AsgQuery query) {
        return patternValue(query);
    }
    //endregion

    //region Private Methods
    private String patternValue(AsgQuery query) {
        List<AsgEBase<? extends EBase>> elements = new ArrayList<>(query.getElements());

        StringJoiner joiner = new StringJoiner(":", "", "");
        for (int i = 0; i < elements.size(); i++) {
            AsgEBase<? extends EBase> e = elements.get(i);
            if (e.geteBase() instanceof QuantBase) {
                List<AsgEBase<? extends EBase>> next = e.getNext();
                String join = next.stream().map(n -> Integer.toString(n.geteNum())).collect(Collectors.joining("|"));
                joiner.add(e.geteBase().getClass().getSimpleName() + "[" + e.geteNum() + "]").add("{" + join + "}");
            } else if (e.geteBase() instanceof EEntityBase) {
                String prefix = "";
                if ((i > 0 && ((elements.get(i - 1).geteBase() instanceof RelPropGroup) || (elements.get(i - 1).geteBase() instanceof Rel)))) {
                    prefix = "==>";
                }
                joiner.add(prefix + EEntityBase.class.getSimpleName() + "[" + e.geteNum() + "]");
            } else if (e.geteBase() instanceof Rel)
                joiner.add("==>" + Relation.class.getSimpleName() + "(" + e.geteNum() + ")");
            else if (e.geteBase() instanceof EProp)
                joiner.add("?" + "[" + e.geteNum() + "]" + QueryDescriptor.printProps(new EPropGroup((EProp) e.geteBase())));
            else if (e.geteBase() instanceof EPropGroup)
                joiner.add("?" + "[" + e.geteNum() + "]" + QueryDescriptor.printProps((EPropGroup) e.geteBase()));
            else if (e.geteBase() instanceof RelProp)
                joiner.add("?" + "[" + e.geteNum() + "]" + QueryDescriptor.printProps(new RelPropGroup((RelProp) e.geteBase())));
            else if (e.geteBase() instanceof RelPropGroup)
                joiner.add("?" + "[" + e.geteNum() + "]" + QueryDescriptor.printProps((RelPropGroup) e.geteBase()));
            else
                joiner.add(e.geteBase().getClass().getSimpleName() + "[" + e.geteNum() + "]");
        }
        return joiner.toString();
    }

    public static String toString(AsgQuery query) {
        return new AsgQueryDescriptor().describe(query);
    }

    static <T extends EBase> void print(List<String> builder, Optional<AsgEBase<T>> item, boolean isTail, boolean child, int level, int currentLine) {
        if (!item.isPresent()) return;
        AsgEBase<T> element = item.get();

        String text = QueryDescriptor.getPrefix(isTail, element.geteBase()) + shortLabel(element, new StringJoiner(":"));
        if (child) {
            char[] zeros = new char[builder.get(level - 1).length() - 5];
            Arrays.fill(zeros, ' ');
            builder.add("\n" + String.valueOf(zeros) + text);
        } else {
            builder.set(level + currentLine, builder.get(level + currentLine) + text);
        }

        if (element.hasNext()
                || element.geteBase() instanceof BasePropGroup
                || element.getB().stream().filter(p -> (p.geteBase() instanceof BasePropGroup)).findAny().isPresent()) {
            if (element.geteBase() instanceof QuantBase) {
                List<AsgEBase<? extends EBase>> next = element.getNext();
                level = builder.size();
                for (int i = 0; i < next.size(); i++) {
                    print(builder, Optional.of(next.get(i)), true, true, level, i);
                }
            } else if (element.geteBase() instanceof EEntityBase) {
                print(builder, element.getNext().isEmpty() ? Optional.empty() : Optional.of(element.getNext().get(0)), element.getNext().isEmpty(), false, level, currentLine);
            } else if (element.geteBase() instanceof Rel) {
                final Optional<AsgEBase<? extends EBase>> bellow = element.getB().stream().filter(p -> (p.geteBase() instanceof BasePropGroup)).findAny();
                if (bellow.isPresent()) {
                    print(builder, bellow.isPresent() ? Optional.of(bellow.get()) : Optional.empty(), true, true, level + 1, currentLine);
                }
                print(builder, element.getNext().isEmpty() ? Optional.empty() : Optional.of(element.getNext().get(0)), element.getNext().isEmpty(), false, level, currentLine);
            } else if (element.geteBase() instanceof EProp) {
                print(builder, element.getNext().isEmpty() ? Optional.empty() : Optional.of(element.getNext().get(0)), true, true, level + 1, currentLine);
            } else if (element.geteBase() instanceof RelProp) {
                print(builder, element.getNext().isEmpty() ? Optional.empty() : Optional.of(element.getNext().get(0)), true, true, level + 1, currentLine);
            } else if (element.geteBase() instanceof BasePropGroup) {
                //print props
                level = builder.size();
                final List<EProp> props = ((BasePropGroup) element.geteBase()).getProps();
                for (int i = 0; i < props.size(); i++) {
                    print(builder, Optional.of(new AsgEBase<>(props.get(i))), true, true, level, i);
                }

                //print group's props
                level = builder.size();
                final List<BasePropGroup> groups = ((BasePropGroup) element.geteBase()).getGroups();
                for (int i = 0; i < groups.size(); i++) {
                    print(builder, Optional.of(new AsgEBase<>(groups.get(i))), true, true, level, i);
                }
            }
        }
    }

    static String shortLabel(AsgEBase<? extends EBase> e, StringJoiner joiner) {
        final EBase eBase = e.geteBase();
        if (eBase instanceof QuantBase) {
            List<AsgEBase<? extends EBase>> next = e.getNext();
            String join = next.stream().map(p -> Integer.toString(p.geteNum())).collect(Collectors.joining("|"));
            joiner.add("Q" + "[" + e.geteNum() + ":" + ((QuantBase) eBase).getqType() + "]").add("{" + join + "}");
        } else if (eBase instanceof EUntyped)
            joiner.add("UnTyp" + "[" + ":" + ((EUntyped) eBase).getvTypes() + " " + ((EUntyped) eBase).geteTag() + "#" + e.geteNum() + "]");
        else if (eBase instanceof EConcrete)
            joiner.add("Conc" + "[" + ":" + ((EConcrete) eBase).geteType() + " " + ((EConcrete) eBase).geteTag() + "#" + e.geteNum() + "]");
        else if (eBase instanceof ETyped)
            joiner.add("Typ" + "[" + ":" + ((ETyped) eBase).geteType() + " " + ((ETyped) eBase).geteTag() + "#" + e.geteNum() + "]");
        else if (eBase instanceof Rel)
            joiner.add("Rel" + "(" + ":" + ((Rel) eBase).getrType() + " " + ((Rel) eBase).getWrapper() + "#" + e.geteNum() + ")");
        else if (eBase instanceof EPropGroup)
            joiner.add("?[..]" + "[" + e.geteNum() + "]" + ((eBase instanceof RankingProp) ?
                    "boost:" + ((RankingProp) eBase).getBoost() : ""));
        else if (eBase instanceof RelPropGroup)
            joiner.add("?[..]" + "[" + e.geteNum() + "]" + ((eBase instanceof RankingProp) ?
                    "boost:" + ((RankingProp) eBase).getBoost() : ""));
        else if (eBase instanceof EProp)
            joiner.add("?" + "[" + e.geteNum() + "]" + QueryDescriptor.printProps(new EPropGroup((EProp) eBase)));
        else if (eBase instanceof RelProp)
            joiner.add("?" + "[" + e.geteNum() + "]" + QueryDescriptor.printProps(new RelPropGroup((RelProp) eBase)));
        else
            joiner.add(e.getClass().getSimpleName() + "[" + e.geteNum() + "]");

        return joiner.toString();
    }

    public static String print(AsgQuery query) {
        List<String> builder = new LinkedList<>();
        builder.add("└── " + "Start");
        Iterator<AsgEBase<EBase>> iterator = AsgQueryUtil.elements(query).iterator();
        if (iterator.hasNext()) {
            iterator.next();
            if (iterator.hasNext())
                print(builder, Optional.of(iterator.next()), false, true, 1, 0);
        }
        return builder.toString();
    }

    public static String print(AsgEBase<? extends EBase> element) {
        List<String> builder = new LinkedList<>();
        builder.add("└── ");
        print(builder, Optional.of(element), false, false, 0, 0);
        return builder.toString();
    }

    //endregion
}
