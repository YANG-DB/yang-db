package com.kayhut.fuse.asg;

import com.google.common.base.Supplier;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.queryAsg.AsgQuery;
import com.kayhut.fuse.model.queryAsg.EBaseAsg;
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

        //working with the first element
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

    //region Private Methods
    private void buildSubGraphRec(EBaseAsg eBaseAsg, Map<Integer, EBase> queryElements) {
        Stream.ofAll(new NextEbaseFactory().supply(eBaseAsg.geteBase()))
                .forEach(eNum -> {
                    EBaseAsg eBaseAsgNext = EBaseAsg.EBaseAsgBuilder.anEBaseAsg()
                            .withEBase(queryElements.get(eNum))
                            .build();

                    eBaseAsg.addNextChild(eBaseAsgNext);
                    buildSubGraphRec(eBaseAsgNext, queryElements);
                });
    }
    //endregion

    //region Fields
    private Query query;
    //endregion
}
