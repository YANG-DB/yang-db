package com.kayhut.fuse.unipop;

import com.google.common.collect.ImmutableSet;
import com.kayhut.fuse.unipop.controller.SearchPromiseElementController;
import com.kayhut.fuse.unipop.controller.SearchPromiseVertexController;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.controller.ControllerManagerFactory;
import org.unipop.query.controller.UniQueryController;
import org.unipop.structure.UniGraph;

import java.util.Set;

/**
 * Created by User on 19/03/2017.
 */
public class TestControllerManagerFactory implements ControllerManagerFactory {

    //region ControllerManagerFactory Implementation
    @Override
    public ControllerManager create(UniGraph graph) {
        return new ControllerManager() {
            @Override
            public Set<UniQueryController> getControllers() {
                return ImmutableSet.of(
                        new SearchPromiseElementController(graph),
                        new SearchPromiseVertexController());
            }

            @Override
            public void close() {

            }
        };
    }
    //endregion
}
