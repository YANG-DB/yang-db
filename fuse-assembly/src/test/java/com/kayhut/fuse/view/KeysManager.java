package com.kayhut.fuse.view;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.fx_viewer.util.FxShortcutManager;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.view.View;

import java.util.List;

import static com.kayhut.fuse.view.FuseViewer.filter;

public class KeysManager extends FxShortcutManager {
    private Graph g;

    public KeysManager(Graph g) {
        this.g = g;
    }

    @Override
    public void init(GraphicGraph graph, View view) {
        super.init(graph, view);
        view.addListener(KeyEvent.KEY_PRESSED, (EventHandler<KeyEvent>) event -> {
            if (event.getCode() == KeyCode.SUBTRACT) {
                final List<Node> nodes = filter(g, n -> n.getAttribute("ui.selected") != null && n.getAttribute("ui.selected").toString().equals("true"));
                nodes.forEach(n -> n.setAttribute("ui.visibility-mode","hidden"));

            } else if (event.getCode() == KeyCode.ADD) {
            }

        });
    }
}
