package com.kayhut.fuse.stat;

import com.kayhut.fuse.stat.Util.EsUtil;
import com.kayhut.fuse.stat.Util.StatUtil;
import com.kayhut.fuse.stat.es.client.ClientProvider;
import com.kayhut.fuse.stat.es.populator.ElasticDataPopulator;
import com.kayhut.fuse.stat.model.configuration.*;
import com.kayhut.fuse.stat.model.configuration.histogram.HistogramComposite;
import com.kayhut.fuse.stat.model.configuration.histogram.HistogramManual;
import com.kayhut.fuse.stat.model.configuration.histogram.HistogramNumeric;
import com.kayhut.fuse.stat.model.configuration.histogram.HistogramString;
import com.kayhut.fuse.stat.model.result.StringStatResult;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Created by benishue on 27-Apr-17.
 */
public class StatCalculator {

    public static void main(String[] args)  {

        Logger logger = org.slf4j.LoggerFactory.getLogger(StatCalculator.class);

        validateNumberOfArguments(args, logger);

        TransportClient dataClient = null;
        TransportClient statClient = null;
        try {
            Configuration configuration = new StatConfiguration(args[0]).getInstance();
            dataClient = ClientProvider.getDataClient(configuration);
            statClient = ClientProvider.getStatClient(configuration);

            statIndexName = configuration.getString("statistics.index.name");
            statTypeNumericName = configuration.getString("statistics.type.numeric.name");
            statTypeStringName = configuration.getString("statistics.type.string.name");

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
                                buildHistogramForNumericFields(logger, dataClient, statClient, statContainer, indexName, typeName);
                                buildHistogramForManualFields(logger, dataClient, statClient , statContainer, indexName, typeName);
                                buildHistogramForStringFields(logger, dataClient, statClient, statContainer, indexName, typeName);
                                buildHistogramForCompositeFields(logger, dataClient, statClient, statContainer, indexName, typeName);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            dataClient.close();
            statClient.close();
        }
    }

    //region Private Methods
    private static void buildHistogramForNumericFields(Logger logger, TransportClient dataClient, TransportClient statClient, StatContainer statContainer, String indexName, String typeName) {
        try {
            Optional<List<Field>> fieldsWithNumericHistogram = StatUtil.getFieldsWithNumericHistogramOfType(statContainer, typeName);
            if (fieldsWithNumericHistogram.isPresent()){
                for(Field field : fieldsWithNumericHistogram.get())
                {
                    String fieldName = field.getField();
                    HistogramNumeric histogramNumeric = ((HistogramNumeric)field.getHistogram());
                    double min = histogramNumeric.getMin();
                    double max = histogramNumeric.getMax();
                    long numOfBins = histogramNumeric.getNumOfBins();
                    List<StringStatResult> buckets = EsUtil.getNumericHistogramResults(dataClient, indexName, typeName, fieldName, min, max, numOfBins);
                    PopulateBuckets(statIndexName, statTypeNumericName, statClient, buckets);
                }
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private static void buildHistogramForManualFields(Logger logger, TransportClient dataClient, TransportClient statClient, StatContainer statContainer, String indexName, String typeName) {
        try {
            Optional<List<Field>> fieldsWithManualHistogram = StatUtil.getFieldsWithManualHistogramOfType(statContainer, typeName);
            if (fieldsWithManualHistogram.isPresent()){

                for(Field field : fieldsWithManualHistogram.get())
                {
                    String fieldName = field.getField();
                    HistogramManual histogramManual = ((HistogramManual)field.getHistogram());
                    String dataType = histogramManual.getDataType();
                    List<StringStatResult> buckets = EsUtil.getManualHistogramResults(dataClient,indexName,typeName,fieldName,dataType,histogramManual.getBuckets());
                    if ("string".equals(dataType)) {
                        PopulateBuckets(statIndexName, statTypeStringName, statClient, buckets);
                    }
                    if ("numeric".equals(dataType)) {
                        PopulateBuckets(statIndexName, statTypeNumericName, statClient, buckets);
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private static void buildHistogramForStringFields(Logger logger, TransportClient dataClient, TransportClient statClient, StatContainer statContainer, String indexName, String typeName) {
        try {
            Optional<List<Field>> fieldsWithStringHistogram = StatUtil.getFieldsWithStringHistogramOfType(statContainer, typeName);

            if(fieldsWithStringHistogram.isPresent()) {
                for(Field field : fieldsWithStringHistogram.get()) {
                    String fieldName = field.getField();
                    HistogramString histogram = (HistogramString) field.getHistogram();
                    List<Bucket> stringBuckets = StatUtil.calculateAlphabeticBuckets(
                            Integer.valueOf(histogram.getFirstCharCode()),
                            histogram.getNumOfChars(),
                            histogram.getPrefixSize(),
                            histogram.getInterval());

                    List<StringStatResult> buckets = EsUtil.getStringHistogramResults(dataClient, indexName, typeName,fieldName, stringBuckets);
                    PopulateBuckets(statIndexName, statTypeStringName, statClient, buckets);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private static void buildHistogramForCompositeFields(Logger logger, TransportClient dataClient, TransportClient statClient, StatContainer statContainer, String indexName, String typeName)  {
        try {
            Optional<List<Field>> fieldsWithCompositeHistogram = StatUtil.getFieldsWithCompositeHistogramOfType(statContainer, typeName);
            if (fieldsWithCompositeHistogram.isPresent()){

                for(Field field : fieldsWithCompositeHistogram.get())
                {
                    String fieldName = field.getField();
                    HistogramComposite histogramComposite = ((HistogramComposite)field.getHistogram());
                    String dataType = histogramComposite.getDataType();
                    List<StringStatResult> manualBuckets = EsUtil.getManualHistogramResults(dataClient, indexName, typeName, fieldName, dataType, histogramComposite.getManualBuckets());

                    if ("string".equals(dataType)) {
                        HistogramString subHistogram = (HistogramString) histogramComposite.getAutoBuckets();
                        List<Bucket> stringBuckets = StatUtil.calculateAlphabeticBuckets(
                                Integer.valueOf(subHistogram.getFirstCharCode()),
                                subHistogram.getNumOfChars(),
                                subHistogram.getPrefixSize(),
                                subHistogram.getInterval());
                        List<StringStatResult> autoBuckets = EsUtil.getStringHistogramResults(dataClient, indexName, typeName,fieldName, stringBuckets);

                        List<StringStatResult> combinedBuckets = Stream.concat(autoBuckets.stream(), manualBuckets.stream()).collect(Collectors.toList());
                        PopulateBuckets(statIndexName, statTypeStringName, statClient, combinedBuckets);
                    }
                    if ("numeric".equals(dataType)) {
                        HistogramNumeric subHistogram = (HistogramNumeric) histogramComposite.getAutoBuckets();
                        double min = subHistogram.getMin();
                        double max = subHistogram.getMax();
                        long numOfBins = subHistogram.getNumOfBins();
                        List<StringStatResult> autoBuckets = EsUtil.getNumericHistogramResults(dataClient, indexName, typeName, fieldName, min, max, numOfBins);

                        List<StringStatResult> combinedBuckets = Stream.concat(autoBuckets.stream(), manualBuckets.stream()).collect(Collectors.toList());
                        PopulateBuckets(statIndexName, statTypeNumericName, statClient, combinedBuckets);
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private static void PopulateBuckets(String statIndexName, String statTypeName, TransportClient statClient, List<StringStatResult> buckets) throws IOException {

        new ElasticDataPopulator(
                statClient,
                statIndexName,
                statTypeName,
                "id",
                () -> StatUtil.prepareStatDocs(buckets)
        ).populate();
    }

    private static void validateNumberOfArguments(String[] args, Logger logger) {
        if (args.length != NUM_OF_ARGUMENTS) {
            logger.error("Expected " + NUM_OF_ARGUMENTS + " argument(s): ");
            logger.error("\n\t<path to field configuration file>");
            System.exit(1);
        }
    }
    //endregion

    //region static fields
    private final static int NUM_OF_ARGUMENTS = 1;
    private static String statIndexName;
    private static String statTypeNumericName;
    private static String statTypeStringName;
    //endregion
}

