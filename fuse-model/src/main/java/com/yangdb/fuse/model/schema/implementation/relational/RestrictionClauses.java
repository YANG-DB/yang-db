package com.yangdb.fuse.model.schema.implementation.relational;

import java.util.List;

/***
 * This is used for multi-column restrictions. This clauses will be handled as ANDs.
 *
 *
 */
public class RestrictionClauses {

    /***
     * List of clause inside this restriction. The clause in this list should be applied as ORs.
     */
    private List<RestrictionClause> restrictionClause;

    /***
     * Default constructor.
     */
    public RestrictionClauses() {}

    /***
     * list of restriction clauses to be checked.
     * @param restrictionClause clause to be checked.
     */
    public RestrictionClauses(final List<RestrictionClause> restrictionClause) {
        super();
        this.restrictionClause = restrictionClause;
    }

    /**
     * @return the restrictionClause
     */
    public List<RestrictionClause> getRestrictionClause() {
        return restrictionClause;
    }

    /**
     * @param restrictionClause the restrictionClause to set
     */
    public void setRestrictionClause(final List<RestrictionClause> restrictionClause) {
        this.restrictionClause = restrictionClause;
    }
}
