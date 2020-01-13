package com.findings;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.data.Constants;
import com.data.TestProject;
import com.data.TestReport;
import com.dataProcess.TestProjectReader;

public class BugSubmitInterval {
	//统计两个缺陷之间的时间间隔
	public void bugSubmitIntervalCounter ( String projectFolder ) {
		TestProjectReader projReader = new TestProjectReader();
		ArrayList<TestProject> projList = projReader.loadTestProjectList( projectFolder );
		
		//Integer[] sepBeginList = { 2, 4, 6, 10, 15, 20, 30, 50, 150};
		Integer[] sepBeginList = { 1, 2, 4, 6, 10, 15, 20, 30, 50, 100};
		Double[] processSepList = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0 };
		HashMap<Integer, Integer> interNumList = new HashMap<Integer, Integer>(); //<interval, number belong to this interval>
		HashMap<String, Integer[]> interNumDetailList = new HashMap<String, Integer[]>();
		HashMap<String, Integer> xSizedInterNumList = new HashMap<String, Integer>();   //某个项目是否包含长度大于x的interval
		for ( int i =0; i < projList.size(); i++ ){
			String projectName = projList.get(i).getProjectName();
			xSizedInterNumList.put( projectName, 0);
		}
		
		for ( int i =0; i < projList.size(); i++ ){
			TestProject project = projList.get( i );
			HashSet<String> dupTagList = new HashSet<String>();
			
			ArrayList<TestReport> reportList = project.getTestReportsInProj();
			Integer priorBug = -1;
			for ( int j =0; j < reportList.size(); j++ ){
				TestReport report = reportList.get( j );
				String bugTag = report.getTag();
				String dupTag = report.getDuplicate();
				
				if ( bugTag.equals("审核通过") && !dupTagList.contains( dupTag) ){
					dupTagList.add( dupTag );
					int interval = j - priorBug;
					
					int count = 1;
					if ( interNumList.containsKey( interval ))
						count += interNumList.get( interval );
					interNumList.put( interval, count );
					
					int interSep = 0;
					for ( int k =0; k < sepBeginList.length; k++ ){
						if ( sepBeginList[k] >= interval  ){
							interSep = k;
							break;
						}
					}
					Double processIndex = 1.0* (j+1) / reportList.size();
					int processSep = 0;
					for ( int k =0; k < processSepList.length; k++ ){
						if ( processSepList[k] >= processIndex ){
							processSep = k;
							break;
						}
					}
					
					Integer[] interDetail = null;
					int begin = 1;
					if ( interSep > 0 ){
						begin = sepBeginList[interSep-1] + 1;
					}
					String sepStr = begin + "-" + sepBeginList[interSep] ;
					if ( interNumDetailList.containsKey( sepStr )){
						interDetail = interNumDetailList.get( sepStr );
					}else{
						interDetail = new Integer[processSepList.length];
						for ( int k =0; k < interDetail.length;k++){
							interDetail[k] = 0;
						}
					}
					//System.out.println( interSep + " " + processSep + " " + interDetail[processSep]);
					interDetail[processSep] ++;
					interNumDetailList.put( sepStr, interDetail );
					
					if ( interval >= 10 && priorBug != -1 ){
						//System.out.println ( j+ " " + reportList.get(j).getSubmitTime() );
						//System.out.println ( priorBug + " " + reportList.get( priorBug).getSubmitTime() );
						Long duration = (reportList.get(j).getSubmitTime().getTime() - reportList.get(priorBug).getSubmitTime().getTime())/1000/60/60;
						System.out.println ( duration );
					}
					if ( interval >= 10 &&  processIndex >= 0.5){
						xSizedInterNumList.put( project.getProjectName(), 1);
					}
					
					priorBug = j;
				}
			}
		}
		
		//output to file
		BufferedWriter writer;
		try {
			writer = new BufferedWriter( new FileWriter ( "data/output/findings/bugSubmitInterval-test.csv" ));
			
			writer.write( "interval" + ","  );
			for ( int i =1; i < processSepList.length; i++ ){
				writer.write( processSepList[i-1] +"-" + processSepList[i] + "," );
			}
			writer.newLine();
			for ( String interval : interNumDetailList.keySet() ) {
				Integer[] detail = interNumDetailList.get( interval );
				writer.write( interval.replace("-", " ").toString() + ",");
				for ( int i = 1; i < detail.length; i++ ){   //begin at 1 
					writer.write( detail[i] +",");
				}
				writer.newLine();
			}
			writer.flush();
			writer.close();
			
			writer.close();
			
			writer = new BufferedWriter( new FileWriter ( "data/output/findings/bugSubmitInterval-forProjects-test.csv" ));
			
			writer.write( "projectName" + ","  );	
			writer.newLine();
			for ( String projectName : xSizedInterNumList.keySet() ) {
				writer.write( projectName +  "," + xSizedInterNumList.get( projectName ));
				writer.newLine();
			}
			writer.flush();
			writer.close();
			
			writer.close();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	}
	
	
	public static void main ( String[] args ){
		BugSubmitInterval interTool = new BugSubmitInterval();
		interTool.bugSubmitIntervalCounter( Constants.PROJECT_FOLDER );
	}
}
