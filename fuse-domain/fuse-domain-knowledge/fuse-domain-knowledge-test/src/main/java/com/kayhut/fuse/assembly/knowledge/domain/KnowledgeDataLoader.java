package com.kayhut.fuse.assembly.knowledge.domain;

/*-
 * #%L
 * fuse-domain-knowledge-test
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

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lior.perry pc on 5/12/2018.
 */
public class KnowledgeDataLoader {
    private final String cFixedContext1 = "context1";
    private final String cPersonCategory = "person";
    private final String cCarCategory = "car";

    private final String cUser1 = "Abraham";
    private final String cUser2 = "Moses";
    private final String cUser3 = "Rebbeca";
    private final String cUser4 = "Jacob";
    private final String cUser5 = "Sara";
    private final String cUser6 = "Monique";

    private final int cAuthCount = 1;
    private final List<String> cAuthsList = Arrays.asList("source1.procedure1", "source2.procedure2");

    private SimpleDateFormat sdf;

    public KnowledgeDataLoader() {
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private void setDefaultEntityValues(Entity e) {
        e.setContext(cFixedContext1);
        e.setAuthorization(cAuthsList);
        e.setAuthorizationCount(cAuthCount);
    }

    // Creation & Deleting rellevant indices
    private void startIndices() {

    }

    private List<KnowledgeReference> _knowledgeReferenceList;
    private List<KnowledgeEntity> _knowledgeEntitiesList;

    private void initLists() {
        _knowledgeReferenceList = new ArrayList<KnowledgeReference>();
        _knowledgeEntitiesList = new ArrayList<KnowledgeEntity>();
    }

    // Population of indices
    private void populateIndices() {

    }

    // Cleaning relevant indices
    private void removeIndices() {

    }

    public void setFixedScenario_1() {
        initLists();

        // Setting of the fixed scenario

        // [1.1] Defining Entities
        KnowledgeEntity markPerson = new KnowledgeEntity();
        KnowledgeEntity rubenPerson = new KnowledgeEntity();
        KnowledgeEntity dorisPerson = new KnowledgeEntity();
        KnowledgeEntity hanaPerson = new KnowledgeEntity();
        KnowledgeEntity bmwCar = new KnowledgeEntity();
        KnowledgeEntity skodaCar = new KnowledgeEntity();
        KnowledgeEntity fiatCar = new KnowledgeEntity();
        // [1.2] Defining Entity-Values
        KnowledgeEntityValue markAgeProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue markNickNameProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue markNameProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue rubenAgeProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue rubenNickNameProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue rubenNameProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue dorisAgeProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue dorisNameProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue dorisNickNameProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue hanaAgeProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue hanaNameProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue hanaNickNameProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue bmwColorProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue bmwLicensePlateProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue bmwPriceProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue skodaColorProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue skodaLicensePlateProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue skodaPriceProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue fiatColorProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue fiatLicensePlateProperty = new KnowledgeEntityValue();
        KnowledgeEntityValue fiatPriceProperty = new KnowledgeEntityValue();
        // [1.3] Defining References
        KnowledgeReference ref1 = new KnowledgeReference();
        KnowledgeReference ref2 = new KnowledgeReference();
        KnowledgeReference ref3 = new KnowledgeReference();
        KnowledgeReference ref4 = new KnowledgeReference();
        KnowledgeReference ref5 = new KnowledgeReference();
        KnowledgeReference ref6 = new KnowledgeReference();
        // [1.4] Defining Insights
        KnowledgeInsight insight1 = new KnowledgeInsight();
        KnowledgeInsight insight2 = new KnowledgeInsight();
        KnowledgeInsight insight3 = new KnowledgeInsight();
        // [1.5] Defining Relations
        KnowledgeRelation relation1 = new KnowledgeRelation();
        KnowledgeRelation relation2 = new KnowledgeRelation();
        KnowledgeRelation relation3 = new KnowledgeRelation();
        KnowledgeRelation relation4 = new KnowledgeRelation();
        // [1.6] Defining Relation-Values
        KnowledgeRelationValue rel1MethodProperty = new KnowledgeRelationValue();
        KnowledgeRelationValue rel1DateProperty = new KnowledgeRelationValue();
        KnowledgeRelationValue rel1PriorityProperty = new KnowledgeRelationValue();
        KnowledgeRelationValue rel2MethodProperty = new KnowledgeRelationValue();
        KnowledgeRelationValue rel2DateProperty = new KnowledgeRelationValue();
        KnowledgeRelationValue rel2PriorityProperty = new KnowledgeRelationValue();
        KnowledgeRelationValue rel3MethodProperty = new KnowledgeRelationValue();
        KnowledgeRelationValue rel3DateProperty = new KnowledgeRelationValue();
        KnowledgeRelationValue rel3PriorityProperty = new KnowledgeRelationValue();
        KnowledgeRelationValue rel4MethodProperty = new KnowledgeRelationValue();
        KnowledgeRelationValue rel4DateProperty = new KnowledgeRelationValue();
        KnowledgeRelationValue rel4PriorityProperty = new KnowledgeRelationValue();

        // [2.1] Filling Entities
        Entity markPEntity = markPerson.getEntity();
        markPEntity.setCategory(cPersonCategory);
        markPEntity.setCreationUser(cUser1);
        markPEntity.setLastUpdateUser(cUser6);
        markPEntity.setCreationTime(new Date(6000));
        markPEntity.setLastUpdateTime(new Date(6500));
        setDefaultEntityValues(markPEntity);
        markPEntity.setLogicalId("1");

        Entity bmwCEntity = bmwCar.getEntity();
        bmwCEntity.setCategory(cCarCategory);
        bmwCEntity.setCreationUser(cUser1);
        bmwCEntity.setLastUpdateUser(cUser6);
        bmwCEntity.setCreationTime(new Date(6000));
        bmwCEntity.setLastUpdateTime(new Date(6500));
        setDefaultEntityValues(bmwCEntity);

        // [2.2] Filling Entity-Values


        // [2.3] Filling References
        Reference rref1 = ref1.getRef();
        rref1.setSystem("system1");
        rref1.setTitle("I am a mediocre reference, not of much value");
        rref1.setUrl("http://some-host:1035/ref1");
        Reference rref2 = ref1.getRef();
        rref2.setSystem("system1");
        rref2.setTitle("I am a reasonable reference, you may rely on me if you wish");
        rref2.setUrl("http://some-host:1035/ref2");
        Reference rref3 = ref1.getRef();
        rref3.setSystem("system1");
        rref3.setTitle("I am a mediocre reference, not of much value");
        rref3.setUrl("http://some-host:1045/ref3");
        Reference rref4 = ref1.getRef();
        rref4.setSystem("system1");
        rref4.setTitle("I am extremely valued reference. I've got all the important stuff there.");
        rref4.setUrl("http://some-host:1035/ref4");
        Reference rref5 = ref1.getRef();
        rref5.setSystem("system2");
        rref5.setTitle("I am an average refrence. With all the nonsense material produced by google AI.");
        rref5.setUrl("http://some-host:1035/ref5");
        Reference rref6 = ref1.getRef();
        rref6.setSystem("system2");
        rref6.setTitle("I am an average refrence. With all the nonsense material produced by google AI.");
        rref6.setUrl("http://some-host:1035/ref6");

        // [2.4] Filling Insights
        Insight iinsight1 = new Insight();
        iinsight1.setContent("This is a rather simple insight. It only dictates that Mark is a burgan man as he is holding a skoda only as his car.");
        insight1.setId(1);
        insight1.addEntity(skodaCar);
        insight1.addRef(ref1);
        Insight iinsight2 = new Insight();
        iinsight2.setContent("BMW is one of the more expansive cars, and therefore it indicates something about the wealth of the person holding it.");
        insight2.setId(2);
        insight2.addEntity(bmwCar);
        insight2.addEntity(rubenPerson);
        insight1.addRef(ref3);
        Insight iinsight3 = new Insight();
        iinsight3.setContent("Hana has become a quite indicative name for a person who has deep feeling for the Bible. This kind of person is relligous as list at the spiritual level.");
        insight3.setId(3);
        insight3.addEntity(hanaPerson);
        insight3.addRef(ref5);
        insight3.addRef(ref6);

        // [2.6] Filling Relations
        relation1.setId("1");
        relation1.setEntity(markPerson, true);
        relation1.setEntity(skodaCar, false);
        Relation rrelation1 = relation1.getRelation();
        rrelation1.setCategory("owns");
        rrelation1.setContext("context1");
        rrelation1.setAuthorizationCount(cAuthCount);
        rrelation1.setAuthorization(cAuthsList);
        rrelation1.setCreationUser("Dina");
        rrelation1.setLastUpdateUser("Dina");

        // [2.7] Filling Relation-Values
        rel1MethodProperty.setId("1");
        rel1MethodProperty.setRelation(relation1);
        RelationValue rel1MethodPropertyValue = rel1MethodProperty.getRelationValue();
        rel1MethodPropertyValue.setFieldId("method");
        rel1MethodPropertyValue.setStringValue("Bank-Transfer");
    }
}
