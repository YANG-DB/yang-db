package com.kayhut.fuse.generator.model.entity;

/**
 * Created by benishue on 15-May-17.
 */
public class Dragon extends EntityBase {

    //region Ctrs
    private Dragon() {
        //Used by the Builder
    }

    public Dragon(String id, String name) {
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

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    //endregion

    //region Public Methods
    @Override
    public String toString() {
        return "dragon{" +
                "name='" + name + '\'' +
                ", power=" + power +
                '}';
    }

    public String[] getRecord(){
        return new String[] { this.getId(), this.name, Integer.toString(this.getPower())};
    }
    //endregion

    //region Fields
    private String name;
    private int power;
    //endregion

    //region Builder
    public static final class DragonBuilder {
        private String id;
        private String name;
        private int power;

        private DragonBuilder() {
        }

        public static DragonBuilder aDragon() {
            return new DragonBuilder();
        }

        public DragonBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public DragonBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public DragonBuilder withPower(int power) {
            this.power = power;
            return this;
        }

        public Dragon build() {
            Dragon dragon = new Dragon();
            dragon.setId(id);
            dragon.setName(name);
            dragon.setPower(power);
            return dragon;
        }
    }
    //endregion

}
