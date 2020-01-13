package com.recommendOverview;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.data.Constants;
import com.data.TestProject;
import com.data.TestReport;
import com.dataProcess.TestProjectReader;
import com.evaluation.PerformanceEvaluation;
import com.recommendBasic.MapSortTool;
import com.recommendBasic.RecTimePoint;
import com.recommendBasic.WorkerActiveHistory;
import com.recommendBasic.WorkerExpertiseHistory;
import com.recommendBasic.WorkerPreferenceHistory;
import com.recommendRank.LearningDataPreparation;
import com.recommendRerank.DiversityBasedRerank;

public class WorkerRecommendation {
	public void PrepareTrainingModel ( int stablePara , int testSetGroupIndex ){
		TestProjectReader projectReader = new TestProjectReader();
		ArrayList<TestProject> projectList = projectReader.loadTestProjectAndTaskList( Constants.PROJECT_FOLDER, Constants.TASK_DES_FOLDER );
		RecTimePoint pointTool = new RecTimePoint ();
		LearningDataPreparation dataPrepareTool = new LearningDataPreparation ();
		
		WorkerActiveHistory actHistory = new WorkerActiveHistory();
		HashMap<String, HashMap<Date, ArrayList<String>>> workerActiveHistory = actHistory.readWorkerActiveHistory( "data/output/history/active.txt" ) ; 
		WorkerExpertiseHistory expHistory = new WorkerExpertiseHistory();
		HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerExpertiseHistory = expHistory.readWorkerExpertiseHistory( "data/output/history/expertise.txt" );
		WorkerPreferenceHistory prefHistory = new WorkerPreferenceHistory();
		HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerPreferenceHistory = prefHistory.readWorkerExpertiseHistory( "data/output/history/preference.txt" );
		
		int trainSetBeginIndex = (testSetGroupIndex - 1) * Constants.TEST_SET_GROUP_SIZE;
		if ( testSetGroupIndex == 10 )
			trainSetBeginIndex = 180;		
		int testSetBeginIndex = testSetGroupIndex * Constants.TEST_SET_GROUP_SIZE;
		String fileTrain = "D:\\java-workstation-2019\\CrowdWorkerSelection\\data\\output\\model\\" + testSetGroupIndex + "\\training.txt"; 
		String fileModel = "D:\\java-workstation-2019\\CrowdWorkerSelection\\data\\output\\model\\" + testSetGroupIndex + "\\model.model";
		//train set is trainSetBeginIndex - testSetBeginIndex-1
		for (int k = trainSetBeginIndex ; k < testSetBeginIndex; k++ ){   //太小的话，没有history；不具有预测性; 取之前3个group size的，太大的话，训练数据太多
			TestProject project = projectList.get( k );
			ArrayList<Integer> recPoints = pointTool.decideRecTimePoint( project, stablePara, 5  );
			for ( int j =0; j < recPoints.size(); j++ ){
				int recPoint = recPoints.get( j );
				System.out.println ( "Processing training set " + k + " recPoint " + recPoint );
				dataPrepareTool.PrepareTrainORTestData(project, project.getTestTask(), recPoint, fileTrain, true, workerActiveHistory, workerExpertiseHistory, 
						workerPreferenceHistory);
			}
		}
		
		String commandCmd = "cmd /k ";
		String commandRank = "java -jar D:\\RankLib-master\\bin\\RankLib.jar -train " + fileTrain + " -ranker 6 -norm zscore -save " + fileModel;		
		String command = commandCmd + commandRank;
		System.out.println ( command );
		/*
		try {
			Process process = Runtime.getRuntime().exec( command);
			Thread.sleep( 300000 );   //5 minutes
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}
	
	//isPrepareTestingData 是指需要重新生成训练数据, isConductTesting 是指重新进行模型，生成结果
	public void conductWorkerRecommendation ( int stablePara, int testSetGroupIndex, Boolean isPrepareTestingData, Boolean isConductTesting ){	
		TestProjectReader projectReader = new TestProjectReader();
		ArrayList<TestProject> projectList = projectReader.loadTestProjectAndTaskList( Constants.PROJECT_FOLDER, Constants.TASK_DES_FOLDER );
		RecTimePoint pointTool = new RecTimePoint ();
		LearningDataPreparation dataPrepareTool = new LearningDataPreparation ();
		
		WorkerActiveHistory actHistory = new WorkerActiveHistory();
		HashMap<String, HashMap<Date, ArrayList<String>>> workerActiveHistory = actHistory.readWorkerActiveHistory( "data/output/history/active.txt" ) ; 
		WorkerExpertiseHistory expHistory = new WorkerExpertiseHistory();
		HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerExpertiseHistory = expHistory.readWorkerExpertiseHistory( "data/output/history/expertise.txt" );
		WorkerPreferenceHistory prefHistory = new WorkerPreferenceHistory();
		HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerPreferenceHistory = prefHistory.readWorkerExpertiseHistory( "data/output/history/preference.txt" );
		
		String fileModel = "D:\\java-workstation-2019\\CrowdWorkerSelection\\data\\output\\model\\" + testSetGroupIndex + "\\model.model";
		int testSetBeginIndex = testSetGroupIndex * Constants.TEST_SET_GROUP_SIZE;
		
		PerformanceEvaluation evaluation = new PerformanceEvaluation();
		DiversityBasedRerank reRankTool = new DiversityBasedRerank();
		
		
		for ( int k = testSetBeginIndex ; k < testSetBeginIndex + Constants.TEST_SET_GROUP_SIZE ; k++ ){  			
			TestProject project = projectList.get( k );
			System.out.println ( "Processing test set " + k + " " + project.getProjectName() );
			
			ArrayList<Integer> recPoints = pointTool.decideRecTimePoint(project, stablePara, 0 );   //.decideRecTimePoint(project, stablePara, 5);
			System.out.println( "recPoints: ");
			for ( int i =0; i < recPoints.size(); i++ ){
				System.out.print ( recPoints.get(i) + " ");
			}
			System.out.println( );
			
			for ( int j =0; j < recPoints.size(); j++ ){
				int recPoint = recPoints.get( j);
				HashSet<String> workerPerfTask= new HashSet<String>();
				for ( int i =0; i <= recPoint; i++ ){
					TestReport report = project.getTestReportsInProj().get(i);
					workerPerfTask.add( report.getUserId());
				}
				
				String fileTest = "D:\\java-workstation-2019\\CrowdWorkerSelection\\data\\output\\model\\" + testSetGroupIndex + "\\testing\\testing-" + k + "-" + recPoint + ".txt";
				String fileResult = "D:\\java-workstation-2019\\CrowdWorkerSelection\\data\\output\\model\\" + testSetGroupIndex + "\\result\\result-" + k + "-" + recPoint + ".txt";
				
				LinkedHashMap<String, ArrayList<Double>> workersFeaturesList = null;
				if ( isPrepareTestingData  ){
					workersFeaturesList = dataPrepareTool.PrepareTrainORTestData(project, project.getTestTask(), recPoint, fileTest, false, workerActiveHistory, workerExpertiseHistory, workerPreferenceHistory );
					if ( workersFeaturesList.size() == 0 )
						continue;
				}else{
					//当数据都已经生成了之后，直接读取就行
					workersFeaturesList = new LinkedHashMap<String, ArrayList<Double>>();
					try {
						BufferedReader readerTemp = new BufferedReader ( new FileReader ( new File ( fileTest )));
						String line = "";
						while ( ( line = readerTemp.readLine() ) != null ){
							ArrayList<Double> features = new ArrayList<Double>();
							String[] temp = line.split( " ");
							String workerId = temp[temp.length-1];
							for ( int i =2; i < temp.length-2; i++ ){
								if ( !temp[i].contains(":"))
									continue;
								String[] temp2 = temp[i].split(":");
								features.add( Double.parseDouble(temp2[1]) );
							}
							workersFeaturesList.put( workerId, features );
						}
						readerTemp.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				if ( isConductTesting ){
					String commandCmd = "cmd /c ";
					String commandRank = "java -jar D:\\RankLib-master\\bin\\RankLib.jar -load " + fileModel + " -rank " + fileTest  + " -norm zscore -score " + fileResult ;
					String command = commandCmd + commandRank;
					System.out.println ( command );
					try {
						Process process = Runtime.getRuntime().exec( command);
						Thread.sleep( 3000 );   //5 seconds
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				
				}
				
				ArrayList<String> candWorkerList = new ArrayList<String>();
				for ( String workerId : workersFeaturesList.keySet() ){
					candWorkerList.add( workerId );
				}
				
				//读取RankLib生成的结果
				HashMap<String, Double> workerPredictResults = new HashMap<String, Double>();
				BufferedReader reader;
				try {
					reader = new BufferedReader ( new FileReader ( new File ( fileResult )));
					String line = "";
					int index = 0;
					while ( ( line = reader.readLine() ) != null ){
						String[] temp = line.split( "\t");
						Double prob = Double.parseDouble( temp[2]);
						workerPredictResults.put( candWorkerList.get( index), prob );
						index++;
					}
					reader.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				MapSortTool sortTool = new MapSortTool ();
				List<Map.Entry<String,Double>> rankedRecWorkers = sortTool.sortHashMapStringDoubleDESC( workerPredictResults );
				//rankedWorkersList 和 reRankedWorkersList 是两个生成的结果
				LinkedHashMap<String, Double> rankedWorkersList = new LinkedHashMap<String, Double>();
				for ( int i =0; i < rankedRecWorkers.size(); i++ ){
					String workerId = rankedRecWorkers.get(i).getKey();
					rankedWorkersList.put( workerId, rankedRecWorkers.get(i).getValue() );
				}
				
				//evaluation result
				LinkedHashMap<Integer, Double> bugDetectRatePredict = evaluation.bugDetectionRatePredicted ( rankedWorkersList, project, recPoint );
				LinkedHashMap<Integer, Double> bugDetectRateTruth = evaluation.bugDetectionRateGroundTruth( project, recPoint );
				Boolean isOutput = false;
				for ( Integer index : bugDetectRateTruth.keySet() ){
					Double value = bugDetectRateTruth.get( index );
					if ( value > 0 ){
						isOutput = true;
						break;
					}
				}
				
				String performanceFoler = "data/output/performance/";						
				if ( isOutput )
					this.storePredictResults(bugDetectRatePredict, bugDetectRateTruth, performanceFoler + "rank/performance-" + project.getProjectName() + "+" + recPoint + ".csv");
				
				//reRanked evaluation result
				LinkedHashMap<String, String> reRankedWorkersListTotal = reRankTool.reRankRecWorkersByWorkerDiversity (rankedWorkersList, project.getTestReportsInProj().get(recPoint).getSubmitTime(), 
						workerExpertiseHistory, 0.75, workersFeaturesList, workerPerfTask );
				//这里返回的是hashmap的value是几项分数的汇总，以便输出到文件中便于查看
				LinkedHashMap<String, Double> reRankedWorkersList = new LinkedHashMap<String, Double>();
				for ( String workerId : reRankedWorkersListTotal.keySet() ){
					String[] temp = reRankedWorkersListTotal.get(workerId).split( "  ");
					Double value = Double.parseDouble( temp[0] );
					reRankedWorkersList.put( workerId, value);
				}
				LinkedHashMap<Integer, Double> bugDetectRateReranked = evaluation.bugDetectionRatePredicted ( reRankedWorkersList, project, recPoint );
				if ( isOutput ) 
					this.storePredictResults(bugDetectRateReranked, bugDetectRateTruth, performanceFoler + "/rerank/performance-" + project.getProjectName() + "+" + recPoint + ".csv");
			
				if ( isOutput ){
					String recWorkerFile = performanceFoler + "/recWorkers-" + project.getProjectName() + "+" + recPoint + ".csv";
					this.storeRecommendWorkers( rankedWorkersList, reRankedWorkersListTotal, project, recPoint, recWorkerFile);
				}				
			}	
		}
	}
	
	public void storeRecommendWorkers (LinkedHashMap<String, Double> rankedWorkersList, LinkedHashMap<String, String> reRankedWorkersList, TestProject project, int recPoint, 
			String outFile ){
		try {
			ArrayList<String> rankedWorkers = new ArrayList<String>();
			ArrayList<String> reRankedWorkers = new ArrayList<String>();
			for ( String workerId: rankedWorkersList.keySet() ){
				rankedWorkers.add( workerId );
			}
			for ( String workerId : reRankedWorkersList.keySet() ){
				reRankedWorkers.add( workerId );
			}
			
			BufferedWriter writer = new BufferedWriter ( new FileWriter ( new File ( outFile )));
			writer.write( "rankedWorkers" + "," + "score" + "," + "reRankedWorkers" + "," + "score" + "," + "trueWorkers" + "," + "bugTag" + "," + "dupTag");
			writer.newLine();
			for ( int i = recPoint+1; i < project.getTestReportsInProj().size(); i++ ){
				int point = i - recPoint - 1;
				if ( point >= rankedWorkers.size() ){
					writer.write( " " + "," +" " + "," + " " +"," + " " + ",");
				}else{
					writer.write( rankedWorkers.get(point) + "," + rankedWorkersList.get( rankedWorkers.get(point)) + ",");
					writer.write( reRankedWorkers.get(point) + "," + reRankedWorkersList.get( reRankedWorkers.get(point)) + ",");
				}
				
				writer.write( project.getTestReportsInProj().get(i).getUserId() + "," + project.getTestReportsInProj().get(i).getTag() + "," +  project.getTestReportsInProj().get(i).getDuplicate() );
				writer.newLine();
			}
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void storePredictResults (LinkedHashMap<Integer, Double> bugDetectRatePredict, LinkedHashMap<Integer, Double> bugDetectRateTruth, String outFile ){
		try {
			//System.out.println( "************************** bugDetectedRatePredict:" + bugDetectRatePredict.size() + "  bugDetectRateTruth:" + bugDetectRateTruth.size() );
			BufferedWriter writer = new BufferedWriter ( new FileWriter ( new File ( outFile )));
			LinkedHashMap<Integer, Double> refDetectRate = bugDetectRatePredict;
			if ( bugDetectRatePredict.size() < bugDetectRateTruth.size() ){
				refDetectRate = bugDetectRateTruth;
			}
			for ( Integer workerNum: refDetectRate.keySet() ){
				Double bugsPredict = bugDetectRatePredict.get( workerNum);
				Double bugsTruth = bugDetectRateTruth.get( workerNum );
				
				writer.write( workerNum + "," + bugsPredict + "," + bugsTruth );
				writer.newLine();
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 
	public static void main ( String[] args ){
		WorkerRecommendation workerRec = new WorkerRecommendation();
		for ( int i = 10; i < 20; i++ ){
			workerRec.conductWorkerRecommendation( 4, i, true, true );
		}		
	}
}
