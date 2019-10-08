package com.yangdb.fuse.assembly.knowledge.service;

import com.yangdb.test.BaseSuiteMarker;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        RankingScoreBasedE2ETests.class
})
public class KnowledgeRankingTestSuite implements BaseSuiteMarker {
}

