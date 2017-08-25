package MDLPA.helpers;

import java.util.*;

/**
 * A helper utility for hash tables manipulation.
 * 
 * @author Oualid Boutemine <boutemine.oualid@courrier.uqam.ca>
 * 
 * [1] Boutemine, O., & Bouguessa, M. (2017). Mining Community Structures in Multidimensional Networks. ACM Transactions on Knowledge Discovery from Data (TKDD), 11(4), 51. 
 */
public class MapUtils
{
    public static <TK, TV> TK getKeyByValue(
        Map<TK, TV> map,
        TV value
    ) {
        for (Map.Entry<TK, TV> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        
        return null;
    }
        
    public static <TK, TV extends Comparable<? super TV>> Map<TK,TV> shuffle(
        Map<TK, TV> map,
        Random randomizer
    ) {
        List<Map.Entry<TK, TV>> list =
            new LinkedList<Map.Entry<TK, TV>>(map.entrySet());
        
        Collections.shuffle(list, randomizer);
        
        Map<TK, TV> result = new LinkedHashMap<TK, TV>();
        for (Map.Entry<TK, TV> entry : list)
        {
            result.put(entry.getKey(), entry.getValue());
        }
        
        return result;        
    }
}