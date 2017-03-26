package com.kayhut.fuse.asg.builder;

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
public class RecTwoPassAsgQuerySupplier implements Supplier<AsgQuery> {

    //region Constructor
    public RecTwoPassAsgQuerySupplier(Query query) {
        this.query = query;
    }
    //endregion

    //region Supplier Implementation
    @Override
    public AsgQuery get() {
        Map<Integer, EBase> queryElements = new HashMap<>();
        Stream.ofAll(query.getElements()).forEach(eBase -> queryElements.put(eBase.geteNum(), eBase));

        //Working with the first element
        Start start = (Start)queryElements.get(0);

        //Building the root of the AsgQuery (i.e., start Ebase)
        AsgEBase asgEBaseStart = AsgEBase.EBaseAsgBuilder.anEBaseAsg()
                .withEBase(start).build();

        buildSubGraphRec(asgEBaseStart, queryElements);

        AsgQuery asgQuery = AsgQuery.AsgQueryBuilder.anAsgQuery()
                .withName(query.getName())
                .withOnt(query.getOnt())
                .withStart(asgEBaseStart).build();

        return asgQuery;
    }
    //endregion

    //region Private Methods
    private void buildSubGraphRec(AsgEBase asgEBaseCurrent, Map<Integer, EBase> queryElements) {
        EBase eBaseCurrent = asgEBaseCurrent.geteBase();

        Stream.ofAll(new NextEbaseFactory().supply(eBaseCurrent)).forEach(eNum -> {
             EBase eBaseNext =  queryElements.get(eNum);
             AsgEBase asgEBaseNext = AsgEBase.EBaseAsgBuilder.anEBaseAsg()
                        .withEBase(eBaseNext)
                        .build();

            asgEBaseCurrent.addNextChild(asgEBaseNext);

            buildSubGraphRec(asgEBaseNext, queryElements);
        });


        Stream.ofAll(new BEbaseFactory().supply(eBaseCurrent)).forEach(
                eNum -> {
                    EBase eBaseB =  queryElements.get(eNum);
                    AsgEBase asgEBaseB = AsgEBase.EBaseAsgBuilder.anEBaseAsg()
                            .withEBase(eBaseB)
                            .build();

                    asgEBaseCurrent.addBChild(asgEBaseB);
                    buildSubGraphRec(asgEBaseB, queryElements);
                }
        );
    }

   //endregion

    //region Fields
    private Query query;
    private Map<Integer, AsgEBase> queryAsgElements = new HashMap<>();

    //endregion
}
