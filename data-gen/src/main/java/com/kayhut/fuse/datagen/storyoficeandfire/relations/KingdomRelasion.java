
package com.kayhut.fuse.datagen.storyoficeandfire.relations;

import java.util.ArrayList;
import java.util.List;
import com.kayhut.fuse.datagen.storyoficeandfire.entities.*;


/**
 *
 * @author shmuel mashiach
 */
public class KingdomRelasion {
    
    public Kingdom kingdom ;
    private SubjectOf subjectOf ;
    private RegisteredIn registeredIn ;
    private OriginatedIn<Horse> horseOrigin ;
    private OriginatedIn<Dragon> dragonOrigin ;
    
    public KingdomRelasion(Kingdom k) {
        this.kingdom = k;
        this.subjectOf = new SubjectOf() ;
        this.registeredIn = new RegisteredIn() ;
        this.horseOrigin = new OriginatedIn<Horse>() ;
        this.dragonOrigin = new OriginatedIn<Dragon>() ;
    }

    public void setSubjectOf(People p) {
        p.kingdom_id = this.kingdom.id ;
        subjectOf.addSubject(p);
    }
    
    public void setRegisteredIn(Guild g) {
        
        registeredIn.addSubject(g);
    } 
    
    public void setHorseOrigin(Horse h) {
        
        horseOrigin.addSubject(h);
    }
    
    public void setDragonOrigin(Dragon d) {
        
        dragonOrigin.addSubject(d);
    }
    
    public int getsubjectOfSize() {
        return this.subjectOf.subjects.size();
    } 
    
    public int getRegisteredInSize() {
        return registeredIn.subjects.size() ;
    }
    
    public int getHorseOriginSize() {
        return horseOrigin.subjects.size() ;
    }
    
    public int getDragonOriginSize() {
        return dragonOrigin.subjects.size() ;
    }
    
    public String getSubjectOfRelation(int idx) {
        return subjectOf.subjects.get(idx).genShortString();
        
    } 
    
    public String getRegisteredInRelation(int idx) {
        return registeredIn.subjects.get(idx).genString() ;
      
    }
    
    public String getHorseOriginRelation(int idx) {
        return horseOrigin.subjects.get(idx).genShortString() ;
        
    }
    
    public String getDragonOriginRelation(int idx) {
        return dragonOrigin.subjects.get(idx).genString() ;
        
    }
}

class SubjectOf {
    
    public  List<People> subjects;
    
    public SubjectOf() {
        this.subjects = new ArrayList<People>() ;
    } 
    
    public void addSubject(People p) {
        this.subjects.add(p) ;
    }
    
    public People getSubject(int idx) {
       return this.subjects.get(idx);
    }
    
       
}

class RegisteredIn {
    
    public List<Guild> subjects ;
    
    public RegisteredIn() {
        this.subjects = new ArrayList<Guild>() ;
    }
    
    public void addSubject(Guild p) {
        this.subjects.add(p) ;
    }
    
    public Guild getSubject(int idx) {
       return this.subjects.get(idx);
    }  
}

class OriginatedIn <T> {
    
    public List<T> subjects ;
    
    public OriginatedIn() {
        this.subjects = new ArrayList<T>() ;
    }
    
    public void addSubject(T t) {
        this.subjects.add(t) ;
    }
    
    public T getSubject(int idx) {
        return this.subjects.get(idx);
    }
    
}