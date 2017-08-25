package MDLPA.helpers;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Helper utility for sets manipulation.
 * @author Oualid Boutemine <boutemine.oualid@courrier.uqam.ca>
 * 
 * [1] Boutemine, O., & Bouguessa, M. (2017). Mining Community Structures in Multidimensional Networks. ACM Transactions on Knowledge Discovery from Data (TKDD), 11(4), 51. 
 */
public class SetUtils {
    
    /**
     * Recovers the intersection of two generic sets of the same type.
     */
    public static <T> Set<T> getIntersection(Set<T> set1, Set<T> set2){
        Set<T> smallerSet;
        Set<T> largerSet;
        
        Set<T> intersection = new HashSet<T>();
        
        if (set1.size() <= set2.size()) {
            smallerSet = set1;
            largerSet = set2;           
        } else {
            smallerSet = set2;
            largerSet = set1;
        }
        
        for (T element : smallerSet) {
            if (largerSet.contains(element)) {
                intersection.add(element);
            }           
        }
        
        return intersection;
    }
    
    /**
     * Recovers the intersection of a set of generic sets of the same type.
     */
    public static <T> Set<T> getIntersection(Collection<Set<T>> sets){
        if (sets == null || sets.isEmpty())
            return null;

        Iterator<Set<T>> iterator = sets.iterator();

        if (sets.size() == 1)
            return iterator.next();
        
        Set<T> first = iterator.next();
        Set<T> intersection = first;
        
        while(iterator.hasNext() && !intersection.isEmpty()){
            intersection = getIntersection(intersection, iterator.next());
        }
        
        return intersection;
    }

    /**
     * Checks whether @param set1 contains elements from @param set2
     */
    public static <T> boolean containsAny(Set<T> set1, Set<T> set2) {
        boolean result = false;
       
        for(T element : set2)
        {
            if (set1.contains(element))
            {
                 result = true;
                 break;
            }
        }
        
        return result;
    }
    
    /**
     * Checks whether a given set contains all elements of another set.
     * returns true if the members of @param set2 are all contained in @param set1.
     * Elements are represented through bit activated positions.
     */
    public static boolean contains(BitSet set1, BitSet set2) {
        BitSet exclusiveIntersection = (BitSet)set1.clone();
        
        exclusiveIntersection.or(set2);
        exclusiveIntersection.xor(set1);
        
        return exclusiveIntersection.cardinality() == 0;
    }

    /**
     * Estimates the distance between two sets using the Jaccard coefficient.
     */
    public static double getJaccardCoefficient(BitSet set1, BitSet set2) {
        BitSet intersection = (BitSet)set1.clone();
        intersection.and(set2); // Intersection.

        BitSet union = (BitSet)set1.clone();
        union.or(set2); // Union.

        return intersection.cardinality() / 
            (double)union.cardinality();
    }
}