package com.recommendBasic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.data.TestProject;
import com.data.TestReport;
import com.data.TestTask;
import com.dataProcess.ReportSegment;


public class RecContextModeling {
	Date earliestTime;
	
	public RecContextModeling() {
		// TODO Auto-generated constructor stub
		try {
			SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			earliestTime = dateFormat.parse( "2015-01-01 00:00:00" );
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//某个时间点上，所有人员之前所有活动的汇总，将WorkerActiveHistory基于时间进行截取
	public HashMap<String, HashMap<Date, ArrayList<String>>> modelActivenessContext ( TestProject project, int recTimePoint, HashMap<String, HashMap<Date, ArrayList<String>>> workerActiveHistory ){
		Date curTime = project.getTestReportsInProj().get( recTimePoint ).getSubmitTime();
		
		HashMap<String, HashMap<Date, ArrayList<String>>> curActiveList = new HashMap<String, HashMap<Date, ArrayList<String>>>();
		for ( String workerId : workerActiveHistory.keySet() ){
			HashMap<Date, ArrayList<String>> activeHistory = workerActiveHistory.get( workerId );
			HashMap<Date, ArrayList<String>> curActive = new HashMap<Date, ArrayList<String>>();
			for ( Date date : activeHistory.keySet() ){
				if ( date.getTime() <= curTime.getTime() ){
					curActive.put( date, activeHistory.get( date));
				}
			}
			curActiveList.put( workerId, curActive );
		}
		
		return curActiveList;
	}
	
	//在某个时间点上，所有人员之前所有活动的汇总，将workerExpertiseHistory基于时间进行截取
	public HashMap<String, HashMap<Date, ArrayList<List<String>>>> modelExpertiseRawContext ( TestProject project, int recTimePoint, HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerExpertiseHistory ){
		Date curTime = project.getTestReportsInProj().get( recTimePoint ).getSubmitTime();
		
		HashMap<String, HashMap<Date, ArrayList<List<String>>>> curExpertiseList = new HashMap<String, HashMap<Date, ArrayList<List<String>>>>();
		for ( String workerId : workerExpertiseHistory.keySet() ){
			HashMap<Date, ArrayList<List<String>>> expertiseHistory = workerExpertiseHistory.get( workerId );
			HashMap<Date, ArrayList<List<String>>> curExpertise = new HashMap<Date, ArrayList<List<String>>>();
			for ( Date date : expertiseHistory.keySet() ){
				if ( date.getTime() <= curTime.getTime() ){
					curExpertise.put( date, expertiseHistory.get( date ));
				}
			}
			curExpertiseList.put( workerId, curExpertise );
		}
		
		return curExpertiseList;
	}
	
	public HashMap<String, HashMap<Date, ArrayList<List<String>>>> modelPreferenceRawContext ( TestProject project, int recTimePoint, HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerPreferenceHistory ){
		return this.modelExpertiseRawContext(project, recTimePoint, workerPreferenceHistory);
	}
	
	
	//某个时间点上，所有人员之前的所有bug report提交的内容构成的经验
	public HashMap<String, HashMap<String, Integer>> modelExpertiseContext ( TestProject project, int recTimePoint, HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerExpertiseHistory ){
		Date curTime = project.getTestReportsInProj().get( recTimePoint ).getSubmitTime();
		
		//<workerId, <term, bugNum>>
		HashMap<String, HashMap<String, Integer>> curExpertiseList = new HashMap<String, HashMap<String, Integer>>();
		for ( String workerId: workerExpertiseHistory.keySet() ){
			HashMap<Date, ArrayList<List<String>>> history = workerExpertiseHistory.get( workerId );
			
			HashMap<String, Integer> curExpertise = new HashMap<String, Integer>();
			if ( curExpertiseList.containsKey( workerId )){
				curExpertise = curExpertiseList.get( workerId );
			}
			
			for ( Date date : history.keySet() ){
				if ( date.getTime() <= curTime.getTime() ){
					ArrayList<List<String>> reportsList = history.get( date );
					for ( int i =0; i <reportsList.size(); i++ ){
						List<String> termList = reportsList.get( i );
						for ( int j =0; j < termList.size(); j++ ){
							String term = termList.get( j );
							int bugNum = 1;
							if ( curExpertise.containsKey( term )){
								bugNum = curExpertise.get( term ) +1 ;
							}
							curExpertise.put( term, bugNum );
						}
					}
				}
			}
			
			curExpertiseList.put( workerId, curExpertise );
		}
		return curExpertiseList;
	}
	
	public HashMap<String, HashMap<String, Integer>> modelPreferenceContext ( TestProject project, int recTimePoint, HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerPreferenceHistory ){
		return this.modelExpertiseContext(project, recTimePoint, workerPreferenceHistory);
		//<workerId, <term, reportNum>>
	}
	
	//某个时间点上，当前测试任务已经提交的缺陷情况，这些缺陷在term上的分布情况
	public HashMap<String, Double> modelTestContext ( TestProject project, int recTimePoint, TestTask task ){
		ArrayList<String> taskDescription = task.getTaskDescription();
		
		ArrayList<TestReport> submitReportList = new ArrayList<TestReport>();
		ArrayList<ArrayList<String>> submitTermsList = new ArrayList<ArrayList<String>>();
		for ( int i = 0; i <= recTimePoint && i < project.getTestReportsInProj().size(); i++ ){
			TestReport report = project.getTestReportsInProj().get( i);
			String bugTag = report.getTag();
			if ( bugTag.equals("审核不通过"))
				continue;
			submitReportList.add( report );
			
			//String[] termArray = segTool.segmentTestReport(report);
			String[] wordsDetail = report.getBugDetail().split( " ");
			String[] wordsSteps = report.getReproSteps().split( " ");
			ArrayList<String> termList = new ArrayList<String>();
			for ( int j=0; j < wordsDetail.length; j++ )
				termList.add( wordsDetail[j] );
			for( int j =0; j < wordsSteps.length; j++ ){
				termList.add( wordsSteps[j] );
			}
			
			submitTermsList.add( termList );
		}
		
		//<term, bug report with term/bug report> , terms are the these in the task descriptions
		HashMap<String, Double> termAdequacyList = new HashMap<String, Double>();
		for ( int i =0; i < taskDescription.size(); i++ ){
			String term = taskDescription.get( i );
			int bugWithTerm = 0;
			for ( int j =0; j < submitTermsList.size(); j++ ){
				ArrayList<String> termsList = submitTermsList.get( j );
				if ( termsList.contains( term )){
					bugWithTerm++;
				}
			}
			
			Double termAdeq = 0.0;
			if ( submitTermsList.size() != 0 ){
				termAdeq = (1.0*bugWithTerm) / submitTermsList.size();
			}
			
			termAdequacyList.put( term, termAdeq );
		}
		
		return termAdequacyList;
	}
}
