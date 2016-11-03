package util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MyJavaUtil {
	
	//這個sort 使用完後建議在新建一個LinkedHashMap ,用putAll 的方式把排序結果放進去,不然有時再用get 會出問題
	/**
	 * Descending order HashMap by value
	 * @param map : HashMap<String, Double>
	 */
	public static Map<String, Double> sortByComparatorDouble(final Map<String, Double> map) {
    	List<Entry<String,Double>> map_arr = new LinkedList<Entry<String,Double>>( map.entrySet() );
        
        Collections.sort( map_arr , new Comparator<Entry<String, Double>>() {
            public int compare(Entry<String,Double> v1 , Entry<String,Double> v2 )
            {
                return v2.getValue().compareTo( v1.getValue() );//descending
            }
        });
        
        LinkedHashMap<String,Double> sortedByComparator = new LinkedHashMap<String,Double>();
        for(Entry<String,Double> e : map_arr)
        {
        	sortedByComparator.put(e.getKey() , e.getValue() );
        }
	    return sortedByComparator;
	}
	
	//這個sort 使用完後建議在新建一個LinkedHashMap ,用putAll 的方式把排序結果放進去,不然有時再用get 會出問題
	/**
	 * Descending order HashMap by value
	 * @param map : HashMap<String, Integer>
	 */
	public static Map<String, Integer> sortByComparatorInt(final Map<String, Integer> map) {
    	List<Entry<String,Integer>> map_arr = new LinkedList<Entry<String,Integer>>( map.entrySet() );
        
        Collections.sort( map_arr , new Comparator<Entry<String, Integer>>() {
            public int compare(Entry<String,Integer> v1 , Entry<String,Integer> v2 )
            {
                return v2.getValue().compareTo( v1.getValue() );//descending
            }
        });
        
        LinkedHashMap<String,Integer> sortedByComparator = new LinkedHashMap<String,Integer>();
        for(Entry<String,Integer> e : map_arr)
        {
        	sortedByComparator.put(e.getKey() , e.getValue() );
        }
	    return sortedByComparator;
	}
}
