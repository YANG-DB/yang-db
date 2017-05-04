package com.kayhut.fuse.stat;

import com.kayhut.fuse.stat.Util.EsUtil;
import com.kayhut.fuse.stat.Util.StatUtil;
import com.kayhut.fuse.stat.es.client.ClientProvider;
import com.kayhut.fuse.stat.es.populator.ElasticDataPopulator;
import com.kayhut.fuse.stat.model.configuration.*;
import com.kayhut.fuse.stat.model.result.BucketStatResult;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;


/**
 * Created by benishue on 27-Apr-17.
 */
public class StatCalculator {

    public static void main(String[] args) throws Exception {

        Logger logger = org.slf4j.LoggerFactory.getLogger(StatCalculator.class);

        validateNumberOfArguments(args, logger);

        Configuration configuration = new StatConfiguration(args[0]).getInstance();

        TransportClient dataClient = ClientProvider.getDataClient(configuration);

        Optional<StatContainer> statConfiguration = StatUtil.getStatConfigurationObject(configuration);

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
                            buildHistogramForStringFields(configuration, logger, dataClient, statContainer, indexName, typeName);
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
                    long max = Long.parseLong(histogramNumeric.getMax());
                    long numOfBins = Long.parseLong(histogramNumeric.getNumOfBins());
                    List<BucketStatResult> buckets = EsUtil.getNumericHistogramResults(dataClient, indexName, typeName, fieldName, min, max, numOfBins);
                    PopulateBuckets(configuration, buckets);
                }
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
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

    private static void buildHistogramForStringFields(Configuration configuration, Logger logger,  TransportClient esClient, StatContainer statContainer, String indexName, String typeName) {
        try {
            Optional<List<Field>> fieldsWithStringHistogram = StatUtil.getFieldsWithStringHistogramOfType(statContainer, typeName);

            if(fieldsWithStringHistogram.isPresent()) {
                for(Field field : fieldsWithStringHistogram.get()) {
                    String fieldName = field.getField();
                    HistogramString histogram = (HistogramString) field.getHistogram();
                    List<Bucket> stringBuckets = StatUtil.calculateAlphabeticBuckets(
                            Integer.valueOf(histogram.getFirstCharCode()),
                            Integer.valueOf(histogram.getNumOfChars()),
                            Integer.valueOf(histogram.getPrefixSize()),
                            Integer.valueOf(histogram.getInterval()));

                    List<BucketStatResult> buckets = EsUtil.getStringHistogramResults(esClient, indexName, typeName,fieldName, stringBuckets);
                    PopulateBuckets(configuration, buckets);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
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
                () -> StatUtil.prepareStatDocs(finalNumericBuckets)
        ).populate();
    }

    private static void validateNumberOfArguments(String[] args, Logger logger) {
        if (args.length != NUM_OF_ARGUMENTS) {

            logger.error("Expected " + NUM_OF_ARGUMENTS + " argument(s): ");

            //print usage message here
            logger.error("\n\t<path to field configuration file>");

            System.exit(1);

        }
    }

    //region static fields
    private final static int NUM_OF_ARGUMENTS = 1;
    //endregion
}

