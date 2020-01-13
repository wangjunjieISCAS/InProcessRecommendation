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

public class WorkerExpertiseHistory {
	
	//全部worker在该数据集上的所有bug report的提交时间以及内容；当在某个时间点进行推荐时，该时间点后面的活动自动忽略，只选取该时间点之前的活动作为该人员的经验
	public HashMap<String, HashMap<Date, ArrayList<List<String>>>> retrieveWorkerExpertiseHistory ( ArrayList<TestProject> projectList ){
		//only store the bug report
		//HashMap<String, HashMap<Date, ArrayList<String>>> workerExpertiseHistory = new HashMap<String, HashMap<Date, ArrayList<String>>>();
		//this is not right, because for a specific time, a worker can submit several bug report; this data structure should distinguish different reports
		HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerExpertiseHistory = new HashMap<String, HashMap<Date, ArrayList<List<String>>>>();
		//<worker, <Date, >>, List<String> is the terms from one report
		ReportSegment segTool = new ReportSegment();
		
		for ( int i =0; i < projectList.size(); i++ ){
			TestProject project = projectList.get( i );
			
			ArrayList<TestReport> reportList = project.getTestReportsInProj();
			for ( int j =0; j < reportList.size(); j++ ){
				TestReport report = reportList.get( j );
				String bugTag = report.getTag();
				if ( bugTag.equals( "审核不通过"))
					continue;
				
				String workerId = report.getUserId();
				Date submitTime = report.getSubmitTime();
				
				String[] termArray = segTool.segmentTestReport( report );
				List<String> termList = (List<String>) Arrays.asList( termArray );
				
				if ( workerExpertiseHistory.containsKey( workerId )){
					HashMap<Date, ArrayList<List<String>>> history = workerExpertiseHistory.get( workerId );
					if ( history.containsKey(submitTime )){
						ArrayList<List<String>> reportsList = history.get( submitTime );
						reportsList.add( termList );
						history.put( submitTime, reportsList);
					}else{
						ArrayList<List<String>> reportsList = new ArrayList<List<String>>();
						reportsList.add( termList );
						history.put( submitTime, reportsList);
					}
					workerExpertiseHistory.put( workerId, history );
				}
				else{
					HashMap<Date, ArrayList<List<String>>> history = new HashMap<Date, ArrayList<List<String>>>();
					ArrayList<List<String>> reportsList = new ArrayList<List<String>>();
					reportsList.add( termList );
					history.put( submitTime, reportsList);
					
					workerExpertiseHistory.put( workerId, history );
				}
			}
		}
		return workerExpertiseHistory;
	}
	
	public void storeWorkerExpertiseHistory ( HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerExpertiseHistory, String fileName ){ 
		try {
			BufferedWriter writer = new BufferedWriter ( new FileWriter ( new File ( fileName )));
			for ( String workerId : workerExpertiseHistory.keySet() ){
				HashMap<Date, ArrayList<List<String>>> expertiseHistory = workerExpertiseHistory.get( workerId );
				writer.write( "worker: " + workerId);
				writer.newLine();
				for ( Date date : expertiseHistory.keySet() ){
					writer.write( Constants.dateFormat.format( date ) + ":=" ) ;
					ArrayList<List<String>> history = expertiseHistory.get( date );
					for ( int i =0; i < history.get(0).size(); i++ ){
						writer.write( history.get(0).get(i) + " ");
					}
					for ( int i =1; i < history.size(); i++ ){
						writer.write( "&&");
						List<String> info = history.get(i);
						for ( int j =0; j < info.size(); j++)	{					
							writer.write( history.get(i).get(j) + " ");
						}							
					}
					writer.newLine();
				}
			}
			writer.write( "END");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public HashMap<String, HashMap<Date, ArrayList<List<String>>>> readWorkerExpertiseHistory ( String fileName ){
		HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerActiveHistory = new HashMap<String, HashMap<Date, ArrayList<List<String>>>>();
		try {
			BufferedReader reader = new BufferedReader ( new FileReader ( new File ( fileName )));
			String line = null;
			HashMap<Date, ArrayList<List<String>>> activeHistory = new HashMap<Date, ArrayList<List<String>>>();
			String curWorker = "";
			while (  (line = reader.readLine()) != null ){				
				if ( line.startsWith("worker:") || line.startsWith( "END")) {
					if ( !curWorker.equals("")){
						workerActiveHistory.put( curWorker, activeHistory );
					}					
					activeHistory = new HashMap<Date, ArrayList<List<String>>>();
					curWorker = line.replace("worker:", "").trim();
				}else{
					String[] temp = line.split( ":=");
					Date date = Constants.dateFormat.parse( temp[0]);
					ArrayList<List<String>> history = new ArrayList<List<String>>();
					if ( temp.length > 1){
						String[] temp2 = temp[1].split("&&");
						for ( int i =0; i < temp2.length; i++ ){
							String[] temp3 = temp2[i].split(" ");
							List<String> info = new ArrayList<String>();
							for ( int j =0; j < temp3.length; j++ )
								info.add( temp3[j] );
							history.add( info);
						}	
					}									
					activeHistory.put( date, history );
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return workerActiveHistory;
	}
	
	
	
	
	
	public static void main ( String args[] ){
		WorkerExpertiseHistory history = new WorkerExpertiseHistory();
		/*
		TestProjectReader projectReader = new TestProjectReader();
		ArrayList<TestProject> projectList = projectReader.loadTestProjectList( Constants.PROJECT_FOLDER );
 		HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerExpertiseHistory = history.retrieveWorkerExpertiseHistory(projectList );
		history.storeWorkerExpertiseHistory(workerExpertiseHistory);
		*/
		HashMap<String, HashMap<Date, ArrayList<List<String>>>> storedHistory = history.readWorkerExpertiseHistory( "data/output/history/expertise.txt");
		
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
