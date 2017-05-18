package com.kayhut.fuse.generator.model.entity;

import com.kayhut.fuse.generator.model.enums.Gender;

import java.util.Date;
import java.util.List;

/**
 * Created by benishue on 15-May-17.
 */
public class Person extends EntityBase {

    //region Getters & Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Date getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(Date deathDate) {
        this.deathDate = deathDate;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    //endregion

    @Override
    public String[] getRecord() {
        return new String[0];
    }

    //region Fields
    private String firstName ;
    private String lastName ;
    private Gender gender ;
    private Date birthDate ;
    private Date deathDate ;
    private int height ;
    private boolean isOldestOffspring ;
    private boolean isAlive ;
    private int parentId ;
    private int kingdom_id ;
    private List<Integer> offspringsIdList ;
    private String personInfo ;
    private Date since ;
    private Date till ;
    private int birthYear ;
    //endregion
}
