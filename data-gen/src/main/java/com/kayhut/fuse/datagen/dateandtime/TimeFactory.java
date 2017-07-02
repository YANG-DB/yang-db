
package com.kayhut.fuse.datagen.dateandtime;

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
