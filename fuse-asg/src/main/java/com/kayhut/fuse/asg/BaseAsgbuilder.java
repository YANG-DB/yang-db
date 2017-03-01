package com.kayhut.fuse.asg;

import com.google.common.eventbus.Subscribe;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.queryAsg.AsgQuery;
import com.kayhut.fuse.model.queryAsg.EBaseAsg;
import org.neo4j.shell.kernel.apps.cypher.Foreach;

import java.util.*;

/**
 * Created by benishue on 27-Feb-17.
 */
public class BaseAsgbuilder implements Asgbuilder {

    private Map<Integer, EBase> queryElementsMap;
    private static Query q1Obj = new Query();

    @Override
    @Subscribe
    public AsgQuery transformQueryToAsgQuery(Query query) {
        queryElementsMap = new HashMap<Integer,EBase>();
        for(EBase eBase : query.getElements())
        {
            queryElementsMap.put(eBase.geteNum(),eBase);
        }

        AsgQuery asgQuery = AsgQuery.AsgQueryBuilder.anAsgQuery()
                .withName(query.getName())
                .withOnt(query.getOnt()).build();

        //working with the first element
        Start start = (Start)queryElementsMap.get(0);

        //Building the root of the AsgQuery (i.e., start Ebase)
        EBaseAsg eBaseAsgStart = EBaseAsg.EBaseAsgBuilder.anEBaseAsg()
                .withEBase(start)
                .withParents(null).build();


        transformQueryToAsgQuery(eBaseAsgStart);
        asgQuery.setStart(eBaseAsgStart);
        return asgQuery;
    }


    public void transformQueryToAsgQuery(EBaseAsg eBaseAsg) {

        EBase eBase = eBaseAsg.geteBase();
        NextEbaseFactory factory = new NextEbaseFactory();
        List<Integer> listNext = factory.supply(eBase);

        if (listNext.contains(0)) //The last element doesn't have next (i.e., Next = 0)
            return;
        for(int i : listNext )
        {
            EBaseAsg eBaseAsgNext = EBaseAsg.EBaseAsgBuilder.anEBaseAsg()
                    .withEBase(queryElementsMap.get(i))
                    .build();
            eBaseAsgNext.AddToParentsList(eBaseAsg);
            eBaseAsg.AddToNextList(eBaseAsgNext);
            transformQueryToAsgQuery(eBaseAsgNext);
        }
    }


}
