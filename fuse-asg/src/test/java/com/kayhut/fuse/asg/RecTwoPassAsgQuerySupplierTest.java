package com.kayhut.fuse.asg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.queryAsg.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by benishue on 27-Feb-17.
 */
public class RecTwoPassAsgQuerySupplierTest {

    private static Query q1Obj = new Query();
    private static Query q5Obj = new Query();

    @Test
    public void transformQuery1ToAsgQuery() throws Exception {
        Supplier<AsgQuery> asgSupplier = new RecTwoPassAsgQuerySupplier(q1Obj);
        AsgQuery asgQuery = asgSupplier.get();
        assertEquals(asgQuery.getStart().geteBase().geteNum(), 0);

        //start = parent -> son (next element) -> call get parents -> (start) -> get eNum
        assertEquals(asgQuery.getStart().getNext().get(0).getParents().get(0).geteBase().geteNum(), 0);

        assertEquals(asgQuery.getStart().getNext().get(0).geteBase().geteNum(), 1);

        assertEquals(asgQuery.getStart().getNext().get(0).getNext().get(0).geteBase().geteNum(), 2);

        EBaseAsg elementEbaseAsg3 = asgQuery.getStart().getNext().get(0).getNext().get(0).getNext().get(0);

        assertEquals(elementEbaseAsg3.geteBase().geteNum(), 3);

        //Parent of element( eNum = 3) => element with eNum = 2
        assertEquals(elementEbaseAsg3.getParents().get(0).geteBase().geteNum(), 2);

        //No next for the last element
        assertEquals(elementEbaseAsg3.getNext().size() ,0);
    }

    @Test
    public void transformQuery5ToAsgQuery() throws Exception {
        Supplier<AsgQuery> asgSupplier = new RecTwoPassAsgQuerySupplier(q5Obj);
        AsgQuery asgQuery = asgSupplier.get();
        assertEquals(asgQuery.getStart().geteBase().geteNum(), 0);

        //start = parent -> son (next element) -> call get parents -> (start) -> get eNum
        assertEquals(asgQuery.getStart().getNext().get(0).getParents().get(0).geteBase().geteNum(), 0);


        EBaseAsg elementEbaseAsg1 = asgQuery.getStart().getNext().get(0);
        EBaseAsg elementEbaseAsg2 = elementEbaseAsg1.getNext().get(0);


        //{"eNum": 2, ..., "next": [3,5,11]
        HashSet<Integer> setOfNext= new HashSet<Integer>();
        for (EBaseAsg eBaseAsg : elementEbaseAsg2.getNext())
        {
            setOfNext.add(eBaseAsg.geteBase().geteNum());
        }
        HashSet<Integer> expectedNextSet = new HashSet<Integer>(Arrays.asList(3,5,11));
        Assert.assertEquals(expectedNextSet, setOfNext);


    }

    private static void createQ1()
    {
        q1Obj.setOnt("Dragons");
        q1Obj.setName("Q1");
        List<EBase> elements = new ArrayList<EBase>();

        /*
        {
          "eNum": 0,
          "type": "Start",
          "next": 1
        }
         */

        Start start  = new Start();
        start.seteNum(0);
        start.setNext(1);
        elements.add(start);

        /*
         {
          "eNum": 1,
          "type": "EConcrete",
          "eTag": "A",
          "eID": "12345678",
          "eType": 1,
          "eName": "Brandon Stark",
          "next": 2
         }
        */
        EConcrete eConcrete = new EConcrete();
        eConcrete.seteNum(1);
        eConcrete.seteTag("A");
        eConcrete.seteID("12345678");
        eConcrete.seteType(1);
        eConcrete.seteName("Brandon Stark");
        eConcrete.setNext(2);
        elements.add(eConcrete);

        /*
        {
          "eNum": 2,
          "type": "Rel",
          "rType": 1,
          "dir": "R",
          "next": 3
        }
         */
        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setrType(1);
        rel.setDir("R");
        rel.setNext(3);
        elements.add(rel);


        /*
        {
          "eNum": 3,
          "type": "ETyped",
          "eTag": "B",
          "eType": 2
        }
        */
        ETyped eTyped = new ETyped();
        eTyped.seteNum(3);
        eTyped.seteTag("B");
        eTyped.seteType(2);
        elements.add(eTyped);

        q1Obj.setElements(elements);
    }

    private static void createQ5(){
        try {
            q5Obj = new ObjectMapper().readValue("{\"ont\":\"Dragons\",\"name\":\"Q5\",\"elements\":[{\"eNum\":0,\"type\":\"Start\",\"next\":1},{\"eNum\":1,\"type\":\"ETyped\",\"eTag\":\"A\",\"eType\":1,\"next\":2},{\"eNum\":2,\"type\":\"Quant1\",\"qType\":\"all\",\"next\":[3,5,11]},{\"eNum\":3,\"type\":\"Rel\",\"rType\":1,\"dir\":\"R\",\"next\":4},{\"eNum\":4,\"type\":\"ETyped\",\"eTag\":\"B\",\"eType\":2},{\"eNum\":5,\"type\":\"Rel\",\"rType\":4,\"dir\":\"R\",\"next\":6},{\"eNum\":6,\"type\":\"ETyped\",\"eTag\":\"C\",\"eType\":1,\"next\":7},{\"eNum\":7,\"type\":\"Rel\",\"rType\":1,\"dir\":\"R\",\"next\":8},{\"eNum\":8,\"type\":\"ETyped\",\"eTag\":\"D\",\"eType\":2,\"next\":9},{\"eNum\":9,\"type\":\"Rel\",\"rType\":3,\"dir\":\"R\",\"next\":10},{\"eNum\":10,\"type\":\"ETyped\",\"eTag\":\"B\",\"eType\":2},{\"eNum\":11,\"type\":\"Rel\",\"rType\":4,\"dir\":\"R\",\"next\":12},{\"eNum\":12,\"type\":\"ETyped\",\"eTag\":\"E\",\"eType\":1,\"next\":13},{\"eNum\":13,\"type\":\"Rel\",\"rType\":1,\"dir\":\"R\",\"next\":14},{\"eNum\":14,\"type\":\"ETyped\",\"eTag\":\"D\",\"eType\":2}],\"nonidentical\":[[\"C\",\"E\"]]}", Query.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setup() {
    }

    @BeforeClass
    public static void setUpOnce() {
        createQ1();
        createQ5();
    }



}