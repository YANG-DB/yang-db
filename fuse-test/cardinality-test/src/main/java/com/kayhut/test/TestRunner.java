package com.kayhut.test;

import com.clearspring.analytics.stream.cardinality.CardinalityMergeException;
import com.google.common.math.Stats;
import com.kayhut.test.tests.*;
import com.opencsv.CSVWriter;
import javaslang.Tuple2;
import javaslang.Tuple3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TestRunner {
    public static void main(String[] args) throws CardinalityMergeException, IOException {
        List<Tuple2<Integer,TestResults>> errors = new ArrayList<>();

        try(CSVWriter writer = new CSVWriter(Files.newBufferedWriter(Paths.get("intersections-minhash-1024.csv").toAbsolutePath()))) {

            String[] header = new String[]{"p", "bucket size", "error ratio", "absolute error", "absolute zero error", "average intersection size", "overlap ratio"};
            String[] values = new String[header.length];
            writer.writeNext(header);
            for (int p = 4; p < 16; p++) {
                for (int bucketWidth = 10; bucketWidth <= 50; bucketWidth += 10) {
                    for (double overlap = 0; overlap < 1; overlap += 0.25) {
                        Random random = new Random(1234);
                        int sp = 18;
                        int numInstances = 1000000;
                        Test test = new MultiModalGaussianIntersectionMinHashTest(random, p, sp, numInstances, bucketWidth,1024, overlap);
                        //Test test = new MultiModalGaussianIntersectionTest(random, p, sp, numInstances, bucketWidth, overlap);
                        //System.out.println("Results for p = " + p + " ,sp = " + sp + ", bucketWidth = " + bucketWidth);
                        TestResults results = test.run();

                        errors.add(new Tuple2<>(p, results));
                        values[0] = String.valueOf(p);
                        values[1] = results.getAverageBucketSize().toString();
                        values[2] = String.valueOf(results.getErrorStats().mean());
                        values[3] = String.valueOf(results.getAbsoluteErrorStats().mean());
                        values[4] = String.valueOf(results.getZeroAbsoluteErrorStats().mean());
                        values[5] = String.valueOf(results.getIntersectionSizesStats().mean());
                        values[6] = String.valueOf(overlap / (1+overlap));
                        writer.writeNext(values);
                    }
                }
            }
        }
/*
        System.out.println("p\tbucket-size\terror-ratio\tabs-error\tabs-zero-error\tintersection-size");
        System.out.println("----------------------");
        for (int i = 0; i < errors.size(); i++) {
            Tuple2<Integer, TestResults> current = errors.get(i);
            System.out.println(
                    current._1 + "\t" +
                    current._2.getAverageBucketSize()+ "\t"+
                    current._2.getErrorStats().mean() + "\t"+
                    current._2.getAbsoluteErrorStats().mean() + "\t"+
                    current._2.getZeroAbsoluteErrorStats().mean() + "\t"+
                    current._2.getIntersectionSizesStats().mean()


            );
        }

*/
    }
}
