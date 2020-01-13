package com.recommendRank;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.data.TestProject;
import com.recommendBasic.RecContextModeling;

public class ProbabilisticExpertise {
	
	//P(wi|tj)=P(wi,tj)/P(tj) * adjust factor (sum_df(wk)/df(wi))
	public HashMap<String, HashMap<String, Double>> obtainWorkerExpertiseForTermWithAdjustfactor ( TestProject project, ArrayList<String> taskDescription, 
			HashMap<String, HashMap<Date, ArrayList<List<String>>>> curExpertiseList ){
		
		HashMap<String, HashMap<String, Double>> workerContribForTermList = this.obtainWorkerExpertiseForTerm(curExpertiseList, taskDescription);
		HashMap<String, Double> workerAdjustList = this.obtainWorkerExpertiseAdjustfactor( curExpertiseList );
		
		HashMap<String, HashMap<String, Double>> workerContribForTermListWithAdjust = new HashMap<String, HashMap<String, Double>>();
		for ( String workerId : workerContribForTermList.keySet() ){
			HashMap<String, Double> contribList = workerContribForTermList.get( workerId );
			HashMap<String, Double> contribListWithAdjust = new HashMap<String, Double>();
			
			Double adjust = 1.0;
			if ( workerAdjustList.containsKey( workerId )){
				adjust = workerAdjustList.get( workerId ); 
			}
			for ( String term: contribList.keySet() ){
				Double contrib = contribList.get( term );
				contrib = contrib * adjust;
				contribListWithAdjust.put( term, contrib );
			}
			
			workerContribForTermListWithAdjust.put( workerId, contribListWithAdjust );
		}
		return workerContribForTermListWithAdjust;
	}
	
	//P(wi|tj)=P(wi,tj)/P(tj), tj出现的情况下是worker wi的概率
	public HashMap<String, HashMap<String, Double>> obtainWorkerExpertiseForTerm ( HashMap<String, HashMap<Date, ArrayList<List<String>>>> curExpertiseList, ArrayList<String> taskDescription ){
		//P(tj)
		HashMap<String, Integer> bugCountForTermList = new HashMap<String, Integer>();
		for ( int i =0; i < taskDescription.size(); i++ ){
			String term = taskDescription.get( i );
			int bugReportCount = 0;
			for ( String workerId: curExpertiseList.keySet() ){
				HashMap<Date, ArrayList<List<String>>> expertiseList = curExpertiseList.get( workerId );
				for ( Date date : expertiseList.keySet() ){
					ArrayList<List<String>> expertise = expertiseList.get( date );
					for ( int j =0; j < expertise.size(); j++ ){
						List<String> terms = expertise.get(j );
						if ( terms.contains( term )){
							bugReportCount ++;
						}
					}
				}
			}
			bugCountForTermList.put( term, bugReportCount );
		}
		
		//<workerId, <term, P(wi|tj)>>
		HashMap<String, HashMap<String, Double>> workerContribForTermList = new HashMap<String, HashMap<String, Double>>();
		for ( int i =0; i < taskDescription.size(); i++ ){
			String term = taskDescription.get( i);
			
			for ( String workerId: curExpertiseList.keySet() ){
				int bugReportCountThisTerm = 0;
				
				HashMap<Date, ArrayList<List<String>>> expertiseList = curExpertiseList.get( workerId );
				for ( Date date : expertiseList.keySet() ){
					ArrayList<List<String>> expertise = expertiseList.get( date );
					for ( int j =0; j < expertise.size(); j++ ){
						List<String> terms = expertise.get(j );
						if ( terms.contains( term )){
							bugReportCountThisTerm++;
						}
					}
				}
				double contrib = 0.0;
				if ( bugCountForTermList.containsKey( term ) && bugCountForTermList.get( term) != 0 ){
					contrib = (1.0*bugReportCountThisTerm) / bugCountForTermList.get( term );
				}
				
				HashMap<String, Double> contribList = new HashMap<String, Double>();
				if ( workerContribForTermList.containsKey( workerId )){
					contribList = workerContribForTermList.get( workerId );
				}
				contribList.put( term, contrib );
				workerContribForTermList.put( workerId, contribList );
			}
		}
		
		return workerContribForTermList;
	}
	
	//P(wi|tj)值太小，用adjustfactor修正下 sum_df(wk)/df(wj); <workerId, adjust factor>
	//用的是当前context下的workerExpertise，不需要再判断时间了
	public HashMap<String, Double> obtainWorkerExpertiseAdjustfactor ( HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerExpertiseHistory ){
		HashMap<String, Integer> workerBugCountList = new HashMap<String, Integer>();
		int totalBugCount = 0;
		for ( String workerId : workerExpertiseHistory.keySet() ){
			int workerBugCount = 0;
			HashMap<Date, ArrayList<List<String>>> expHistory = workerExpertiseHistory.get( workerId );
			for ( Date date : expHistory.keySet() ){
				ArrayList<List<String>> history = expHistory.get( date );
				workerBugCount += history.size();
			}
			workerBugCountList.put( workerId, workerBugCount );
			totalBugCount += workerBugCount;
		}
		
		HashMap<String, Double> workerAdjustList = new HashMap<String, Double>();
		for ( String workerId : workerBugCountList.keySet() ){
			Integer bugCount = workerBugCountList.get( workerId );
			Double factor = (1.0*totalBugCount) / 0.1;
			if ( bugCount != 0 ) 
				factor = (1.0*totalBugCount) / bugCount;
			workerAdjustList.put( workerId, factor );
		}
		return workerAdjustList;
	}
	
}
