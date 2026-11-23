package com.yangdb.fuse.dispatcher.asg;

import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.query.Rel.Direction.R;

/**
 * Q13: the transformer is bound as a singleton (AsgModule), so one instance serves every query.
 * Each transform must return only the elements of its own query.
 */
public class QueryToAsgTransformerTest {

    private static Query threeHops() {
        return Query.Builder.instance().withName("q1").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Person", 2, 0),
                        new Rel(2, "own", R, null, 3, 0),
                        new ETyped(3, "B", "Dragon", 4, 0),
                        new Rel(4, "fire", R, null, 5, 0),
                        new ETyped(5, "C", "Dragon", 0, 0)
                )).build();
    }

    private static Query oneEntity() {
        return Query.Builder.instance().withName("q2").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "H", "Horse", 0, 0)
                )).build();
    }

    private static Set<Integer> eNums(AsgQuery query) {
        return query.getElements().stream().map(AsgEBase::geteNum).collect(Collectors.toSet());
    }

    private static Set<Integer> eNums(Query query) {
        return query.getElements().stream().map(EBase::geteNum).collect(Collectors.toSet());
    }

    @Test
    public void testSecondQueryHasOnlyItsOwnElements() {
        QueryToAsgTransformer transformer = new QueryToAsgTransformer();

        AsgQuery first = transformer.transform(threeHops());
        Assert.assertEquals(eNums(threeHops()), eNums(first));

        AsgQuery second = transformer.transform(oneEntity());
        Assert.assertEquals(eNums(oneEntity()), eNums(second));
        List<String> types = second.getElements().stream()
                .map(AsgEBase::geteBase)
                .filter(e -> e instanceof ETyped)
                .map(e -> ((ETyped) e).geteType())
                .collect(Collectors.toList());
        Assert.assertEquals(Arrays.asList("Horse"), types);
    }

    @Test
    public void testLaterQueryDoesNotChangeEarlierResult() {
        QueryToAsgTransformer transformer = new QueryToAsgTransformer();

        AsgQuery first = transformer.transform(threeHops());
        transformer.transform(oneEntity());

        Assert.assertEquals(eNums(threeHops()), eNums(first));
        Assert.assertTrue(first.getElements().stream().allMatch(e -> e.geteBase() instanceof Start
                || first.getOrigin().getElements().contains(e.geteBase())));
    }
}
