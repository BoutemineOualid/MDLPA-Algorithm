package MDLPA.helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A helper utility to simplify iterator operations.
 * 
 * @author Oualid Boutemine <boutemine.oualid@courrier.uqam.ca>
 * 
 * [1] Boutemine, O., & Bouguessa, M. (2017). Mining Community Structures in Multidimensional Networks. ACM Transactions on Knowledge Discovery from Data (TKDD), 11(4), 51. 
 */
public class IteratorUtils {
    /**
     * Returns a list of elements from an iterator.
     */
    public static <T> List<T> toList(Iterator<T> iterator) {
        List<T> result = new ArrayList<T>();
        
        while(iterator.hasNext()) {
            result.add(iterator.next());
        }
        
        return result;
    }
    
    /**
     * Returns a set of elements from an iterator.
     */
    public static <T> Set<T> toSet(Iterator<T> iterator) {
        Set<T> result = new HashSet<T>();
        
        while(iterator.hasNext()) {
            result.add(iterator.next());
        }
        
        return result;
    }
}
