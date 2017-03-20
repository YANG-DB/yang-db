package com.kayhut.fuse.gta;

import com.kayhut.fuse.dispatcher.Cursor;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

/**
 * Created by liorp on 3/20/2017.
 */
public class TraversalCursor implements Cursor {
    private Traversal traversal;

    public TraversalCursor(Traversal traversal) {
        this.traversal = traversal;
    }

    public Traversal getTraversal() {
        return traversal;
    }
}
