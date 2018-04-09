package com.kayhut.fuse.model.results;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;

public class CsvQueryResult extends QueryResultBase implements TextContent{

    public CsvQueryResult() {
    }

    public CsvQueryResult(String[] csvLines) {
        this.csvLines = csvLines;
    }

    public CsvQueryResult(String[] csvLines, String[] header) {
        this.csvLines = csvLines;
        this.header = header;
    }

    public void setCsvLines(String[] csvLines) {
        this.csvLines = csvLines;
    }

    public String[] getCsvLines() {
        return csvLines;
    }

    public String getResultType(){
        return "csv";
    }

    public String[] getHeader() {
        return header;
    }

    public void setHeader(String[] header) {
        this.header = header;
    }

    @Override
    public int getSize() {
        return this.csvLines.length;
    }

    private String[] csvLines;
    private String[] header;

    @Override
    public String content() {
        StringBuilder builder = new StringBuilder();
        if(this.header != null) {
            builder.append(String.join(",", header));
            builder.append('\n');
        }
        for (int i = 0; i < csvLines.length; i++) {
            builder.append(csvLines[i]);
            if(i != csvLines.length-1){
                builder.append('\n');
            }
        }
        return builder.toString();
    }


    public static final class Builder {
        public Builder() {
            this.lines = new ArrayList<>();
        }
        public static Builder instance() {
            return new Builder();
        }
        public Builder withLine(String line){
            this.lines.add(line);
            return this;
        }

        public Builder withHeader(String[] header){
            this.header = header;
            return this;
        }

        public CsvQueryResult build(){
            if(this.header != null) {
                return new CsvQueryResult(Stream.ofAll(this.lines).toJavaArray(String.class), this.header);
            }else{
                return new CsvQueryResult(Stream.ofAll(this.lines).toJavaArray(String.class));
            }
        }

        private List<String> lines;
        private String[] header;
    }
}
