package jp.ac.tokushima_u.is.ll.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides static methods for creating {@code List} instances easily, and other
 * utility methods for working with lists. 
 */
public class Lists {

    /**
     * Creates an empty {@code ArrayList} instance.
     *
     * <p><b>Note:</b> if you only need an <i>immutable</i> empty List, use
     * {@link Collections#emptyList} instead.
     *
     * @return a newly-created, initially-empty {@code ArrayList}
     */
    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<E>();
    }

    /**
     * Creates a resizable {@code ArrayList} instance containing the given
     * elements.
     *
     * <p><b>Note:</b> due to a bug in javac 1.5.0_06, we cannot support the
     * following:
     *
     * <p>{@code List<Base> list = Lists.newArrayList(sub1, sub2);}
     *
     * <p>where {@code sub1} and {@code sub2} are references to subtypes of
     * {@code Base}, not of {@code Base} itself. To get around this, you must
     * use:
     *
     * <p>{@code List<Base> list = Lists.<Base>newArrayList(sub1, sub2);}
     *
     * @param elements the elements that the list should contain, in order
     * @return a newly-created {@code ArrayList} containing those elements
     */
    public static <E> ArrayList<E> newArrayList(E... elements) {
        int capacity = (elements.length * 110) / 100 + 5;
        ArrayList<E> list = new ArrayList<E>(capacity);
        Collections.addAll(list, elements);
        return list;
    }
 
    
    public static List<String> stringToArray(String value){
    	String[]result = value.split(",");
    	List<String> array = new ArrayList<String>();
    	for(int i=0;i<result.length;i++)
    		array.add(result[i]);
    	return array;
    }
    
    public static List<String> stringToArray(String value, String sep){
    	String[]result = value.split(sep);
    	List<String> array = new ArrayList<String>();
    	for(int i=0;i<result.length;i++)
    		array.add(result[i]);
    	return array;
    }
    
    public static String arrayToString(List<String> arrays){
    	return arrayToString(arrays, null);
    }
    
    public static String arrayToString(List<String> arrays, String sep){
    	   String result = "";
    	   if(sep==null||sep.length()==0)
    		   sep = ",";
           for(String v:arrays){
           	if(result.length()>0)
           		result = result + sep;
           	result = result + v;
           }
           return result;
    }
    
}
