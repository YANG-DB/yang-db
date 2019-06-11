package com.kayhut.fuse.epb.plan.statistics;

/*-
 * #%L
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javaslang.Tuple2;

import java.util.*;

/**
 * Created by moti on 4/18/2017.
 */
public interface Statistics {
    Statistics merge(Statistics other);

    class SummaryStatistics implements Statistics{
        private double total;
        private double cardinality;

        public SummaryStatistics(double total, double cardinality) {
            this.total = total;
            this.cardinality = cardinality;
        }

        public double getTotal() {
            return total;
        }

        public double getCardinality() {
            return cardinality;
        }

        @Override
        public Statistics merge(Statistics other) {
            SummaryStatistics otherCard = (SummaryStatistics) other;
            return new SummaryStatistics(this.getTotal() + otherCard.getTotal(),this.getCardinality() + otherCard.getCardinality());
        }
    }


    /**
     * Created by moti on 31/03/2017.
     */
    class HistogramStatistics<T extends Comparable<T>> implements Statistics{
        private static final double CARDINALITY_LAMBDA = 1.0;
        private List<BucketInfo<T>> buckets;

        public HistogramStatistics(List<BucketInfo<T>> buckets) {
            this.buckets = buckets;
        }

        public List<BucketInfo<T>> getBuckets() {
            return buckets;
        }

        // Currently assumes equal buckets, should be repaired
        @Override
        public Statistics merge(Statistics other) {
            if(other == null){
                return this;
            }
            if(other instanceof HistogramStatistics){
                HistogramStatistics<T> otherHistogram = (HistogramStatistics<T>) other;
                List<BucketInfo<T>> buckets = new ArrayList<>();

                for (int i = 0; i < this.buckets.size();i++){
                    BucketInfo<T> myBucket = this.buckets.get(i);
                    BucketInfo<T> otherBucket = otherHistogram.getBuckets().get(i);
                    BucketInfo<T>  bucketInfo = new BucketInfo<>(myBucket.getTotal() + otherBucket.getTotal(),
                            myBucket.getCardinality() + otherBucket.cardinality,
                            myBucket.getLowerBound(),
                            myBucket.getHigherBound());
                    buckets.add(bucketInfo);
                }


                return new HistogramStatistics<>(buckets);
            }

            throw new IllegalArgumentException();
        }

        public Optional<BucketInfo<T>> findBucketContaining(T value){
            BucketInfo<T> dummyInfo = new BucketInfo<>(0L, 0L, value, value);
            int searchResult = Collections.binarySearch(buckets, dummyInfo, (o1, o2) -> {
                if(o1.isSingleValue()){
                    if(o2.isValueInRange(o1.lowerBound)){
                        return 0;
                    }else{
                        if(o1.lowerBound.compareTo(o2.higherBound)>=0)
                            return 1;
                        return -1;
                    }
                }else{
                    if(o2.isSingleValue()){
                        if(o1.isValueInRange(o2.lowerBound)){
                            return 0;
                        }
                        if(o1.lowerBound.compareTo(o2.higherBound)>=0)
                            return 1;
                        return -1;

                    }
                    else{
                        if (o1.getHigherBound().compareTo(o2.getLowerBound()) < 0)
                            return -1;
                        if (o1.getLowerBound().compareTo(o2.getHigherBound()) >= 0)
                            return 1;
                    }
                }
                return 0;
            });
            if(searchResult >= 0){
                return Optional.of(buckets.get(searchResult));
            }
            return Optional.empty();
        }

        public List<BucketInfo<T>> findBucketsAbove(T value, boolean inclusive){
            int i = 0;
            if(buckets.isEmpty())
                return Collections.emptyList();

            BucketInfo<T> currentBucket = buckets.get(i);
            while(i < buckets.size() && ((currentBucket.getHigherBound().compareTo(value)<=0  && !currentBucket.isSingleValue())||
                                        (currentBucket.isSingleValue() &&
                                                ((currentBucket.getHigherBound().compareTo(value) <= 0 && !inclusive)
                                                || (currentBucket.getHigherBound().compareTo(value) < 0 && inclusive))))){
                i++;
                if(i<buckets.size())
                    currentBucket = buckets.get(i);
            }
            return buckets.subList(i, buckets.size());
        }

        public List<BucketInfo<T>> findBucketsBelow(T value, boolean inclusive){
            if(buckets.isEmpty())
                return Collections.emptyList();
            int i = buckets.size()-1;
            BucketInfo<T> currentBucket = buckets.get(i);
            while(i >=0 && ((currentBucket.getLowerBound().compareTo(value) > 0 && inclusive) || (currentBucket.getLowerBound().compareTo(value) >= 0 && !inclusive))){
                i--;
                if(i >= 0)
                    currentBucket = buckets.get(i);
            }
            return buckets.subList(0, i+1);
        }
        /**
         * _1 = total count
         * _2 = cardinality
         * @return
         */
        public Tuple2<Double, Double> getCardinality() {
            double card = buckets.stream().mapToDouble(BucketInfo::getCardinality).sum();
            double total = buckets.stream().mapToDouble(BucketInfo::getTotal).sum();
            return new Tuple2<>(total,card);
        }

        public static <T extends Comparable<T>> HistogramStatistics<T> combine(List<HistogramStatistics<T>> histograms){
            List<BucketInfo<T>> buckets = new ArrayList<>();
            List<Integer> indices = new ArrayList<>();
            for(int i = 0; i < histograms.size(); i++){
                indices.add(0);
            }

            while(haveBucketsToHandle(histograms, indices)){
                T minBucket = findMinBucketLowerBound(histograms, indices);
                List<BucketInfo<T>> bucketsToMerge = new LinkedList<>();
                for(int i = 0;i<histograms.size();i++){
                    HistogramStatistics<T> currentHistogram = histograms.get(i);
                    Integer bucketIndex = indices.get(i);
                    if(bucketIndex < currentHistogram.getBuckets().size() && minBucket.equals(currentHistogram.getBuckets().get(bucketIndex).getLowerBound()))
                    {
                        bucketsToMerge.add(currentHistogram.getBuckets().get(bucketIndex));
                        indices.set(i, bucketIndex + 1);
                    }
                }

                buckets.add(mergeBucketList(bucketsToMerge));
            }

            return new HistogramStatistics<>(buckets);
        }

        private static <T extends Comparable<T>> boolean haveBucketsToHandle(List<HistogramStatistics<T>> histograms, List<Integer> indices) {
            for (int i = 0;i<histograms.size();i++){
                if(indices.get(i) < histograms.get(i).getBuckets().size())
                    return true;
            }
            return false;
        }

        private static <T extends Comparable<T>> BucketInfo<T> mergeBucketList(List<BucketInfo<T>> bucketsToMerge) {
            if(bucketsToMerge.size() == 0)
                return null;

            long total = bucketsToMerge.stream().mapToLong(BucketInfo::getTotal).sum();
            long card = bucketsToMerge.get(0).isSingleValue() ? 1 : Math.round(bucketsToMerge.stream().mapToDouble(BucketInfo::getCardinality).average().getAsDouble());
            BucketInfo<T> newBucket = new BucketInfo<>(total, card, bucketsToMerge.get(0).getLowerBound(), bucketsToMerge.get(0).getHigherBound());
            return newBucket;
        }

        private static <T extends Comparable<T>> T findMinBucketLowerBound(List<HistogramStatistics<T>> histograms, List<Integer> indices) {
            T minValue = null;
            for(int i = 0;i<histograms.size();i++){
                HistogramStatistics<T> currentHistogram = histograms.get(i);
                Integer bucketIndex = indices.get(i);
                if(bucketIndex < currentHistogram.getBuckets().size() &&
                        (minValue == null || minValue.compareTo(currentHistogram.getBuckets().get(bucketIndex).getLowerBound()) > 0)){
                    minValue = currentHistogram.getBuckets().get(bucketIndex).getLowerBound();
                }
            }
            return minValue;
        }
    }

    /**
     * Created by moti on 31/03/2017.
     */
    class BucketInfo<T extends Comparable<T>> {

        public BucketInfo() {
        }

        public BucketInfo(Long total, Long cardinality, T lowerBound, T higherBound) {
            this.total = total;
            this.cardinality = cardinality;
            this.lowerBound = lowerBound;
            this.higherBound = higherBound;
        }

        public Long getCardinality() {
            return cardinality;
        }

        public Long getTotal() {
            return total;
        }

        public T getLowerBound() { return lowerBound; }

        public T getHigherBound() { return higherBound; }

        //lower bound - inclusive, higher bound - non-inclusive
        public boolean isValueInRange(T value) {
            if(isSingleValue() && lowerBound.equals(value))
                return true;
            if (higherBound != null && value.compareTo(higherBound) >= 0) {
                return false;
            }
            if (lowerBound != null && value.compareTo(lowerBound) < 0) {
                return false;
            }
            return true;
        }

        public SummaryStatistics getCardinalityObject(){
            return new SummaryStatistics(total, cardinality);
        }

        public boolean isSingleValue(){
            return this.lowerBound.equals(this.higherBound);
        }

        private T lowerBound;
        private T higherBound;
        private Long total;
        private Long cardinality;

    }
}
