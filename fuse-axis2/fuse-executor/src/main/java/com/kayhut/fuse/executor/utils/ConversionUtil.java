package com.kayhut.fuse.executor.utils;

import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.unipop.controller.utils.CollectionUtil;
import com.kayhut.fuse.unipop.predicates.ExistsP;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.List;

/**
 * Created by Roman on 10/05/2017.
 */
public class ConversionUtil {
    public static String convertDirectionGraphic(Rel.Direction dir) {
        switch (dir) {
            case R: return "-->";
            case L: return "<--";
            case RL: return "<-->";
        }

        return null;
    }
}
