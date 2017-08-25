package MDLPA;

import MDLPA.helpers.Color;
import MDLPA.helpers.DimensionUtils;
import MDLPA.helpers.FormattingUtils;
import MDLPA.helpers.GraphColorizer;
import MDLPA.helpers.IteratorUtils;
import MDLPA.helpers.MapUtils;
import MDLPA.helpers.SetUtils;
import MDLPA.helpers.NodeIndexComparer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import org.gephi.clustering.api.Cluster;
import org.gephi.clustering.spi.Clusterer;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;

/**
 * Implementation of MDLPA[1] clusterer for Gephi.
 * Multidimensional information is represented on edge labels as a comma-separated string of dimensions names between
 * node pairs.
 * Example : label of edge (n0,n1) = "d0,d2,d5" means that the pair (n0,n1) is connected by three edges belonging to dimensions d0, d2 and d5 respectively.
 * The multidimensional information was represented as so because the version 0.8.2 of the Gephi platform (on which this plugin is based) doesn't support multigraphs.
 * The latest platform version (0.9.1) however natively supports Multigraphs, yet it doesn't offer any clustering API/modules.
 * Aside from providing a rich and fairly easy-to-use OOP model, the Gephi platform offers advanced graph data visualization and manipulation features which simplifies the interpretation of results.
 * 
 * @author Oualid Boutemine <boutemine.oualid@courrier.uqam.ca>
 * 
 * [1] Boutemine, O., & Bouguessa, M. (2017). Mining Community Structures in Multidimensional Networks. ACM Transactions on Knowledge Discovery from Data (TKDD), 11(4), 51. 
 */
public class MDLPA implements Clusterer, LongTask {
    // Used to colorize the nodes based on their clusters.
    private static final  GraphColorizer graphColorizer = new GraphColorizer();
    
    private final static Random randomizer = new Random(System.currentTimeMillis());
    private ProgressTicket progress = null;
    private Graph graph;
    private boolean isCancelled = false;

    // Use this flag to print the node-cluster membership list when the processing is done.
    private boolean printNodeClusterMemberships = true;
    
    // Use this flag to print the list of clusters and their relevant dimensions.
    private boolean printClustersAndRelevantDimensions = true;
    
    // Holds the list of node memberships.
    protected final Map<Node, Color> nodeMemberships = new HashMap<Node, Color>();    

    // Represents the separator between the dimensions label of the connecting edges.
    private String dimensionsSeparator = ",";
    
    /**
     * Used to hold the list of nodes of the processed network in order to support shuffling for simulated parallel processing.
     * Will impact the memory footprint for larger networks. TODO #13 Refactor this logic.
     */
    private List<Node> V;

    /**
     * The list of initial attraction weights W0 for each neighbor of each node.
     * The key holds the node v, the value maps w0 for each possible neighbor u.
     */
    private final Map<Node,Map<Node,Double>> W0 = new HashMap<Node,Map<Node,Double>>();
    
    /**
     * The list of revised W0 for each neighbor of each node.
     */
    private final Map<Node,Map<Node,Double>> W = new HashMap<Node,Map<Node,Double>>();
    
    // Maps each existing dimensions label (ex D1) to a unique integer identifier.
    // Used for bit set operations.
    private final Map<String, Integer> dimensionIds = new HashMap<String, Integer>();
    
    // Represents each substitute edge in the graph by the associated dimensions.
    // Remember that gephi can't handle multigraph, the trick is to mark the dimensions on the label of the edge.
    private final Map<Edge, BitSet> edgeDimensions = new HashMap<Edge,BitSet>();
    
    /**
     * Holds the relevant dimensions Dv for each v in V.
     * Each relevant dimension d in Dv is represented by the activated bits in the corresponding BitSet Dv.
     */
    private final Map<Node, BitSet> DV = new HashMap<Node,BitSet>();

    // Saves a reference to the detected clusters.
    private List<Cluster> detectedClusters = new ArrayList<Cluster>();
    
    public MDLPA() {
    }
    
    public void setPrintNodeClusterMemberships(boolean value) {
        this.printNodeClusterMemberships = value;
    }
    
    public void setPrintClustersAndRelevantDimensions(boolean value) {
        this.printClustersAndRelevantDimensions = value;
    }
    
    public void setDimensionsSeparator(String value) {
        this.dimensionsSeparator = value;
    }
    
    @Override
    public void execute(GraphModel gm) {
        try {
            graph = gm.getGraphVisible();
            graph.readLock();
            
            if (progress != null) {
                this.progress.start();
            }
            
            this.printProgressMessage("Setting up environment.");
            
            // Initialize nodes list to allow shuffling for simulated parallel processing of node memberships.
            V = IteratorUtils.toList(graph.getNodes().iterator());
            
            this.printProgressMessage("Puting each node in it's own cluster");
            
            // Assigning each node to its own cluster.
            // Clusters are represented by unique colors so as to simplify graph coloring in the post processing phase.
            for(Node v : V) {
                Color cluster = new Color().randomize(v.getId());
                nodeMemberships.put(v, cluster);
                
                v.getNodeData()
                 .setLabel(v
                    .getNodeData()
                    .getId()
                 );
            }

            this.printProgressMessage("Setting up edge dimensions");
            
            // Making a big integer representation of the dimensions appearing between any pair of nodes.
            initializeEdgeDimensions();
            
            this.printProgressMessage("Calculating w0");

            // Calculting w0 for all nodes v in V.
            calculateW0(V);
            
            this.printProgressMessage("Selecting initial relevant dimensions Dv' for each node v.");
            initializeRelevantNodeDimensionsDv();

            this.printProgressMessage("Initialization completed. Starting the clustering process.");
            // Start the clustering
            while(!allNodesAssignedToDominantClusterInNeighbourhood() && !isCancelled) {
                this.printProgressMessage("Starting a new propagation cycle.");
                
                // This will simulate a random parallel processing
                Collections.shuffle(V);

                // Go over the nodes list and update their memberships according to the update rule of MDLPA [1]
                for(Node v : V) {
                    Map.Entry<Color,List<Node>> dominantCluster = getDominantClusterInNeighbourhood(v);
                    
                    updateDvAndw(v, dominantCluster);
                    
                    nodeMemberships.put(v, dominantCluster.getKey());
                }
                
                this.printProgressMessage("Propagation cycle ended.");
            }

            this.printProgressMessage("Regrouping nodes into clusters based on memberships labels lv.");
            
            // Regrouping the nodes based on their memberships.
            detectedClusters = regroupNodesIntoClusters();
            
            this.printProgressMessage("Colorizing the graph based on cluster labels.");
            colorizeGraph(detectedClusters);
            
            // Todo #10 save the results to a file.
            // Print the results if needed.
            if (printNodeClusterMemberships) {
                printNodesAssignments(detectedClusters);
            }
            
            if (printClustersAndRelevantDimensions) {
                printClustersAndRelevantDimensions(detectedClusters);
            }
            
            this.printProgressMessage("Finished");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            isCancelled = true;
        }
        finally {
            W0.clear();
            W.clear();
            dimensionIds.clear();
            edgeDimensions.clear();
            nodeMemberships.clear();
            DV.clear();
            V.clear();

            graph.readUnlockAll();
        }
    }
    
    /**
     * This methods makes a BitSet representation of each edge where the set
     * of connecting dimensions is extracted and represented in a bit array.
     * Example: label of edge (n0,n1) = "d0,d2,d5" will be represented as
     * [X._.X._._.X] (assuming that the ids of those dimensions map to each bit position).
     * Bitwise operators are then used for sets manipulation.
     * Thus we can manipulate the graph as if it were a multigraph.
     */
    private void initializeEdgeDimensions(){
        List<Edge> edges = IteratorUtils.toList(graph.getEdges().iterator());
        
        for (Edge edge : edges) {
            if (isCancelled)
                break;
            
            String label = edge
                .getEdgeData()
                .getLabel();
            
            // Connecting dimensions between the nodes v, u of the edge.
            String[] connectingDimensions = label.split(dimensionsSeparator);
            
            BitSet Dvu = new BitSet();
            int dimensionId;
            
            for (String dimension : connectingDimensions)
            {
                // Dimension ids start from 0 with 1 graded increments.
                if (dimensionIds.containsKey(dimension)) {
                    dimensionId = dimensionIds.get(dimension);
                }
                else
                {
                    dimensionId = dimensionIds.size();
                    dimensionIds.put(dimension, dimensionId);
                }
                
                // Set the corresponding bit position.
                Dvu.set(dimensionId);
            }
            
            edgeDimensions.put(edge, Dvu);
        }
    }
    
    /**
     * Estimates w0 values for each node v in V.
     */
    private void calculateW0(List<Node> V) {
        for(Node v : V) {
            if (isCancelled)
                break;

            Set<Node> Nv = getNeighbors(v);
            calculateW0InNeigborhood(v, Nv);
        }

        // Initializing W with W0
        for (Map.Entry<Node, Map<Node, Double>> w0Onv : W0.entrySet()) {
            if (isCancelled)
                break;

            Node v = w0Onv.getKey();
            Map<Node, Double> w0 = w0Onv.getValue();
            Map<Node, Double> clonedw0 = new HashMap<Node,Double>();
            
            for (Map.Entry<Node,Double> w0vu : w0.entrySet()) {
                Node u = w0vu.getKey();
                double weight = w0vu.getValue();
                clonedw0.put(u, weight);
            }
            
            W.put(v, clonedw0);
        }
    }
    
    /**
     * Estimates w0 for each neighbor of v, u in Nv.
     * @param v : the node for which the w0 are estimated for each one of its neighbors.
     * @param Nv : neighbors of v.
     */
    private void calculateW0InNeigborhood(Node v, Set<Node> Nv) {
        Map<Node, Double> neighborsW0 = new HashMap<Node, Double>();
        Set<BitSet> neighborsDvu = new HashSet<BitSet>();
        
        for (Node u : Nv) {
            if (isCancelled)
                break;

            Edge vu = graph.getEdge(v, u);
            
            // Getting the dimensions connecting the pair (node, neighbor)
            BitSet Dvu = edgeDimensions.get(vu);
            
            neighborsDvu.add(Dvu);
        }
        
        Map<BitSet, Double> w0Cache = new HashMap<BitSet, Double>();
        
        // Calculating w0 for each neighbor u of v.
        for (Node u : Nv) {
            if (isCancelled)
                break;

            Edge vu = graph.getEdge(v, u);
            BitSet Dvu = edgeDimensions.get(vu);
            
            // Calculating w0 based on DRxOR metric.
            double w0 = 0;
            if (w0Cache.containsKey(Dvu))
                 w0 = w0Cache.get(Dvu);
            else
            {
                w0 = DimensionUtils
                    .calculateDimensionsRelevanceXOR(Dvu, neighborsDvu);
                
                w0Cache.put(Dvu, w0);
            }
           
            neighborsW0.put(u, w0);
        }
        
        // Saving w0(v, u) for each u in Nv.
        this.W0.put(v, neighborsW0);
    }
            
    /**
     * Selects Dv_0 for each v in V
     */
    private void initializeRelevantNodeDimensionsDv() {
        // For each v in V, we take the attraction weights w0(v, u) for each one of its neighbors u.
        for (Map.Entry<Node, Map<Node, Double>> Nv : W0.entrySet()) {
            if (isCancelled)
                break;

            Node v = Nv.getKey();
            
            // Getting the neighbor linking to it with the most relevant dimensions, if more than one neighbor 
            // that links with dimensions having a higher relevance, take them both and aggregate their dimensions.
            Map<Node, Double> w0Onv = Nv.getValue();
            
            if (w0Onv.isEmpty())
            {
                // No registered neighbors, Isolated node, no relevant dimensions.
                DV.put(v, new BitSet());
                continue;
            }

            // Summing overs w0 applied on v which share the same linking dimensions Dvu
            Map<BitSet, Double> accumulatedw0ForDvu = new HashMap<BitSet, Double>();

            // For each w0 applied on v
            for (Map.Entry<Node, Double> w0vu : w0Onv.entrySet()) {
                Node u = w0vu.getKey();
                Edge vu = graph.getEdge(v, u);
                BitSet Dvu = edgeDimensions.get(vu);
                
                // If this Dvu already exists in the neighborhood of v, then add the current w0 to the previous accumulatedValue
                if (accumulatedw0ForDvu.containsKey(Dvu))
                {
                    double accumulatedW0 = w0vu.getValue() + accumulatedw0ForDvu.get(Dvu);
                    accumulatedw0ForDvu.put(Dvu, accumulatedW0);
                }
                else
                {
                    accumulatedw0ForDvu.put(Dvu, w0vu.getValue());
                }
            }
            
            double maxAccumulatedW0 = Collections.max(accumulatedw0ForDvu.values());
            
            // Get Dvu which corresponds to the highest combined w0 in the neighborhood of v.
            BitSet Dv = new BitSet();
            
            for (Map.Entry<BitSet, Double> w0ForDvu : accumulatedw0ForDvu.entrySet()) {
                double accumulatedw0 = w0ForDvu.getValue();
                BitSet Dvu = w0ForDvu.getKey();
                
                if (accumulatedw0 == maxAccumulatedW0)
                    Dv.or(Dvu); // Take the union if more than Dvu support the highest w0 in v's neighborhood.
            }

            DV.put(v, Dv);
            
            // Make sure to update the attraction weights according to the new relevant dimensions set.
            updateW(v, Dv);
        }
    }
    
    /**
     * Updates the relevant dimensions Dv of v based on the relevant dimensions of the neighbors belonging to the winning cluster.
     * If the group Dv changes, w(u, v) get updated for each neighbor u in Nv.
     * @param v: node for which the group of relevant dimensions Dv is to be updated.
     * @param winningCluster: neighbors u of v that belong to the cluster which applies the highest combined w.
     */
    
    private void updateDvAndw(Node v, Map.Entry<Color,List<Node>> winningCluster) {
        List<Node> winningClusterNeighbors = winningCluster.getValue();
        if (winningClusterNeighbors == null || winningClusterNeighbors.isEmpty())
            return;
        
        // Relevant Dimensions of neighbors u which belong to the dominant cluster in the neighborhood of v. 
        BitSet DU = new BitSet();
        
        // Set of all dimensions Dvu connecting v to its neighbors u in the dominant cluster.
        BitSet combinedDvu = new BitSet();

        for(Node u : winningClusterNeighbors) {
            BitSet Du = DV.get(u);
            DU.or(Du);
            
            Edge vu = graph.getEdge(v, u);
            BitSet Dvu = edgeDimensions.get(vu);
            combinedDvu.or(Dvu);
        }

        // Intersecting the two sets to filter out irrelevant dimensions possibly caught in the first propagation cycles.
        DU.and(combinedDvu);
        
        // Updating Dv.
        BitSet newDv = (BitSet)DU.clone();       
        BitSet Dv = DV.get(v);
        
        // Now lets check whether the node's relevant dimensions list has changed.
        newDv.xor(Dv);
        
        // Nothing's changed, no need to update w(u,v)
        if (newDv.cardinality() == 0)
            return;

        DV.put(v, DU);
        
        // Updating w(u, v) carried by this node v on its neighbors u in Nv
        updateW(v, DU);
    }

    /**
     * Updates the attraction weights w(u,v) applied by v on its neighbors u in Nv
     */
    private void updateW(Node v, BitSet newDv) {
        Set<Node> Nv = getNeighbors(v);
        
        for(Node u : Nv) {
            if (isCancelled)
                break;

            Edge vu = graph.getEdge(v, u);
            BitSet Dvu = edgeDimensions.get(vu);

            // Estimate the distance between the new Dv and the connecting dimensions Dvu using the jaccard coefficient.
            double distance = SetUtils.getJaccardCoefficient(newDv, Dvu);
            
            // Revising w(u, v)
            // Take all w0 applied on u
            Map<Node, Double> w0u = W0.get(u);
            double w0uv = w0u.get(v);
            
            // Revise it.
            double w = w0uv * distance;
            
            // Save it back to the attraction weights w applied on u by its neighbors Nu.
            Map<Node, Double> wuNu = W.get(u);
            wuNu.put(v, w);
        }
    }
    
    /**
     * Returns the dominant cluster in the neighborhood of a node v based on the maximum combined attraction weight w.
     */
    private Map.Entry<Color, List<Node>> getDominantClusterInNeighbourhood(Node v) {
        Set<Node> Nv = getNeighbors(v);

        if (Nv.isEmpty())
        {
            Color lv = nodeMemberships.get(v);

            return new AbstractMap.SimpleEntry<Color, List<Node>>(lv, new ArrayList<Node>());
        }        
        
        Map<Color, Double> combinedClusterWeights = new LinkedHashMap<Color, Double>();
        Map<Node, Double> neighborsWeights = W.get(v);

        for (Node u : Nv) {
            if (isCancelled)
                break;
            
            Color lu = nodeMemberships.get(u);
            
            double wvu = neighborsWeights.get(u);
                    
            if (combinedClusterWeights.containsKey(lu)) {
                wvu += combinedClusterWeights.get(lu);
            }
            
            combinedClusterWeights.put(lu, wvu);
        }
        
        // Picking the cluster with the heighest w, if the two or more clusters apply the same w,
        // then pick one randomly.
        // LinkedHashMap will always return values in the same order they were inserted. We need to shuffle to randomly pick a dominant cluster.
        combinedClusterWeights = MapUtils.shuffle(combinedClusterWeights, randomizer);
        
        double maxWeight = Collections.max(combinedClusterWeights.values());
        
        // Take a random dominant cluster regardless of the current membership lv of v.
        Color newlv = MapUtils.getKeyByValue(combinedClusterWeights, maxWeight);

        List<Node> dominantNeighbors = new ArrayList<Node>();
        
        // Finding neighbors that belong to the dominant cluster.
        for (Node u : neighborsWeights.keySet()) {
            if (nodeMemberships.get(u) == newlv)
                dominantNeighbors.add(u);
        }
        
        Map.Entry<Color, List<Node>> dominantCluster = new AbstractMap.SimpleEntry<Color, List<Node>>(
            newlv,
            dominantNeighbors
        );
        
        return dominantCluster;
    }
    
    /*
    * Checks whether all nodes are currently adopting the dominant label according to the propagation rule of MDLPA.
    * Invoked at the end of each propagation cycle.
    */
    private boolean allNodesAssignedToDominantClusterInNeighbourhood(){
        boolean result = true;
        
        for (Node v : V) {
            Color lv = nodeMemberships.get(v);
            
            Color dominantCluster = getDominantClusterInNeighbourhood(v).getKey();
            result = lv == dominantCluster;
            
            if (!result)
                break;
        }
        
        return result;
    }
    
    /**
     * Regroups v in V into K clusters based on lv
     */
    private List<Cluster> regroupNodesIntoClusters() {
        Map<Color, Set<Node>> nodeGroups = new HashMap<Color, Set<Node>>();
        
        for(Map.Entry<Node, Color> nodeMembership : nodeMemberships.entrySet()) {
            Set<Node> clusterNodes = new HashSet<Node>();
            
            Color lv = nodeMembership.getValue();
            if (nodeGroups.containsKey(lv)) {
                clusterNodes = nodeGroups.get(lv);
            }
            
            Node v = nodeMembership.getKey();
            clusterNodes.add(v);
            
            nodeGroups.put(lv, clusterNodes);
        }
        
        ArrayList<Cluster> clusters = new ArrayList<Cluster>();
        int counter = 0;
        
        for(Map.Entry<Color, Set<Node>> group : nodeGroups.entrySet()) {
            // Identifying the relevant dimensions Dk of cluster Ck = (Vk, Dk)
            Set<Node> Vk = group.getValue();
            BitSet Dk = new BitSet();
     
            // Taking the union of the relevant dimensions Dv of all members v in Vk
            for (Node v : Vk) {
                Dk.or(DV.get(v));
            }
            
            String clusterName = Integer.toString(counter);
            Color lCk = group.getKey();
            
            List<String> relevantDimensionsNames = getDimensionNames(Dk);
                    
            MultidimensionalCluster cluster = new MultidimensionalCluster(
                clusterName,
                lCk,
                Vk,
                relevantDimensionsNames
            );
            
            clusters.add(cluster);
            counter++;
        }
        
        return clusters;
    }
    
    /**
     * Recovers back the original dimension names from their BitSet representation.
     */
    private List<String> getDimensionNames(BitSet dimensions) {
        List<String> dimensionNames = new ArrayList<String>();
        
        for (Map.Entry<String,Integer> dimensionId : dimensionIds.entrySet())
        {
            String dimensionName = dimensionId.getKey();
            
            int id = dimensionId.getValue();
            
            if (dimensions.get(id)) {
                dimensionNames.add(dimensionName);
            }
        }
 
        return dimensionNames;
    }
    
    @Override
    public Cluster[] getClusters() {
        return detectedClusters.toArray(new MultidimensionalCluster[0]);
    }

    @Override
    public void setProgressTicket(ProgressTicket pt) {
        this.progress = pt;
    }
    
    @Override
    public boolean cancel() {
        this.progress.finish("Cancelled");

        return this.isCancelled = true;
    }

    /**
     * Prints a progress message in the status bar of gephi.
     */
    public void printProgressMessage(String message) {
        if (this.progress == null)
            return ;
        
        this.progress.progress(message);
    }
    
    /**
     * Returns the neighbors of the provided node.
     */
    private Set<Node> getNeighbors(Node node){
        return IteratorUtils
            .toSet(graph
                .getNeighbors(node)
                .iterator()
            );
    }
    
    /**
     * Colorizes the nodes of the graph based on the representative colors of the associated clusters.
     */
    protected void colorizeGraph(List<Cluster> clusters) {
        if (clusters != null && clusters.size() > 0) {
            graphColorizer.colorizeGraph(clusters.toArray(new MultidimensionalCluster[0]));
        }
    }
    
    /* Displays the list of memberships in the following format: 
     * node_i:Cluster_j where (node_i:) is represented by the index of the row and wont be displayed.
     * Example :
     * 1
     * 1
     * 3
     * 4
     * 2
     * 2
     * 4
     * Each line indicates the cluster membership of the corresponding node. For example, nodes {n0, n1} belong to cluster 1.
     * Nodes (n3, n6) belong to cluster 4 and so on ..
     * This format was adopted to allow compatibility with other implementations in MATLAB which generate similar clustering results.
     */
    protected void printNodesAssignments(List<Cluster> clusters) {
        // Project the list of clusters into their corresponding representative colors.
        // The list is supposed to contain distinct elements after the grouping.
        // The index of the cluster in the list represents the printed membership index.
        ArrayList<Color> clusterColors = new ArrayList<Color>();
        
        for (Cluster cluster:clusters) {
            clusterColors.add(((MultidimensionalCluster)cluster).getColor());
        }
    
        // Now build the list of memberships according the adopted format.
        
        // First sort list of nodes according to their ids.
        Collections.sort(V, new NodeIndexComparer());
        
        StringBuilder resultBuilder = new StringBuilder();

        for(Node v : V) {
            Color cluster  = nodeMemberships.get(v);
            int index = clusterColors.indexOf(cluster) + 1;
            
            resultBuilder = resultBuilder
                .append(index)
                .append("\n");
        }
        
        // Display the result in a message dialog.
        showPopup(
            "Node Memberships",
            resultBuilder
                .toString()
                .trim()
        );
    }
    
    /**
     * Prints the list of clusters and their relevant dimensions in the following format: 
     * clusters = "[v1, .., vi], ..., [vj, ..., vn]"
     * relevantDimensions = "[d1, .., dk], ..., [d2, .., do]" where d1, d2, dk, do represent real dimension name as specified on the edges.
     * 
     * Notes: For singleton clusters, the set of relevant dimensions will be empty ([])
     * Two clusters might have the same or different set of relevant dimensions.
     * The cardinality of the sets of relevant dimensions varies from 0 to o where o is the number of dimensions of the network.
     * An empty set of relevant dimensions means that the node is isolated from any connected component.
     */
    private void printClustersAndRelevantDimensions(List<Cluster> clusters) {
        StringBuilder clusterGroupsBuilder = new StringBuilder();
        StringBuilder clusterRelevantDimensionsBuilder = new StringBuilder();
        
        for (Cluster cluster : clusters)
        {
            String clusterMembers = FormattingUtils.getCommaSeperatedRepresentation(cluster.getNodes());
            
            // Adding the new cluster to the formatted list of clusters.
            clusterGroupsBuilder
                .append("[")
                .append(clusterMembers)
                .append("],");
            
            // Adding relevant dimensions.
            MultidimensionalCluster mdCluster = (MultidimensionalCluster)cluster;
            
            List<String> relevantDimensionsList = mdCluster
                .getRelevantDimensions();
            
            if (relevantDimensionsList.isEmpty())
                clusterRelevantDimensionsBuilder.append("[],");
            else
            {
                
                String relevantDimensions = FormattingUtils.getCommaSeperatedRepresentation(mdCluster.getRelevantDimensions());
                
                clusterRelevantDimensionsBuilder
                    .append("[")
                    .append(relevantDimensions)
                    .append("],");
            }
        }
        
        String clusterGroups = clusterGroupsBuilder.toString();
        clusterGroups =
            "clusters = {" +
            clusterGroups
                .substring(0, clusterGroups.length() - 1) +
            "};";

        String relevantDimensions = clusterRelevantDimensionsBuilder.toString();
        relevantDimensions =
            "relevantDimensions = {" +
            relevantDimensions
                .substring(0, relevantDimensions.length() - 1) +
            "};";
        
        // Display the result in a message dialog.
        showPopup(
            "Clusters and their relevant dimensions",
            clusterGroups + "\n" + relevantDimensions
        );
    }
    
    private void showPopup(String title, String content) {
        JTextArea container = new JTextArea(content);
        container.setEditable(true);
        
        JOptionPane.showMessageDialog(
            null,
            container,
            title,
            JOptionPane.OK_OPTION
        );
    }
}
