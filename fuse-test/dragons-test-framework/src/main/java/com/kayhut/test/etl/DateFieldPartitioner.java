package com.kayhut.test.etl;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by moti on 6/5/2017.
 */
public class DateFieldPartitioner implements Partitioner {
    private String partitionField;
    private String partitionFormat;
    private SimpleDateFormat simpleDateFormat ;

    public DateFieldPartitioner(String partitionField, String partitionFormat, String dateFormat) {
        this.partitionField = partitionField;
        this.partitionFormat = partitionFormat;
        simpleDateFormat = new SimpleDateFormat(dateFormat);
    }

    @Override
    public String getPartition(Map<String, String> document) {
        String date = simpleDateFormat.format(new Date(Long.parseLong(document.get(partitionField))));
        return String.format(partitionFormat, date);
    }
}
