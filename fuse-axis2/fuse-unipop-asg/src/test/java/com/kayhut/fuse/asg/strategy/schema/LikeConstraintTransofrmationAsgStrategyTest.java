package com.kayhut.fuse.asg.strategy.schema;

import org.junit.Test;

/**
 * Created by roman.margolis on 05/02/2018.
 */
public class LikeConstraintTransofrmationAsgStrategyTest {
    @Test
    public void test1() {
        String[] parts1 = "*blah*".split("\\*");   // eq ngram
        String[] parts2 = "blah*".split("\\*");    // wildcard
        String[] parts3 = "*blah".split("\\*");    // wildcard
        String[] parts4 = "blah".split("\\*");     // eq keyword

        String[] parts5 = "*bl*ah*".split("\\*");  // eq keyword AND eq keyword
        String[] parts6 = "bl*ah*".split("\\*");   // wildcard AND eq ngram
        String[] parts7 = "*bl*ah".split("\\*");   // eq ngram AND wildcard
        String[] parts8 = "bl*ah".split("\\*");    // wildcard AND wildcard
        int x = 5;
    }
}
