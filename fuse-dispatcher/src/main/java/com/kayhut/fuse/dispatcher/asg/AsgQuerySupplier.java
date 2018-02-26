package com.kayhut.fuse.dispatcher.asg;

import com.google.common.base.Supplier;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Start;
import javaslang.collection.Stream;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by benishue on 27-Feb-17.
 */
public class AsgQuerySupplier implements Supplier<AsgQuery> {

    //region Constructor
    public AsgQuerySupplier(Query query, NextFactory nextFactory, BellowFactory bellowFactory) {
        this.query = query;
        this.factory = nextFactory;
        this.bellowFactory = bellowFactory;
    }
    //endregion

    //region Supplier Implementation
    @Override
    public AsgQuery get() {
        Map<Integer, EBase> queryElements = new HashMap<>();
        Stream.ofAll(query.getElements()).forEach(eBase -> queryElements.put(eBase.geteNum(), eBase));

        //Working with the first element
        Start start = (Start) queryElements.get(0);

        //Building the root of the AsgQuery (i.e., start Ebase)
        AsgEBase asgEBaseStart = AsgEBase.Builder.get()
                .withEBase(start).build();

        queryAsgElements.put(asgEBaseStart.geteNum(),asgEBaseStart);
        buildSubGraphRec(asgEBaseStart, queryElements);

        AsgQuery asgQuery = AsgQuery.AsgQueryBuilder.anAsgQuery()
                .withName(query.getName())
                .withOnt(query.getOnt())
                .withStart(asgEBaseStart)
                .withElements(queryAsgElements.values())
                .build();
        return asgQuery;
    }
    //endregion

    //region Private Methods
    private void buildSubGraphRec(AsgEBase asgEBaseCurrent, Map<Integer, EBase> queryElements) {
        EBase eBaseCurrent = asgEBaseCurrent.geteBase();

        Stream.ofAll(factory.supplyNext(eBaseCurrent))
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


        Stream.ofAll(bellowFactory.supplyBellow(eBaseCurrent)).forEach(
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
    private Query query;
    private NextFactory factory;
    private BellowFactory bellowFactory;
    private Map<Integer, AsgEBase<? extends EBase>> queryAsgElements = new HashMap<>();

    //endregion
}
