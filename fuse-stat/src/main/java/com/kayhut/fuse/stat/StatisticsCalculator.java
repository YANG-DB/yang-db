package com.kayhut.fuse.stat;

import com.kayhut.fuse.stat.Util.EsUtil;
import com.kayhut.fuse.stat.Util.StatUtil;
import com.kayhut.fuse.stat.configuration.StatisticsConfiguration;
import com.kayhut.fuse.stat.model.*;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.client.transport.TransportClient;

import java.awt.font.NumericShaper;
import java.util.*;


/**
 * Created by benishue on 27-Apr-17.
 */
public class StatisticsCalculator {

    public static void main(String[] args) throws Exception {

        Configuration configuration = new StatisticsConfiguration("statistics.properties").getInstance();

        String statConfigurationFilePath = configuration.getString("statistics.configuration.file");

        TransportClient esClient = StatUtil.getClient(configuration);

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
                            BuildHistogramForNumericField(esClient, statContainer, indexName, typeName);
                        }
                    }
                }
            }
        }


//        StatUtil.bulkIndexing(esClient, "C:\\Code\\fuse\\fuse-stat\\src\\main\\resources\\dragons_mock.txt",
//                "index1","dragon");
//        StatUtil.showTypeFieldsNames(esClient,"game","football");
    }

    private static void BuildHistogramForNumericField(TransportClient esClient, StatContainer statContainer, String indexName, String typeName) {
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

