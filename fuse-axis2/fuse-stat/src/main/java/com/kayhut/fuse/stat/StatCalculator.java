package com.kayhut.fuse.stat;

import com.kayhut.fuse.stat.configuration.StatConfiguration;
import com.kayhut.fuse.stat.es.client.ClientProvider;
import com.kayhut.fuse.stat.es.populator.ElasticDataPopulator;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.configuration.*;
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
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
            logger.error(e.getMessage());
            e.printStackTrace();
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
                        buildHistogramForNumericFields(dataClient, statClient, statContainer, index, type);
                        buildHistogramForManualFields(dataClient, statClient, statContainer, index, type);
                        buildHistogramForStringFields(dataClient, statClient, statContainer, index, type);
                        buildHistogramForCompositeFields(dataClient, statClient, statContainer, index, type);
                        buildHistogramForTermFields(dataClient, statClient, statContainer, index, type);
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
        if (statIndex == null ||
                statTypeNumeric == null ||
                statTypeString == null ||
                statTypeTerm == null ||
                statTypeGlobal == null) {

            throw new IllegalArgumentException("Missing Arguments for the Statistics type names - " +
                    "\nStat Index: " + statIndex +
                    "\nStat Type Numeric Name: " + statTypeNumeric +
                    "\nStat Type String Name: " + statTypeString +
                    "\nStat Type Term Name: " + statTypeTerm +
                    "\nStat Type Global Name: " + statTypeGlobal
            );
        }
        statIndexName = statIndex;
        statTypeNumericName = statTypeNumeric;
        statTypeStringName = statTypeString;
        statTypeTermName = statTypeTerm;
        statTypeGlobalName = statTypeGlobal;
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
                    List<StatRangeResult> buckets = EsUtil.getNumericHistogramResults(dataClient,
                            index,
                            type,
                            field.getField(),
                            hist.getMin(),
                            hist.getMax(),
                            (long) hist.getNumOfBins());
                    populateBuckets(statIndexName, statTypeNumericName, statClient, buckets);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
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
                    HistogramManual hist = ((HistogramManual) field.getHistogram());
                    if (field.getFilter() != null && !field.getFilter().isEmpty()) { //Use for global cardinality
                        Filter direction = field.getFilter().stream()
                                .filter(filter -> filter.getName().equals("direction"))
                                .findFirst().get();
                        List<StatRangeResult> buckets = EsUtil.getGlobalCardinalityHistogramResults(
                                dataClient,
                                index,
                                type,
                                field.getField(),
                                direction.getValue(), hist.getBuckets());
                        populateBuckets(statIndexName, statTypeGlobalName, statClient, buckets);
                    } else {
                        List<StatRangeResult> buckets = EsUtil.getManualHistogramResults(dataClient,
                                index,
                                type,
                                field.getField(),
                                hist.getDataType(),
                                hist.getBuckets());
                        if (hist.getDataType() == DataType.string) {
                            populateBuckets(statIndexName, statTypeStringName, statClient, buckets);
                        }
                        if (hist.getDataType() == DataType.numeric) {
                            populateBuckets(statIndexName, statTypeNumericName, statClient, buckets);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
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

                    List<StatRangeResult> buckets = EsUtil.getStringBucketsStatResults(
                            dataClient,
                            index,
                            type,
                            field.getField(),
                            stringBuckets);
                    populateBuckets(statIndexName, statTypeStringName, statClient, buckets);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
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

                        List<StatRangeResult> combinedBuckets = Stream
                                .concat(autoBuckets.stream(), manualBuckets.stream())
                                .collect(Collectors.toList());
                        populateBuckets(statIndexName, statTypeStringName, statClient, combinedBuckets);
                    }
                    if (hist.getDataType() == DataType.numeric) {
                        HistogramNumeric subHist = (HistogramNumeric) hist.getAutoBuckets();
                        List<StatRangeResult> autoBuckets = EsUtil.getNumericHistogramResults(
                                dataClient,
                                index,
                                type,
                                field.getField(),
                                subHist.getMin(),
                                subHist.getMax(),
                                (long) subHist.getNumOfBins());

                        List<StatRangeResult> combinedBuckets = Stream.
                                concat(autoBuckets.stream(), manualBuckets.stream())
                                .collect(Collectors.toList());
                        populateBuckets(statIndexName, statTypeNumericName, statClient, combinedBuckets);
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
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
                    List<StatTermResult> buckets = EsUtil.getTermHistogramResults(
                            dataClient,
                            index,
                            type,
                            field.getField(),
                            hist.getDataType(),
                            hist.getBuckets());
                    populateBuckets(statIndexName, statTypeTermName, statClient, buckets);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void populateBuckets(String statIndex,
                                        String statType,
                                        TransportClient statClient,
                                        List<? extends StatResultBase> buckets) throws IOException {

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
            logger.error("Expected %d argument(s): ", NUM_OF_ARGUMENTS);
            logger.error("\n\t<path to field configuration file>");
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

