package com.kayhut.fuse.generator.generator.model.barbasi.albert.graphstream;

import com.kayhut.fuse.generator.generator.model.ScaleFreeModel;
import com.kayhut.fuse.generator.model.graph.Edge;
import com.kayhut.fuse.generator.model.graph.Node;
import com.kayhut.fuse.generator.model.graph.NodesEdges;
import org.graphstream.algorithm.generator.BaseGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class BarabasiAlbertGenerator extends BaseGenerator {
    /**
     * Degree of each entity.
     */
    protected ArrayList<Integer> degrees;

    /**
     * The maximum number of links created when a new entity is added.
     */
    protected int maxLinksPerStep;

    /**
     * Does the generator generates exactly {@link #maxLinksPerStep}.
     */
    protected boolean exactlyMaxLinksPerStep = false;

    /**
     * The sum of degrees of all nodes
     */
    protected int sumDeg;

    /**
     * The sum of degrees of nodes not connected to the new entity
     */
    protected int sumDegRemaining;

    /**
     * Set of indices of nodes connected to the new entity
     */
    protected Set<Integer> connected;

    /**
     * New generator.
     */
    public BarabasiAlbertGenerator() {
        this(1, false);
    }

    public BarabasiAlbertGenerator(int maxLinksPerStep) {
        this(maxLinksPerStep, false);
    }

    public BarabasiAlbertGenerator(int maxLinksPerStep,
                                   boolean exactlyMaxLinksPerStep) {
        this.directed = false;
        this.maxLinksPerStep = maxLinksPerStep;
        this.exactlyMaxLinksPerStep = exactlyMaxLinksPerStep;
    }

    /**
     * Maximum number of edges created when a new entity is added.
     *
     * @return The maximum number of links per step.
     */
    public int getMaxLinksPerStep() {
        return maxLinksPerStep;
    }

    /**
     * True if the generator produce exactly {@link #getMaxLinksPerStep()}, else
     * it produce a random number of links ranging between 1 and
     * {@link #getMaxLinksPerStep()}.
     *
     * @return Does the generator generates exactly
     *         {@link #getMaxLinksPerStep()}.
     */
    public boolean produceExactlyMaxLinkPerStep() {
        return exactlyMaxLinksPerStep;
    }

    /**
     * Set how many relation (maximum) to create for each new entity added.
     *
     * @param max
     *            The new maximum, it must be strictly greater than zero.
     */
    public void setMaxLinksPerStep(int max) {
        maxLinksPerStep = max > 0 ? max : 1;
    }

    /**
     * Set if the generator produce exactly {@link #getMaxLinksPerStep()}
     * (true), else it produce a random number of links ranging between 1 and
     * {@link #getMaxLinksPerStep()} (false).
     *
     * @param on
     *            Does the generator generates exactly
     *            {@link #getMaxLinksPerStep()}.
     */
    public void setExactlyMaxLinksPerStep(boolean on) {
        exactlyMaxLinksPerStep = on;
    }


    public void begin() {
        addNode("0");
        addNode("1");
        addEdge("0_1", "0", "1");
        degrees = new ArrayList<>();
        degrees.add(1);
        degrees.add(1);
        sumDeg = 2;
        connected = new HashSet<>();
    }

    public NodesEdges init() {
        NodesEdges nodesEdges = new NodesEdges();
        addNode("0");
        nodesEdges.addNode(new Node("0"));
        addNode("1");
        nodesEdges.addNode(new Node("1"));
        addEdge("0_1", "0", "1");
        nodesEdges.addEdge(new Edge("0","1"));
        degrees = new ArrayList<>();
        degrees.add(1);
        degrees.add(1);
        sumDeg = 2;
        connected = new HashSet<>();

        return nodesEdges;
    }

    public NodesEdges init(int startingNodeIndex) {
        NodesEdges nodesEdges = new NodesEdges();
        String node0 = Integer.toString(startingNodeIndex);
        addNode(node0);
        nodesEdges.addNode(new Node(node0));
        String node1 = Integer.toString(startingNodeIndex + 1 );
        addNode(node1);
        nodesEdges.addNode(new Node(node1));
        addEdge(node0 + "_"+ node1, node0, node1);
        nodesEdges.addEdge(new Edge(node0,node1));
        degrees = new ArrayList<>();
        degrees.add(1);
        degrees.add(1);
        sumDeg = 2;
        connected = new HashSet<>();

        return nodesEdges;
    }

    /**
     * Step of the generator. Add a entity and try to connect it with some others.
     *
     * The number of links is randomly chosen between 1 and the maximum number
     * of links per step specified in {@link #setMaxLinksPerStep(int)}.
     *
     * The complexity of this method is O(n) with n the number of nodes if the
     * number of edges created per new entity is 1, else it is O(nm) with m the
     * number of edges generated per entity.
     *
     * @see org.graphstream.algorithm.generator.Generator#nextEvents()
     */
    public boolean nextEvents() {
        // Generate a new entity.
        int nodeCount = degrees.size();
        String newId = Integer.toString(nodeCount);
        addNode(newId);

        // Attach to how many existing nodes?
        int n = maxLinksPerStep;
        if (!exactlyMaxLinksPerStep)
            n = random.nextInt(n) + 1;
        n = Math.min(n, nodeCount);

        // Choose the nodes to attach to.
        sumDegRemaining = sumDeg;
        for (int i = 0; i < n; i++)
            chooseAnotherNode();

        for (int i : connected) {
            addEdge(newId + "_" + i, newId, Integer.toString(i));
            degrees.set(i, degrees.get(i) + 1);
        }
        connected.clear();
        degrees.add(n);
        sumDeg += 2 * n;

        // It is always possible to add an element.
        return true;
    }


    public NodesEdges evolve() {
        NodesEdges nodesEdges = new NodesEdges();
        // Generate a new entity.
        int nodeCount = degrees.size();
        String newId = Integer.toString(nodeCount);
        addNode(newId);
        nodesEdges.addNode(new Node(newId));

        // Attach to how many existing nodes?
        int n = maxLinksPerStep;
        if (!exactlyMaxLinksPerStep)
            n = random.nextInt(n) + 1;
        n = Math.min(n, nodeCount);

        // Choose the nodes to attach to.
        sumDegRemaining = sumDeg;
        for (int i = 0; i < n; i++)
            chooseAnotherNode();

        for (int i : connected) {
            addEdge(newId + "_" + i, newId, Integer.toString(i));
            degrees.set(i, degrees.get(i) + 1);
            nodesEdges.addEdge(new Edge(newId,Integer.toString(i)));
        }

        connected.clear();
        degrees.add(n);
        sumDeg += 2 * n;

        return nodesEdges;
    }

    /**
     * Choose randomly one of the remaining nodes
     */
    protected void chooseAnotherNode() {
        int r = random.nextInt(sumDegRemaining);
        int runningSum = 0;
        int i = 0;
        while (runningSum <= r) {
            if (!connected.contains(i))
                runningSum += degrees.get(i);
            i++;
        }
        i--;
        connected.add(i);
        sumDegRemaining -= degrees.get(i);
    }


    /**
     * Clean degrees.
     *
     * @see org.graphstream.algorithm.generator.Generator#end()
     */
    @Override
    public void end() {
        degrees.clear();
        degrees = null;
        connected = null;
        super.end();
    }
}