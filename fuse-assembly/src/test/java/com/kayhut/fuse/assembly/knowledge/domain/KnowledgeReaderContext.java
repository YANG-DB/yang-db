package com.kayhut.fuse.assembly.knowledge.domain;

import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantBase;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.QueryResultBase;
import com.kayhut.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.kayhut.fuse.services.engine2.data.util.FuseClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kayhut.fuse.model.OntologyTestUtils.NAME;
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

        public KnowledgeQueryBuilder withEntity(String eTag) {
            this.elements.add(new ETyped(currentEnum(), eTag, "Entity", nextEnum(), 0));
            Quant1 quant1 = new Quant1(currentEnum(), QuantType.all, new ArrayList<>(), 0);
            this.elements.add(quant1);
            entityStack.push(quant1);
            nextEnum();//continue
            return this;
        }


        public KnowledgeQueryBuilder withFile(String eTag) {
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasEntityFile", R, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag, "File", 0, 0));
            nextEnum();//continue
            return this;
        }

        public KnowledgeQueryBuilder withRef(String eTag) {
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasEntityReference", R, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag, "Reference", 0, 0));
            nextEnum();//continue
            return this;
        }

        public KnowledgeQueryBuilder withInsight(String eTag) {
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasInsight", R, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag, "Insight", 0, 0));
            nextEnum();//continue
            return this;
        }

        public Query build() {
            if(this.elements.get(this.elements.size()-1) instanceof EEntityBase) {
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

}
