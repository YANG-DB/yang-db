package com.yangdb.fuse.model.query;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2026 The YangDb Graph Database Project
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.model.Next;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.entity.EUntyped;
import com.yangdb.fuse.model.query.optional.OptionalComp;
import com.yangdb.fuse.model.query.properties.BaseProp;
import com.yangdb.fuse.model.query.properties.BasePropGroup;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.properties.constraint.WhereByConstraint;
import com.yangdb.fuse.model.query.properties.constraint.WhereByFacet;
import com.yangdb.fuse.model.query.properties.projection.IdentityProjection;
import com.yangdb.fuse.model.query.quant.HQuant;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Prints a V1 query as one line of text, for people: logs, docs, the {@code POST /fuse/query/v1/print} endpoint.
 * The notation is described in docs/spec/v1/README.md ("Text form"). Output only: there is no parser.
 * <p>
 * Example: {@code start (A:Person) all{ {firstName = "Brandon"} | -[:Own]-> (B:Dragon) }}
 * <p>
 * Never throws on a malformed query: a dangling reference prints as {@code <missing 9>}, a pointer back to an element
 * already on the path as {@code <cycle 1>}, and a type outside the V1 core as {@code <HQuant>}.
 */
public final class V1TextPrinter {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Map<ConstraintOp, String> SYMBOLS = new EnumMap<>(ConstraintOp.class);

    static {
        SYMBOLS.put(ConstraintOp.eq, "=");
        SYMBOLS.put(ConstraintOp.ne, "!=");
        SYMBOLS.put(ConstraintOp.gt, ">");
        SYMBOLS.put(ConstraintOp.ge, ">=");
        SYMBOLS.put(ConstraintOp.lt, "<");
        SYMBOLS.put(ConstraintOp.le, "<=");
    }

    private final Map<Integer, EBase> byNum = new HashMap<>();
    private final Deque<Integer> path = new ArrayDeque<>();

    private V1TextPrinter(Query query) {
        if (query.getElements() != null) {
            query.getElements().stream().filter(Objects::nonNull).forEach(e -> byNum.putIfAbsent(e.geteNum(), e));
        }
    }

    public static String print(Query query) {
        return new V1TextPrinter(query).start(query);
    }

    private String start(Query query) {
        Optional<EBase> start = query.getElements() == null ? Optional.empty()
                : query.getElements().stream().filter(e -> e instanceof Start).findFirst();
        if (!start.isPresent()) {
            return "<no start>";
        }
        return ("start " + chain(((Start) start.get()).getNext())).trim();
    }

    /** the element numbered {@code eNum} and everything after it, or "" for "none" (absent, 0 or -1) */
    private String chain(Integer eNum) {
        if (eNum == null || eNum <= 0) {
            return "";
        }
        if (path.contains(eNum)) {
            return "<cycle " + eNum + ">";
        }
        EBase element = byNum.get(eNum);
        if (element == null) {
            return "<missing " + eNum + ">";
        }
        path.push(eNum);
        try {
            String self = element(element);
            String rest = element instanceof Next && !(element instanceof Quant1) && !(element instanceof OptionalComp)
                    && !(element instanceof HQuant) && ((Next<?>) element).getNext() instanceof Integer
                    ? chain((Integer) ((Next<?>) element).getNext()) : "";
            return rest.isEmpty() ? self : self + " " + rest;
        } finally {
            path.pop();
        }
    }

    private String element(EBase e) {
        if (e instanceof Start) return "start";
        if (e instanceof EConcrete) {
            EConcrete c = (EConcrete) e;
            return "(" + tag(c.geteTag()) + ":" + c.geteType() + " #" + json(c.geteID()) + ")";
        }
        if (e instanceof ETyped) {
            ETyped t = (ETyped) e;
            return "(" + tag(t.geteTag()) + ":" + t.geteType() + ")";
        }
        if (e instanceof EUntyped) {
            EUntyped u = (EUntyped) e;
            return "(" + tag(u.geteTag()) + ":" + types(u.getvTypes(), u.getNvTypes()) + ")";
        }
        if (e instanceof Rel) return rel((Rel) e);
        if (e instanceof Quant1) {
            Quant1 q = (Quant1) e;
            List<Integer> next = q.getNext() == null ? Collections.emptyList() : q.getNext();
            return q.getqType() + "{ " + next.stream().map(this::chain).collect(Collectors.joining(" | ")) + " }";
        }
        if (e instanceof OptionalComp) return "optional{ " + chain(((OptionalComp) e).getNext()) + " }";
        if (e instanceof BaseProp) return "{" + prop((BaseProp) e) + "}";
        if (e instanceof BasePropGroup) return group((BasePropGroup<?, ?>) e);
        return "<" + e.getClass().getSimpleName() + ">";
    }

    private String rel(Rel rel) {
        String type;
        if (rel instanceof RelUntyped) {
            type = types(((RelUntyped) rel).getvTypes(), ((RelUntyped) rel).getNvTypes());
        } else {
            type = String.valueOf(rel.getrType());
        }
        if (rel instanceof RelPattern && ((RelPattern) rel).getLength() != null) {
            type += "*" + ((RelPattern) rel).getLength().getLower() + ".." + ((RelPattern) rel).getLength().getUpper();
        }
        String filters = below(rel.getB());
        String body = "[" + tag(rel.geteTag()) + ":" + type + (filters.isEmpty() ? "" : " " + filters) + "]";
        Rel.Direction dir = rel.getDir() == null ? Rel.Direction.R : rel.getDir();
        switch (dir) {
            case L:
                return "<-" + body + "-";
            case RL:
                return "-" + body + "-";
            default:
                return "-" + body + "->";
        }
    }

    /** what a relation's 'b' points at: a RelProp, a RelPropGroup or (deprecated) an HQuant over RelProps */
    private String below(Integer b) {
        if (b == null || b <= 0) {
            return "";
        }
        EBase e = byNum.get(b);
        if (e == null) {
            return "<missing " + b + ">";
        }
        if (e instanceof HQuant) {
            HQuant h = (HQuant) e;
            List<Integer> bs = h.getB() == null ? Collections.emptyList() : h.getB();
            String inner = bs.stream().map(byNum::get)
                    .map(p -> p instanceof BaseProp ? prop((BaseProp) p) : "<missing>")
                    .collect(Collectors.joining(", "));
            return (h.getqType() == QuantType.all || h.getqType() == null ? "" : h.getqType().toString()) + "{" + inner + "}";
        }
        return element(e);
    }

    private String group(BasePropGroup<?, ?> group) {
        List<String> parts = new ArrayList<>();
        group.getProps().forEach(p -> parts.add(prop(p)));
        group.getGroups().forEach(g -> parts.add(group((BasePropGroup<?, ?>) g)));
        QuantType q = group.getQuantType();
        return (q == null || q == QuantType.all ? "" : q.toString()) + "{" + String.join(", ", parts) + "}";
    }

    private String prop(BaseProp prop) {
        Constraint con = prop.getCon();
        if (con == null) {
            if (prop.getProj() == null || prop.getProj() instanceof IdentityProjection) {
                return prop.getpType();
            }
            return prop.getpType() + " <" + prop.getProj().getClass().getSimpleName() + ">";
        }
        String op = con.getOp() == null ? "?" : SYMBOLS.getOrDefault(con.getOp(), opName(con.getOp()));
        if (con instanceof WhereByConstraint) {
            WhereByFacet w = (WhereByFacet) con;
            String quantifier = w.getJoinType() == WhereByFacet.JoinType.FULL ? "all"
                    : w.getJoinType() == WhereByFacet.JoinType.FOR_EACH ? "any" : "?";
            return prop.getpType() + " " + op + " " + quantifier + "(" + w.getTagEntity() + "." + w.getProjectedField() + ")";
        }
        if (con.getClass() != Constraint.class) {
            return prop.getpType() + " " + op + " <" + con.getClass().getSimpleName() + ">";
        }
        if (ConstraintOp.noValueOps.contains(con.getOp())) {
            return prop.getpType() + " " + op;
        }
        if ((con.getOp() == ConstraintOp.inRange || con.getOp() == ConstraintOp.notInRange) && con.getExpr() instanceof List
                && ((List<?>) con.getExpr()).size() == 2) {
            List<?> range = (List<?>) con.getExpr();
            String iType = con.getiType() == null || con.getiType().length() != 2 ? "[]" : con.getiType();
            return prop.getpType() + " " + op + " " + iType.charAt(0) + json(range.get(0)) + ", " + json(range.get(1)) + iType.charAt(1);
        }
        return prop.getpType() + " " + op + " " + json(con.getExpr());
    }

    private static String opName(ConstraintOp op) {
        try {
            return JSON.writeValueAsString(op).replace("\"", "");
        } catch (JsonProcessingException e) {
            return op.name();
        }
    }

    private static String types(Set<String> include, Set<String> exclude) {
        String in = include == null || include.isEmpty() ? "*" : new TreeSet<>(include).stream().collect(Collectors.joining("|"));
        String out = exclude == null || exclude.isEmpty() ? ""
                : new TreeSet<>(exclude).stream().map(t -> " !" + t).collect(Collectors.joining());
        return in + out;
    }

    private static String tag(String tag) {
        return tag == null ? "" : tag;
    }

    private static String json(Object value) {
        try {
            return JSON.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }
}
