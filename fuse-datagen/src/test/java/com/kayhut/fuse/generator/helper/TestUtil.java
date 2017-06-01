package com.kayhut.fuse.generator.helper;

import com.google.common.collect.LinkedHashMultiset;

import java.util.List;
import java.util.Set;

/**
 * Created by benishue on 22-May-17.
 */
public class TestUtil {

    public static Set<Integer> findDuplicates(List<Integer> input) {
        // Linked* preserves insertion order so the returned Sets iteration order is somewhat like the original list
        LinkedHashMultiset<Integer> duplicates = LinkedHashMultiset.create(input);

        // Remove all entries with a count of 1
        duplicates.entrySet().removeIf(entry -> entry.getCount() == 1);

        return duplicates.elementSet();
    }
}
