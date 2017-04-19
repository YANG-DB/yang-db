package com.kayhut.fuse.epb.plan.statistics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by moti on 4/18/2017.
 */
public interface Statistics {
    Statistics merge(Statistics other);

    /**
     * Created by moti on 31/03/2017.
     */
    class CardinalityStatistics implements Statistics {
        private long cardinality;
        private long total;

        public CardinalityStatistics(long cardinality, long total) {
            this.cardinality = cardinality;
            this.total = total;
        }

        public long getCardinality() {
            return cardinality;
        }

        public long getTotal() {
            return total;
        }

        @Override
        public Statistics merge(Statistics other) {
            if(other == null){
                return this;
            }
            if(other instanceof CardinalityStatistics) {
                return new CardinalityStatistics((long)(((CardinalityStatistics) other).getCardinality() + this.cardinality / 2.0),
                        ((CardinalityStatistics) other).getTotal() + this.total);
            }
            throw new IllegalArgumentException();
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

        private T lowerBound;
        private T higherBound;
        private Long total;
        private Long cardinality;

    }
}
