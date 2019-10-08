package com.yangdb.fuse.assembly.knowledge.service;

import com.yangdb.test.BaseSuiteMarker;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        KnowledgeIdGenSnowflakeTests.class
})
public class KnowledgeIdGenTestSuite implements BaseSuiteMarker {
}
