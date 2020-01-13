package com.recommendRank;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.data.Constants;
import com.data.TestProject;
import com.data.TestReport;
import com.data.TestTask;
import com.recommendBasic.MapSortTool;
import com.recommendBasic.RecContextModeling;
import com.recommendBasic.RecTimePoint;

public class LearningDataPreparation {
	public HashMap<String, HashMap<String, Double>> probExpertise;
	public LearningDataPreparation (){
		probExpertise = new HashMap<String, HashMap<String, Double>>();
	}
	
	//��ȡ���е�learning features 
	public LinkedHashMap<String, ArrayList<Double>> PrepareTrainORTestData ( TestProject project , TestTask task, int recTimePoint, String outFile, boolean isTrain,
			HashMap<String, HashMap<Date, ArrayList<String>>> workerActiveHistory, HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerExpertiseHistory, 
			HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerPreferenceHistory ){
		FeatureRetrievalActive activeFeatureTool = new FeatureRetrievalActive ();
		FeatureRetrievalExpertise expertiseFeatureTool = new FeatureRetrievalExpertise ();
		FeatureRetrievalPreference preferenceFeatureTool = new FeatureRetrievalPreference ();
		
		int revRecTimePoint = recTimePoint;    //for comparison experiment
		Date curTime = project.getTestReportsInProj().get(revRecTimePoint).getSubmitTime();
		
		RecContextModeling contextTool = new RecContextModeling ();
		//HashMap<String, Double> testContext = contextTool.modelTestContext(project, recTimePoint, task);
		//for comparison experiment
		
		HashMap<String, Double> testContext = new HashMap<String, Double>();
		ArrayList<String> testDescrip = project.getTestTask().getTaskDescription();
		for ( int i =0; i < testDescrip.size(); i++){
			testContext.put( testDescrip.get(i), 1.0 );
		}
		
		HashMap<String, HashMap<Date, ArrayList<String>>> curActiveList = contextTool.modelActivenessContext(project, revRecTimePoint, workerActiveHistory);
		HashMap<String, HashMap<Date, ArrayList<List<String>>>> curExpertiseList = contextTool.modelExpertiseRawContext(project, revRecTimePoint, workerExpertiseHistory);
		HashMap<String, HashMap<Date, ArrayList<List<String>>>> curPreferenceList = contextTool.modelPreferenceRawContext(project, revRecTimePoint, workerPreferenceHistory);
		
		HashSet<String> bugWorkerList = this.retrievePredictionLabel(project, recTimePoint);
		HashSet<String> noBugWorkerList = new HashSet<String>();
		if ( isTrain ) 
			noBugWorkerList = this.retrieveNegativeSamplingTrainSelected(project, recTimePoint, bugWorkerList, task, testContext, curActiveList, curExpertiseList);
		else
			noBugWorkerList = this.retrieveNegativeSamplingTest(project, recTimePoint, bugWorkerList, curActiveList);
		
		if ( bugWorkerList.size() == 0 || noBugWorkerList.size() == 0 ){   //ֻ��һ��label
			return new LinkedHashMap<String, ArrayList<Double>>() ;
		}
		
		HashMap<String, ArrayList<Double>> activeFeatureList = activeFeatureTool.retrieveActiveFeatures(curActiveList, curTime);
		HashMap<String, ArrayList<Double>> expertiseFeatureList = expertiseFeatureTool.retrieveExpertiseFeatures(project, task, revRecTimePoint, curExpertiseList, testContext );
		HashMap<String, ArrayList<Double>> preferenceFeatureList = preferenceFeatureTool.retrievePreferenceFeatures(project, task, revRecTimePoint, curPreferenceList, testContext );
		probExpertise = expertiseFeatureTool.getProbExpertise();
		
		HashMap<String, ArrayList<Double>> positiveSampleList = new HashMap<String, ArrayList<Double>>();
		HashMap<String, ArrayList<Double>> negativeSampleList = new HashMap<String, ArrayList<Double>>();
		LinkedHashMap<String, ArrayList<Double>> workersFeaturesList = new LinkedHashMap<String, ArrayList<Double>>();
		
		for ( String workerId : bugWorkerList ){
			ArrayList<Double> featureList = new ArrayList<Double>();
			featureList.addAll( activeFeatureList.get( workerId ));
			featureList.addAll( expertiseFeatureList.get( workerId));
			featureList.addAll( preferenceFeatureList.get( workerId));
			
			positiveSampleList.put( workerId, featureList );
			workersFeaturesList.put( workerId, featureList );
		}
		
		ArrayList<Double> defaultExpertise = new ArrayList<Double>();
		int featureSize = 0;
		for ( String workerId : expertiseFeatureList.keySet() ){
			featureSize = expertiseFeatureList.get( workerId ).size();
			break;
		}
		for ( int i =0; i < featureSize; i++ ){
			defaultExpertise.add( 0.0);
		}
		for ( String workerId : noBugWorkerList ){
			ArrayList<Double> featureList = new ArrayList<Double>();
			featureList.addAll( activeFeatureList.get( workerId));
			if ( expertiseFeatureList.containsKey( workerId )){
				featureList.addAll( expertiseFeatureList.get( workerId));
				featureList.addAll( preferenceFeatureList.get( workerId ));
			}else{
				featureList.addAll( defaultExpertise );
				featureList.addAll( defaultExpertise );
			}
			
			negativeSampleList.put( workerId, featureList );
			workersFeaturesList.put( workerId, featureList );
		}
		//this.outputLearningData(outFile, positiveSampleList, negativeSampleList, isTrain );
		this.outputLearningDataRankLib(outFile, positiveSampleList, negativeSampleList);
		
		return workersFeaturesList;
	}
	
	//������context-aware��Ԥ�⣬��������bugָ���Ǻ�recTimePoint֮ǰ�ύ��ȱ�ݲ��ظ��ģ����ص���һ���ڸ���Ŀ recTimePoint֮���ύȱ�ݵ���
	public HashSet<String> retrievePredictionLabel ( TestProject project, int recTimePoint ){
		ArrayList<TestReport> reportList = project.getTestReportsInProj();
		HashSet<String> submittedBugList = new HashSet<String>();
		for ( int i =0; i <= recTimePoint; i++ ){
			TestReport report = reportList.get( i );
			String bugTag = report.getTag();
			String dupTag = report.getDuplicate();
			if ( bugTag.equals("���ͨ��")){
				submittedBugList.add( dupTag );
			}			
		}
		
		HashSet<String> bugWorkerList = new HashSet<String>();
		for ( int i = recTimePoint+1; i < reportList.size(); i++ ){
			TestReport report = reportList.get( i );
			String bugTag = report.getTag();
			String dupTag = report.getDuplicate();
			if ( bugTag.equals( "���ͨ��") && !submittedBugList.contains( dupTag)){
				bugWorkerList.add( report.getUserId() );
			}
		}
		
		return bugWorkerList;
	}
	
	//��Ҫ��ȡһЩ�ڸ���Ŀ��û�з���ȱ�ݵ��ˣ�Ŀǰֻ�ǿ��˵�ǰ��Ŀ���ύ���浫��û�з���ȱ�ݵ��ˣ����ڻῼ�Ǵ�ȫ����Ա���ҵ�һЩ������;
	//���ǵ�һЩ�˻��ύ������棬��Щ������ȱ�ݣ���Щû�У�������Ҫ���ⲿ����ȥ��;
	public HashSet<String> retrieveNegativeSampling ( TestProject project, int recTimePoint, HashSet<String> bugWorkerList ){
		ArrayList<TestReport> reportList = project.getTestReportsInProj();
		HashSet<String> submittedBugList = new HashSet<String>();
		for ( int i =0; i <= recTimePoint; i++ ){
			TestReport report = reportList.get( i );
			String bugTag = report.getTag();
			String dupTag = report.getDuplicate();
			if ( bugTag.equals("���ͨ��")){
				submittedBugList.add( dupTag );
			}			
		}
		
		HashSet<String> noBugWorkerList = new HashSet<String>();
		for ( int i = recTimePoint+1; i < reportList.size(); i++ ){
			TestReport report = reportList.get( i );
			String workerId = report.getUserId();
			String bugTag = report.getTag();
			String dupTag = report.getDuplicate();
			if ( bugTag.equals("��˲�ͨ��") && !bugWorkerList.contains( workerId)){
				noBugWorkerList.add( workerId );
			}
			if ( bugTag.equals( "���ͨ��") && submittedBugList.contains( dupTag)){
				noBugWorkerList.add( workerId );
			}
		}		
		return noBugWorkerList;
	}
	
	//��һ���bugWorkerList��ͬsize�� ����Ծ ���Ҳ��������Ƶ�expertise ���� 
	public HashSet<String> retrieveNegativeSamplingTrainSelected ( TestProject project, int recTimePoint, HashSet<String> bugWorkerList, TestTask task, HashMap<String, Double> testContext, 
			HashMap<String, HashMap<Date, ArrayList<String>>> curActiveList, HashMap<String, HashMap<Date, ArrayList<List<String>>>> curExpertiseList  ){
		Date curTime = project.getTestReportsInProj().get( recTimePoint).getSubmitTime();
		MapSortTool sortTool = new MapSortTool();
		
		//�򵥵�ƥ�䣻��expertise term��testContext�ж�����ͬ��term
		HashMap<String, Double> simList = new HashMap<String, Double>();
		for ( String workerId : curExpertiseList.keySet() ){
			HashMap<Date, ArrayList<List<String>>> expertiseList = curExpertiseList.get( workerId );
			ArrayList<String> termsList = new ArrayList<String>();
			for ( Date date : expertiseList.keySet() ){
				for ( int i =0; i < expertiseList.get( date).size(); i++ ){
					termsList.addAll( expertiseList.get( date).get( i ));
				}
			}
			int sameTermNum = 0;
			for ( String term : testContext.keySet() ){
				if ( termsList.contains( term )){
					sameTermNum++;
				}
			}
			simList.put( workerId, sameTermNum * 1.0 );
		}
		List<Map.Entry<String,Double>> rankedCosineSimList = sortTool.sortHashMapStringDoubleASC( simList );
		
		int selectedNum = bugWorkerList.size();
		HashMap<String, Double> activeWorkerList = new HashMap<String, Double>();
		for ( String workerId : curActiveList.keySet() ){
			HashMap<Date, ArrayList<String>> activeList = curActiveList.get( workerId );
			for ( Date date : activeList.keySet() ){
				Double duration = 1.0 * ( curTime.getTime() - date.getTime() ) / (1000 * 60 * 60 );    //in hours
				activeWorkerList.put( workerId, duration );
			}
		}
		List<Map.Entry<String,Double>> rankedActiveWorkerList = sortTool.sortHashMapStringDoubleDESC( activeWorkerList );
		
		//ȡ��Ա������list�е�����ľ�ֵ
		//���ҵ�����list���涼�е���Ա
		HashMap<String, Integer> combRankedList = new HashMap<String, Integer>();
		for ( int i =0; i < rankedCosineSimList.size(); i++ ){
			String workerId = rankedCosineSimList.get( i).getKey();
			if ( activeWorkerList.containsKey(  workerId )){
				combRankedList.put( workerId, i );
			}
		}		
		for ( int i =0; i < rankedActiveWorkerList.size(); i++ ){
			String workerId = rankedActiveWorkerList.get(i).getKey();
			if ( combRankedList.containsKey( workerId )){
				int rank = i + combRankedList.get( workerId );
				combRankedList.put( workerId, rank );
			}			
		}
		
		HashSet<String> noBugWorkerList = new HashSet<String>();
		List<Map.Entry<String, Integer>> rankedCombList = sortTool.sortHashMapStringIntegerASC( combRankedList );
		for ( int i =0;  i < rankedCombList.size() && noBugWorkerList.size() < selectedNum ; i++ ){
			String workerId = rankedCombList.get( i ).getKey();
			if ( !bugWorkerList.contains( workerId)){
				noBugWorkerList.add( workerId);
			}
		}
		return noBugWorkerList;
	}
	
	//����test set��Ҳ����prediction�׶Σ���Ҫ�������е�active worker; ��Ϊ��Ҫ�����еĽ���Ԥ��
	public HashSet<String> retrieveNegativeSamplingTest ( TestProject project, int recTimePoint, HashSet<String> bugWorkerList, HashMap<String, HashMap<Date, ArrayList<String>>> curActiveList ){
		Date curTime = project.getTestReportsInProj().get( recTimePoint).getSubmitTime();
		
		HashSet<String> noBugWorkerList = new HashSet<String>();
		for ( String workerId : curActiveList.keySet() ){
			HashMap<Date, ArrayList<String>> activeList = curActiveList.get( workerId );
			for ( Date date : activeList.keySet() ){
				Double duration = 1.0 * ( curTime.getTime() - date.getTime() ) / (1000 * 60 * 60 );    //in hours
				if ( duration <= Constants.ACTIVE_THRES * 1.0 && !bugWorkerList.contains( workerId )){
					noBugWorkerList.add( workerId );
				}
			}
		}
		
		return noBugWorkerList;
	}
	
	public void outputLearningData (String outFile, HashMap<String, ArrayList<Double>>  positiveSampleList, HashMap<String, ArrayList<Double>>  negativeSampleList, Boolean isTrain ){
		try {
			boolean isHeader = false;
			File file = new File ( outFile );
			if ( !file.exists() ){
				isHeader = true;
			}
			BufferedWriter writer = new BufferedWriter ( new FileWriter ( file, true ));
			
			if ( isTrain == false || isHeader == true  ){
				String[] header = { "durationLastBug", "durationLastReport", "bug8Hour", "bug24Hour", "bug1Week", "bug2Week", 
							"bugPast", "report8Hour", "report24Hour", "report1Week", "report2Week", 
							"reportPast", "probSim", "cosineSim", "eucSim", "jaccardSim05", 
							"jaccardSim1", "jaccardSim2", "jaccardSim3", "jaccardSim4", 
							"probSimP", "cosineSimP", "eucSimP", "jaccardSim05P", 
							"jaccardSim1P", "jaccardSim2P", "jaccardSim3P", "jaccardSim4P",
							"category"  };
				for ( int i =0; i < header.length; i++ ){
					writer.write( header[i] + ",");
				}
				writer.newLine();
			}
			for ( String workerId : positiveSampleList.keySet() ){
				ArrayList<Double> samples = positiveSampleList.get( workerId );
				for ( int j =0; j < samples.size(); j++){
					writer.write( samples.get(j) + ",");
				}
				writer.write( "yes" );
				//writer.write( workerId );
				writer.newLine();
			}
			for ( String workerId : negativeSampleList.keySet() ){
				ArrayList<Double> samples = negativeSampleList.get( workerId );
				for ( int j =0; j < samples.size(); j++ ){
					writer.write( samples.get(j) + ",");
				}
				writer.write( "no" );
				//writer.write( workerId );
				writer.newLine();
			}			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void outputLearningDataRankLib (String outFile, HashMap<String, ArrayList<Double>>  positiveSampleList, HashMap<String, ArrayList<Double>>  negativeSampleList ){
		try {
			BufferedWriter writer = new BufferedWriter ( new FileWriter ( new File ( outFile ), true ));
			for ( String workerId : positiveSampleList.keySet() ){
				ArrayList<Double> samples = positiveSampleList.get( workerId );
				writer.write( "2 qid:1 ");
				for ( int i =0; i < samples.size(); i++ ){
					writer.write( i+1 + ":" + samples.get( i ) + " ");
				}
				writer.write( " # " + workerId );
				writer.newLine();
			}
			for ( String workerId : negativeSampleList.keySet() ){
				ArrayList<Double> samples = negativeSampleList.get( workerId );
				writer.write( "1 qid:1 ");
				for ( int i =0; i < samples.size(); i++ ){
					writer.write( i+1 + ":" + samples.get( i ) + " ");
				}
				writer.write( " # " + workerId );
				writer.newLine();
			}
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public HashMap<String, HashMap<String, Double>> getProbExpertise() {
		return probExpertise;
	}
	 
}
