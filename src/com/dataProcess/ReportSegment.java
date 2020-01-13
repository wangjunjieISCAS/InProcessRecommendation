package com.dataProcess;

import java.util.ArrayList;
import java.util.HashMap;

import com.data.TestProject;
import com.data.TestReport;

public class ReportSegment {

	public String[] segmentTestReport ( TestReport report ){
		String[] wordsDetail = WordSegment.segmentWordWithNV ( report.getBugDetail() );
		String[] wordsSteps = WordSegment.segmentWordWithNV ( report.getReproSteps() );
		
		String[] reportContent = new String[wordsDetail.length + wordsSteps.length];
		System.arraycopy( wordsDetail, 0, reportContent, 0, wordsDetail.length);
		System.arraycopy( wordsSteps, 0, reportContent, wordsDetail.length, wordsSteps.length);
		
		return reportContent;
	}
	
	public HashMap<String, Integer> segmentTestReportMap ( TestReport report){
		HashMap<String, Integer> reportWordMap = new HashMap<String, Integer>();
		
		String[] reportContent = this.segmentTestReport (report);
		for ( int i = 0; i < reportContent.length; i++ ) {
			String word = reportContent[i];
			int count = 1;
			if ( reportWordMap.containsKey( word)) {
				count += reportWordMap.get( word);
			}
			
			reportWordMap.put( word, count );
		}
		
		return reportWordMap;
	}
	
	public HashMap<String, Integer> segmentTestProjectMap ( TestProject project ){
		HashMap<String, Integer> projectWordMap = this.segmentTestReportListMap( project.getTestReportsInProj() );
		return projectWordMap;
	}
	
	public HashMap<String, Integer> segmentTestReportListMap ( ArrayList<TestReport> reportList ){
		HashMap<String, Integer> reportListWordMap = new HashMap<String, Integer>();
		
		for ( int i =0; i < reportList.size(); i++) {
			TestReport report = reportList.get( i );
			
			HashMap<String, Integer> reportWordMap = this.segmentTestReportMap(report);
			
			for ( String key: reportWordMap.keySet() ) {
				Integer value = reportWordMap.get( key );
				if ( reportListWordMap.containsKey( key )) {
					value += reportListWordMap.get( key );
				}
				
				reportListWordMap.put( key, value);
			}
		}
		
		return reportListWordMap;
	}
}
