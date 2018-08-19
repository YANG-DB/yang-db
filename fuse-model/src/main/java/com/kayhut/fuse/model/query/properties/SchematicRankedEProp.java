package com.kayhut.fuse.model.query.properties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SchematicRankedEProp extends SchematicEProp implements RankingProp {

    public SchematicRankedEProp(int eNum, String pType, String schematicName, Constraint con, long boost) {
        super(eNum, pType, schematicName, con);
        this.boost = boost;
    }

    public SchematicRankedEProp(SchematicEProp schematicEProp, long boost) {
        this(schematicEProp.geteNum(), schematicEProp.getpType(), schematicEProp.getSchematicName(), schematicEProp.getCon(), boost);
    }

    @Override
    public long getBoost() {
        return boost;
    }

    private final long boost;
}
