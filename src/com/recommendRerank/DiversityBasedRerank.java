package com.recommendRerank;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.recommendBasic.MapSortTool;

public class DiversityBasedRerank {
	//在后续评价某个worker的diversity时，用该人员和之前某个人员进行比较，如果最大的相似性大于某个值，则移到后面
	public LinkedHashMap<String, String> reRankRecWorkersByWorkerDiversity ( LinkedHashMap<String, Double> rankedWorkersList , Date curTime, 
			HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerExpertiseHistory, Double termThres, 
			LinkedHashMap<String, ArrayList<Double>> workersFeaturesList , HashSet<String> workerPerfTask ){
		HashMap<String, HashMap<String, Integer>> workerExpertiseList = this.retrieveRefinedExpertise(rankedWorkersList, curTime, workerExpertiseHistory);
		
		LinkedHashMap<String, Double> remainWorkersList = new LinkedHashMap<String, Double>();
		LinkedHashMap<String, Double> newRankedWorkersList = new LinkedHashMap<String, Double>();
		ArrayList<String> addedWorkersList = new ArrayList<String>();
		int index = 0;
		for ( String workerId : rankedWorkersList.keySet() ){
			if ( workerPerfTask.contains( workerId)){
				addedWorkersList.add( workerId);
				continue;
			}
			
			if ( index == 0  ){
				remainWorkersList.put(workerId, 0.0 );
				index++;
			}
			
			//contribution terms
			HashMap<String, Integer> expertise = null;
			if ( workerExpertiseList.containsKey( workerId ) ){
				expertise = workerExpertiseList.get( workerId );
			}
			
			Double maxSim = 0.0;
			for ( String exWorker : remainWorkersList.keySet() ){
				HashMap<String, Integer> exExpertise = null;
				if ( workerExpertiseList.containsKey( exWorker ) ){
					exExpertise = workerExpertiseList.get( exWorker );
				}
				if ( expertise == null || exExpertise == null ){
					continue;
				}
				int sameTerms = 0;
				for ( String term : expertise.keySet() ){
					if ( exExpertise.containsKey( term )){
						sameTerms ++;
					}						
				}
				
				int refSize = expertise.size();
				if ( expertise.size() > exExpertise.size() )
					refSize = exExpertise.size();
				Double ratio = (1.0*sameTerms) / refSize;
				//System.out.println( sameTerms + "  " + expertise.size() + "  " + exExpertise.size() + "  " + ratio );
				if ( ratio > maxSim )
					maxSim = ratio;
			}
			
			if ( maxSim >= termThres ){
				remainWorkersList.put( workerId, maxSim);
			}else{
				newRankedWorkersList.put( workerId, maxSim );
			}
		}
		
		for ( int i =0; i < addedWorkersList.size(); i++ ){
			newRankedWorkersList.put( addedWorkersList.get(i), -1.0);
		}
		LinkedHashMap<String, String> reRankedWorkersList = new LinkedHashMap<String, String>();
		for ( String workerId : remainWorkersList.keySet() ){
			reRankedWorkersList.put( workerId, rankedWorkersList.get(workerId )+ "  " + "remain" + "  " + remainWorkersList.get(workerId) + "  " + workersFeaturesList.get( workerId).get(0) );
		}
		for ( String workerId : newRankedWorkersList.keySet() ){
			reRankedWorkersList.put( workerId, rankedWorkersList.get(workerId) + "  " + "newRanked" + "  " + newRankedWorkersList.get(workerId) + "  " + workersFeaturesList.get( workerId).get(0) );
		}
		
		return reRankedWorkersList;
	}
	
	
	public HashMap<String, HashMap<String, Integer>> retrieveRefinedExpertise ( LinkedHashMap<String, Double> rankedWorkersList , Date curTime, HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerExpertiseHistory){
		HashMap<String, HashMap<String, Integer>> workerExpertiseList = new HashMap<String, HashMap<String, Integer>>();  //只是第一批次过滤完的worker
		for ( String workerId : workerExpertiseHistory.keySet() ){
			if ( !rankedWorkersList.containsKey( workerId ))
				continue;
			HashMap<Date, ArrayList<List<String>>> expertiseHistory = workerExpertiseHistory.get( workerId );
			
			HashMap<String, Integer> expertiseList = new HashMap<String, Integer>();
			for ( Date time : expertiseHistory.keySet() ){
				if ( time.getTime() <= curTime.getTime() ){
					ArrayList<List<String>> termLists = expertiseHistory.get( time );
					for ( int i =0; i < termLists.size(); i++ ){
						for ( int j =0; j < termLists.get(i).size(); j++ ){
							int count = 1;
							String term = termLists.get(i).get(j);
							if ( expertiseList.containsKey( term )){
								count += expertiseList.get(term);
							}
							expertiseList.put( term, count );
						}
					}
				}
			}
			workerExpertiseList.put( workerId, expertiseList );
		}
		return workerExpertiseList;
	}
}
