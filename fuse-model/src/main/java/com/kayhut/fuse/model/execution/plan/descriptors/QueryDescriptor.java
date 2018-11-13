package com.kayhut.fuse.model.execution.plan.descriptors;

/*-
 * #%L
 * QueryDescriptor.java - fuse-model - kayhut - 2,016
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

import com.kayhut.fuse.model.Next;
import com.kayhut.fuse.model.asgQuery.IQuery;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.*;
import com.kayhut.fuse.model.query.quant.QuantBase;
import javaslang.collection.Stream;

import java.util.*;
import java.util.stream.Collectors;

import static com.kayhut.fuse.model.query.Query.QueryUtils.findByEnum;

public class QueryDescriptor implements Descriptor<Query> {
    //region Descriptor Implementation
    @Override
    public String describe(Query query) {
        return patternValue(query);
    }
    //endregion

    //region Private Methods
    private String patternValue(Query query) {
        Collection<EBase> elements = query.getElements() != null ? query.getElements() : Collections.EMPTY_LIST;
        StringJoiner joiner = new StringJoiner(":", "", "");
        elements.forEach(e -> label(e, joiner));
        return joiner.toString();
    }

    static String label(EBase e, StringJoiner joiner) {
        if (e instanceof QuantBase) {
            List<Integer> next = ((Next<List>) e).getNext();
            String join = next.stream().map(Object::toString).collect(Collectors.joining("|"));
            joiner.add(e.getClass().getSimpleName() + "[" + e.geteNum() + "]").add("{" + join + "}");
        } else
            joiner.add(e.getClass().getSimpleName() + "[" + e.geteNum() + "]");

        return joiner.toString();
    }

    static String shortLabel(EBase e, StringJoiner joiner) {
        if (e instanceof QuantBase) {
            List<Integer> next = ((Next<List>) e).getNext();
            String join = next.stream().map(Object::toString).collect(Collectors.joining("|"));
            joiner.add("Q" + "[" + e.geteNum() + "]").add("{" + join + "}");
        } else if (e instanceof EUntyped)
            joiner.add("UnTyp" + "[" + e.geteNum() + "]");
        else if (e instanceof EConcrete)
            joiner.add("Conc" + "[" + ((EConcrete) e).geteType() + ":" + e.geteNum() + "]");
        else if (e instanceof ETyped)
            joiner.add("Typ" + "[" + ((ETyped) e).geteType() + ":" + e.geteNum() + "]");
        else if (e instanceof Rel)
            joiner.add("Rel" + "(" + ((Rel) e).getrType() + ":" + e.geteNum() + ")");
        else if (e instanceof EPropGroup)
            joiner.add("?" + "[" + e.geteNum() + "]" + printProps((EPropGroup) e));
        else if (e instanceof EProp)
            joiner.add("?" + "[" + e.geteNum() + "]" + printProps(new EPropGroup((EProp) e)));
        else if (e instanceof RelProp)
            joiner.add("?" + "[" + e.geteNum() + "]" + printProps(new RelPropGroup((RelProp) e)));
        else if (e instanceof RelPropGroup)
            joiner.add("?" + "[" + e.geteNum() + "]" + printProps((RelPropGroup) e));
        else
            joiner.add(e.getClass().getSimpleName() + "[" + e.geteNum() + "]");

        return joiner.toString();
    }

    static String printProps(RelPropGroup propGroup) {
        String[] pStrings = Stream.ofAll(propGroup.getProps())
                .map(p -> {
                    if (p.getCon() != null) {
                        return p.getpType() + "<" + p.getCon().getOp() + "," + p.getCon().getExpr() + ">";
                    } else if (p.getProj() != null) {
                        return p.getpType() + "<" + p.getProj().getClass().getSimpleName() + ">";
                    } else {
                        return p.getpType();
                    }
                }).toJavaArray(String.class);

        return ":" + Arrays.toString(pStrings);
    }

    static String printProps(EPropGroup propGroup) {
        String[] pStrings = Stream.ofAll(propGroup.getProps())
                .map(QueryDescriptor::printProp).toJavaArray(String.class);

        return ":" + Arrays.toString(pStrings);
    }

    static String printProp(EProp p) {
        if (p instanceof RankingProp) {
            return "boost:"+((RankingProp) p).getBoost() +"  " + p.getpType() + "<" + p.getCon().getOp() + "," + p.getCon().getExpr() + ">";
        } else if (p.getCon() != null) {
            return p.getpType() + "<" + p.getCon().getOp() + "," + p.getCon().getExpr() + ">";
        } else if (p.getProj() != null) {
            return p.getpType() + "<" + p.getProj().getClass().getSimpleName() + ">";
        } else {
            return p.getpType();
        }
    }
    //endregion

    public static String toString(Query query) {
        return new QueryDescriptor().describe(query);
    }

    static void print(List<String> builder, IQuery<EBase> query, Optional<? extends EBase> element, boolean isTail, boolean child, int level, int currentLine) {
        if (!element.isPresent()) return;
        String text = getPrefix(isTail, element.get()) + shortLabel(element.get(), new StringJoiner(":"));
        if (child) {
            char[] zeros = new char[builder.get(level - 1).length() - 5];
            Arrays.fill(zeros, ' ');
            builder.add("\n" + String.valueOf(zeros) + text);
        } else {
            builder.set(level + currentLine, builder.get(level + currentLine) + text);
        }


        if (Next.class.isAssignableFrom(element.get().getClass())) {
            if (element.get() instanceof QuantBase) {
                List<Integer> next = ((QuantBase) element.get()).getNext();
                level = builder.size();
                for (int i = 0; i < next.size(); i++) {
                    print(builder, query, findByEnum(query, next.get(i)), true, true, level, i);
                }
            } else if (element.get() instanceof EEntityBase) {
                print(builder, query, findByEnum(query, ((EEntityBase) element.get()).getNext()), !((Next) element.get()).hasNext(), false, level, currentLine);
            } else if (element.get() instanceof Rel) {
                print(builder, query, findByEnum(query, ((Rel) element.get()).getNext()), !((Next) element.get()).hasNext(), false, level, currentLine);
            } else if (element.get() instanceof ScoreEProp ) {
                print(builder, query, element, true, true, level + 1, currentLine);
            } else if (element.get() instanceof EPropGroup ) {
                level = builder.size();
                for (int i = 0; i < ((ScoreEPropGroup) element.get()).getGroups().size(); i++) {
                    EPropGroup ePropGroup = ((ScoreEPropGroup) element.get()).getGroups().get(i);
                    print(builder, query, Optional.of(ePropGroup), true, true, level, i);
                }
            } else if (element.get() instanceof EProp ) {
                print(builder, query, element, true, true, level + 1, currentLine);
            } else if (element.get() instanceof RelProp || element.get() instanceof RelPropGroup) {
                print(builder, query, element, true, true, level + 1, currentLine);
            }
        }
    }

    public static String getPrefix(boolean isTail, EBase element) {
        String prefix = (isTail ? "└" : "-");
        if (element instanceof Rel) {
            String postfix = (((Rel) element).getDir().equals(Rel.Direction.R) ? "-> " : "<--");
            return prefix + postfix;
        } else
            return (isTail ? "└─" : "──");
    }

    public static String print(IQuery<EBase> query) {
        List<String> builder = new LinkedList<>();
        builder.add("└── " + "Start");
        Iterator<EBase> iterator = query.getElements().iterator();
        iterator.next();
        print(builder, query, Optional.of(iterator.next()), false, true, 1, 0);
        return builder.toString();
    }

}
