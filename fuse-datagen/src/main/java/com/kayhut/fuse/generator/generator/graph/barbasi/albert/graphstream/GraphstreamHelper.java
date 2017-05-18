package com.kayhut.fuse.generator.generator.graph.barbasi.albert.graphstream;

import com.kayhut.fuse.generator.util.CSVUtil;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.file.FileSinkGraphML;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by benishue on 18-May-17.
 */
public class GraphstreamHelper {

    public static void drawGraph(Graph g){
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
        exportButton.addActionListener(e -> {


            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to save");

            int userSelection = fileChooser.showSaveDialog(myJFrame);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String filePath = fileToSave.getAbsolutePath();
                exportGraphMLFile(g,filePath);

                System.out.println("GraphML file exported to: " + filePath);
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

    public static void printScaleFreeDataSummary(Graph g, String path){
        try {
            Map<Integer, Long> sortedMap = new TreeMap<>(g.getNodeSet().stream().map(node -> node.getOutDegree())
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())));

            String filePath = URLDecoder.decode(path + "/ScaleFreeSummary_" + g.getId() + ".csv", "UTF-8");

            CSVUtil.appendResult(new String[] { "NumberOfNodes", "Degree" }, filePath);
            sortedMap.forEach((degree, count) -> {
                CSVUtil.appendResult(new String[] { count + "", degree + ""},filePath);
            });
            System.out.println("\nFile Created Successfully: " + filePath);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void exportGraphMLFile(Graph graph, String path){
        try {
            FileSinkGraphML fileSinkGraphML = new FileSinkGraphML();
            String decodedPath = URLDecoder.decode(path, "UTF-8");
            fileSinkGraphML.writeAll(graph, decodedPath + "/" + graph.getId() + ".txt");
            System.out.println("\nFile Created Successfully: " + decodedPath + "/" +  graph.getId() + ".txt");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
