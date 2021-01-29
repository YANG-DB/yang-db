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
 * QueryDescriptor.java - fuse-model - yangdb - 2,016
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

import com.yangdb.fuse.model.Below;
import com.yangdb.fuse.model.Container;
import com.yangdb.fuse.model.Next;
import com.yangdb.fuse.model.asgQuery.IQuery;
import com.yangdb.fuse.model.descriptors.Descriptor;
import com.yangdb.fuse.model.descriptors.GraphDescriptor;
import com.yangdb.fuse.model.query.*;
import com.yangdb.fuse.model.query.entity.*;
import com.yangdb.fuse.model.query.optional.OptionalComp;
import com.yangdb.fuse.model.query.properties.*;
import com.yangdb.fuse.model.query.quant.QuantBase;
import com.yangdb.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.*;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.query.Query.QueryUtils.findByEnum;
import static com.yangdb.fuse.model.query.Query.QueryUtils.getPath;

public class QueryDescriptor implements Descriptor<Query>, GraphDescriptor<Query> {


    //region Descriptor Implementation
    @Override
    public String describe(Query query) {
        return patternValue(query);
    }
    //endregion

    public static String describe(EBase entity) {
        return label(entity, new StringJoiner(":", "", ""));
    }

    public static String describe(Rel relation) {
        return label(relation, new StringJoiner(":", "", ""));
    }

    /**
     * print entity + property filters
     *
     * @param entity
     * @param ePropGroup
     * @return
     */
    public static String describe(EEntityBase entity, EPropGroup ePropGroup) {
        StringJoiner joiner = new StringJoiner(":", "", "");
        label(entity, joiner);
        joiner.add(printProps(ePropGroup));
        return joiner.toString();
    }

    /**
     * print relation + property filters
     *
     * @param rel
     * @param rPropGroup
     * @return
     */
    public static String describe(Rel rel, RelPropGroup rPropGroup) {
        StringJoiner joiner = new StringJoiner(":", "", "");
        label(rel, joiner);
        joiner.add(printProps(rPropGroup));
        return joiner.toString();
    }

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
        } else {
            if (e instanceof EConcrete)
                joiner.add(e.getClass().getSimpleName() + "[" + ((EConcrete) e).geteType() + ":" + e.geteNum() + ":ID[" + ((EConcrete) e).geteID() + "]]");
            else
                joiner.add(e.getClass().getSimpleName() + "[" + e.geteNum() + "]");
        }
        return joiner.toString();
    }

    static String shortLabel(EBase e, StringJoiner joiner, boolean printId) {
        String id = printId ? Integer.toString(e.geteNum()) : "$";
        if (e instanceof QuantBase) {
            List<Integer> next = ((Next<List>) e).getNext();
            if (next != null) {
                String join = next.stream().map(Object::toString).collect(Collectors.joining("|"));
                joiner.add("Q" + "[" + id + "]").add("{" + join + "}");
            }
        } else if (e instanceof EUntyped)
            joiner.add("EUntyped" + "(" + "[" + String.join(",", ((Untyped) e).getvTypes()) + "]" + ":" + id + ")");
        else if (e instanceof EConcrete)
            joiner.add("Conc" + "[" + ((EConcrete) e).geteType() + ":" + id + ":ID[" + ((EConcrete) e).geteID() + "]]");
        else if (e instanceof ETyped)
            joiner.add("Typ" + "[" + ((ETyped) e).geteType() + ":" + id + "]");
        else if (e instanceof RelUntyped)
            joiner.add("RelUntyped" + "(" + "[" + String.join(",", ((Untyped) e).getvTypes()) + "]" + ":" + id + ")");
        else if (e instanceof Rel)
            joiner.add("Rel" + "(" + ((Rel) e).getrType() + ":" + id + ")");
        else if (e instanceof EPropGroup)
            joiner.add("?" + "[" + id + "]" + printProps((EPropGroup) e));
        else if (e instanceof EProp)
            joiner.add("?" + "[" + id + "]" + printProps(new EPropGroup((EProp) e)));
        else if (e instanceof RelProp)
            joiner.add("?" + "[" + id + "]" + printProps(new RelPropGroup((RelProp) e)));
        else if (e instanceof RelPropGroup)
            joiner.add("?" + "[" + id + "]" + printProps((RelPropGroup) e));
        else
            joiner.add(e.getClass().getSimpleName() + "[" + id + "]");

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

    static String printDetailedProp(BaseProp p) {
        if (p instanceof RankingProp) {
            return "boost:" + ((RankingProp) p).getBoost() + "Typ:[" + p.getpType() + "] [" + p.getpType() + "[" + p.getCon().getOp() + "," + p.getCon().getExpr() + "]";
        } else if (p.getCon() != null) {
            return "Typ:[" + p.getpType() + "] [" + p.getCon().getOp() + "," + p.getCon().getExpr() + "]";
        } else if (p.getProj() != null) {
            return "Typ:[" + p.getpType() + "] [" + p.getProj().getClass().getSimpleName() + "]";
        } else {
            return p.getpType();
        }
    }

    static String printProp(EProp p) {
        if (p instanceof RankingProp) {
            return "boost:" + ((RankingProp) p).getBoost() + "  " + p.getpType() + "<" + p.getCon().getOp() + "," + p.getCon().getExpr() + ">";
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

    static void print(List<String> builder, IQuery<EBase> query, Optional<? extends EBase> element, boolean isTail, boolean child, int level, int currentLine, boolean printId) {
        if (!element.isPresent()) return;
        String text = getPrefix(isTail, element.get()) + shortLabel(element.get(), new StringJoiner(":"), printId);
        if (child) {
            char[] zeros = new char[builder.get(level - 1).length() - 5];
            Arrays.fill(zeros, ' ');
            builder.add("\n" + String.valueOf(zeros) + text);
        } else {
            builder.set(level + currentLine, builder.get(level + currentLine) + text);
        }


        if (Next.class.isAssignableFrom(element.get().getClass())
                || element.get() instanceof BasePropGroup) {
            if (element.get() instanceof QuantBase) {
                List<Integer> next = ((QuantBase) element.get()).getNext();
                if (next != null) {
                    level = builder.size();
                    for (int i = 0; i < next.size(); i++) {
                        print(builder, query, findByEnum(query, next.get(i)), true, true, level, i, printId);
                    }
                }
            } else if (element.get() instanceof EEntityBase) {
                print(builder, query, findByEnum(query, ((EEntityBase) element.get()).getNext()), !((Next) element.get()).hasNext(), false, level, currentLine, printId);
            } else if (element.get() instanceof Rel) {
                print(builder, query, findByEnum(query, ((Rel) element.get()).getNext()), !((Next) element.get()).hasNext(), false, level, currentLine, printId);
                if (((Rel) element.get()).getB() > 0 && findByEnum(query, ((Rel) element.get()).getB()).isPresent())
                    print(builder, query, findByEnum(query, ((Rel) element.get()).getB()), false, true, level + 1, currentLine, printId);
            } else if (element.get() instanceof ScoreEProp) {
                print(builder, query, element, true, true, level + 1, currentLine, printId);
            } else if (element.get() instanceof EPropGroup) {
                level = builder.size();
                for (int i = 0; i < ((EPropGroup) element.get()).getGroups().size(); i++) {
                    EPropGroup ePropGroup = ((EPropGroup) element.get()).getGroups().get(i);
                    print(builder, query, Optional.of(ePropGroup), true, true, level, i, printId);
                }
            } else if (element.get() instanceof EProp || element.get() instanceof RelProp) {
                print(builder, query, element, true, true, level + 1, currentLine, printId);
            } else if (element.get() instanceof RelPropGroup) {
                level = builder.size();
                for (int i = 0; i < ((RelPropGroup) element.get()).getGroups().size(); i++) {
                    BasePropGroup rPropGroup = ((RelPropGroup) element.get()).getGroups().get(i);
                    print(builder, query, Optional.of(rPropGroup), true, true, level, i, printId);
                }
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
        return print(query, true);
    }

    public static String print(IQuery<EBase> query, boolean printId) {
        List<String> builder = new LinkedList<>();
        builder.add("└── " + "Start");
        Iterator<EBase> iterator = query.getElements().iterator();
        if (iterator.hasNext()) {
            iterator.next();
            if (iterator.hasNext()) {
                EBase next = iterator.next();
                print(builder, query, Optional.ofNullable(next), false, true, 1, 0, printId);
            }
        }
        return builder.toString();
    }

    public static String printGraph(Query query) {
        return new QueryDescriptor().visualize(query);
    }

    @Override
    public String visualize(Query query) {
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
        Iterator<EBase> iterator = query.getElements().iterator();
        if (iterator.hasNext()) {
            dot(query, sb, iterator.next());
        }
        sb.append("\n\t }");
        return sb.toString();
    }

    /**
     * print first (root) level of query
     *
     * @param query
     * @param root
     * @param element
     */
    public static void dot(Query query, StringBuilder root, EBase element) {
        List<EBase> path = getPath(query, element.geteNum(), eBase -> Container.class.isAssignableFrom(eBase.getClass()));
        path.stream()
                .filter(e -> Container.class.isAssignableFrom(e.getClass()))
                .forEach(e -> root.append(printQuant(query, (Container) e)));
        //print non quant elements
        root.append(printElementsDef(query, path));
        root.append(printElements(path));
    }

    public static String printProps(Query query, BasePropGroup element) {
        //add subgraph for the entire quant
        StringBuilder prpoBuilder = new StringBuilder();
        prpoBuilder.append(" \n subgraph cluster_Props_" + element.geteNum() + " { \n");
        prpoBuilder.append(" \t color=green; \n");
        prpoBuilder.append(" \t node [shape=component]; \n");
        prpoBuilder.append(" \t " + element.geteNum() + " [color=green, shape=folder, label=\"" + element.getQuantType() + "\"]; \n");
        // label the prop group type
        prpoBuilder.append(" \t label = \" Props[" + element.geteNum() + "];\"; \n");
        //print the prop group list path itself
        //non inclusive for additional group inside the path - they will be printed separately
        List<BaseProp> props = (List<BaseProp>) element.getProps()
                .stream()
                .map(p -> ((BaseProp) p).clone())
                .collect(Collectors.toList());

        //give specific number to each property in the group
        for (int i = 0; i < props.size(); i++) {
            props.get(i).seteNum(element.geteNum() * 100 + i);
        }

        prpoBuilder.append("\n " + printElementsDef(query, props));
        prpoBuilder.append("\n " + element.geteNum() + "->" + printElements(props));
        removeRedundentArrow(prpoBuilder);

        prpoBuilder.append("\n } \n");
        return prpoBuilder.toString();
    }

    /**
     * print quant subgraph
     *
     * @param query
     * @param element
     * @return
     */
    public static String printQuant(Query query, Container element) {
        QuantType quantType = element.getqType();
        int id  = ((EBase) element).geteNum();
        //add subgraph for the entire quant
        StringBuilder quantBuilder = new StringBuilder();
        quantBuilder.append(" \n subgraph cluster_Q_" + id   + " { \n");
        quantBuilder.append(" \t color=blue; \n");
        quantBuilder.append(" \t node [style=filled]; \n");

        if(element instanceof QuantBase ) {
            quantBuilder.append(" \t color=blue; \n");
            quantBuilder.append(" \t " + id + " [color=blue, shape=folder, label=\"" + quantType + "\"]; \n");
        } if(element instanceof OptionalComp) {
            quantBuilder.append(" \t color=yellow; \n");
            quantBuilder.append(" \t " + id + " [color=yellow, shape=tab, label=\"" + quantType + "\"]; \n");
        }

        // label the quant type
        quantBuilder.append(" \t label = \" "+ element.getClass().getSimpleName()+"[" + id + "];\"; \n");

        //print the quant list path itself
        //non inclusive for additional quants inside the path - they will be printed separately
        Object next = element.getNext();
        // next can be either List<Int> or Int
        if(next instanceof Collection) {
            ((Collection)next).forEach(
                    path -> quantBuilder.append(printPath(query, id, getPath(query, (Integer) path, eBase -> Container.class.isAssignableFrom(eBase.getClass()))))
            );
        } else {
            quantBuilder.append(printPath(query, id, getPath(query, (int) next, eBase -> Container.class.isAssignableFrom(eBase.getClass()))));
        }
        quantBuilder.append("\n } \n");
        return quantBuilder.toString();
    }

    public static String printPath(Query query, int id, List<? extends EBase> path) {
        StringBuilder builder = new StringBuilder();
        //print container (quant / optional )
        path.stream()
                .filter(e -> Container.class.isAssignableFrom(e.getClass()))
                .forEach(e -> builder.append(printQuant(query, (Container) e)));

        //print the quant itself
        builder.append("\n " + printElementsDef(query, path));
        builder.append("\n " + id + "->" + printElements(path));
        removeRedundentArrow(builder);
        return builder.toString();
    }

    public static String printElementsDef(Query query, List<? extends EBase> path) {
        StringBuilder builder = new StringBuilder();
        path.forEach(element -> {
                    //print relationship symbol for the quant
                    if (element instanceof Rel) {
                        //append directed rel
                        builder.append(element.geteNum() + " [ label=\"" + shortLabel(element, new StringJoiner(""), true) + "\", shape = " + (((Rel) element).getDir().equals(Rel.Direction.R) ? "rarrow" : "larrow") + "]; \n");
                    }//print props symbol
                    else if (element instanceof BaseProp) {
                        builder.append(element.geteNum() + " [ label=\"" + printDetailedProp((BaseProp) element) + "\" ,shape = component]; \n");
                    }//print prop group step
                    else if (element instanceof BasePropGroup) {
                        builder.append(printProps(query, (EPropGroup) element));
                    }//print prop group step
                    else if (element instanceof Below) {
                        builder.append(printBelowProps(query, (Below) element));
                    }//print typed steps
                    else if (element instanceof Typed) {
                        // - if typed print the type
                        builder.append(element.geteNum() + " [ label=\"" + shortLabel(element, new StringJoiner(""), true) + "\" ,shape = Mrecord]; \n");
                    }
                    //todo - print rel-typed patterns
                }
        );
        return builder.toString();
    }

    private static String printBelowProps(Query query, Below<Integer> element) {
        StringBuilder builder = new StringBuilder();
        //verify below exist
        if (element.getB() > 0) {
            EBase below = findByEnum(query, element.getB()).get();
            //populate prop
            if (BaseProp.class.isAssignableFrom(below.getClass())) {
                //todo
            //populate prop group
            } else if (BasePropGroup.class.isAssignableFrom(below.getClass())) {
                //todo
            }
        }
        return builder.toString();
    }

    public static void removeRedundentArrow(StringBuilder builder) {
        if (builder.toString().endsWith("->"))
            builder.delete(builder.toString().length() - 2, builder.toString().length());
    }

    /**
     * go over the elements and print its dot graph
     *
     * @param path
     */
    public static String printElements(List<? extends EBase> path) {
        StringBuilder builder = new StringBuilder();

        path.forEach(element -> {
                    //print relationship symbol for the quant
                    if (element instanceof Start) {
                        builder.append("start->");

                    }//print rel
                    else if (element instanceof Rel) {
                        //append directed rel
                        builder.append(element.geteNum() + "->");
                    }
                    //print props symbol
                    else if (element instanceof BaseProp) {
                        builder.append(element.geteNum() + "->");
                    }
                    //print containers
                    else if (element instanceof Container) {
                        //details of this container will be covered inside the subgraph
                        builder.append(element.geteNum() + "->");
                    }
                    //print group
                    else if (element instanceof BasePropGroup) {
                        //details of this quant will be covered inside the subgraph
                        builder.append(element.geteNum() + "->");
                    }
                    //default node
                    else if (element instanceof Typed) {
                        // - if typed print the type
                        builder.append(element.geteNum() + "->");
                    }
                    //todo - print rel-typed patterns
                }
        );
        removeRedundentArrow(builder);
        return builder.toString();
    }

}
