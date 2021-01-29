package com.yangdb.fuse.model.execution.plan.descriptors;

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
 * AsgQueryDescriptor.java - fuse-model - yangdb - 2,016
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

import com.yangdb.fuse.model.Container;
import com.yangdb.fuse.model.Next;
import com.yangdb.fuse.model.Tagged;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.descriptors.Descriptor;
import com.yangdb.fuse.model.descriptors.GraphDescriptor;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.entity.EUntyped;
import com.yangdb.fuse.model.query.optional.OptionalComp;
import com.yangdb.fuse.model.query.properties.*;
import com.yangdb.fuse.model.query.quant.QuantBase;
import com.yangdb.fuse.model.query.quant.QuantType;
import org.apache.commons.lang3.StringUtils;

import javax.management.relation.Relation;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.execution.plan.descriptors.QueryDescriptor.removeRedundentArrow;


/**
 * Created by roman.margolis on 28/11/2017.
 */
public class AsgQueryDescriptor implements Descriptor<AsgQuery>, GraphDescriptor<AsgQuery> {
    //region Descriptor Implementation
    @Override
    public String describe(AsgQuery query) {
        return patternValue(query);
    }

    public String describe(AsgQuery query, boolean printId) {
        return patternValue(query, printId);
    }
    //endregion

    //region Private Methods
    private String patternValue(AsgQuery query) {
        return patternValue(query, true);
    }

    private String patternValue(AsgQuery query, boolean printId) {
        List<AsgEBase<? extends EBase>> elements = new ArrayList<>(query.getElements());

        StringJoiner joiner = new StringJoiner(":", "", "");
        for (int i = 0; i < elements.size(); i++) {
            AsgEBase<? extends EBase> e = elements.get(i);
            int id = e.geteNum();
            if (e.geteBase() instanceof QuantBase) {
                List<AsgEBase<? extends EBase>> next = e.getNext();
                String join = next.stream().map(n -> Integer.toString(n.geteNum())).collect(Collectors.joining("|"));
                joiner.add(e.geteBase().getClass().getSimpleName() + "[" + id + "]").add("{" + join + "}");
            } else if (e.geteBase() instanceof EEntityBase) {
                String prefix = "";
                if ((i > 0 && ((elements.get(i - 1).geteBase() instanceof RelPropGroup) || (elements.get(i - 1).geteBase() instanceof Rel)))) {
                    prefix = "==>";
                }
                joiner.add(prefix + EEntityBase.class.getSimpleName() + "[" + id + "]");
            } else if (e.geteBase() instanceof Rel)
                joiner.add("==>" + Relation.class.getSimpleName() + "(" + id + ")");
            else if (e.geteBase() instanceof EProp)
                joiner.add("?" + "[" + id + "]" + QueryDescriptor.printProps(new EPropGroup((EProp) e.geteBase())));
            else if (e.geteBase() instanceof EPropGroup)
                joiner.add("?" + "[" + id + "]" + QueryDescriptor.printProps((EPropGroup) e.geteBase()));
            else if (e.geteBase() instanceof RelProp)
                joiner.add("?" + "[" + id + "]" + QueryDescriptor.printProps(new RelPropGroup((RelProp) e.geteBase())));
            else if (e.geteBase() instanceof RelPropGroup)
                joiner.add("?" + "[" + id + "]" + QueryDescriptor.printProps((RelPropGroup) e.geteBase()));
            else
                joiner.add(e.geteBase().getClass().getSimpleName() + "[" + id + "]");
        }
        return joiner.toString();
    }

    public static String toString(AsgQuery query) {
        return new AsgQueryDescriptor().describe(query);
    }

    static <T extends EBase> void print(List<String> builder, Optional<AsgEBase<T>> item, boolean isTail, boolean child, int level, int currentLine, boolean printId) {
        if (!item.isPresent()) return;
        AsgEBase<T> element = item.get();

        String text = QueryDescriptor.getPrefix(isTail, element.geteBase()) + shortLabel(element, new StringJoiner(":"), printId);
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
                    print(builder, Optional.of(next.get(i)), true, true, level, i, printId);
                }
            } else if (element.geteBase() instanceof EEntityBase) {
                print(builder, element.getNext().isEmpty() ? Optional.empty() : Optional.of(element.getNext().get(0)), element.getNext().isEmpty(), false, level, currentLine, printId);
            } else if (element.geteBase() instanceof Rel) {
                final Optional<AsgEBase<? extends EBase>> bellow = element.getB().stream().filter(p -> (p.geteBase() instanceof BasePropGroup)).findAny();
                if (bellow.isPresent()) {
                    print(builder, bellow.isPresent() ? Optional.of(bellow.get()) : Optional.empty(), true, true, level + 1, currentLine, printId);
                }
                print(builder, element.getNext().isEmpty() ? Optional.empty() : Optional.of(element.getNext().get(0)), element.getNext().isEmpty(), false, level, currentLine, printId);
            } else if (element.geteBase() instanceof EProp) {
                print(builder, element.getNext().isEmpty() ? Optional.empty() : Optional.of(element.getNext().get(0)), true, true, level + 1, currentLine, printId);
            } else if (element.geteBase() instanceof RelProp) {
                print(builder, element.getNext().isEmpty() ? Optional.empty() : Optional.of(element.getNext().get(0)), true, true, level + 1, currentLine, printId);
            } else if (element.geteBase() instanceof BasePropGroup) {
                //print props
                level = builder.size();
                final List<EProp> props = ((BasePropGroup) element.geteBase()).getProps();
                for (int i = 0; i < props.size(); i++) {
                    print(builder, Optional.of(new AsgEBase<>(props.get(i))), true, true, level, i, printId);
                }

                //print group's props
                level = builder.size();
                final List<BasePropGroup> groups = ((BasePropGroup) element.geteBase()).getGroups();
                for (int i = 0; i < groups.size(); i++) {
                    print(builder, Optional.of(new AsgEBase<>(groups.get(i))), true, true, level, i, printId);
                }
            }
        }
    }

    static String shortLabel(AsgEBase<? extends EBase> e, StringJoiner joiner, boolean printId) {
        final EBase eBase = e.geteBase();
        String id = printId ? Integer.toString(e.geteNum()) : "?";
        if (eBase instanceof QuantBase) {
            List<AsgEBase<? extends EBase>> next = e.getNext();
            String join = next.stream().map(p -> Integer.toString(p.geteNum())).collect(Collectors.joining("|"));
            joiner.add("Q" + "[" + id + ":" + ((QuantBase) eBase).getqType() + "]").add("{" + join + "}");
        } else if (eBase instanceof EUntyped)
            joiner.add("UnTyp" + "[" + ":" + ((EUntyped) eBase).getvTypes() + " " + ((Tagged) eBase).geteTag() + "#" + id + "]");
        else if (eBase instanceof EConcrete)
            joiner.add("Conc" + "[" + ":" + ((EConcrete) eBase).geteType() + " " + ((Tagged) eBase).geteTag() + "#" + id + "]");
        else if (eBase instanceof ETyped)
            joiner.add("Typ" + "[" + ":" + ((ETyped) eBase).geteType() + " " + ((Tagged) eBase).geteTag() + "#" + id + "]");
        else if (eBase instanceof Rel)
            joiner.add("Rel" + "(" + ":" + ((Rel) eBase).getrType() + " " + ((Rel) eBase).getWrapper() + "#" + id + ")");
        else if (eBase instanceof EPropGroup)
            joiner.add("?[..]" + "[" + id + "]" + ((eBase instanceof RankingProp) ?
                    "boost:" + ((RankingProp) eBase).getBoost() : ""));
        else if (eBase instanceof RelPropGroup)
            joiner.add("?[..]" + "[" + id + "]" + ((eBase instanceof RankingProp) ?
                    "boost:" + ((RankingProp) eBase).getBoost() : ""));
        else if (eBase instanceof EProp)
            joiner.add("?" + "[" + id + "]" + QueryDescriptor.printProps(new EPropGroup((EProp) eBase)));
        else if (eBase instanceof RelProp)
            joiner.add("?" + "[" + id + "]" + QueryDescriptor.printProps(new RelPropGroup((RelProp) eBase)));
        else
            joiner.add(e.getClass().getSimpleName() + "[" + id + "]");

        return joiner.toString();
    }

    public static String print(AsgQuery query) {
        return print(query, true);
    }

    public static String print(AsgQuery query, boolean printId) {
        List<String> builder = new LinkedList<>();

        //query
        builder.add("└── " + "Start");
        Iterator<AsgEBase<EBase>> iterator = AsgQueryUtil.elements(query).iterator();
        if (iterator.hasNext()) {
            iterator.next();
            if (iterator.hasNext())
                print(builder, Optional.of(iterator.next()), false, true, 1, 0, printId);
        }
        String queryString = builder.toString();

        //projection fields
        if (!query.getProjectedFields().isEmpty())
            queryString = "Projected fields:" + StringUtils.join(query.getProjectedFields(), "|") + "\n" + queryString;

        return queryString;
    }

    public static String print(AsgEBase<? extends EBase> element) {
        return print(element, true);
    }

    public static String print(AsgEBase<? extends EBase> element, boolean printId) {
        List<String> builder = new LinkedList<>();
        builder.add("└── ");
        print(builder, Optional.of(element), false, false, 0, 0, printId);
        return builder.toString();
    }

    public static String printGraph(AsgQuery query) {
        return new AsgQueryDescriptor().visualize(query);
    }

    public String visualize(AsgQuery query) {
        StringBuilder sb = new StringBuilder();
        // name
        sb.append("digraph G { \n");
        //left to right direction
        sb.append("\t rankdir=LR; \n");
        //general node shape
        sb.append("\t node [shape=Mrecord]; \n");
        //append start node shape (first node in query elements list)
        sb.append("\t start [shape=Mdiamond, color=blue, style=\"rounded\"]; \n");

        //iterate over the query
        Iterator<AsgEBase<? extends EBase>> iterator = query.getElements().iterator();
        if (iterator.hasNext()) {
            dot(query, sb);
        }
        sb.append("\n\t }");
        return sb.toString();
    }

    /**
     * print first (root) level of query
     *
     * @param query
     * @param root
     */
    public void dot(AsgQuery query, StringBuilder root) {
        List<AsgEBase<? extends EBase>> path = getPath(query.getStart());
        path.stream()
                .filter(e -> Container.class.isAssignableFrom(e.geteBase().getClass()))
                .forEach(e -> root.append(printQuant(query, e)));
        //print non quant elements
        root.append(printElementsDef(query, path));
        root.append(printElements(path));
    }

    private static String printElements(List<AsgEBase<? extends EBase>> path) {
        return QueryDescriptor.printElements(path.stream().map(AsgEBase::geteBase).collect(Collectors.toList()));
    }

    private static String printElementsDef(AsgQuery query, List<AsgEBase<? extends EBase>> path) {
        return QueryDescriptor.printElementsDef(query.getOrigin(), path.stream().map(AsgEBase::geteBase).collect(Collectors.toList()));
    }

    /**
     * print quant subgraph
     *
     * @param query
     * @param element
     * @return
     */
    private static String printQuant(AsgQuery query, AsgEBase<? extends EBase> element) {
        int id = element.geteNum();
        QuantType quantType = ((Container) element.geteBase()).getqType();
        //add subgraph for the entire quant
        StringBuilder quantBuilder = new StringBuilder();
        quantBuilder.append(" \n subgraph cluster_Q_" + element.geteNum() + " { \n");

        if(element.geteBase() instanceof QuantBase ) {
            quantBuilder.append(" \t color=blue; \n");
            quantBuilder.append(" \t " + id + " [color=blue, shape=folder, label=\"" + quantType + "\"]; \n");
        } if(element.geteBase() instanceof OptionalComp) {
            quantBuilder.append(" \t color=yellow; \n");
            quantBuilder.append(" \t " + id + " [color=yellow, shape=tab, label=\"" + quantType + "\"]; \n");
        }
        quantBuilder.append(" \t node [style=filled]; \n");

        // label the quant type
        quantBuilder.append(" \t label = \" "+((Container) element.geteBase()).getClass().getSimpleName()+"[" + id + "];\"; \n");
        //print the quant list path itself
        //non inclusive for additional quants inside the path - they will be printed separately
        element.getNext().forEach(
                path -> quantBuilder.append(printPath(query, id, getPath(path)))
        );
        quantBuilder.append("\n } \n");
        return quantBuilder.toString();
    }

    public static String printPath(AsgQuery query, int id, List<AsgEBase<? extends EBase>> path) {
        StringBuilder builder = new StringBuilder();
        //print quant container
        path.stream()
                .filter(e -> Container.class.isAssignableFrom(e.geteBase().getClass()))
                .forEach(e -> builder.append(printQuant(query, e)));

        //print the quant itself
        builder.append("\n " + printElementsDef(query, path));
        builder.append("\n " + id + "->" + printElements(path));
        removeRedundentArrow(builder);
        return builder.toString();
    }

    public static List<AsgEBase<? extends EBase>> getPath(AsgEBase<? extends EBase> element) {
        return getPath(element, asgEBase -> Container.class.isAssignableFrom(asgEBase.geteBase().getClass()));
    }

    public static List<AsgEBase<? extends EBase>> getPath(AsgEBase<? extends EBase> element, Predicate<AsgEBase<? extends EBase>> stopCondition) {
        List<AsgEBase<? extends EBase>> path = new ArrayList<>();
        //add element to path
        path.add(element);

        //verify stop conidtion
        if (stopCondition.test(element))
            return path;

        //continue to add the next elements recursively - taking only the non container elements here
        // for example the quants/optional and other containers will be managed elsewhere
        if (!element.getNext().isEmpty()) {
            path.addAll(getPath(element.getNext().get(0), stopCondition));
        }

        return path;

    }

    //endregion
}
