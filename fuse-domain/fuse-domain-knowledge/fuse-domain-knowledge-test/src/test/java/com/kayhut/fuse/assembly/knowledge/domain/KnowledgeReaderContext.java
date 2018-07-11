package com.kayhut.fuse.assembly.knowledge.domain;

import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.QueryResultBase;
import com.kayhut.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.kayhut.fuse.utils.FuseClient;
import javaslang.Tuple2;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.kayhut.fuse.model.OntologyTestUtils.NAME;
import static com.kayhut.fuse.model.query.Rel.Direction.L;
import static com.kayhut.fuse.model.query.Rel.Direction.R;

public class KnowledgeReaderContext {

    static public class KnowledgeQueryBuilder {
        private Query.Builder knowledge;
        private List<EBase> elements;
        private AtomicInteger counter = new AtomicInteger(0);
        private Stack<Quant1> entityStack = new Stack<>();

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
            knowledge = Query.Builder.instance().withName(NAME.name).withOnt("Knowledge");
            elements = new ArrayList<>();
        }

        public static KnowledgeQueryBuilder start() {
            KnowledgeQueryBuilder builder = new KnowledgeQueryBuilder();
            builder.elements.add(new Start(builder.currentEnum(), builder.nextEnum()));
            return builder;
        }

        public KnowledgeQueryBuilder withGlobalEntity(String eTag, Filter... filters) {
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasEntity", L, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), LogicalEntity.type, LogicalEntity.type, nextEnum(), 0));
            this.elements.add(new Rel(currentEnum(), "hasEntity", R, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag, EntityBuilder.type, nextEnum(), 0));
            nextEnum();//continue
            return this;
        }

        public KnowledgeQueryBuilder withEntity(String eTag, Filter... filters) {
            this.elements.add(new ETyped(currentEnum(), eTag, EntityBuilder.type, nextEnum(), 0));
            Quant1 quant1 = new Quant1(currentEnum(), QuantType.all, new ArrayList<>(), 0);
            this.elements.add(quant1);
            entityStack.push(quant1);
            nextEnum();//continue
            Arrays.stream(filters).forEach(filter -> {
                entityStack.peek().getNext().add(currentEnum());
                elements.add(filter.build(currentEnum()));
                nextEnum();//continue
            });
            return this;
        }

        public KnowledgeQueryBuilder relatedTo(String eTag, String sideB, Filter... filters) {
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasRelation", R, EntityBuilder.type, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag, RelationBuilder.type, nextEnum(), 0));
            Quant1 quant1 = new Quant1(currentEnum(), QuantType.all, new ArrayList<>(), 0);
            this.elements.add(quant1);
            entityStack.push(quant1);
            nextEnum();//continue

            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasRelation", L, EntityBuilder.type, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), sideB, EntityBuilder.type, nextEnum(), 0));

            nextEnum();//continue
            return this;
        }

        public KnowledgeQueryBuilder withFile(String eTag, Filter... filters) {
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasEfile", R, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag, FileBuilder.type, 0, 0));
            nextEnum();//continue
            return this;
        }

        public KnowledgeQueryBuilder withValue(String eTag, Filter... filters) {
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasEvalue", R, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag, ValueBuilder.type, 0, 0));
            nextEnum();//continue
            return this;
        }

        public KnowledgeQueryBuilder withRef(String eTag, Filter... filters) {
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasEntityReference", R, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag, RefBuilder.type, 0, 0));
            nextEnum();//continue
            return this;
        }

        public KnowledgeQueryBuilder withInsight(String eTag, Filter... filters) {
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasInsight", R, null, nextEnum(), 0));
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

    static public QueryResultBase query(FuseClient fuseClient, FuseResourceInfo fuseResourceInfo, Query query)
            throws IOException, InterruptedException {
        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        // Create object of cursorRequest
        CreateGraphCursorRequest cursorRequest = new CreateGraphCursorRequest();
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), cursorRequest);
        // Press on page to get the relevant page
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);
        // Waiting until it gets the response
        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }
        // return the relevant data
        return fuseClient.getPageData(pageResourceInfo.getDataUrl());
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

        public Filter with(QuantType quantType,String field, Constraint constraint) {
            if (!fields.containsKey(quantType)) {
                fields.put(quantType, new ArrayList<>());
            }
            final List<Tuple2<String, Constraint>> list = fields.get(QuantType.all);
            list.add(new Tuple2<>(field, constraint));
            return this;
        }

        public Filter and(String field, Constraint constraint) {
            return with(QuantType.all, field,constraint);
        }

        public Filter or(String field, Constraint constraint) {
            return with(QuantType.some, field,constraint);
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
