package com.kayhut.fuse.dispatcher.asg;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.asg.builder.BNextFactory;
import com.kayhut.fuse.dispatcher.asg.builder.NextEbaseFactory;
import com.kayhut.fuse.dispatcher.query.QueryTransformer;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.ParameterizedQuery;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Start;
import javaslang.collection.Stream;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roman on 12/15/2017.
 */
public class QueryToAsgTransformer implements QueryTransformer<Query, AsgQuery> {
    //region Constructors
    @Inject
    public QueryToAsgTransformer() {
        this.nextFactory = new NextEbaseFactory();
        this.bellowFactory = new BNextFactory();
    }
    //endregion

    //region QueryTransformer Implementation
    @Override
    public AsgQuery transform(Query query) {
        if(query==null)
            throw new IllegalArgumentException("Query was null - probably serialization from input failed");

        Map<Integer, EBase> queryElements = new HashMap<>();
        Stream.ofAll(query.getElements()).forEach(eBase -> queryElements.put(eBase.geteNum(), eBase));

        //Working with the first element
        Start start = (Start) queryElements.get(0);

        //Building the root of the AsgQuery (i.e., start Ebase)
        AsgEBase asgEBaseStart = AsgEBase.Builder.get()
                .withEBase(start).build();

        queryAsgElements.put(asgEBaseStart.geteNum(),asgEBaseStart);
        buildSubGraphRec(asgEBaseStart, queryElements);

        AsgQuery.AsgQueryBuilder builder = AsgQuery.AsgQueryBuilder.anAsgQuery()
                .withName(query.getName())
                .withOnt(query.getOnt())
                .withStart(asgEBaseStart)
                .withElements(queryAsgElements.values());

        if(query instanceof ParameterizedQuery) {
            builder.withParams(((ParameterizedQuery) query).getParams());
        }
        return builder.build();
    }
    //endregion

    //region Private Methods
    private void buildSubGraphRec(AsgEBase asgEBaseCurrent, Map<Integer, EBase> queryElements) {
        EBase eBaseCurrent = asgEBaseCurrent.geteBase();

        Stream.ofAll(nextFactory.supplyNext(eBaseCurrent))
                .filter(b -> queryElements.get(b) != null)
                .forEach(eNum -> {
                    EBase eBaseNext = queryElements.get(eNum);
                    AsgEBase asgEBaseNext = AsgEBase.Builder.get()
                            .withEBase(eBaseNext)
                            .build();
                    queryAsgElements.put(eNum,asgEBaseNext);
                    asgEBaseCurrent.addNextChild(asgEBaseNext);

                    buildSubGraphRec(asgEBaseNext, queryElements);
                });


        Stream.ofAll(bellowFactory.supplyBellow(eBaseCurrent))
                .filter(b -> queryElements.get(b) != null)
                .forEach(
                eNum -> {
                    EBase eBaseB = queryElements.get(eNum);
                    AsgEBase asgEBaseB = AsgEBase.Builder.get()
                            .withEBase(eBaseB)
                            .build();

                    queryAsgElements.put(eNum,asgEBaseB);
                    asgEBaseCurrent.addBChild(asgEBaseB);
                    buildSubGraphRec(asgEBaseB, queryElements);
                }
        );
    }
    //endregion

    //region Fields
    private NextFactory nextFactory;
    private BellowFactory bellowFactory;
    private Map<Integer, AsgEBase<? extends EBase>> queryAsgElements = new HashMap<>();

    //endregion

}
