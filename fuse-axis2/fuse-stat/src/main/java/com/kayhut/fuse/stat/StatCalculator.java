package com.kayhut.fuse.stat;

import com.google.common.base.Stopwatch;
import com.kayhut.fuse.stat.configuration.StatConfiguration;
import com.kayhut.fuse.stat.es.client.ClientProvider;
import com.kayhut.fuse.stat.es.populator.ElasticDataPopulator;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.configuration.*;
import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.histogram.*;
import com.kayhut.fuse.stat.model.result.StatGlobalCardinalityResult;
import com.kayhut.fuse.stat.model.result.StatRangeResult;
import com.kayhut.fuse.stat.model.result.StatResultBase;
import com.kayhut.fuse.stat.model.result.StatTermResult;
import com.kayhut.fuse.stat.util.EsUtil;
import com.kayhut.fuse.stat.util.StatUtil;
import javaslang.collection.Stream;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


/**
 * Created by benishue on 27-Apr-17.
 */
public class StatCalculator {

    private static final Logger logger = LoggerFactory.getLogger(StatCalculator.class);

    public static void main(String[] args) {

        if (!isValidNumberOfArguments(args)) {
            throw new IllegalArgumentException("Invalid/Missing Arguments");
        }

        TransportClient dataClient = null;
        TransportClient statClient = null;

        try {
            Configuration configuration = new StatConfiguration(args[0]).getInstance();
            logger.info("Loading configuration file at : '{}'", ((PropertiesConfiguration) configuration).getPath());
            dataClient = ClientProvider.getDataClient(configuration);
            statClient = ClientProvider.getStatClient(configuration);
            loadDefaultStatParameters(configuration);

            Optional<StatContainer> statConfiguration = StatUtil.getStatConfigurationObject(configuration);
            if (statConfiguration.isPresent()) {
                buildStatisticsBasedOnConfiguration(dataClient, statClient, statConfiguration.get());
            } else {
                throw new IllegalArgumentException("Statistics Configuration is Invalid / Empty");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (dataClient != null) {
                dataClient.close();
            }
            if (statClient != null) {
                statClient.close();
            }
        }
    }

    //region Public Methods
    public static void buildStatisticsBasedOnConfiguration(
            TransportClient dataClient,
            TransportClient statClient,
            StatContainer statContainer) {
        for (Mapping mapping : statContainer.getMappings()) {
            for (String index : mapping.getIndices()) {
                for (String type : mapping.getTypes()) {
                    Optional<Type> typeConfiguration = StatUtil.getTypeConfiguration(statContainer, type);
                    if (typeConfiguration.isPresent()) {
                        Stopwatch stopwatch = Stopwatch.createStarted();
                        logger.info("Starting to calculate statistics for Index: {}, Type: {}", index, type);
                        buildHistogramForNumericFields(dataClient, statClient, statContainer, index, type);
                        buildHistogramForManualFields(dataClient, statClient, statContainer, index, type);
                        buildHistogramForStringFields(dataClient, statClient, statContainer, index, type);
                        buildHistogramForCompositeFields(dataClient, statClient, statContainer, index, type);
                        buildHistogramForTermFields(dataClient, statClient, statContainer, index, type);
                        buildHistogramForDynamicFields(dataClient, statClient, statContainer, index, type);
                        stopwatch.stop();
                        logger.info("Finished to calculate statistics for Index: {}, Type: {}, took {} Seconds",
                                index,
                                type,
                                stopwatch.elapsed(TimeUnit.SECONDS));

                    }
                }
            }
        }
    }

    public static void loadDefaultStatParameters(String statIndex,
                                                 String statTypeNumeric,
                                                 String statTypeString,
                                                 String statTypeTerm,
                                                 String statTypeGlobal) {

        statIndexName = statIndex == null ? "stat" : statIndex;
        statTypeNumericName = statTypeNumeric == null ? "bucketNumeric" : statTypeNumeric;
        statTypeStringName = statTypeString == null ? "bucketString" : statTypeString;
        statTypeTermName = statTypeTerm == null ? "bucketTerm" : statTypeTerm;
        statTypeGlobalName = statTypeGlobal == null ? "bucketGlobal" : statTypeGlobal;
    }
    //endregion

    //region Private Methods
    private static void buildHistogramForNumericFields(
            TransportClient dataClient,
            TransportClient statClient,
            StatContainer statContainer,
            String index,
            String type) {
        try {
            Optional<List<Field>> fields = StatUtil.getFieldsWithNumericHistogram(
                    statContainer,
                    type);
            if (fields.isPresent() && !fields.get().isEmpty()) {
                for (Field field : fields.get()) {
                    HistogramNumeric hist = ((HistogramNumeric) field.getHistogram());
                    List<BucketRange<? extends Number>> numericBuckets = new ArrayList<>();

                    if (hist.getDataType() == DataType.numericDouble) {
                        numericBuckets.addAll(
                                StatUtil.createDoubleBuckets(
                                        hist.getMin().doubleValue(),
                                        hist.getMax().doubleValue(),
                                        hist.getNumOfBins()));
                    } else if (hist.getDataType() == DataType.numericLong) {
                        numericBuckets.addAll(
                                StatUtil.createLongBuckets(
                                        hist.getMin().longValue(),
                                        hist.getMax().longValue(),
                                        hist.getNumOfBins()));
                    }

                    List<StatRangeResult<? extends Number>> bucketsResults = EsUtil.getNumericHistogramResults(
                            dataClient,
                            index,
                            type,
                            field.getField(),
                            hist.getDataType(),
                            numericBuckets);

                    populateBuckets(statIndexName, statTypeNumericName, statClient, bucketsResults);
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred while trying to calculate statistics for Index: {}, Type: {}", index, type);
            logger.error(e.getMessage(), e);
        }
    }

    private static void buildHistogramForManualFields(
            TransportClient dataClient,
            TransportClient statClient,
            StatContainer statContainer,
            String index,
            String type) {
        try {
            Optional<List<Field>> fields = StatUtil.getFieldsWithManualHistogram(
                    statContainer,
                    type);
            if (fields.isPresent() && !fields.get().isEmpty()) {

                for (Field field : fields.get()) {
                    Histogram hist = field.getHistogram();
                    if (field.getFilter() != null && !field.getFilter().isEmpty()) { //Use for global cardinality
                        Filter direction = field.getFilter().stream()
                                .filter(filter -> "direction".equals(filter.getName()))
                                .findFirst().get();
                        List<StatGlobalCardinalityResult> bucketsResults = EsUtil.getGlobalCardinalityHistogramResults(
                                dataClient,
                                index,
                                type,
                                field.getField(),
                                direction.getValue(),
                                ((HistogramManual<?>)hist).getBuckets());
                        populateBuckets(statIndexName, statTypeGlobalName, statClient, bucketsResults);
                    } else if (hist.getDataType() == DataType.numericDouble || hist.getDataType() == DataType.numericLong) {
                        List<StatRangeResult<Number>> bucketsResults = EsUtil.getManualHistogramResults(
                                dataClient,
                                index,
                                type,
                                field.getField(),
                                field.getHistogram().getDataType(),
                                ((HistogramManual<Number>)hist).getBuckets());
                        populateBuckets(statIndexName, statTypeNumericName, statClient, bucketsResults);
                    } else if (hist.getDataType() == DataType.string) {
                        List<StatRangeResult<Number>> bucketsResults = EsUtil.getManualHistogramResults(
                                dataClient,
                                index,
                                type,
                                field.getField(),
                                field.getHistogram().getDataType(),
                                ((HistogramManual<Number>)hist).getBuckets());
                        populateBuckets(statIndexName, statTypeStringName, statClient, bucketsResults);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred while trying to calculate statistics for Index: {}, Type: {}", index, type);
            logger.error(e.getMessage(), e);
        }
    }

    private static void buildHistogramForStringFields(
            TransportClient dataClient,
            TransportClient statClient,
            StatContainer statContainer,
            String index,
            String type) {
        try {
            Optional<List<Field>> fields = StatUtil.getFieldsWithStringHistogram(
                    statContainer,
                    type);

            if (fields.isPresent() && !fields.get().isEmpty()) {
                for (Field field : fields.get()) {
                    HistogramString hist = (HistogramString) field.getHistogram();
                    List<BucketRange<String>> stringBuckets = StatUtil.calculateAlphabeticBuckets(
                            Integer.parseInt(hist.getFirstCharCode()),
                            hist.getNumOfChars(),
                            hist.getPrefixSize(),
                            hist.getInterval());

                    List<StatRangeResult> bucketsResults = EsUtil.getStringBucketsStatResults(
                            dataClient,
                            index,
                            type,
                            field.getField(),
                            stringBuckets);
                    populateBuckets(statIndexName, statTypeStringName, statClient, bucketsResults);
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred while trying to calculate statistics for Index: {}, Type: {}", index, type);
            logger.error(e.getMessage(), e);
        }
    }

    private static void buildHistogramForCompositeFields(
            TransportClient dataClient,
            TransportClient statClient,
            StatContainer statContainer,
            String index,
            String type) {
        try {
            Optional<List<Field>> fields = StatUtil.getFieldsWithCompositeHistogram(
                    statContainer,
                    type);
            if (fields.isPresent() && !fields.get().isEmpty()) {

                for (Field field : fields.get()) {
                    HistogramComposite hist = ((HistogramComposite) field.getHistogram());
                    List<StatRangeResult> manualBuckets = EsUtil.getManualHistogramResults(
                            dataClient,
                            index,
                            type,
                            field.getField(),
                            hist.getDataType(),
                            hist.getManualBuckets());

                    if (hist.getDataType() == DataType.string) {
                        HistogramString subHist = (HistogramString) hist.getAutoBuckets();
                        List<BucketRange<String>> stringBuckets = StatUtil.calculateAlphabeticBuckets(
                                Integer.parseInt(subHist.getFirstCharCode()),
                                subHist.getNumOfChars(),
                                subHist.getPrefixSize(),
                                subHist.getInterval());
                        List<StatRangeResult> autoBuckets = EsUtil.getStringBucketsStatResults(
                                dataClient,
                                index,
                                type,
                                field.getField(),
                                stringBuckets);

                        List<StatRangeResult> combinedBuckets = Stream.ofAll(manualBuckets).appendAll(autoBuckets).toJavaList();
                        populateBuckets(statIndexName, statTypeStringName, statClient, combinedBuckets);
                    }

                    if (hist.getDataType() == DataType.numericDouble || hist.getDataType() == DataType.numericLong) {
                        HistogramNumeric subHist = (HistogramNumeric) hist.getAutoBuckets();
                        List<BucketRange<? extends Number>> numericBuckets = hist.getDataType() == DataType.numericDouble ?
                                new ArrayList<>(StatUtil.createDoubleBuckets(
                                    subHist.getMin().doubleValue(),
                                    subHist.getMax().doubleValue(),
                                    subHist.getNumOfBins())) :
                                hist.getDataType() == DataType.numericLong ?
                                        new ArrayList<>(StatUtil.createLongBuckets(
                                                subHist.getMin().longValue(),
                                                subHist.getMax().longValue(),
                                                subHist.getNumOfBins())) : Collections.emptyList();

                        List<StatRangeResult<? extends Number>> autoBuckets = EsUtil.getNumericHistogramResults(
                                dataClient,
                                index,
                                type,
                                field.getField(),
                                subHist.getDataType(),
                                numericBuckets);

                        List<StatRangeResult> combinedBuckets = Stream.ofAll(manualBuckets).appendAll(autoBuckets).toJavaList();
                        populateBuckets(statIndexName, statTypeNumericName, statClient, combinedBuckets);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred while trying to calculate statistics for Index: {}, Type: {}", index, type);
            logger.error(e.getMessage(), e);
        }
    }

    private static void buildHistogramForTermFields(
            TransportClient dataClient,
            TransportClient statClient,
            StatContainer statContainer,
            String index,
            String type) {
        try {
            Optional<List<Field>> fields = StatUtil.getFieldsWithTermHistogram(
                    statContainer,
                    type);

            if (fields.isPresent() && !fields.get().isEmpty()) {
                for (Field field : fields.get()) {
                    HistogramTerm hist = (HistogramTerm) field.getHistogram();
                    List<StatTermResult> bucketsResults = EsUtil.getTermHistogramResults(
                            dataClient,
                            index,
                            type,
                            field.getField(),
                            hist.getDataType(),
                            hist.getBuckets());
                    populateBuckets(statIndexName, statTypeTermName, statClient, bucketsResults);
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred while trying to calculate statistics for Index: {}, Type: {}", index, type);
            logger.error(e.getMessage(), e);
        }
    }

    private static void buildHistogramForDynamicFields(
            TransportClient dataClient,
            TransportClient statClient,
            StatContainer statContainer,
            String index,
            String type) {
        try {
            Optional<List<Field>> fields = StatUtil.getFieldsWithDynamicHistogram(
                    statContainer,
                    type);

            if (fields.isPresent() && !fields.get().isEmpty()) {
                for (Field field : fields.get()) {
                    HistogramDynamic hist = (HistogramDynamic) field.getHistogram();
                    // !! Currently we support only dynamic histogram of numeric !!
                    List<StatRangeResult<Number>> bucketsResults = EsUtil.getDynamicHistogramResults(
                            dataClient,
                            index,
                            type,
                            field.getField(),
                            hist.getDataType(),
                            hist.getNumOfBins());
                    populateBuckets(statIndexName, statTypeNumericName, statClient, bucketsResults);
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred while trying to calculate statistics for Index: {}, Type: {}", index, type);
            logger.error(e.getMessage(), e);
        }
    }

    private static void populateBuckets(String statIndex,
                                        String statType,
                                        TransportClient statClient,
                                        List<? extends StatResultBase> buckets) throws Exception {

        new ElasticDataPopulator(
                statClient,
                statIndex,
                statType,
                "id",
                () -> StatUtil.prepareStatDocs(buckets)
        ).populate();
    }

    private static void loadDefaultStatParameters(Configuration configuration) {
        loadDefaultStatParameters(configuration.getString("statistics.index.name"),
                configuration.getString("statistics.type.numeric.name"),
                configuration.getString("statistics.type.string.name"),
                configuration.getString("statistics.type.term.name"),
                configuration.getString("statistics.type.global.name")
        );
    }


    private static boolean isValidNumberOfArguments(String[] args) {
        boolean result = true;
        if (args.length != NUM_OF_ARGUMENTS) {
            logger.error("Expected {} argument(s): <path to field configuration file>", NUM_OF_ARGUMENTS);
            result = false;
        }
        return result;
    }
    //endregion

    //region static fields
    private final static int NUM_OF_ARGUMENTS = 1;
    private static String statIndexName;
    private static String statTypeNumericName;
    private static String statTypeStringName;
    private static String statTypeTermName;
    private static String statTypeGlobalName;
    //endregion
}

