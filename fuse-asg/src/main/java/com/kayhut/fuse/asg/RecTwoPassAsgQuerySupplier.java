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

        buildSubGraphRecForNext(eBaseAsgStart, queryElements);

        buildSubGraphForB(queryElements);

        AsgQuery asgQuery = AsgQuery.AsgQueryBuilder.anAsgQuery()
                .withName(query.getName())
                .withOnt(query.getOnt())
                .withStart(eBaseAsgStart).build();

        return asgQuery;
    }
    //endregion

    //region Private Methods
    private void buildSubGraphRecForNext(EBaseAsg eBaseAsg, Map<Integer, EBase> queryElements) {
        Stream.ofAll(new NextEbaseFactory().supply(eBaseAsg.geteBase())).forEach(eNum -> {
             EBase ebase =  queryElements.get(eNum);
             EBaseAsg eBaseAsgNext = EBaseAsg.EBaseAsgBuilder.anEBaseAsg()
                        .withEBase(ebase)
                        .build();
            eBaseAsg.addNextChild(eBaseAsgNext);
            buildSubGraphRecForNext(eBaseAsgNext, queryElements);
            addAsgElementToMap(eBaseAsg.geteBase().geteNum(),eBaseAsg);
            addAsgElementToMap(eBaseAsgNext.geteBase().geteNum(),eBaseAsgNext);
        });
    }

    private void buildSubGraphForB(Map<Integer, EBase> queryElements) {
        queryElements.forEach( (eNum,eBase) -> {
            Stream.ofAll(new BEbaseFactory().supply(eBase)).forEach(
                    b -> {
                        EBaseAsg eBaseAsg = this.queryAsgElements.get(eNum);
                        EBaseAsg eBaseAsgBChild = EBaseAsg.EBaseAsgBuilder.anEBaseAsg()
                                .withEBase(queryElements.get(b))
                                .build();
                        eBaseAsg.addBChild(eBaseAsgBChild);
                    }
            );
        });
    }

    private void addAsgElementToMap(int key, EBaseAsg eBaseAsg)
    {
        if (!this.queryAsgElements.containsKey(key))
        {
            this.queryAsgElements.put(key,eBaseAsg);
        }
    }
   //endregion

    //region Fields
    private Query query;
    private Map<Integer, EBaseAsg> queryAsgElements = new HashMap<>();

    //endregion
}
