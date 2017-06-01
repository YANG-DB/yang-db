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
    public String toString() {
        return "Person{" +
                "id='" + getId() + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender=" + gender +
                ", birthDate=" + birthDate +
                ", deathDate=" + deathDate +
                ", height=" + height +
                '}';
    }

    @Override
    public String[] getRecord() {
        return new String[]{this.getId(), this.firstName, this.lastName, this.gender.toString(),
                Long.toString(this.birthDate.getTime()), Long.toString(this.deathDate.getTime()),
                Integer.toString(this.height)};
    }

    //region Fields
    private String firstName;
    private String lastName;
    private Gender gender;
    private Date birthDate;
    private Date deathDate;
    private int height;
//    private boolean isOldestOffspring ;
//    private boolean isAlive ;
//    private int parentId ;
//    private int kingdom_id ;
//    private List<Integer> offspringsIdList ;
//    private String personInfo ;
//    private Date since;
//    private Date till;
//    private int birthYear ;
    //endregion


    public static final class PersonBuilder {
        private String id;
        private String firstName;
        private String lastName;
        private Gender gender;
        private Date birthDate;
        private Date deathDate;
        private int height;

        private PersonBuilder() {
        }

        public static PersonBuilder aPerson() {
            return new PersonBuilder();
        }

        public PersonBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public PersonBuilder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public PersonBuilder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public PersonBuilder withGender(Gender gender) {
            this.gender = gender;
            return this;
        }

        public PersonBuilder withBirthDate(Date birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public PersonBuilder withDeathDate(Date deathDate) {
            this.deathDate = deathDate;
            return this;
        }

        public PersonBuilder withHeight(int height) {
            this.height = height;
            return this;
        }

        public Person build() {
            Person person = new Person();
            person.setId(id);
            person.setFirstName(firstName);
            person.setLastName(lastName);
            person.setGender(gender);
            person.setBirthDate(birthDate);
            person.setDeathDate(deathDate);
            person.setHeight(height);
            return person;
        }
    }

}
