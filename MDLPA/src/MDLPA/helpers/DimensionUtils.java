package MDLPA.helpers;

import java.util.BitSet;
import java.util.Set;

/**
 * Helper utility to calculate the DRxOR metric.
 * @author Oualid Boutemine <boutemine.oualid@courrier.uqam.ca>
 * 
 * [1] Boutemine, O., & Bouguessa, M. (2017). Mining Community Structures in Multidimensional Networks. ACM Transactions on Knowledge Discovery from Data (TKDD), 11(4), 51. 
 */
public class DimensionUtils {
    /**
     * Estimates the Dimension Relevance xOR metric (DRxOR) of a set of dimensions in the neighborhood of a node 
     * (represented by the list of sets of linking dimensions to its neighbors).
     * @param dimensions: The set of dimensions for which the DRxOR is to be estimated.
     * @param neighborsLinkingDimensions: Neighborhood of the node for which the relevance of @param dimensions is to be estimated.
     * Each element of the set represents the BitSet representation of the linking dimensions to its corresponding neighbor.
     */
    public static double calculateDimensionsRelevanceXOR(BitSet dimensions, Set<BitSet> neighborsLinkingDimensions) {
        if (neighborsLinkingDimensions.size() <= 1)
            return 1;
        
        double numberOfExclusivlyReachableNeighbors = 0;
        
        for (BitSet Dvu : neighborsLinkingDimensions) {
            if (SetUtils.contains(dimensions, Dvu)) // Check whether Dvu ⊆ dimensions
                numberOfExclusivlyReachableNeighbors++;
        }
                
        int totalNumberOfNeighbors = neighborsLinkingDimensions.size();
        
        double DRxOR = numberOfExclusivlyReachableNeighbors / totalNumberOfNeighbors;
        return DRxOR;
    }
}