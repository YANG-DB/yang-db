package com.kayhut.fuse.stat;

import com.kayhut.fuse.stat.configuration.StatConfiguration;
import com.kayhut.fuse.stat.es.client.ClientProvider;
import com.kayhut.fuse.stat.es.populator.ElasticDataPopulator;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.configuration.Field;
import com.kayhut.fuse.stat.model.configuration.Mapping;
import com.kayhut.fuse.stat.model.configuration.StatContainer;
import com.kayhut.fuse.stat.model.configuration.Type;
import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.histogram.*;
import com.kayhut.fuse.stat.model.result.StatRangeResult;
import com.kayhut.fuse.stat.model.result.StatResultBase;
import com.kayhut.fuse.stat.model.result.StatTermResult;
import com.kayhut.fuse.stat.util.EsUtil;
import com.kayhut.fuse.stat.util.StatUtil;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Created by benishue on 27-Apr-17.
 */
public class StatCalculator {

    public static void main(String[] args) {

        Logger logger = org.slf4j.LoggerFactory.getLogger(StatCalculator.class);

        if (!isValidNumberOfArguments(args, logger))
            System.exit(1);

        TransportClient dataClient = null;
        TransportClient statClient = null;

        try {
            Configuration configuration = new StatConfiguration(args[0]).getInstance();
            dataClient = ClientProvider.getDataClient(configuration);
            statClient = ClientProvider.getStatClient(configuration);

            statIndexName = configuration.getString("statistics.index.name");
            statTypeNumericName = configuration.getString("statistics.type.numeric.name");
            statTypeStringName = configuration.getString("statistics.type.string.name");
            statTypeTermName = configuration.getString("statistics.type.term.name");

            Optional<StatContainer> statConfiguration = StatUtil.getStatConfigurationObject(configuration);

            if (statConfiguration.isPresent()) {
                StatContainer statContainer = statConfiguration.get();

                for (Mapping mapping : statContainer.getMappings()) {
                    List<String> indices = mapping.getIndices();
                    List<String> types = mapping.getTypes();
                    for (String indexName : indices) {
                        for (String typeName : types) {
                            Optional<Type> typeConfiguration = StatUtil.getTypeConfiguration(statContainer, typeName);
                            if (typeConfiguration.isPresent()) {
                                buildHistogramForNumericFields(logger, dataClient, statClient, statContainer, indexName, typeName);
                                buildHistogramForManualFields(logger, dataClient, statClient, statContainer, indexName, typeName);
                                buildHistogramForStringFields(logger, dataClient, statClient, statContainer, indexName, typeName);
                                buildHistogramForCompositeFields(logger, dataClient, statClient, statContainer, indexName, typeName);
                                buildHistogramForTermFields(logger, dataClient, statClient, statContainer, indexName, typeName);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (dataClient != null)
                dataClient.close();
            if (statClient != null)
                statClient.close();
        }
    }

    //region Private Methods
    private static void buildHistogramForNumericFields(Logger logger,
                                                       TransportClient dataClient,
                                                       TransportClient statClient,
                                                       StatContainer statContainer,
                                                       String indexName,
                                                       String typeName) {
        try {
            Optional<List<Field>> fieldsWithNumericHistogram = StatUtil.getFieldsWithNumericHistogramOfType(statContainer, typeName);
            if (fieldsWithNumericHistogram.isPresent()) {
                for (Field field : fieldsWithNumericHistogram.get()) {
                    String fieldName = field.getField();
                    HistogramNumeric histogramNumeric = ((HistogramNumeric) field.getHistogram());
                    double min = histogramNumeric.getMin();
                    double max = histogramNumeric.getMax();
                    long numOfBins = histogramNumeric.getNumOfBins();
                    List<StatRangeResult> buckets = EsUtil.getNumericHistogramResults(dataClient, indexName, typeName, fieldName, min, max, numOfBins);
                    populateBuckets(statIndexName, statTypeNumericName, statClient, buckets);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private static void buildHistogramForManualFields(Logger logger,
                                                      TransportClient dataClient,
                                                      TransportClient statClient,
                                                      StatContainer statContainer,
                                                      String indexName,
                                                      String typeName) {
        try {
            Optional<List<Field>> fieldsWithManualHistogram = StatUtil.getFieldsWithManualHistogramOfType(statContainer, typeName);
            if (fieldsWithManualHistogram.isPresent()) {

                for (Field field : fieldsWithManualHistogram.get()) {
                    String fieldName = field.getField();
                    HistogramManual histogramManual = ((HistogramManual) field.getHistogram());
                    DataType dataType = histogramManual.getDataType();
                    List<StatRangeResult> buckets = EsUtil.getManualHistogramResults(dataClient, indexName, typeName, fieldName, dataType, histogramManual.getBuckets());
                    if (dataType == DataType.string) {
                        populateBuckets(statIndexName, statTypeStringName, statClient, buckets);
                    }
                    if (dataType == DataType.numeric) {
                        populateBuckets(statIndexName, statTypeNumericName, statClient, buckets);
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private static void buildHistogramForStringFields(Logger logger,
                                                      TransportClient dataClient,
                                                      TransportClient statClient,
                                                      StatContainer statContainer,
                                                      String indexName,
                                                      String typeName) {
        try {
            Optional<List<Field>> fieldsWithStringHistogram = StatUtil.getFieldsWithStringHistogramOfType(statContainer, typeName);

            if (fieldsWithStringHistogram.isPresent()) {
                for (Field field : fieldsWithStringHistogram.get()) {
                    String fieldName = field.getField();
                    HistogramString histogram = (HistogramString) field.getHistogram();
                    List<BucketRange<String>> stringBuckets = StatUtil.calculateAlphabeticBuckets(
                            Integer.parseInt(histogram.getFirstCharCode()),
                            histogram.getNumOfChars(),
                            histogram.getPrefixSize(),
                            histogram.getInterval());

                    List<StatRangeResult> buckets = EsUtil.getStringBucketsStatResults(dataClient, indexName, typeName, fieldName, stringBuckets);
                    populateBuckets(statIndexName, statTypeStringName, statClient, buckets);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private static void buildHistogramForCompositeFields(Logger logger,
                                                         TransportClient dataClient,
                                                         TransportClient statClient,
                                                         StatContainer statContainer,
                                                         String indexName,
                                                         String typeName) {
        try {
            Optional<List<Field>> fieldsWithCompositeHistogram = StatUtil.getFieldsWithCompositeHistogramOfType(statContainer, typeName);
            if (fieldsWithCompositeHistogram.isPresent()) {

                for (Field field : fieldsWithCompositeHistogram.get()) {
                    String fieldName = field.getField();
                    HistogramComposite histogramComposite = ((HistogramComposite) field.getHistogram());
                    DataType dataType = histogramComposite.getDataType();
                    List<StatRangeResult> manualBuckets = EsUtil.getManualHistogramResults(dataClient, indexName, typeName, fieldName, dataType, histogramComposite.getManualBuckets());

                    if (dataType == DataType.string) {
                        HistogramString subHistogram = (HistogramString) histogramComposite.getAutoBuckets();
                        List<BucketRange<String>> stringBuckets = StatUtil.calculateAlphabeticBuckets(
                                Integer.parseInt(subHistogram.getFirstCharCode()),
                                subHistogram.getNumOfChars(),
                                subHistogram.getPrefixSize(),
                                subHistogram.getInterval());
                        List<StatRangeResult> autoBuckets = EsUtil.getStringBucketsStatResults(dataClient, indexName, typeName, fieldName, stringBuckets);

                        List<StatRangeResult> combinedBuckets = Stream.concat(autoBuckets.stream(), manualBuckets.stream()).collect(Collectors.toList());
                        populateBuckets(statIndexName, statTypeStringName, statClient, combinedBuckets);
                    }
                    if (dataType == DataType.numeric) {
                        HistogramNumeric subHistogram = (HistogramNumeric) histogramComposite.getAutoBuckets();
                        double min = subHistogram.getMin();
                        double max = subHistogram.getMax();
                        long numOfBins = subHistogram.getNumOfBins();
                        List<StatRangeResult> autoBuckets = EsUtil.getNumericHistogramResults(dataClient, indexName, typeName, fieldName, min, max, numOfBins);

                        List<StatRangeResult> combinedBuckets = Stream.concat(autoBuckets.stream(), manualBuckets.stream()).collect(Collectors.toList());
                        populateBuckets(statIndexName, statTypeNumericName, statClient, combinedBuckets);
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private static void buildHistogramForTermFields(Logger logger,
                                                    TransportClient dataClient,
                                                    TransportClient statClient,
                                                    StatContainer statContainer,
                                                    String indexName,
                                                    String typeName) {
        try {
            Optional<List<Field>> fieldsWithTermHistogram = StatUtil.getFieldsWithTermHistogramOfType(statContainer, typeName);

            if (fieldsWithTermHistogram.isPresent()) {
                for (Field field : fieldsWithTermHistogram.get()) {
                    String fieldName = field.getField();
                    HistogramTerm histogramTerm = (HistogramTerm) field.getHistogram();
                    DataType dataType = histogramTerm.getDataType();
                    List<StatTermResult> buckets = EsUtil.getTermHistogramResults(dataClient, indexName, typeName, fieldName, dataType, histogramTerm.getBuckets());
                    populateBuckets(statIndexName, statTypeTermName, statClient, buckets);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }


    private static void populateBuckets(String statIndexName,
                                        String statTypeName,
                                        TransportClient statClient,
                                        List<? extends StatResultBase> buckets) throws IOException {

        new ElasticDataPopulator(
                statClient,
                statIndexName,
                statTypeName,
                "id",
                () -> StatUtil.prepareStatDocs(buckets)
        ).populate();
    }


    private static boolean isValidNumberOfArguments(String[] args, Logger logger) {
        if (args.length != NUM_OF_ARGUMENTS) {
            logger.error("Expected %d argument(s): ", NUM_OF_ARGUMENTS);
            logger.error("\n\t<path to field configuration file>");
            return false;
        }
        return true;
    }
    //endregion

    //region static fields
    private final static int NUM_OF_ARGUMENTS = 1;
    private static String statIndexName;
    private static String statTypeNumericName;
    private static String statTypeStringName;
    private static String statTypeTermName;
    //endregion
}

