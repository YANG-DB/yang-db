package com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.generators;

import com.google.common.base.Stopwatch;
import com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.roulettes.*;
import com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.roulettes.rolltree.datatypes.Bucket;
import javaslang.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * BAGraphGenerator creates a Barabasi-Albert model using different roulette wheel algorithms.
 * <p>
 * For more info, take a look at the paper:
 * A. Hadian, S. Nobari, B. Minaei-Bidgoli, and Q. Qu, ROLL: Fast in-memory generation of gigantic scale-free networks. In Proceedings of the ACM 	SIGMOD International Conference on Management of Data, 2016.
 * <p>
 * http://dl.acm.org/citation.cfm?doid=2882903.2882964
 *
 * @author Ali Hadian
 */
public class BAGraphGenerator {
    private static final Logger logger = LoggerFactory.getLogger(BAGraphGenerator.class);

    public NodesList nodesRouletteWheel = new SimpleRWNodeList();
    public Random random = new Random();
    public static long numEdges = 0;
    public static int numNodes = 0;

    public enum SamplingMode {SIMPLE, ROLL_BUCKET, ROLL_TREE, SA, ROLL_BUCKET_SORTED, ROLL_TREE_REDUCED}

    //public static SamplingMode samplingMode = SamplingMode.SIMPLE;
    public static SamplingMode samplingMode = SamplingMode.ROLL_TREE;
    public static int numNodesFinal = 0;
    public static int m = 2;
    public static Integer m_0 = null;
    public long start = System.nanoTime();
    public static BufferedWriter graphFileWriter = null;
    public static String graphDistributionOutputFileName = null;    //only supported for ROLL-Tree
    public static long samplingTime, maintenanceTime, numComparisons, totalTime;
    public BufferedWriter outWriter = null;

    //public static HashSet<Long> nodesSet;
    public static ArrayList<Tuple2> edgesSet;

    public BAGraphGenerator(int numNodes) throws Exception {
        super();
        BAGraphGenerator.numNodesFinal = numNodes;
        this.nodesRouletteWheel = new SimpleRWNodeList();
        //this.nodesSet = new HashSet();
        edgesSet = new ArrayList<>();
        switch (samplingMode) {
            case SIMPLE:
                nodesRouletteWheel = new SimpleRWNodeList();
                break;
            case ROLL_BUCKET:
                nodesRouletteWheel = new RollBucketNodeList();
                break;
            case ROLL_TREE:
                nodesRouletteWheel = new RollTreeNodeList();
                break;
            case SA:
                nodesRouletteWheel = new SANodeList();
                break;
            //the following modes are extensions of the previous methods. These tricks are discussed in the paper.
            case ROLL_TREE_REDUCED:
                nodesRouletteWheel = new RollTreeNodeList_WithReducedInsertions();
                break;
            case ROLL_BUCKET_SORTED:
                nodesRouletteWheel = new RollBucketNodeList_SORTED();
                break;
            default:
                throw new Exception("Wrong sampling Mode: " + samplingMode);
        }
    }

    protected BAGraphGenerator() throws Exception {
        super();
        //throw new Exception("DO NOT CALL THIS METHOD!");
    }


    /**
     * Initialize a model with m_0(=m) nodes.
     */
    private void initializeGraph() {
        if (m <= 0) {
            System.out.println("Initialization method should be revisited");
            System.exit(1);
        }
        int final_m0 = (m_0 == null)? m : m_0;
        nodesRouletteWheel.createInitNodes(final_m0);
        numNodes = final_m0 + 1;
        numEdges = final_m0;
    }

    /**
     * Builds the model and writes the edge list to the output file
     */
    public void createGraph() {
        NumberFormat.getInstance().setGroupingUsed(true);
        NumberFormat.getInstance().setMaximumFractionDigits(3);
        NumberFormat.getInstance().setMinimumFractionDigits(1);
        NumberFormat.getInstance().setRoundingMode(RoundingMode.HALF_UP);

        start = System.nanoTime();
        long t = System.nanoTime();
        initializeGraph();
        maintenanceTime += System.nanoTime() - t; //initialization has no sampling time, so it is all maintenance time.
        for (int i = numNodes; i < numNodesFinal; i++) {
            nodesRouletteWheel.connectMRandomNodeToThisNewNode(m, i);
            numNodes++;
            numEdges += m;
        }
        totalTime += System.nanoTime() - t;

        if (nodesRouletteWheel instanceof RollTreeNodeList)
            System.err.printf("%s %d %d %d %d %d %d %d %d %.2f %.2f %d %d\n", samplingMode, numNodes, m, numEdges, numComparisons, totalTime, samplingTime, maintenanceTime,
                    ((RollTreeNodeList) nodesRouletteWheel).getBuckets().size(),
                    ((RollTreeNodeList) nodesRouletteWheel).getRoot().getCodeWordLength(numEdges),
                    ((RollTreeNodeList) nodesRouletteWheel).getHuffmanCodeWordLength(numEdges),
                    ((RollTreeNodeList) nodesRouletteWheel).numInserts,
                    ((RollTreeNodeList) nodesRouletteWheel).numDeletes);
        else if (nodesRouletteWheel instanceof RollBucketNodeList)
            System.err.printf("%s %d %d %d %d %d %d %d 0 0 0\n", samplingMode, numNodes, m, numEdges, numComparisons, totalTime, samplingTime, maintenanceTime,((RollBucketNodeList) nodesRouletteWheel).getNumBuckets());
        else
            System.err.printf("%s %d %d %d %d %d %d %d 0 0 0\n", samplingMode, numNodes, m, numEdges, numComparisons, totalTime, samplingTime, maintenanceTime);

        try {
            if (graphFileWriter != null)
                graphFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Print data distribution, if the output file name is provided using the "-d" switch
        if(nodesRouletteWheel instanceof RollTreeNodeList && graphDistributionOutputFileName != null) {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(graphDistributionOutputFileName))) {
                Map<Integer, Bucket> buckets = ((RollTreeNodeList) nodesRouletteWheel).getBuckets();
                for (int d: buckets.keySet().stream().mapToInt(x -> x).sorted().toArray())//.stream().sorted().collect(Collectors.toList()))
                    writer.write(d + "\t" + buckets.get(d).getSize() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void printStat(String Stat) {
        System.out.print(new Date());
        System.out.print("\t");
        System.out.println(Stat);
    }

    /**
     * writes the edge node1-->node2 to the output file ("node1\tnode2\n")
     *
     * @param node1
     * @param node2
     */
    public static void addEdge(long node1, long node2) {
        if (BAGraphGenerator.graphFileWriter != null) {
            try {
//                if (!nodesSet.contains(node1)) {
//                    nodesSet.add(node1);
//                }
//                if (!nodesSet.contains(node2)) {
//                    nodesSet.add(node2);
//                }
//                FillNodesSet(node1);
//                FillNodesSet(node2);
                edgesSet.add(new Tuple2<>(node1,node2));
                graphFileWriter.write(node1 + "," + node2 + "\n");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

        }
    }

//    private static void FillNodesSet(long i) {
//        if (!nodesSet[(int) i]){
//            nodesSet[(int) i] = true;
//        }
//    }

    public static void main(String[] args) throws Exception {
        if (Arrays.asList(args).contains("-exp")) {
            FigureGenerator.main(args);
            return;
        }
        int n = -1; //number of nodes
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-m":
                    BAGraphGenerator.m = Integer.parseInt(args[i + 1]);
                    break;
                case "-n":
                    n = Integer.parseInt(args[i + 1]);
                    break;
                case "-o":
                    BAGraphGenerator.graphFileWriter = new BufferedWriter(new FileWriter(new File(args[i + 1])));
                    break;
                case "-s":
                    BAGraphGenerator.samplingMode = SamplingMode.valueOf(args[i + 1].toUpperCase().replace('-', '_'));
                    break;
                case "-d":
                    BAGraphGenerator.graphDistributionOutputFileName = args[i + 1];
                    break;
                case "-m0":
                    BAGraphGenerator.m_0 = Integer.parseInt(args[i + 1]);
                    break;
            }
        }
        if (n == -1) {
            System.out.println("Usage: java -jar prog.jar \n"
                    + "\t -n num_nodes \n"
                    + "\t [-m edges_per_node DEFAULT=2] \n"
                    + "\t [-s sampling mode, use one of the following roulette wheel methods: \n"
                    + "\t\t SIMPLE \t\t Simple roulette wheel, which performs linear scans \n"
                    + "\t\t ROLL_BUCKET \t\t Roll-bucket (ROLL, sec 4.1)\n"
                    + "\t\t ROLL_TREE  \t\t Roll-tree (ROLL, sec 4.2)\n"
                    + "\t\t SA \t\t\t Roulette Wheel implemented by Stochastic Acceptance \n"
                    + "\t\t ROLL_BUCKET_SORTED \t A modified version of Roll-bucket (ROLL, last paragraph on sec. 4.1)\n"
                    + "\t\t ROLL_TREE_REDUCED \t A modified version of Roll-tree, with reduced insertions (ROLL, sec 4.2: \"Decreased tree operations\") \n"
                    + "\t [-o outFileName  DEFAULT=null (does not output the model)] \n\n"
                    + "Output performance measures (space delimited): samplingMode numNodes m numEdges numComparisons totalTime samplingTime maintenanceTime numBuckets [numBuckets AvgCodeWordLength HuffmanAvgCodeWordLength Total_buckets_inserted Total_buckets_removed]");
            System.exit(1);
        }

        if (n <= m) {
            System.out.println("n should be larger than m");
            System.exit(1);
        }
        Stopwatch stopwatch = Stopwatch.createStarted();

//        BAGraphGenerator generator = new BAGraphGenerator(n);
//        generator.createGraph();
        generateMassiveBAgraph(1000000, 3, SamplingMode.ROLL_TREE, "Try.csv");
        System.out.println(stopwatch.elapsed(TimeUnit.SECONDS));
        int i = 5;
    }

    public static List<Tuple2> generateMassiveBAgraph(int numOfNodes, int edgesPerNode, SamplingMode samplingMode, String filePath){
        try {
            BAGraphGenerator generator = new BAGraphGenerator(numOfNodes);
            BAGraphGenerator.m = edgesPerNode;
            BAGraphGenerator.samplingMode = samplingMode;
            BAGraphGenerator.graphFileWriter = new BufferedWriter(new FileWriter(new File(filePath)));
            BAGraphGenerator.graphDistributionOutputFileName = filePath.replace(".csv", "") + "_scaleFreeSummary.txt";
            generator.createGraph();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return edgesSet;
    }
}