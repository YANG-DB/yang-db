package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.asg.strategy.RuleBoostProvider;
import com.kayhut.fuse.model.query.properties.EProp;

public class KnowledgeRuleBoostProvider implements RuleBoostProvider {
    @Override
    public long getBoost(EProp eProp, int ruleIndex) {

        switch (eProp.getCon().getExpr().toString()) {
            case "title":
                return (long) (2* Math.pow (10, (4-ruleIndex)*2));
            case "nicknames":
                return (long) Math.pow (10, (4-ruleIndex)*2);
        }
        return 1;
    }
}
