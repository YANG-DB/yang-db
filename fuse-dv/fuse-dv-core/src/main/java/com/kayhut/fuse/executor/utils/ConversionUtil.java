package com.kayhut.fuse.executor.utils;

import com.kayhut.fuse.model.query.Rel;

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
