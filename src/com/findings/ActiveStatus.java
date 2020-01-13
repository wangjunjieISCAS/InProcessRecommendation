package com.findings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import com.data.Constants;
import com.data.TestProject;
import com.dataProcess.TestProjectReader;
import com.recommendBasic.WorkerActiveHistory;


public class ActiveStatus {
	public void activeWithinTimeIntervalCounter ( String projectFolder  ){
		TestProjectReader projReader = new TestProjectReader();
		ArrayList<TestProject> projList = projReader.loadTestProjectList( projectFolder );
		
		Date[] sepDateList = new Date[20];
		try {
			Date dateFirst = Constants.dateFormat.parse( "2015-11-01 00:00:00");
			Date dateLast = Constants.dateFormat.parse( "2016-07-01 00:00:00");
			
			float duration = (int) ((dateLast.getTime() - dateFirst.getTime())/(24*60 * 60*1000));
			Integer durDays = (int) (duration / 20);
			System.out.println ( durDays );
			
			for ( int i = 0; i < sepDateList.length; i++ ){
				Long sepValue = dateFirst.getTime() + (durDays)*(i+1) * 24*60*60*1000L;
				Date sepDate = new Date ( sepValue );
				sepDateList[i] = sepDate;
			}			
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		WorkerActiveHistory activeHistory = new WorkerActiveHistory();
		HashMap<String, HashMap<Date, ArrayList<String>>> workerActiveHistory = activeHistory.retrieveWorkerActiveHistory(projList);
		
		ArrayList<String> workerList = this.obtainWorkers();
		//Random rand = new Random( 12345);
		int pickNum = 20;
		ArrayList<String> pickWorkerList = new ArrayList<String>();
		for ( int i =0; i < pickNum; i++ ){
			int pick = workerList.size()-i-1;
			pickWorkerList.add( workerList.get( pick) );			
		}	
		
		HashMap<String, Integer[]> workerActiveOccur = new HashMap<String, Integer[]>();
		for ( int i =0; i < pickWorkerList.size(); i++){
			HashMap<Date, ArrayList<String>> workerAct = workerActiveHistory.get( pickWorkerList.get(i));
			Integer[] actInfo = new Integer[20];
			for ( int j=0; j < actInfo.length; j++ ){
				actInfo[j] = 1;
			}
			for ( Date date : workerAct.keySet() ){
				HashSet<String> projNum = new HashSet<String>();
				ArrayList<String> actList = workerAct.get( date );
				for ( int j =0; j < actList.size(); j++ ){
					String[] temp = actList.get(j).split("-");
					projNum.add( temp[0]);
				}
				
				int actNum = projNum.size();
				int index = -1;
				//那个格子里面
				for ( int j =0; j < sepDateList.length; j++ ){
					Date insDate = sepDateList[j];
					if ( insDate.after( date )){
						index = j;
						break;
					}
				}
				if ( index != -1 ){
					actInfo[index] += actNum ;
				}
			}
			workerActiveOccur.put( pickWorkerList.get(i), actInfo );
		}
		
		try {
			BufferedWriter writer = new BufferedWriter( new FileWriter ( "data/output/findings/active.csv" ));
			
			for ( int i =0; i < 20; i++ ){
				for ( String worker : workerActiveOccur.keySet() ){
					Integer[] actOccur = workerActiveOccur.get( worker );
					writer.write( Math.log( actOccur[i]) + ",");
				}
				writer.newLine();
			}
			/*
			for ( String worker : workerActiveOccur.keySet() ) {
				Integer[] actOccur = workerActiveOccur.get( worker );
				for ( int i = 0; i < actOccur.length; i++ ){   
					writer.write( Math.log(actOccur[i]) +",");
				}
				writer.newLine();
			}*/
			writer.flush();
			writer.close();
			
			writer.close();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	}
	
	
	public ArrayList<String> obtainWorkers ( ){
		ArrayList<String> workerList = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader ( new FileReader ( new File ( "data/output/findings/workers.csv" ) ));
			String line = "";
			while ( (line = reader.readLine()) != null ){
				workerList.add( line.trim() );				
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return workerList;
	}
	
	public static void main ( String[] args ){
		ActiveStatus activeTool = new ActiveStatus ();
		activeTool.activeWithinTimeIntervalCounter( Constants.PROJECT_FOLDER );
	}
}
