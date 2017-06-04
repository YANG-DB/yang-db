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
        HistogramNumeric histogramDragonAge = HistogramNumeric.Builder.get()
                .withMin(10).withMax(100).withNumOfBins(10).build();

        HistogramString histogramDragonName = HistogramString.Builder.get()
                .withPrefixSize(3)
                .withInterval(10).withNumOfChars(26).withFirstCharCode("97").build();

        HistogramManual histogramDragonAddress = HistogramManual.Builder.get()
                .withBuckets(Arrays.asList(
                        new BucketRange("abc", "dzz"),
                        new BucketRange("efg", "hij"),
                        new BucketRange("klm", "xyz")
                )).withDataType(DataType.string)
                .build();

        HistogramComposite histogramDragonColor = HistogramComposite.Builder.get()
                .withManualBuckets(Arrays.asList(
                        new BucketRange("00", "11"),
                        new BucketRange("22", "33"),
                        new BucketRange("44", "55")
                )).withDataType(DataType.string)
                .withAutoBuckets(HistogramString.Builder.get()
                        .withFirstCharCode("97")
                        .withInterval(10)
                        .withNumOfChars(26)
                        .withPrefixSize(3).build())
                .build();

        HistogramTerm histogramTerm = HistogramTerm.Builder.get()
                .withDataType(DataType.string).withBuckets(Arrays.asList(
                        new BucketTerm("male"),
                        new BucketTerm("female")
                )).build();

        HistogramTerm histogramDocType = HistogramTerm.Builder.get()
                .withDataType(DataType.string).withBuckets(Collections.singletonList(
                        new BucketTerm("dragon")
                )).build();


        HistogramManual histogramFireEntity = HistogramManual.Builder.get()
                .withBuckets(Arrays.asList(
                        new BucketRange("0", "~")
                )).withDataType(DataType.string)
                .build();


        Field nameField = new Field("name", histogramDragonName);
        Field ageField = new Field("age", histogramDragonAge);
        Field addressField = new Field("address", histogramDragonAddress);
        Field colorField = new Field("color", histogramDragonColor);
        Field genderField = new Field("gender", histogramTerm);
        Field dragonTypeField = new Field("_type", histogramDocType);

        Field fireEntityAOutField = new Field("entityA.id",
                histogramFireEntity,
                Arrays.asList(new Filter("direction", "OUT")));

        Field fireEntityAInField = new Field("entityA.id",
                histogramFireEntity,
                Arrays.asList(new Filter("direction", "IN")));

        Type typeDragon = new Type("dragon", Arrays.asList(ageField, nameField, addressField, colorField, genderField, dragonTypeField));
        Type typeFire = new Type("fire", Arrays.asList(fireEntityAInField, fireEntityAOutField));

        Mapping mappingDragon = Mapping.Builder.get().withIndices(Arrays.asList("index1", "index2"))
                .withTypes(Collections.singletonList("dragon")).build();

        Mapping mappingFire = Mapping.Builder.get().withIndices(Arrays.asList("index3", "index4"))
                .withTypes(Collections.singletonList("fire")).build();


        return StatContainer.Builder.get()
                .withMappings(Arrays.asList(mappingDragon, mappingFire))
                .withTypes(Arrays.asList(typeDragon, typeFire))
                .build();
    }
}