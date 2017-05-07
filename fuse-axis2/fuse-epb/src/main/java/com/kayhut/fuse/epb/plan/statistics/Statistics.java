package com.kayhut.fuse.epb.plan.statistics;

import javaslang.Tuple2;

import java.util.*;

/**
 * Created by moti on 4/18/2017.
 */
public interface Statistics {
    Statistics merge(Statistics other);

    class Cardinality implements Statistics{
        private double total;
        private double cardinality;

        public Cardinality(double total, double cardinality) {
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
            Cardinality otherCard = (Cardinality) other;
            return new Cardinality(this.getTotal() + otherCard.getTotal(),this.getCardinality() + otherCard.getCardinality());
        }
    }


    /**
     * Created by moti on 31/03/2017.
     */
    class HistogramStatistics<T extends Comparable<T>> implements Statistics{
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
                    BucketInfo<T>  bucketInfo = new BucketInfo<T>(myBucket.getTotal() + otherBucket.getTotal(),
                            myBucket.getCardinality() + otherBucket.cardinality,
                            myBucket.getLowerBound(),
                            myBucket.getHigherBound());
                    buckets.add(bucketInfo);
                }


                return new HistogramStatistics<T>(buckets);
            }

            throw new IllegalArgumentException();
        }

        public Optional<BucketInfo<T>> findBucketContaining(T value){
            BucketInfo<T> dummyInfo = new BucketInfo<T>(0L,0L,value, value);
            int searchResult = Collections.binarySearch(buckets, dummyInfo, new Comparator<BucketInfo<T>>() {
                @Override
                public int compare(BucketInfo<T> o1, BucketInfo<T> o2) {
                    if (o1.getHigherBound().compareTo(o2.getLowerBound()) < 0)
                        return -1;
                    if (o1.getLowerBound().compareTo(o2.getHigherBound()) > 0)
                        return 1;
                    return 0;
                }
            });
            if(searchResult != -1){
                return Optional.of(buckets.get(searchResult));
            }
            return Optional.empty();
        }

        public List<BucketInfo<T>> findBucketsAbove(T value, boolean inclusive){
            int i = 0;
            while(i < buckets.size() && ((buckets.get(i).getHigherBound().compareTo(value)<0 && inclusive) || (buckets.get(i).getHigherBound().compareTo(value)<=0 && !inclusive)) ){
                i++;
            }
            return buckets.subList(i, buckets.size());
        }

        public List<BucketInfo<T>> findBucketsBelow(T value, boolean inclusive){
            int i = buckets.size()-1;
            while(i >=0 && ((buckets.get(i).getLowerBound().compareTo(value) > 0 && inclusive) || (buckets.get(i).getLowerBound().compareTo(value) >= 0 && !inclusive))){
                i--;
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

     }

    /**
     * Created by moti on 31/03/2017.
     */
    class BucketInfo<T extends Comparable<T>> {

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

        public T getLowerBound() { return lowerBound; };
        public T getHigherBound() { return higherBound; };

        //lower bound - inclusive, higher bound - non-inclusive
        public boolean isValueInRange(T value) {
            if (higherBound != null && value.compareTo(higherBound) >= 0) {
                return false;
            }
            if (lowerBound != null && value.compareTo(lowerBound) < 0) {
                return false;
            }
            return true;
        }

        public Cardinality getCardinalityObject(){
            return new Cardinality(total, cardinality);
        }

        private T lowerBound;
        private T higherBound;
        private Long total;
        private Long cardinality;

    }
}
