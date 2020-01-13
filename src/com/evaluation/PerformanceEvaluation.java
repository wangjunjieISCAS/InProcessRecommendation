package com.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.data.TestProject;
import com.data.TestReport;
import com.recommendBasic.MapSortTool;

public class PerformanceEvaluation {	
	public LinkedHashMap<Integer, Double> bugDetectionRatePredicted ( LinkedHashMap<String, Double> rankedWorkersList, TestProject project, Integer recTimePoint ){
		LinkedHashMap<Integer, Double> bugDetectRate = new LinkedHashMap<Integer, Double>();    //<number of workers, number of detected non-duplicate bugs>
		
		HashMap<String, ArrayList<String>> bugDetectForWorkers = new HashMap<String, ArrayList<String>>();    //<workerId, duplicate tags (non-duplicate bug tags)>  only store workers who find bugs 
		ArrayList<TestReport> reportList = project.getTestReportsInProj();
		for ( int i = recTimePoint+1; i < reportList.size(); i++ ){
			TestReport report = reportList.get( i );
			String workerId = report.getUserId();
			String bugTag = report.getTag();
			String dupTag = report.getDuplicate();

			String tag = "0-" + dupTag;
			if ( bugTag.equals("审核通过"))
				tag = "1-" + dupTag;
			
			ArrayList<String> bugList = new ArrayList<String>();
			if ( bugDetectForWorkers.containsKey( workerId ) ){
				bugList = bugDetectForWorkers.get( workerId );
			}
			bugList.add( tag );
			bugDetectForWorkers.put( workerId, bugList );
		}
		
		HashSet<String> nonDupBugs = new HashSet<String>();
		for ( int i =0; i <= recTimePoint; i++ ){
			TestReport report = reportList.get( i );
			if ( report.getTag().equals("审核通过")) 
				nonDupBugs.add( report.getDuplicate() );
		}
		int workerNum = 0, detectedBugs = nonDupBugs.size();
		for ( String workerId : rankedWorkersList.keySet()	){
			if ( bugDetectForWorkers.containsKey( workerId )){
				ArrayList<String> bugs = bugDetectForWorkers.get( workerId );
				for ( int i =0; i < bugs.size(); i++ ){
					String[] temp = bugs.get(i).split("-");
					if ( temp[0].equals("1"))
						nonDupBugs.add( temp[1] );
					
					workerNum ++;
					bugDetectRate.put( workerNum, 1.0*(nonDupBugs.size()-detectedBugs) );
				}
			}			
		}
		
		nonDupBugs.clear();
		for ( int i =0; i <= recTimePoint; i++ ){
			TestReport report = reportList.get( i );
			if ( report.getTag().equals("审核通过")) 
				nonDupBugs.add( report.getDuplicate() );
		}
		Double remainBugs = 0.0;
		for ( int i = recTimePoint+1; i < reportList.size(); i++ ){
			String bugTag = reportList.get(i).getTag();
			String dupTag = reportList.get(i).getDuplicate();
			
			if ( bugTag.equals("审核通过") ){
				nonDupBugs.add( dupTag );
			}
			remainBugs = 1.0*(nonDupBugs.size()-detectedBugs);
		}			
		
		for ( Integer key : bugDetectRate.keySet() ){
			bugDetectRate.put( key, bugDetectRate.get(key)/remainBugs );
		}
		return bugDetectRate;
	}
	
	public LinkedHashMap<Integer, Double> bugDetectionRateGroundTruth ( TestProject project, Integer recTimePoint ){
		LinkedHashMap<Integer, Double> bugDetectRate = new LinkedHashMap<Integer, Double>();    //<number of workers, number of detected non-duplicate bugs>
		
		ArrayList<TestReport> reportList = project.getTestReportsInProj();
		
		HashSet<String> nonDupBugs = new HashSet<String>();
		for ( int i =0; i <= recTimePoint; i++ ){
			TestReport report = reportList.get( i );
			if ( report.getTag().equals("审核通过"))   //
				nonDupBugs.add( report.getDuplicate() );
		}
		
		int detectedBugs = nonDupBugs.size(), workerNum = 0;
		Double remainBugs = 0.0;
		for ( int i = recTimePoint+1; i < reportList.size(); i++ ){
			String bugTag = reportList.get(i).getTag();
			String dupTag = reportList.get(i).getDuplicate();
			
			if ( bugTag.equals("审核通过") ){
				nonDupBugs.add( dupTag );
			}
			workerNum++;
			bugDetectRate.put( workerNum, 1.0*(nonDupBugs.size()-detectedBugs) );
			remainBugs = 1.0*(nonDupBugs.size()-detectedBugs);
		}		
		
		for ( Integer key : bugDetectRate.keySet() ){
			bugDetectRate.put( key, bugDetectRate.get(key)/remainBugs );
		}
		
		return bugDetectRate ;
	}
}
