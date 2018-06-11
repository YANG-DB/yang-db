package com.kayhut.fuse.view;

import org.graphstream.graph.Graph;
import org.graphstream.ui.fx_viewer.util.FxShortcutManager;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.view.View;

public class KeysManager extends FxShortcutManager {
    private Graph g;

    public KeysManager(Graph g) {
        this.g = g;
    }

    @Override
    public void init(GraphicGraph graph, View view) {
        super.init(graph, view);
    }
}
