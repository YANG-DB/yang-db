
package com.kayhut.fuse.datagen.utilities;

import java.util.Random;

/**
 *
 * @author smuel
 */
public class GenerateRandom {
    
    public static int genRandomInt(int min , int max) {
        
        if (min > max)
            throw new IllegalArgumentException("Start cannot exceed End.");
        Random rand = new Random();
        int randInt = rand.nextInt((max - min) + 1) + min ;
        return randInt ;
    }
    
    public static double genRandomDouble(double rangeMin , double rangeMax) {
        if (rangeMin > rangeMax)
            throw new IllegalArgumentException("Start cannot exceed End.");
        Random r = new Random();
        double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
        return randomValue ;
    }
    
    public static boolean testWithProb(int prob) {
        
        int rand = genRandomInt(1,100) ;
        boolean isPos = true ;
        if((rand - (100-prob)) < 0)
            isPos =  false ;
        return isPos;
    }
    
    public static int genRandomWithNormalDistribution(int desiredStandardDeviation , int desiredMean) {
        
        Random r = new Random() ;
        int val = (int) (Math.round(r.nextGaussian()*desiredStandardDeviation + desiredMean));
        return val ;
    }
    
    public static int genRandomWithDiffDelta(int maxValue , int divisor ,double delta) {
        
        double rand = Math.random() ;
        int res ;
        if (rand < delta)
            res = 0 ;
        else if ((rand >= delta) && (rand < (1.0 - delta)) ) 
            res = genRandomInt(1,(int)Math.round((double)maxValue/divisor)) ;
        else // rand >= (1.0 - delta)
            res = genRandomInt((int)Math.round((double)maxValue/2),maxValue);
        return res;
    }
    
}
