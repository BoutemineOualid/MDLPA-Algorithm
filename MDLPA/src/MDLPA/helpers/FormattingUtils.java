package MDLPA.helpers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A helper utility for string formatting.
 * 
 * @author Oualid Boutemine <boutemine.oualid@courrier.uqam.ca>
 * 
 * [1] Boutemine, O., & Bouguessa, M. (2017). Mining Community Structures in Multidimensional Networks. ACM Transactions on Knowledge Discovery from Data (TKDD), 11(4), 51. 
 */
public class FormattingUtils {
    /**
     * Returns a comma-separated string representation of the elements of a list.
     */
    public static String getCommaSeperatedRepresentation(List list) {
        return getCommaSeperatedRepresentation(new HashSet(list));
    }
    
    /**
     * Returns a comma-separated string representation of the elements of an array.
     */
    public static String getCommaSeperatedRepresentation(Object value[]) {
        return getCommaSeperatedRepresentation(Arrays.asList(value));
    }
    
    /**
     * Returns a comma-separated string representation of the elements of a set.
     */
    public static String getCommaSeperatedRepresentation(Set set) {
        if (set == null || set.isEmpty())
            return "";
        
        StringBuilder builder = new StringBuilder();
        
        for(Object element : set)
            builder
                .append(element.toString())
                .append(",");
        
        String buffer = builder.toString();

        if (buffer == "")
            return buffer;
        
        // trim right the last comma.
        return buffer.substring(
            0,
            buffer.length() - 1
        );
    }    
}
