package org.unipop.query.controller;

import org.unipop.structure.UniGraph;

/**
 * Created by User on 19/03/2017.
 */
public interface ControllerManagerFactory {
    ControllerManager create(UniGraph graph);
}
