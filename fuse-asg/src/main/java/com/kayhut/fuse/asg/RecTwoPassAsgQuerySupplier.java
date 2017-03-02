package com.kayhut.fuse.asg;

import com.google.common.base.Supplier;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.queryAsg.AsgQuery;
import com.kayhut.fuse.model.queryAsg.EBaseAsg;
import javaslang.collection.Stream;
import org.neo4j.shell.kernel.apps.cypher.Foreach;

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

        buildSubGraphForB(queryAsgElements, queryElements);

        AsgQuery asgQuery = AsgQuery.AsgQueryBuilder.anAsgQuery()
                .withName(query.getName())
                .withOnt(query.getOnt())
                .withStart(eBaseAsgStart).build();

        return asgQuery;
    }

    private void buildSubGraphForB(Map<Integer, EBaseAsg> queryAsgElements, Map<Integer, EBase> queryElements) {
        queryElements.forEach( (eNum,eBase) -> {
            Stream.ofAll(new BEbaseFactory().supply(eBase)).forEach(
                    b -> {
                        EBaseAsg eBaseAsg = queryAsgElements.get(eNum);
                        EBaseAsg eBaseAsgBChild = EBaseAsg.EBaseAsgBuilder.anEBaseAsg()
                                .withEBase(queryElements.get(b))
                                .build();
                        eBaseAsg.addBChild(eBaseAsgBChild);
                    }
            );
        });
    }

    //region Private Methods
    private void buildSubGraphRecForNext(EBaseAsg eBaseAsg, Map<Integer, EBase> queryElements) {
        Stream.ofAll(new NextEbaseFactory().supply(eBaseAsg.geteBase())).forEach(eNum -> {
             EBase ebase =  queryElements.get(eNum);
             EBaseAsg eBaseAsgNext = EBaseAsg.EBaseAsgBuilder.anEBaseAsg()
                        .withEBase(ebase)
                        .build();
            eBaseAsg.addNextChild(eBaseAsgNext);
            buildSubGraphRecForNext(eBaseAsgNext, queryElements);
            queryAsgElements.put(eBaseAsg.geteBase().geteNum(),eBaseAsg);
            queryAsgElements.put(eBaseAsgNext.geteBase().geteNum(),eBaseAsgNext);
        });
    }
   //endregion

    //region Fields
    private Query query;
    private Map<Integer, EBaseAsg> queryAsgElements = new HashMap<>();

    //endregion
}
