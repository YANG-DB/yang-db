
package com.kayhut.fuse.datagen.dateandtime;

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

/**
 *
 * @author smuel
 */
public class TimeFactory {
    
    public static int hour ;
    public static int minuts ;
    
    public static void initTime() {
        hour = 0 ;
        minuts = 0 ;
    }
    
    public static void setTime(int duration) {
        
        hour +=  (int)Math.round((minuts + duration)/60) ;
        minuts =  (minuts + duration) % 60 ;
         
    }
    
    public static String getTime() {
        String m ;
        if (minuts < 10) 
            m = "0"+String.valueOf(minuts) ;
        else 
            m = String.valueOf(minuts) ;
        
        return String.valueOf(hour)+ ":"+ m ;
    }
}
