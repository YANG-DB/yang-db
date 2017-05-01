package com.kayhut.fuse.stat.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.stat.Util.StatUtil;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;

/**
 * Created by benishue on 30-Apr-17.
 */
public class StatContainerTest {
    @Test
    public void testStatModel() throws Exception {


        HistogramNumeric histogramDragonAge = HistogramNumeric.HistogramNumericBuilder.aHistogramNumeric()
                .withMin("10").withMax("100").withInterval("10").build();
        HistogramString histogramDragonName = HistogramString.HistogramStringBuilder.aHistogramString()
                .withPrefixSize("2")
                .withInterval("3").build();

        HistogramManual histogramDragonAddress = HistogramManual.HistogramManualBuilder.aHistogramManual()
                .withBuckets(Arrays.asList(
                        new Bucket("abc", "dzz"),
                        new Bucket("efg", "hij"),
                        new Bucket("klm", "xyz")
                ))
                .build();


        Field nameField = new Field("name",histogramDragonName);
        Field ageField = new Field("age",histogramDragonAge);
        Field addressField = new Field("address",histogramDragonAddress);


        Type typeDragon = new Type("dragon",Arrays.asList(ageField, nameField, addressField));

        Mapping mapping = Mapping.MappingBuilder.aMapping().withIndices(Arrays.asList("index1","index2"))
                .withTypes(Arrays.asList("dragon")).build();

        StatContainer statContainer = StatContainer.StatContainerBuilder.aStatContainer()
                .withMappings(Arrays.asList(mapping))
                .withTypes(Arrays.asList(typeDragon))
                .build();


        ObjectMapper mapper = new ObjectMapper();
        String statActualJson = mapper.writeValueAsString(statContainer);
        System.out.println(statActualJson);
        String statExpectedJson = StatUtil.readJsonToString("stats_fields.json");




        JSONAssert.assertEquals(statExpectedJson, statActualJson, false);

        StatContainer resultObj = new ObjectMapper().readValue(statExpectedJson, StatContainer.class);
        Assert.assertNotNull(resultObj);

    }



}