package com.kayhut.fuse.model.execution.plan.descriptors;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
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

        if (element.hasNext()) {
            if (element.geteBase() instanceof QuantBase) {
                List<AsgEBase<? extends EBase>> nexts = element.getNext();
                level = builder.size();
                for (int i = 0; i < nexts.size(); i++) {
                    print(builder, Optional.of(nexts.get(i)), true, true, level, i);
                }
            } else if (element.geteBase() instanceof EEntityBase) {
                print(builder, element.getNext().isEmpty() ? Optional.empty() : Optional.of(element.getNext().get(0)), element.getNext().isEmpty(), false, level, currentLine);
            } else if (element.geteBase() instanceof Rel) {
                print(builder, element.getNext().isEmpty() ? Optional.empty() : Optional.of(element.getNext().get(0)), element.getNext().isEmpty(), false, level, currentLine);
            } else if (element.geteBase() instanceof EProp || element.geteBase() instanceof EPropGroup) {
                print(builder, element.getNext().isEmpty() ? Optional.empty() : Optional.of(element.getNext().get(0)), true, true, level + 1, currentLine);
            } else if (element.geteBase() instanceof RelProp || element.geteBase() instanceof RelPropGroup) {
                print(builder, element.getNext().isEmpty() ? Optional.empty() : Optional.of(element.getNext().get(0)), true, true, level + 1, currentLine);
            }
        }
    }

    static String shortLabel(AsgEBase<? extends EBase> e, StringJoiner joiner) {
        if (e.geteBase() instanceof QuantBase) {
            List<AsgEBase<? extends EBase>> next = e.getNext();
            String join = next.stream().map(p->Integer.toString(p.geteNum())).collect(Collectors.joining("|"));
            joiner.add("Q" + "[" + e.geteNum() + "]").add("{" + join + "}");
        } else if (e.geteBase() instanceof EUntyped)
            joiner.add("UnTyp" + "[" + e.geteNum() + "]");
        else if (e.geteBase() instanceof EConcrete)
            joiner.add("Conc" + "["+ ((EConcrete) e.geteBase()).geteType() +":"+ e.geteNum() + "]");
        else if (e.geteBase() instanceof ETyped)
            joiner.add("Typ" + "["+ ((ETyped) e.geteBase()).geteType() +":"+ e.geteNum() + "]");
        else if (e.geteBase() instanceof Rel)
            joiner.add("Rel" + "("+((Rel) e.geteBase()).getrType() +":" + e.geteNum() + ")");
        else if (e.geteBase() instanceof EPropGroup)
            joiner.add("?" + "[" + e.geteNum() + "]" + QueryDescriptor.printProps((EPropGroup) e.geteBase()));
        else if (e.geteBase() instanceof EProp)
            joiner.add("?" + "[" + e.geteNum() + "]" + QueryDescriptor.printProps(new EPropGroup((EProp) e.geteBase())));
        else if (e.geteBase() instanceof RelPropGroup)
            joiner.add("?" + "[" + e.geteNum() + "]" + QueryDescriptor.printProps((RelPropGroup) e.geteBase()));
        else if (e.geteBase() instanceof RelProp)
            joiner.add("?" + "[" + e.geteNum() + "]" + QueryDescriptor.printProps(new RelPropGroup((RelProp) e.geteBase())));
        else
            joiner.add(e.getClass().getSimpleName() + "[" + e.geteNum() + "]");

        return joiner.toString();
    }

    public static String print(AsgQuery query) {
        List<String> builder = new LinkedList<>();
        builder.add("└── " + "Start");
        Iterator<AsgEBase<? extends EBase>> iterator = query.getElements().iterator();
        iterator.next();
        print(builder, Optional.of(iterator.next()), false, true, 1, 0);
        return builder.toString();
    }

    //endregion
}
