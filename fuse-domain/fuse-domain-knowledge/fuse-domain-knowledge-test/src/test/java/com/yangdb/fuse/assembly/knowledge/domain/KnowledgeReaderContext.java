package com.yangdb.fuse.assembly.knowledge.domain;

import com.yangdb.fuse.model.Tagged;
import com.yangdb.fuse.model.query.*;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.entity.EndPattern;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import javaslang.Tuple2;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.yangdb.fuse.assembly.KNOWLEDGE.KNOWLEDGE;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.Filter.filter;
import static com.yangdb.fuse.model.OntologyTestUtils.NAME;
import static com.yangdb.fuse.model.query.Rel.Direction.L;
import static com.yangdb.fuse.model.query.Rel.Direction.R;

public class KnowledgeReaderContext {

    static public class KnowledgeQueryBuilder {
        public static final String HAS_OUT_RELATION = "hasOutRelation";
        public static final String HAS_IN_RELATION = "hasInRelation";
        public static final String HAS_RELATION = "hasRelation";
        public static final String HAS_EFILE = "hasEfile";
        public static final String HAS_EVALUE = "hasEvalue";
        public static final String HAS_ENTITY_REFERENCE = "hasEntityReference";
        public static final String HAS_INSIGHT = "hasInsight";

        private Query.Builder knowledge;
        private List<EBase> elements;
        private AtomicInteger counter = new AtomicInteger(0);
        public Stack<Quant1> entityStack = new Stack<>();

        private int nextEnum() {
            return counter.incrementAndGet();
        }

        private int currentEnum() {
            return counter.get();
        }

        private EBase current() {
            return elements.get(currentEnum());
        }

        private KnowledgeQueryBuilder() {
            knowledge = Query.Builder.instance().withName(NAME.name).withOnt(KNOWLEDGE);
            elements = new ArrayList<>();
        }

        private Quant1 quant() {
            //case we are in quant scope
            Quant1 quant1 = new Quant1(currentEnum(), QuantType.all, new ArrayList<>(), 0);
            this.elements.add(quant1);
            entityStack.push(quant1);
            nextEnum();//continue
            return quant1;
        }

        public static KnowledgeQueryBuilder start() {
            KnowledgeQueryBuilder builder = new KnowledgeQueryBuilder();
            builder.elements.add(new Start(builder.currentEnum(), builder.nextEnum()));
            return builder;
        }

        public KnowledgeQueryBuilder withGlobalEntityValues(String eTag, Filter... filters) {
            return withGlobalEntity(eTag, Collections.EMPTY_LIST,
                    Arrays.asList(filter().with(QuantType.all, "fieldId", Constraint.of(ConstraintOp.inSet, Arrays.asList("title", "nicknames")))));
        }

        public KnowledgeQueryBuilder withGlobalEntity(String eTag) {
            return withGlobalEntity(eTag, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        }

        public KnowledgeQueryBuilder withGlobalEntity(String eTag, List<Filter> entityFilter, List<Filter> entityValueFilter) {
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasEntity", L, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), LogicalEntity.type, LogicalEntity.type, nextEnum(), 0));
            this.elements.add(new Rel(currentEnum(), "hasEntity", R, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag + "#" + currentEnum(), EntityBuilder.type, nextEnum(), 0));
            //add global quant
            quant();
            //add global rel + values
            elements.add(filter().with(QuantType.all, "context", Constraint.of(ConstraintOp.eq, "global"))
                    .build(currentEnum()));
            //adds to quant
            entityStack.peek().getNext().add(currentEnum());
            nextEnum();//continue
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasEvalue", R, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag + "#" + currentEnum(), ValueBuilder.type, nextEnum(), 0));

            quant();

            entityValueFilter.forEach(filter -> {
                //adds to quant
                entityStack.peek().getNext().add(currentEnum());
                //adds to element
                elements.add(filter.build(currentEnum()));
                //next enum
                nextEnum();//continue
            });
            return this;
        }

        public KnowledgeQueryBuilder withEntity(String eTag, Filter... filters) {
            this.elements.add(new ETyped(currentEnum(), eTag, EntityBuilder.type, nextEnum(), 0));
            quant();
            Arrays.stream(filters).forEach(filter -> {
                entityStack.peek().getNext().add(currentEnum());
                elements.add(filter.build(currentEnum()));
                nextEnum();//continue
            });
            return this;
        }

        public KnowledgeQueryBuilder withConcrete(String eTag, String id) {
            this.elements.add(new EConcrete(currentEnum(), eTag, EntityBuilder.type, id, id, nextEnum(), 0));
            return this;
        }

        public KnowledgeQueryBuilder pathToEType(String rType, String eType, int from, int to) {
            //"relatedEntity"
            this.elements.add(new RelPattern(currentEnum(), rType, new com.yangdb.fuse.model.Range(from, to), R, null, nextEnum(), 0));
            this.elements.add(new EndPattern<>(new ETyped(currentEnum(), Tagged.tagSeq("Target"), eType, nextEnum(), 0)));
            return this;
        }


        public KnowledgeQueryBuilder relatedTo(String relTypeIn,String relTypeOut,String eTag, String sideB, Filter... filters) {
            return relatedTo(entityStack.peek(),relTypeIn,relTypeOut, eTag, sideB, filters);
        }

        public KnowledgeQueryBuilder relatedTo(String eTag, String sideB, Filter... filters) {
            return relatedTo(entityStack.peek(), eTag, sideB, filters);
        }

        public KnowledgeQueryBuilder withRel(String rType) {
            this.elements.add(new Rel(currentEnum(), rType, R, EntityBuilder.type, nextEnum(), 0));
            return this;
        }

        public KnowledgeQueryBuilder relatedTo(Quant1 quantEntity, String eTag, String sideB, Filter... filters) {
            return relatedTo(quantEntity,HAS_RELATION,HAS_RELATION,eTag,sideB,filters);
        }

        public KnowledgeQueryBuilder relatedTo(Quant1 quantEntity,String relTypeIn,String relTypeOut, String eTag, String sideB, Filter... filters) {
            quantEntity.getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), relTypeIn, R, EntityBuilder.type + "_" + eTag+"[IN]", nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag, RelationBuilder.type, nextEnum(), 0));
            Quant1 quant1 = new Quant1(currentEnum(), QuantType.all, new ArrayList<>(), 0);
            this.elements.add(quant1);
            entityStack.push(quant1);
            nextEnum();//continue

            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), relTypeOut, L, EntityBuilder.type + "_" + eTag+"[OUT]", nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), sideB, EntityBuilder.type, nextEnum(), 0));

            nextEnum();//continue
            return this;
        }

        public KnowledgeQueryBuilder withFile(String eTag, Filter... filters) {
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), HAS_EFILE, R, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag, FileBuilder.type, 0, 0));
            nextEnum();//continue
            return this;
        }

        public KnowledgeQueryBuilder withValue(String eTag, Filter... filters) {
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), HAS_EVALUE, R, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag, ValueBuilder.type, 0, 0));
            nextEnum();//continue
            Arrays.stream(filters).forEach(filter -> {
                entityStack.peek().getNext().add(currentEnum());
                elements.add(filter.build(currentEnum()));
                nextEnum();//continue
            });
            return this;
        }

        public KnowledgeQueryBuilder withRef(String eTag, Filter... filters) {
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), HAS_ENTITY_REFERENCE, R, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag, RefBuilder.type, 0, 0));
            nextEnum();//continue
            return this;
        }

        public KnowledgeQueryBuilder withInsight(String eTag, Filter... filters) {
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), HAS_INSIGHT, R, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag, InsightBuilder.type, 0, 0));
            nextEnum();//continue
            return this;
        }

        public Query build() {
            if (this.elements.get(this.elements.size() - 1) instanceof EEntityBase) {
                ((EEntityBase) this.elements.get(this.elements.size() - 1)).setNext(0);
            }
            return knowledge.withElements(elements).build();
        }


    }


    public static class Filter {
        private Map<QuantType, List<Tuple2<String, Constraint>>> fields;
        private AtomicInteger eNum;

        private Filter() {
            fields = new HashMap<>();
        }

        public static Filter filter() {
            return new Filter();
        }

        public Filter with(QuantType quantType, String field, Constraint constraint) {
            if (!fields.containsKey(quantType)) {
                fields.put(quantType, new ArrayList<>());
            }
            final List<Tuple2<String, Constraint>> list = fields.get(QuantType.all);
            list.add(new Tuple2<>(field, constraint));
            return this;
        }

        public Filter and(String field, Constraint constraint) {
            return with(QuantType.all, field, constraint);
        }

        public Filter or(String field, Constraint constraint) {
            return with(QuantType.some, field, constraint);
        }

        public EPropGroup build(int eNum) {
            this.eNum = new AtomicInteger(100 * eNum);
            final EPropGroup total = new EPropGroup(eNum);
            fields.forEach((quantType, tuple2s) -> {
                final EPropGroup quantGroup = EPropGroup.of(this.eNum.incrementAndGet(), quantType, new EProp[]{});
                tuple2s.forEach(field -> quantGroup.getProps().add(EProp.of(this.eNum.incrementAndGet(), field._1, field._2)));
                total.getGroups().add(quantGroup);
            });
            return total;
        }
    }

}
