package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.quant.QuantType;

/**
 * Eprop with a boost to the ranking of the query results according to the desired boost
 *
 *   - boosting of the group will be applied with a specific rule that will calculate the entire group's
 *     ranking taking into account each ScoreEProp element within the group
 */
public class ScoreEPropGroup extends EPropGroup implements RankingProp {

    public ScoreEPropGroup(long boost) {
        this.boost = boost;
    }

    public ScoreEPropGroup(int eNum, long boost) {
        super(eNum);
        this.boost = boost;
    }

    public ScoreEPropGroup(EPropGroup group, long boost) {
        this(group.geteNum(),group.quantType,group.props,group.groups,boost);
    }

    public ScoreEPropGroup(long boost, EProp... props) {
        super(props);
        this.boost = boost;
    }

    public ScoreEPropGroup(Iterable<EProp> props, long boost) {
        super(props);
        this.boost = boost;
    }

    public ScoreEPropGroup(int eNum, long boost, EProp... props) {
        super(eNum, props);
        this.boost = boost;
    }

    public ScoreEPropGroup(int eNum, Iterable<EProp> props, long boost) {
        super(eNum, props);
        this.boost = boost;
    }

    public ScoreEPropGroup(int eNum, QuantType quantType, Iterable<EProp> props, long boost) {
        super(eNum, quantType, props);
        this.boost = boost;
    }

    public ScoreEPropGroup(int eNum, QuantType quantType, Iterable<EProp> props, Iterable<EPropGroup> groups, long boost) {
        super(eNum, quantType, props, groups);
        this.boost = boost;
    }

    //endregion
    public long getBoost() {
        return boost;
    }
    //region Properties

    //region Fields
    private long boost;
    //endregion

}
