
package com.kayhut.fuse.datagen.dateandtime;

import com.kayhut.fuse.datagen.utilities.GenerateRandom;

import java.util.GregorianCalendar;
/**
 *
 * @author smuel
 */
public class DateFactory {
    
    public GregorianCalendar gc ;
    
    
    public DateFactory(GregorianCalendar gc) {
        this.gc = (GregorianCalendar) gc.clone();
    }
    
    public DateFactory() {
        this.gc = new GregorianCalendar();
    }
    
    public void setDateFactory(int year, int daysOfYear) {
        this.gc.set(GregorianCalendar.YEAR,year);
        this.gc.set(GregorianCalendar.DAY_OF_YEAR, daysOfYear);
    }
    
    public void generateRandDate(int startYear, int endYear) {
         
        int year = GenerateRandom.genRandomInt(startYear,endYear);
        this.gc.set(GregorianCalendar.YEAR,year);
        int days = GenerateRandom.genRandomInt(1,gc.getActualMaximum(GregorianCalendar.DAY_OF_YEAR));
        this.gc.set(GregorianCalendar.DAY_OF_YEAR,days);
    }  
    
    public void addDays(int days) {
        
        this.gc.add(GregorianCalendar.DAY_OF_YEAR,days);
    }
    
    public boolean isSameDate(GregorianCalendar grc) {
        boolean tst = false ;
        //if (this.gc.equals(gc))
        if ( (this.gc.get(GregorianCalendar.YEAR) == grc.get(GregorianCalendar.YEAR)) && 
                (this.gc.get(GregorianCalendar.DAY_OF_YEAR) == grc.get(GregorianCalendar.DAY_OF_YEAR)) )
            tst = true ;
        return tst ;
    }
  
    
    @Override
    public String toString() {
        String year = String.valueOf(this.gc.get(GregorianCalendar.YEAR));
        String month = String.valueOf(this.gc.get(GregorianCalendar.MONTH)+ 1);
        String day = String.valueOf(this.gc.get(GregorianCalendar.DAY_OF_MONTH)) ;
        return day+ "-" + month + "-" + year  ;              
    }
    
    
}
