package MDLPA.helpers;

import java.util.Comparator;
import org.gephi.graph.api.Node;

/**
 * Compare the order of nodes based on their ids.
 * Used to print node-cluster memberships in an ascending order.
 * 
 * @author Oualid Boutemine <boutemine.oualid@courrier.uqam.ca>
 * 
 * [1] Boutemine, O., & Bouguessa, M. (2017). Mining Community Structures in Multidimensional Networks. ACM Transactions on Knowledge Discovery from Data (TKDD), 11(4), 51. 
 */
public class NodeIndexComparer implements Comparator<Node>{
    @Override
    public int compare(Node v1, Node v2) {
        String id1 = v1.getNodeData().getId();
        int index1 = Integer.parseInt(id1);

        String id2 = v2.getNodeData().getId();
        int index2 = Integer.parseInt(id2);
        
        return Integer.compare(index1, index2);
    }
}