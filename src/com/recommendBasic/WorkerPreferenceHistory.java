package com.recommendBasic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.data.Constants;
import com.data.TestProject;
import com.data.TestReport;
import com.dataProcess.ReportSegment;
import com.dataProcess.TestProjectReader;

public class WorkerPreferenceHistory {
	
	//全部worker在该数据集上的所有report的提交时间以及内容；当在某个时间点进行推荐时，该时间点后面的活动自动忽略，只选取该时间点之前的活动作为该人员的经验
	//该类和WorkerExpertiseHistory的区别在于，该类是对于所有的report进行统计，expertise是对于所有的bug进行统计
	public HashMap<String, HashMap<Date, ArrayList<List<String>>>> retrieveWorkerPreferenceHistory ( ArrayList<TestProject> projectList ){
		HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerPreferenceHistory = new HashMap<String, HashMap<Date, ArrayList<List<String>>>>();
		//<worker, <Date, >>, List<String> is the terms from one report
		ReportSegment segTool = new ReportSegment();
		
		for ( int i =0; i < projectList.size(); i++ ){
			TestProject project = projectList.get( i );
			
			ArrayList<TestReport> reportList = project.getTestReportsInProj();
			for ( int j =0; j < reportList.size(); j++ ){
				TestReport report = reportList.get( j );
				
				String workerId = report.getUserId();
				Date submitTime = report.getSubmitTime();
				
				String[] termArray = segTool.segmentTestReport( report );
				List<String> termList = (List<String>) Arrays.asList( termArray );
				
				if ( workerPreferenceHistory.containsKey( workerId )){
					HashMap<Date, ArrayList<List<String>>> history = workerPreferenceHistory.get( workerId );
					if ( history.containsKey(submitTime )){
						ArrayList<List<String>> reportsList = history.get( submitTime );
						reportsList.add( termList );
						history.put( submitTime, reportsList);
					}else{
						ArrayList<List<String>> reportsList = new ArrayList<List<String>>();
						reportsList.add( termList );
						history.put( submitTime, reportsList);
					}
					workerPreferenceHistory.put( workerId, history );
				}
				else{
					HashMap<Date, ArrayList<List<String>>> history = new HashMap<Date, ArrayList<List<String>>>();
					ArrayList<List<String>> reportsList = new ArrayList<List<String>>();
					reportsList.add( termList );
					history.put( submitTime, reportsList);
					
					workerPreferenceHistory.put( workerId, history );
				}
			}
		}
		return workerPreferenceHistory;
	}
	
	public void storeWorkerExpertiseHistory ( HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerExpertiseHistory, String fileName ){ 
		WorkerExpertiseHistory historyTool = new WorkerExpertiseHistory ();
		historyTool.storeWorkerExpertiseHistory(workerExpertiseHistory, fileName);
	}
	
	public HashMap<String, HashMap<Date, ArrayList<List<String>>>> readWorkerExpertiseHistory ( String fileName ){
		WorkerExpertiseHistory historyTool = new WorkerExpertiseHistory ();
		return historyTool.readWorkerExpertiseHistory(fileName);
	}
	
	public static void main ( String args[] ){
		WorkerPreferenceHistory history = new WorkerPreferenceHistory();
		
		TestProjectReader projectReader = new TestProjectReader();
		ArrayList<TestProject> projectList = projectReader.loadTestProjectList( Constants.PROJECT_FOLDER );
 		HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerPreferenceHistory = history.retrieveWorkerPreferenceHistory(projectList); 
		
 		history.storeWorkerExpertiseHistory(workerPreferenceHistory, "data/output/history/preference.txt" );
		HashMap<String, HashMap<Date, ArrayList<List<String>>>> storedHistory = history.readWorkerExpertiseHistory( "data/output/history/preference.txt");
		
		HashMap<Date, ArrayList<List<String>>> historyInfo = storedHistory.get( "14471438" );
		for ( Date date : historyInfo.keySet() ){
			System.out.println( Constants.dateFormat.format( date ) + " " + historyInfo.get(date).size() );
			ArrayList<List<String>> info = historyInfo.get( date );
			for ( int i =0; i < info.size(); i++ ){
				for ( int j =0; j < info.get(i).size(); j++ ){
					System.out.print( info.get(i).get(j) + "*");
				}
			}
			System.out.println ();					
		}
	}
}
