package com.kayhut.fuse.asg;

import com.google.common.base.Supplier;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.queryAsg.*;
import javaslang.collection.Stream;
import java.util.*;

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
        EBaseAsg eBaseAsgStart = EBaseAsg.EBaseAsgBuilder.anEBaseAsg()
                .withEBase(start)
                .withParents(null).build();

        buildSubGraphRec(eBaseAsgStart, queryElements);

        AsgQuery asgQuery = AsgQuery.AsgQueryBuilder.anAsgQuery()
                .withName(query.getName())
                .withOnt(query.getOnt())
                .withStart(eBaseAsgStart).build();

        return asgQuery;
    }
    //endregion

    //region Private Methods
    private void buildSubGraphRec(EBaseAsg eBaseAsgCurrent, Map<Integer, EBase> queryElements) {
        EBase eBaseCurrent = eBaseAsgCurrent.geteBase();

        Stream.ofAll(new NextEbaseFactory().supply(eBaseCurrent)).forEach(eNum -> {
             EBase eBaseNext =  queryElements.get(eNum);
             EBaseAsg eBaseAsgNext = EBaseAsg.EBaseAsgBuilder.anEBaseAsg()
                        .withEBase(eBaseNext)
                        .build();

            eBaseAsgCurrent.addNextChild(eBaseAsgNext);

            buildSubGraphRec(eBaseAsgNext, queryElements);
        });


        Stream.ofAll(new BEbaseFactory().supply(eBaseCurrent)).forEach(
                eNum -> {
                    EBase eBaseB =  queryElements.get(eNum);
                    EBaseAsg eBaseAsgB = EBaseAsg.EBaseAsgBuilder.anEBaseAsg()
                            .withEBase(eBaseB)
                            .build();

                    eBaseAsgCurrent.addBChild(eBaseAsgB);
                    buildSubGraphRec(eBaseAsgB, queryElements);
                }
        );
    }

   //endregion

    //region Fields
    private Query query;
    private Map<Integer, EBaseAsg> queryAsgElements = new HashMap<>();

    //endregion
}
