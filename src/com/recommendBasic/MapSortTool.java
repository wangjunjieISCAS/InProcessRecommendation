package com.recommendBasic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MapSortTool {
	public List<Map.Entry<String,Double>> sortHashMapStringDoubleDESC ( HashMap<String, Double> recScore) {
		List<Map.Entry<String,Double>> recList = new ArrayList<Map.Entry<String,Double>>(recScore.entrySet());
        Collections.sort(recList,new Comparator<Map.Entry<String,Double>>() {
            //Ωµ–Ú≈≈–Ú
            public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }            
        });
        
        return recList;
	}

	public List<Map.Entry<String,Double>> sortHashMapStringDoubleASC ( HashMap<String, Double> recScore) {
		List<Map.Entry<String,Double>> recList = new ArrayList<Map.Entry<String,Double>>(recScore.entrySet());
        Collections.sort(recList,new Comparator<Map.Entry<String,Double>>() {
            //…˝–Ú≈≈–Ú
            public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }            
        });
        
        return recList;
	}
	
	public List<Map.Entry<String,Integer>> sortHashMapStringIntegerASC ( HashMap<String, Integer> workerRank) {
		List<Map.Entry<String,Integer>> recList = new ArrayList<Map.Entry<String,Integer>>(workerRank.entrySet());
        Collections.sort(recList,new Comparator<Map.Entry<String,Integer>>() {
            //…˝–Ú≈≈–Ú
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }            
        });
        
        return recList;
	}
}
