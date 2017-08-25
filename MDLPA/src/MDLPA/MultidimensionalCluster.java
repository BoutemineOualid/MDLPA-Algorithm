package MDLPA;

import MDLPA.helpers.Color;
import MDLPA.helpers.FormattingUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.gephi.clustering.api.Cluster;
import org.gephi.graph.api.Node;

/**
 * Holds the name, nodes and relevant dimensions of a cluster.
 * 
 * @author Oualid Boutemine <boutemine.oualid@courrier.uqam.ca>
 * 
 * [1] Boutemine, O., & Bouguessa, M. (2017). Mining Community Structures in Multidimensional Networks. ACM Transactions on Knowledge Discovery from Data (TKDD), 11(4), 51. 
 */
public class MultidimensionalCluster implements Cluster {

    private Set<Node> nodes = new HashSet<Node>();
    private final String name;
    private final Color color;
    private final List<String> relevantDimensions;
    private Node metaNode = null;

    public MultidimensionalCluster(
        String name,
        Color color,
        Set<Node> nodes,
        List<String> relevantDimensions
    )
    {
        this.name = name;
        this.color = color;
        this.nodes = nodes;
        this.relevantDimensions = relevantDimensions;
    }
    
    public Color getColor() {
        return this.color;
    }
    
    @Override
    public Node[] getNodes() {
        return nodes.toArray(new Node[0]);
    }

    @Override
    public int getNodesCount() {
        return this.nodes.size();
    }

    @Override
    public String getName() {
        ArrayList<String> sortedDimensions = new ArrayList<String>(relevantDimensions);
        Collections.sort(sortedDimensions);

        String dimensionsList = FormattingUtils.getCommaSeperatedRepresentation(sortedDimensions);

        return String.format(
            "%s [(%s)]",
            name, 
            dimensionsList
        );
    }
    
    public List<String> getRelevantDimensions(){
        return relevantDimensions;
    }
    
    @Override
    public Node getMetaNode() {
        return this.metaNode;
    }

    @Override
    public void setMetaNode(Node node) {
        this.metaNode = node;
    }
    
    public void addNode(Node node){
        this.nodes.add(node);
    }
}