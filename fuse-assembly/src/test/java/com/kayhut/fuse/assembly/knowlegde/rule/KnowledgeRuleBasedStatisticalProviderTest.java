package com.kayhut.fuse.assembly.knowlegde.rule;

import com.kayhut.fuse.assembly.knowlegde.KnowledgeRuleBasedStatisticalProvider;
import com.kayhut.fuse.assembly.knowlegde.KnowlegdeOntology;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created by lior.perry on 3/5/2018.
 */
public class KnowledgeRuleBasedStatisticalProviderTest {
    private KnowledgeRuleBasedStatisticalProvider statisticalProvider;
    private Ontology ontology;

    @Before
    public void setUp() throws Exception {
        statisticalProvider = new KnowledgeRuleBasedStatisticalProvider();
        ontology = KnowlegdeOntology.createOntology();
    }

    @Test
    public void testNodeStatistics() throws IOException {
        StatisticsProvider statisticsProvider = statisticalProvider.get(ontology);
        ETyped eTyped = new ETyped(1, "a", "Entity", 0, 0);
        assertEquals(statisticsProvider.getNodeStatistics(eTyped).getTotal(), 500000,0.5);
        eTyped = new ETyped(1, "a", "Evalue", 0, 0);
        assertEquals(statisticsProvider.getNodeStatistics(eTyped).getTotal(), 1000000,0.5);
        eTyped = new ETyped(1, "a", "Reference", 0, 0);
        assertEquals(statisticsProvider.getNodeStatistics(eTyped).getTotal(), 500000,0.5);
        eTyped = new ETyped(1, "a", "Insight", 0, 0);
        assertEquals(statisticsProvider.getNodeStatistics(eTyped).getTotal(), 25000,0.5);
    }

    @Test
    public void testNodeFilterStatisticsSingleEprop() throws IOException {
        StatisticsProvider statisticsProvider = statisticalProvider.get(ontology);
        ETyped eTyped = new ETyped(1, "a", "Entity", 0, 0);
        EPropGroup group = new EPropGroup(101, new EProp(102, "fieldId", Constraint.of(ConstraintOp.eq,"title")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 500,0.5);

        eTyped = new ETyped(1, "a", "Entity", 0, 0);
        group = new EPropGroup(101, new EProp(102, "stam", Constraint.of(ConstraintOp.eq,"123")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 1000,0.5);

        eTyped = new ETyped(1, "a", "Entity", 0, 0);
        group = new EPropGroup(101, new EProp(102, "stam", (Constraint) null));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 500000,0.5);

        eTyped = new ETyped(1, "a", "Entity", 0, 0);
        group = new EPropGroup(101, new EProp(102, "logicalId", Constraint.of(ConstraintOp.likeAny,Arrays.asList("*","stam"))));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 500000,0.5);

        eTyped = new ETyped(1, "a", "Entity", 0, 0);
        group = new EPropGroup(101, new EProp(102, "logicalId", Constraint.of(ConstraintOp.likeAny,Arrays.asList("a*","b*"))));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 125,0.5);

        eTyped = new ETyped(1, "a", "Entity", 0, 0);
        group = new EPropGroup(101, new EProp(102, "logicalId", Constraint.of(ConstraintOp.like, "**")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 500000,0.5);

        eTyped = new ETyped(1, "a", "Entity", 0, 0);
        group = new EPropGroup(101, new EProp(102, "logicalId", Constraint.of(ConstraintOp.like, "*a*")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 83.3,0.5);

        eTyped = new ETyped(1, "a", "Entity", 0, 0);
        group = new EPropGroup(101, new EProp(102, "context", Constraint.of(ConstraintOp.eq,"^")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 2500,0.5);

        eTyped = new ETyped(1, "a", "Evalue", 0, 0);
        group = new EPropGroup(101, new EProp(102, "fieldId", Constraint.of(ConstraintOp.eq,"title")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 1000,0.5);

        eTyped = new ETyped(1, "a", "Evalue", 0, 0);
        group = new EPropGroup(101, new EProp(102, "context", Constraint.of(ConstraintOp.likeAny,"e4")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 250,0.5);

        eTyped = new ETyped(1, "a", "Evalue", 0, 0);
        group = new EPropGroup(101,
                new EProp(103, "stringValue", Constraint.of(ConstraintOp.like,"123")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 33.3,0.5);

        eTyped = new ETyped(1, "a", "Reference", 0, 0);
        group = new EPropGroup(101, new EProp(102, "url", Constraint.of(ConstraintOp.eq,"45")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 100.0,0.5);

        eTyped = new ETyped(1, "a", "Insight", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "entityIds", Constraint.of(ConstraintOp.likeAny,"234*")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 62.5,0.5);
    }

    @Test
    public void testNodeFilterStatisticsDualEprop() throws IOException {
        StatisticsProvider statisticsProvider = statisticalProvider.get(ontology);
        ETyped eTyped = new ETyped(1, "a", "Entity", 0, 0);
        EPropGroup group = new EPropGroup(101,
                new EProp(102, "fieldId", Constraint.of(ConstraintOp.eq,"title")),
                new EProp(103, "nicknames", Constraint.of(ConstraintOp.eq,"234")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 500,0.5);

        eTyped = new ETyped(1, "a", "Evalue", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "logicalId", Constraint.of(ConstraintOp.eq,"2")),
                new EProp(103, "stringValue", Constraint.of(ConstraintOp.like,Arrays.asList("*","stam"))));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 1000000,0.5);

        eTyped = new ETyped(1, "a", "Evalue", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "fieldId", Constraint.of(ConstraintOp.eq,"title")),
                new EProp(103, "stringValue", Constraint.of(ConstraintOp.like,"34*")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 33.3,0.5);

        eTyped = new ETyped(1, "a", "Evalue", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "fieldId", Constraint.of(ConstraintOp.eq,"nicknames")),
                new EProp(103, "stringValue", Constraint.of(ConstraintOp.like,"*.*")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 33.3,0.5);

        eTyped = new ETyped(1, "a", "Evalue", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "fieldId", Constraint.of(ConstraintOp.eq,"stam")),
                new EProp(103, "stringValue", Constraint.of(ConstraintOp.like,"rg")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 33.3,0.5);

        eTyped = new ETyped(1, "a", "Reference", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "context", Constraint.of(ConstraintOp.likeAny,Arrays.asList("*a","stam"))),
                new EProp(103, "url", Constraint.of(ConstraintOp.eq,"a")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 100,0.5);

        eTyped = new ETyped(1, "a", "Insight", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "entityIds", Constraint.of(ConstraintOp.eq,"*")),
                new EProp(102, "entityIds", Constraint.of(ConstraintOp.likeAny,"234")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 25,0.5);

        eTyped = new ETyped(1, "a", "Insight", 0, 0);
        group = new EPropGroup(101,new EProp(102, "entityIds",
                Constraint.of(ConstraintOp.likeAny,Arrays.asList("*","stam"))));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 25000,0.5);
    }

    @Test
    public void testNodeFilterStatisticsDualEpropMore() throws IOException {
        StatisticsProvider statisticsProvider = statisticalProvider.get(ontology);
        ETyped eTyped = new ETyped(1, "a", "Entity", 0, 0);
        EPropGroup group = new EPropGroup(101,
                new EProp(102, "", Constraint.of(ConstraintOp.like,"123")),
                new EProp(103, "fieldId", Constraint.of(ConstraintOp.eq,"nicknames")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 1000,0.5);

        eTyped = new ETyped(1, "a", "Entity", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "bbb", Constraint.of(ConstraintOp.like,"345*")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 1666.6,0.5);

        eTyped = new ETyped(1, "a", "Entity", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "bbb", Constraint.of(ConstraintOp.eq,"123")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 1000,0.5);

        eTyped = new ETyped(1, "a", "Evalue", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "fieldId", Constraint.of(ConstraintOp.eq,"title")),
                new EProp(103, "stringValue", Constraint.of(ConstraintOp.contains,"*")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 25,0.5);

        eTyped = new ETyped(1, "a", "Reference", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "context", Constraint.of(ConstraintOp.likeAny,Arrays.asList("a","stam"))),
                new EProp(103, "url", Constraint.of(ConstraintOp.eq,"34")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 100,0.5);

        eTyped = new ETyped(1, "a", "Insight", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "entityIds", Constraint.of(ConstraintOp.likeAny,"43")),
                new EProp(103, "url", Constraint.of(ConstraintOp.eq,"A")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 62.5,0.5);
    }

    @Test
    public void testNodeNoSuch() throws IOException {
        StatisticsProvider statisticsProvider = statisticalProvider.get(ontology);
        ETyped eTyped = new ETyped(1, "a", "entity1", 0, 0);
        assertEquals(statisticsProvider.getNodeStatistics(eTyped ).getTotal(), 1000000,0.5);

        eTyped = new ETyped(1, "a", "rs.value", 0, 0);
        EPropGroup group = new EPropGroup(101,
                new EProp(102, "fieldId", Constraint.of(ConstraintOp.eq,"234")),
                new EProp(103, "title123", Constraint.of(ConstraintOp.like,"42")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 5000,0.5);

        eTyped = new ETyped(1, "a", "Reference", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "a", Constraint.of(ConstraintOp.likeAny,"*")),
                new EProp(103, "b", Constraint.of(ConstraintOp.eq,"8")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 500000,0.5);

        eTyped = new ETyped(1, "a", "insighta", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "entityIds", Constraint.of(ConstraintOp.likeAny,"8")),
                new EProp(103, "url", Constraint.of(ConstraintOp.eq,"4567")));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 5000,0.5);
    }


}
