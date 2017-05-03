package com.kayhut.fuse.stat;

import com.kayhut.fuse.stat.Util.EsUtil;
import com.kayhut.fuse.stat.Util.StatUtil;
import com.kayhut.fuse.stat.es.ClientProvider;
import com.kayhut.fuse.stat.es.populator.ElasticDataPopulator;
import com.kayhut.fuse.stat.model.configuration.*;
import com.kayhut.fuse.stat.model.result.BucketStatResult;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;


/**
 * Created by benishue on 27-Apr-17.
 */
public class StatCalculator {

    public static void main(String[] args) throws Exception {

        Logger logger = org.slf4j.LoggerFactory.getLogger(StatCalculator.class);

        Configuration configuration = new StatConfiguration("statistics.properties").getInstance();

        String statConfigurationFilePath = configuration.getString("statistics.configuration.file");

        TransportClient dataClient = ClientProvider.getDataClient(configuration);

        Optional<StatContainer> statConfiguration = StatUtil.getStatConfigurationObject(StatUtil.readJsonToString(statConfigurationFilePath));

        if (statConfiguration.isPresent()) {
            StatContainer statContainer = statConfiguration.get();

            for (Mapping mapping : statContainer.getMappings())
            {
                List<String> indices = mapping.getIndices();
                List<String> types = mapping.getTypes();
                for(String indexName : indices){
                    for (String typeName: types){
                        Optional<Type> typeConfiguration = StatUtil.getTypeConfiguration(statContainer, typeName);
                        if(typeConfiguration.isPresent()){
                            BuildHistogramForNumericFields(configuration, logger, dataClient, statContainer, indexName, typeName);
                            BuildHistogramForManualFields(configuration, logger, dataClient, statContainer, indexName, typeName);
                        }
                    }
                }
            }
        }


//        EsUtil.bulkIndexingFromFile(dataClient, "C:\\Code\\fuse\\fuse-stat\\src\\main\\resources\\dragons_mock.txt",
//                "index1","dragon");
//        EsUtil.showTypeFieldsNames(dataClient,"game","football");
    }

    private static void BuildHistogramForNumericFields(Configuration configuration, Logger logger, TransportClient dataClient, StatContainer statContainer, String indexName, String typeName) {
        try {
            Optional<List<Field>> fieldsWithNumericHistogram = StatUtil.getFieldsWithNumericHistogramOfType(statContainer, typeName);
            if (fieldsWithNumericHistogram.isPresent()){
                for(Field field : fieldsWithNumericHistogram.get())
                {
                    String fieldName = field.getField();
                    HistogramNumeric histogramNumeric = ((HistogramNumeric)field.getHistogram());
                    long min = Long.parseLong(histogramNumeric.getMin());
                    long max = Long.parseLong(histogramNumeric.getMin());
                    long interval = Long.parseLong(histogramNumeric.getInterval());
                    List<BucketStatResult> buckets = EsUtil.getNumericHistogramResults(dataClient, indexName, typeName, fieldName, min, max, interval);
                    PopulateBuckets(configuration, buckets);
                }
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void PopulateBuckets(Configuration configuration, List<BucketStatResult> buckets) throws IOException {
        TransportClient statClient = ClientProvider.getStatClient(configuration);
        String statIndexName = configuration.getString("statistics.index.name");
        String statTypeName = configuration.getString("statistics.type.name");


        List<BucketStatResult> finalNumericBuckets = buckets;
        new ElasticDataPopulator(
                statClient,
                statIndexName,
                statTypeName,
                "id",
                () -> StatUtil.createBuckets(finalNumericBuckets)
                ).populate();
    }

    private static void BuildHistogramForManualFields(Configuration configuration, Logger logger,  TransportClient esClient, StatContainer statContainer, String indexName, String typeName) {
        try {
            Optional<List<Field>> fieldsWithManualHistogram = StatUtil.getFieldsWithManualHistogramOfType(statContainer, typeName);
            if (fieldsWithManualHistogram.isPresent()){

                for(Field field : fieldsWithManualHistogram.get())
                {
                    String fieldName = field.getField();
                    HistogramManual histogramManual = ((HistogramManual)field.getHistogram());
                    List<BucketStatResult> buckets = EsUtil.getManualHistogramResults(esClient,indexName,typeName,fieldName,histogramManual.getDataType(),histogramManual.getBuckets());
                    PopulateBuckets(configuration, buckets);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<Bucket> buildHistogramForStringField(StatContainer statContainer, String typeName, String fieldName) {

        Optional<Field> field = StatUtil.getFieldByName(statContainer, typeName, fieldName);

        if(!field.isPresent() || field.get().getHistogram().getHistogramType() != HistogramType.string) {
            throw new RuntimeException("Buckets calculation for field " + fieldName + " in type " + typeName + " failed. ");
        }

        Field f  = field.get();
        HistogramString histogram = (HistogramString) f.getHistogram();
        List<Bucket> buckets = StatUtil.calculateAlphabeticBuckets(Integer.valueOf(histogram.getFirstCharCode()),
                Integer.valueOf(histogram.getNumOfChars()),
                Integer.valueOf(histogram.getPrefixSize()),
                Integer.valueOf(histogram.getInterval()));

        return buckets;
    }

}

