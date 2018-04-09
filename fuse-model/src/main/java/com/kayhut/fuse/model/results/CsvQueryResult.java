package com.kayhut.fuse.model.results;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;

public class CsvQueryResult extends QueryResultBase {

    public CsvQueryResult() {
    }

    public CsvQueryResult(String[] csvLines) {
        this.csvLines = csvLines;
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

    @Override
    public int getSize() {
        return this.csvLines.length;
    }

    private String[] csvLines;


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

        public CsvQueryResult build(){
            return new CsvQueryResult(Stream.ofAll(this.lines).toJavaArray(String.class));
        }

        private List<String> lines;
    }
}
