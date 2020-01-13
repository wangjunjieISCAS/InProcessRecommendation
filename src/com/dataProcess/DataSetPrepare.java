package com.dataProcess;

import java.util.ArrayList;
import java.util.HashMap;

import com.data.TestProject;
import com.data.TestReport;


public class DataSetPrepare {
	
	public ArrayList<HashMap<String, Integer>> prepareDataSet ( ArrayList<TestProject> projectList ) {
		ArrayList<HashMap<String, Integer>> totalDataSet = new ArrayList<HashMap<String, Integer>>();
		ReportSegment segTool = new ReportSegment();
		
		for ( int i =0; i < projectList.size(); i++ ) {
			TestProject project = projectList.get( i );
			for ( int j =0; j < project.getTestReportsInProj().size(); j++ ) {
				TestReport report = project.getTestReportsInProj().get( j);
				
				HashMap<String, Integer> termMap = segTool.segmentTestReportMap(report);
				//System.out.println ( termMap.keySet());
				
				totalDataSet.add( termMap );
			}
		}
		return totalDataSet;
	}
}
