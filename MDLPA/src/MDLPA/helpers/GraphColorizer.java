package MDLPA.helpers;

import MDLPA.MultidimensionalCluster;
import java.util.Map;
import org.gephi.graph.api.Node;

/**
 * Colorizes the nodes of a graph based on their cluster memberships.
 * 
 * @author Oualid Boutemine <boutemine.oualid@courrier.uqam.ca>
 * 
 * [1] Boutemine, O., & Bouguessa, M. (2017). Mining Community Structures in Multidimensional Networks. ACM Transactions on Knowledge Discovery from Data (TKDD), 11(4), 51. 
 */
public class GraphColorizer {

    public void colorizeGraph(MultidimensionalCluster[] graphClusters) {
        if(graphClusters == null) {
            return;
        }

        for (MultidimensionalCluster cluster : graphClusters) {
            for (Node node : cluster.getNodes()) {
                Color color = cluster.getColor();
                colorizeNode(node, color);
            }
        }
    }
    
    public void colorizeNodes(Map<Node, Color> nodeColors){
        if(nodeColors == null) {
            return;
        }

        for (Map.Entry<Node, Color> nodeColor : nodeColors.entrySet()) {
            colorizeNode(
               nodeColor.getKey(),
               nodeColor.getValue()
            );
        }
    }
    

    public void colorizeNode(Node node, Color color) {
        node.getNodeData().setColor(color.getR(), color.getG(), color.getB());
    }
}
