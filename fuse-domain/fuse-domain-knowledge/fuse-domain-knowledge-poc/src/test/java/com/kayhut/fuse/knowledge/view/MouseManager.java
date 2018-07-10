package com.kayhut.fuse.knowledge.view;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import org.graphstream.graph.Node;
import org.graphstream.ui.fx_viewer.util.FxMouseManager;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.view.View;

public class MouseManager extends FxMouseManager {
    @Override
    public void init(GraphicGraph graph, View view) {
        super.init(graph, view);
        view.addListener(MouseEvent.MOUSE_PRESSED, (EventHandler<MouseEvent>) e -> {
            final GraphicElement element = MouseManager.this.view.findGraphicElementAt(MouseManager.this.getManagedTypes(), e.getX(), e.getY());
            if (element!=null && e.isSecondaryButtonDown()) {
                MouseManager.this.showDetails(element.getId());
            }
        });
    }

    public void showDetails(String id) {
        final Node node = graph.getNode(id);
        if (node.getAttribute("ui.value") != null) {
            if(node.getAttribute("ui.value").equals(node.getAttribute("label"))) {
                node.setAttribute("label", node.getAttribute("ui.label"));
            } else {
                node.setAttribute("label", node.getAttribute("ui.value"));
            }
        }
    }

}
