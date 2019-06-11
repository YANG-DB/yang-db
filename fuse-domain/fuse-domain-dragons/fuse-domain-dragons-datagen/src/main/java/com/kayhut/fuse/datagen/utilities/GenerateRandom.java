
package com.kayhut.fuse.datagen.utilities;

/*-
 * #%L
 * fuse-domain-gragons-datagen
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
