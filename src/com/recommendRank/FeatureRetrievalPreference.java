package com.recommendRank;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.data.TestProject;
import com.data.TestTask;

public class FeatureRetrievalPreference {
	
	//这个类的逻辑和 FeatureRetrievalExpertise相同，基本复用那个类的方法
	public HashMap<String, ArrayList<Double>> retrievePreferenceFeatures ( TestProject project, TestTask task, int recTimePoint, 
			HashMap<String, HashMap<Date, ArrayList<List<String>>>> curPreferenceList, HashMap<String, Double> testAdeq ){   
		
		ProbabilisticExpertise probPreferenceTool = new ProbabilisticExpertise();
		HashMap<String, HashMap<String, Double>> probPreference = probPreferenceTool.obtainWorkerExpertiseForTermWithAdjustfactor(project, task.getTaskDescription(), curPreferenceList );
		//<workerId, <term, P(wi|tj)>>，生成的就是限定在taskDescription的词；不是所有的词；后面计算相似度都将这个P(wi|tj)看作是人员expertise
		
		FeatureRetrievalExpertise retrievalTool = new FeatureRetrievalExpertise();
		HashMap<String, Double> probSimList = retrievalTool.retrieveProbabilisticSimilarity(probPreference, testAdeq);
		//<workerId, similarity value>
		HashMap<String, Double> cosineSimList = retrievalTool.retrieveCosineSimilarity(probPreference, testAdeq);
		HashMap<String, Double> eucSimList = retrievalTool.retrieveEuclideanSimilarity(probPreference, testAdeq);
		HashMap<String, Double> jacSimList = retrievalTool.retrieveJaccardSimilarity(probPreference, testAdeq, 0.0);
		HashMap<String, Double> jacSimList2 = retrievalTool.retrieveJaccardSimilarity(probPreference, testAdeq, 0.1);
		HashMap<String, Double> jacSimList3 = retrievalTool.retrieveJaccardSimilarity(probPreference, testAdeq, 0.2);
		HashMap<String, Double> jacSimList4 = retrievalTool.retrieveJaccardSimilarity(probPreference, testAdeq, 0.3);
		HashMap<String, Double> jacSimList5 = retrievalTool.retrieveJaccardSimilarity(probPreference, testAdeq, 0.4);
		
		HashMap<String, ArrayList<Double>> preferenceFeatureList = new HashMap<String, ArrayList<Double>>();
		for ( String workerId : probSimList.keySet() ){
			ArrayList<Double> featureList = new ArrayList<Double>();
			featureList.add( probSimList.get( workerId ) );
			featureList.add( cosineSimList.get( workerId ));
			featureList.add( eucSimList.get( workerId ));
			featureList.add( jacSimList.get( workerId ));
			featureList.add( jacSimList2.get( workerId ));
			featureList.add( jacSimList3.get( workerId ));
			featureList.add( jacSimList4.get( workerId ));
			featureList.add( jacSimList5.get( workerId ));
			
			preferenceFeatureList.put( workerId, featureList );
		}
		return preferenceFeatureList;
	}
}
