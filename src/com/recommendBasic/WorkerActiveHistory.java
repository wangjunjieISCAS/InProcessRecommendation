package com.recommendBasic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import com.data.Constants;
import com.data.TestProject;
import com.data.TestReport;
import com.dataProcess.TestProjectReader;

public class WorkerActiveHistory {

	public WorkerActiveHistory() {
		// TODO Auto-generated constructor stub
	}
	
	//全部worker在该数据集上的所有的report提交时间；当在某个时间点进行推荐时，该时间点后面的活动自动忽略，只选取该时间点之前的活动
	public HashMap<String, HashMap<Date, ArrayList<String>>> retrieveWorkerActiveHistory ( ArrayList<TestProject> projectList ){
		HashMap<String, HashMap<Date, ArrayList<String>>> workerActiveHistory = new HashMap<String, HashMap<Date, ArrayList<String>>>();
		//projectID-tag, tag =-1 (no bug), or dupTag
		for ( int i =0; i < projectList.size(); i++ ){
			TestProject project = projectList.get( i );
			String projectName = project.getProjectName();
			String[] temp = projectName.split( "/");
			String[] temp2 = temp[temp.length-1].split("-");
			String projectId = temp2[0];
			
			ArrayList<TestReport> reportList = project.getTestReportsInProj();
			for ( int j =0; j < reportList.size(); j++ ){
				TestReport report = reportList.get( j );
				String workerId = report.getUserId();
				Date submitTime = report.getSubmitTime();
				String bugTag = report.getTag();
				String dupTag = report.getDuplicate();
				
				String info = projectId;
				if ( bugTag.equals("审核不通过")){
					info += "-" + "-1";
				}else{
					info += "-" + dupTag;
				}
				
				HashMap<Date, ArrayList<String>> activeHistory = new HashMap<Date, ArrayList<String>>();
				if ( workerActiveHistory.containsKey( workerId )){
					activeHistory = workerActiveHistory.get( workerId );
					ArrayList<String> history = new ArrayList<String>();
					if ( activeHistory.containsKey( submitTime )){
						history = activeHistory.get( submitTime );
						history.add( info );						
					}else{						
						history.add( info );
					}
					activeHistory.put( submitTime, history );
				}
				else{
					ArrayList<String> history = new ArrayList<String>();
					history.add( info);
					activeHistory.put( submitTime, history );
				}
				workerActiveHistory.put( workerId, activeHistory );
			}
		}
		
		return workerActiveHistory;
	}
	
	public void storeWorkerActiveHistory ( HashMap<String, HashMap<Date, ArrayList<String>>> workerActiveHistory, String fileName  ){  ////projectID-tag, tag =-1 (no bug), or dupTag
		try {
			BufferedWriter writer = new BufferedWriter ( new FileWriter ( new File ( fileName )));
			for ( String workerId : workerActiveHistory.keySet() ){
				HashMap<Date, ArrayList<String>> activeHistory = workerActiveHistory.get( workerId );
				writer.write( "worker: " + workerId);
				writer.newLine();
				for ( Date date : activeHistory.keySet() ){
					writer.write( Constants.dateFormat.format( date ) + ":=" ) ;
					ArrayList<String> history = activeHistory.get( date );
					writer.write( history.get( 0 ) );
					for ( int i =1; i < history.size(); i++ ){
						writer.write( "&&" + history.get( i ));
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
	
	public HashMap<String, HashMap<Date, ArrayList<String>>> readWorkerActiveHistory (String fileName ){
		HashMap<String, HashMap<Date, ArrayList<String>>> workerActiveHistory = new HashMap<String, HashMap<Date, ArrayList<String>>>();
		try {
			BufferedReader reader = new BufferedReader ( new FileReader ( new File ( fileName )));
			String line = null;
			HashMap<Date, ArrayList<String>> activeHistory = new HashMap<Date, ArrayList<String>>();
			String curWorker = "";
			while (  (line = reader.readLine()) != null ){				
				if ( line.startsWith("worker:") || line.startsWith( "END")) {
					if ( !curWorker.equals("")){
						workerActiveHistory.put( curWorker, activeHistory );
					}					
					activeHistory = new HashMap<Date, ArrayList<String>>();
					curWorker = line.replace("worker:", "").trim();
				}else{
					String[] temp = line.split( ":=");
					Date date = Constants.dateFormat.parse( temp[0]);
					String[] temp2 = temp[1].split("&&");
					ArrayList<String> history = new ArrayList<String>();
					for ( int i =0; i < temp2.length; i++ ){
						history.add( temp2[i].trim() );
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
		WorkerActiveHistory history = new WorkerActiveHistory();
		
		TestProjectReader projectReader = new TestProjectReader();
		ArrayList<TestProject> projectList = projectReader.loadTestProjectList( Constants.PROJECT_FOLDER );
		
		//HashMap<String, HashMap<Date, ArrayList<String>>> workerActiveHistory = history.retrieveWorkerActiveHistory(projectList);
		//history.storeWorkerActiveHistory(workerActiveHistory);
		
		HashMap<String, HashMap<Date, ArrayList<String>>> storedHistory = history.readWorkerActiveHistory( "data/output/history/active.txt" );
		HashMap<Date, ArrayList<String>> historyInfo = storedHistory.get( "14471438" );
		for ( Date date : historyInfo.keySet() ){
			System.out.println( Constants.dateFormat.format( date ) + " " + historyInfo.get(date).size() );
			ArrayList<String> info = historyInfo.get( date );
			for ( int i =0; i < info.size(); i++ )
				System.out.println( info.get(i).toString() + " ");
		}
			
	}
}
