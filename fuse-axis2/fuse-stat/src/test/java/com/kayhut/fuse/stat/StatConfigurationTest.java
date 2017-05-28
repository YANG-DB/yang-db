package com.kayhut.fuse.stat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.bucket.BucketTerm;
import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.histogram.*;
import com.kayhut.fuse.stat.util.StatUtil;
import com.kayhut.fuse.stat.model.configuration.*;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by benishue on 30-Apr-17.
 */
public class StatConfigurationTest {
    @Test
    public void statModelTest() throws Exception {
        StatContainer statContainer = buildStatContainer();

        ObjectMapper mapper = new ObjectMapper();
        String statActualJson = mapper.writeValueAsString(statContainer);
        System.out.println(statActualJson);
        String statExpectedJson = StatUtil.readJsonToString("src/test/resources/stats_fields_test.json");

        JSONAssert.assertEquals(statExpectedJson, statActualJson, false);

        StatContainer resultObj = new ObjectMapper().readValue(statExpectedJson, StatContainer.class);
        Assert.assertNotNull(resultObj);

    }

    private StatContainer buildStatContainer() {
        HistogramNumeric histogramDragonAge = HistogramNumeric.Builder.aHistogramNumeric()
                .withMin(10).withMax(100).withNumOfBins(10).build();

        HistogramString histogramDragonName = HistogramString.Builder.aHistogramString()
                .withPrefixSize(3)
                .withInterval(10).withNumOfChars(26).withFirstCharCode("97").build();

        HistogramManual histogramDragonAddress = HistogramManual.Builder.aHistogramManual()
                .withBuckets(Arrays.asList(
                        new BucketRange("abc", "dzz"),
                        new BucketRange("efg", "hij"),
                        new BucketRange("klm", "xyz")
                )).withDataType(DataType.string)
                .build();

        HistogramComposite histogramDragonColor = HistogramComposite.Builder.aHistogramComposite()
                .withManualBuckets(Arrays.asList(
                        new BucketRange("00", "11"),
                        new BucketRange("22", "33"),
                        new BucketRange("44", "55")
                )).withDataType(DataType.string)
                .withAutoBuckets(HistogramString.Builder.aHistogramString()
                        .withFirstCharCode("97")
                        .withInterval(10)
                        .withNumOfChars(26)
                        .withPrefixSize(3).build())
                .build();

        HistogramTerm histogramTerm = HistogramTerm.Builder.aHistogramTerm()
                .withDataType(DataType.string).withBuckets(Arrays.asList(
                        new BucketTerm("male"),
                        new BucketTerm("female")
                )).build();

        HistogramTerm histogramDocType = HistogramTerm.Builder.aHistogramTerm()
                .withDataType(DataType.string).withBuckets(Collections.singletonList(
                        new BucketTerm("dragon")
                )).build();


        Field nameField = new Field("name", histogramDragonName);
        Field ageField = new Field("age", histogramDragonAge);
        Field addressField = new Field("address", histogramDragonAddress);
        Field colorField = new Field("color", histogramDragonColor);
        Field genderField = new Field("gender", histogramTerm);
        Field dragonTypeField = new Field("_type", histogramDocType);


        Type typeDragon = new Type("dragon", Arrays.asList(ageField, nameField, addressField, colorField, genderField, dragonTypeField));

        Mapping mapping = Mapping.MappingBuilder.aMapping().withIndices(Arrays.asList("index1", "index2"))
                .withTypes(Collections.singletonList("dragon")).build();

        return StatContainer.Builder.aStatContainer()
                .withMappings(Collections.singletonList(mapping))
                .withTypes(Collections.singletonList(typeDragon))
                .build();
    }
}