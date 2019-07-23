package com.yangdb.es.plugins.script;

import com.yangdb.es.plugins.script.regex.RegexUtil;
import org.junit.Assert;
import org.junit.Test;

public class RegexUtilTests {
    @Test
    public void test_abc() {
        String regex = RegexUtil.wildcardToRegex("abc");
        Assert.assertEquals("abc", regex);
    }

    @Test
    public void test_a_STAR_c() {
        String regex = RegexUtil.wildcardToRegex("a*c");
        Assert.assertEquals("a.*c", regex);
    }

    @Test
    public void test_a_STAR_EscapedSTAR_c() {
        String regex = RegexUtil.wildcardToRegex("a*\\*c");
        Assert.assertEquals("a.*\\*c", regex);
    }

    @Test
    public void test_STAR_EscapedSTAR_Dot_SquareBrackets_abc_QMARK_EscapedQMARK() {
        String regex = RegexUtil.wildcardToRegex("*\\*.[]abc?\\?");
        Assert.assertEquals(".*\\*\\.\\[\\]abc.\\?", regex);
    }
}
