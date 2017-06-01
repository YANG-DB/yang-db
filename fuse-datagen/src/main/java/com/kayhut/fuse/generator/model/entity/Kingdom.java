package com.kayhut.fuse.generator.model.entity;

import java.util.Date;

/**
 * Created by benishue on 15-May-17.
 */
public class Kingdom extends EntityBase {

    //region Ctrs
    private Kingdom() {
    }

    public Kingdom(String id, String name) {
        super(id);
        this.name = name;
    }
    //endregion

    //region Getters & Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKing() {
        return king;
    }

    public void setKing(String king) {
        this.king = king;
    }

    public String getQueen() {
        return queen;
    }

    public void setQueen(String queen) {
        this.queen = queen;
    }

    public Date getIndependenceDay() {
        return independenceDay;
    }

    public void setIndependenceDay(Date independenceDay) {
        this.independenceDay = independenceDay;
    }

    public double getFunds() {
        return funds;
    }

    public void setFunds(double funds) {
        this.funds = funds;
    }

    
    //endregion

    //region Public Methods
    @Override
    public String[] getRecord() {
        return new String[]{this.getId(), this.name, this.king, this.queen, Long.toString(this.independenceDay.getTime()), Double.toString(this.funds)};
    }

    @Override
    public String toString() {
        return "Kingdom{" +
                "id='" + getId() + '\'' +
                ", name='" + name + '\'' +
                ", king='" + king + '\'' +
                ", queen='" + queen + '\'' +
                ", independenceDay=" + independenceDay +
                ", funds=" + funds +
                '}';
    }

    //endregion

    //region Fields
    private String name;
    private String king;
    private String queen;
    private Date independenceDay;
    private double funds;
    //endregion

    //region Builder
    public static final class KingdomBuilder {
        private String id;
        private String name;
        private String king;
        private String queen;
        private Date independenceDay;
        private double funds;

        private KingdomBuilder() {
        }

        public static KingdomBuilder aKingdom() {
            return new KingdomBuilder();
        }

        public KingdomBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public KingdomBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public KingdomBuilder withKing(String king) {
            this.king = king;
            return this;
        }

        public KingdomBuilder withQueen(String queen) {
            this.queen = queen;
            return this;
        }

        public KingdomBuilder withIndependenceDay(Date independenceDay) {
            this.independenceDay = independenceDay;
            return this;
        }

        public KingdomBuilder withFunds(double funds) {
            this.funds = funds;
            return this;
        }

        public Kingdom build() {
            Kingdom kingdom = new Kingdom();
            kingdom.setId(id);
            kingdom.setName(name);
            kingdom.setKing(king);
            kingdom.setQueen(queen);
            kingdom.setIndependenceDay(independenceDay);
            kingdom.setFunds(funds);
            return kingdom;
        }
    }
    //endregion

}
