package com.kayhut.fuse.dispatcher.descriptors;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.Next;
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

import static com.kayhut.fuse.dispatcher.descriptors.QueryDescriptor.getPrefix;
import static com.kayhut.fuse.dispatcher.descriptors.QueryDescriptor.printProps;

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
        List<AsgEBase<EBase>> elements = AsgQueryUtil.elements(
                query.getStart(),
                AsgEBase::getB,
                AsgEBase::getNext,
                asgEBase -> true,
                asgEBase -> true,
                Collections.emptyList());

        StringJoiner joiner = new StringJoiner(":", "", "");
        for (int i = 0; i < elements.size(); i++) {
            AsgEBase<EBase> e = elements.get(i);
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
            else if (e.geteBase() instanceof EPropGroup)
                joiner.add("?" + "[" + e.geteNum() + "]" + printProps((BasePropGroup) e.geteBase()));
            else if (e.geteBase() instanceof RelPropGroup)
                joiner.add("?" + "[" + e.geteNum() + "]" + printProps((BasePropGroup) e.geteBase()));
            else
                joiner.add(e.geteBase().getClass().getSimpleName() + "[" + e.geteNum() + "]");
        }
        return joiner.toString();
    }

    public static String toString(AsgQuery query) {
        return new AsgQueryDescriptor().describe(query);
    }

    static <T extends EBase> void print(List<String> builder, Optional<AsgEBase<T>> element, boolean isTail, boolean child, int level, int currentLine) {
        if (!element.isPresent()) return;
        String text = getPrefix(isTail, element.get().geteBase()) + shortLabel(element.get(), new StringJoiner(":"));
        if (child) {
            char[] zeros = new char[builder.get(level - 1).length() - 5];
            Arrays.fill(zeros, ' ');
            builder.add("\n" + String.valueOf(zeros) + text);
        } else {
            builder.set(level + currentLine, builder.get(level + currentLine) + text);
        }

        if (Next.class.isAssignableFrom(element.get().geteBase().getClass())) {
            if (element.get().geteBase() instanceof QuantBase) {
                List<AsgEBase<? extends EBase>> nexts = element.get().getNext();
                level = builder.size();
                for (int i = 0; i < nexts.size(); i++) {
                    print(builder, Optional.of(nexts.get(i)), true, true, level, i);
                }
            } else if (element.get().geteBase() instanceof EEntityBase) {
                print(builder, element.get().getNext().isEmpty() ? Optional.empty() : Optional.of(element.get().getNext().get(0)), element.get().getNext().isEmpty(), false, level, currentLine);
            } else if (element.get().geteBase() instanceof Rel) {
                print(builder, element.get().getNext().isEmpty() ? Optional.empty() : Optional.of(element.get().getNext().get(0)), element.get().getNext().isEmpty(), false, level, currentLine);
            } else if (element.get().geteBase() instanceof EProp || element.get().geteBase() instanceof EPropGroup) {
                print(builder, element, true, true, level + 1, currentLine);
            } else if (element.get().geteBase() instanceof RelProp || element.get().geteBase() instanceof RelPropGroup) {
                print(builder, element, true, true, level + 1, currentLine);
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
            joiner.add("?" + "[" + e.geteNum() + "]" + printProps((BasePropGroup) e.geteBase()));
        else if (e.geteBase() instanceof RelPropGroup)
            joiner.add("?" + "[" + e.geteNum() + "]" + printProps((BasePropGroup) e.geteBase()));
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
