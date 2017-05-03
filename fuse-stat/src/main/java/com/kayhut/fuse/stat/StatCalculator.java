package com.kayhut.fuse.stat;

import com.kayhut.fuse.stat.Util.EsUtil;
import com.kayhut.fuse.stat.Util.StatUtil;
import com.kayhut.fuse.stat.es.ClientProvider;
import com.kayhut.fuse.stat.es.populator.ElasticDataPopulator;
import com.kayhut.fuse.stat.model.configuration.*;
import com.kayhut.fuse.stat.model.result.BucketStatResult;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.client.transport.TransportClient;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;


/**
 * Created by benishue on 27-Apr-17.
 */
public class StatCalculator {

    public static void main(String[] args) throws Exception {

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
                            BuildHistogramForNumericField(configuration, dataClient, statContainer, indexName, typeName);
                        }
                    }
                }
            }
        }


//        EsUtil.bulkIndexingFromFile(dataClient, "C:\\Code\\fuse\\fuse-stat\\src\\main\\resources\\dragons_mock.txt",
//                "index1","dragon");
//        EsUtil.showTypeFieldsNames(dataClient,"game","football");
    }

    private static void BuildHistogramForNumericField(Configuration configuration, TransportClient dataClient, StatContainer statContainer, String indexName, String typeName) {
        try {
            Optional<List<Field>> fieldsWithNumericHistogram = StatUtil.getFieldsWithNumericHistogramOfType(statContainer, typeName);
            if (fieldsWithNumericHistogram.isPresent()){
                List<BucketStatResult> numericBuckets = new ArrayList<>();
                for(Field field : fieldsWithNumericHistogram.get())
                {
                    String fieldName = field.getField();
                    HistogramNumeric histogramNumeric = ((HistogramNumeric)field.getHistogram());
                    long min = Long.parseLong(histogramNumeric.getMin());
                    long max = Long.parseLong(histogramNumeric.getMin());
                    long interval = Long.parseLong(histogramNumeric.getInterval());
                    numericBuckets = EsUtil.getNumericHistogram(dataClient, indexName, typeName, fieldName, min, max, interval);
                }

                PopulateBuckets(configuration, numericBuckets);

            }
        }
        catch (Exception e) {
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

    private static void BuildHistogramForStringField(TransportClient esClient, StatContainer statContainer, String indexName, String typeName) {
        Optional<List<Field>> fieldsWithNumericHistogram = StatUtil.getFieldsWithNumericHistogramOfType(statContainer, typeName);
        if (fieldsWithNumericHistogram.isPresent()){
            for(Field field : fieldsWithNumericHistogram.get())
            {
                String fieldName = field.getField();
                HistogramNumeric histogramNumeric = ((HistogramNumeric)field.getHistogram());
                long min = Long.parseLong(histogramNumeric.getMin());
                long max = Long.parseLong(histogramNumeric.getMin());
                long interval = Long.parseLong(histogramNumeric.getInterval());

                EsUtil.getNumericHistogram(esClient,indexName,typeName,fieldName,min,max,interval);
            }
        }
    }


}

