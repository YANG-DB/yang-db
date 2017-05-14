package com.kayhut.fuse.stat.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.stat.Util.StatUtil;
import com.kayhut.fuse.stat.model.configuration.*;
import com.kayhut.fuse.stat.model.configuration.histogram.HistogramComposite;
import com.kayhut.fuse.stat.model.configuration.histogram.HistogramManual;
import com.kayhut.fuse.stat.model.configuration.histogram.HistogramNumeric;
import com.kayhut.fuse.stat.model.configuration.histogram.HistogramString;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;

/**
 * Created by benishue on 30-Apr-17.
 */
public class StatConfigurationTest {
    @Test
    public void testStatModel() throws Exception {
        HistogramNumeric histogramDragonAge = HistogramNumeric.HistogramNumericBuilder.aHistogramNumeric()
                .withMin(10).withMax(100).withNumOfBins(10).build();
        HistogramString histogramDragonName = HistogramString.HistogramStringBuilder.aHistogramString()
                .withPrefixSize(3)
                .withInterval(10).withNumOfChars(26).withFirstCharCode("97").build();

        HistogramManual histogramDragonAddress = HistogramManual.HistogramManualBuilder.aHistogramManual()
                .withBuckets(Arrays.asList(
                        new Bucket("abc", "dzz"),
                        new Bucket("efg", "hij"),
                        new Bucket("klm", "xyz")
                )).withDataType("string")
                .build();

        HistogramComposite histogramDragonColor = HistogramComposite.HistogramCompositeBuilder.aHistogramComposite()
                .withManualBuckets(Arrays.asList(
                        new Bucket("00", "11"),
                        new Bucket("22", "33"),
                        new Bucket("44", "55")
                )).withDataType("string")
                .withAutoBuckets(HistogramString.HistogramStringBuilder.aHistogramString()
                .withFirstCharCode("97")
                .withInterval(10)
                .withNumOfChars(26)
                .withPrefixSize(3).build())
                .build();


        Field nameField = new Field("name",histogramDragonName);
        Field ageField = new Field("age",histogramDragonAge);
        Field addressField = new Field("address",histogramDragonAddress);
        Field colorField = new Field("color",histogramDragonColor);


        Type typeDragon = new Type("dragon",Arrays.asList(ageField, nameField, addressField, colorField));

        Mapping mapping = Mapping.MappingBuilder.aMapping().withIndices(Arrays.asList("index1","index2"))
                .withTypes(Arrays.asList("dragon")).build();

        StatContainer statContainer = StatContainer.StatContainerBuilder.aStatContainer()
                .withMappings(Arrays.asList(mapping))
                .withTypes(Arrays.asList(typeDragon))
                .build();


        ObjectMapper mapper = new ObjectMapper();
        String statActualJson = mapper.writeValueAsString(statContainer);
        System.out.println(statActualJson);
        String statExpectedJson = StatUtil.readJsonToString("src/test/resources/stats_fields_test.json");

        JSONAssert.assertEquals(statExpectedJson, statActualJson, false);

        StatContainer resultObj = new ObjectMapper().readValue(statExpectedJson, StatContainer.class);
        Assert.assertNotNull(resultObj);

    }
}