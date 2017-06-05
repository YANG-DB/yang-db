package com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.graphstream;

import com.kayhut.fuse.generator.util.CSVUtil;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkGraphML;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.slf4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by benishue on 18-May-17.
 */
public class GraphstreamHelper {

    private GraphstreamHelper() {
        throw new IllegalAccessError("Utility class");
    }

    public static Graph generateGraph(String graphId, int numOfNodes, int edgesPerNode) {
        Graph graph = new SingleGraph(graphId);
        BarabasiAlbertGenerator gen = new BarabasiAlbertGenerator(edgesPerNode);
        gen.addSink(graph);
        gen.begin();
        while (numOfNodes-- > 0) {
            gen.nextEvents();
        }
        gen.end();
        return graph;
    }

    public static void drawGraph(Graph g, Logger logger){
        g.setStrict(false);
        Viewer viewer = new Viewer(g, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        JFrame myJFrame = new JFrame();
        myJFrame.setPreferredSize(new Dimension(600, 600));
        DefaultView view = (DefaultView) viewer.addDefaultView(false);   // false indicates "no JFrame".
        view.setPreferredSize(new Dimension(500, 500));
        myJFrame.setLayout(new FlowLayout());
        myJFrame.add(view);

        JSlider slider = new JSlider();
        slider.addChangeListener(e -> view.getCamera().setViewPercent(slider.getValue() / 10.0));
        myJFrame.add(slider);

        JButton exportButton = new JButton("Export");
        exportButton.addActionListener((ActionEvent e) -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to save");

            int userSelection = fileChooser.showSaveDialog(myJFrame);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String filePath = fileToSave.getAbsolutePath();
                exportGraphMLFile(g,filePath);
                logger.info("GraphML file exported to: %s", filePath);
            }

        });
        myJFrame.add(exportButton);
        myJFrame.pack();
        myJFrame.setVisible(true);
        myJFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        viewer.enableAutoLayout();
        ViewerPipe vp = viewer.newViewerPipe();
        vp.addViewerListener(new ViewerListener() {
            @Override
            public void viewClosed(String viewName) {
                // dont care
            }

            @Override
            public void buttonPushed(String id) {
                Node n = g.getNode(id);
                JLabel label = new JLabel("Node id: " + n.getId() + ", Degree: " + n.getInDegree());
                label.setToolTipText("Sheker");
                JOptionPane.showMessageDialog(null, label);
            }

            @Override
            public void buttonReleased(String id) {
                // don't care
            }
        });

        for (Node node : g) {
            node.addAttribute("ui.label", node.getId());
        }
        while (true) {
            (view).repaint();
            vp.pump();
        }


    }

    public static void printScaleFreeDataSummary(Graph g, String path) throws IOException {
        Map<Integer, Long> sortedMap = new TreeMap<>(g.getNodeSet().stream().map(Node::getOutDegree)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())));

        String filePath = URLDecoder.decode(path + "//ScaleFreeSummary_" + g.getId() + ".csv", "UTF-8");

        CSVUtil.appendResult(new String[] { "NumberOfNodes", "Degree" }, filePath);
        sortedMap.forEach((degree, count) -> CSVUtil.appendResult(new String[]{count + "", degree + ""}, filePath));
        System.out.println("\nFile Created Successfully: " + filePath);
    }

    public static void exportGraphMLFile(Graph graph, String path){
        try {
            FileSinkGraphML fileSinkGraphML = new FileSinkGraphML();
            String decodedPath = URLDecoder.decode(path, "UTF-8");
            fileSinkGraphML.writeAll(graph, decodedPath);
            System.out.println("\nFile Created Successfully: " + decodedPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
