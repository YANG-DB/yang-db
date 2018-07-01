package com.kayhut.fuse.unipop.process.traversal.traverser;

import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.TraverserGenerator;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;

import java.util.Set;

/**
 * Created by Roman on 1/29/2018.
 */
public class ThinPathTraverserGenerator implements TraverserGenerator {
    //region Constructors
    public ThinPathTraverserGenerator() {
        this.stringOrdinalDictionary = new HashStringOrdinalDictionary();
    }
    //endregion

    //region TraverserGenerator Implementation
    @Override
    public Set<TraverserRequirement> getProvidedRequirements() {
        return null;
    }

    @Override
    public <S> Traverser.Admin<S> generate(S s, Step<S, ?> step, long l) {
        return new ThinPathTraverser<>(s, step, this.stringOrdinalDictionary);
    }
    //endregion

    //region Fields
    private StringOrdinalDictionary stringOrdinalDictionary;
    //endregion
}
