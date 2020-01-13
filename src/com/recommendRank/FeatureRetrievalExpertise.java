package com.recommendRank;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.data.TestProject;
import com.data.TestTask;
import com.recommendBasic.RecContextModeling;

public class FeatureRetrievalExpertise {
	public HashMap<String, HashMap<String, Double>> probExpertise;
	
	public FeatureRetrievalExpertise (){
		probExpertise = new HashMap<String, HashMap<String, Double>>();
	}
	/*
	 * feature list :
	 * 1 probabilistic similarity 
	 * 2 cosine similarity
	 * 3 euclidean similarity
	 * 4,5,6,7,8 jaccard similarity with threshod 0.0, 0.1, 0.2, 0.3, 0.4
	 */
	public HashMap<String, ArrayList<Double>> retrieveExpertiseFeatures ( TestProject project, TestTask task, int recTimePoint, 
			HashMap<String, HashMap<Date, ArrayList<List<String>>>> curExpertiseList, HashMap<String, Double> testAdeq ){   
		//curExpertiseList: <worker, <Date, ArrayList<report terms>>>, 当前context下的expertise
		//testAdeq : <term, adequacy>,  每个term的测试充分性概率，越大表示越充分，1-testAdeq
		
		probExpertise = this.generateProbabiliticExpertise ( project, task, curExpertiseList );
		HashMap<String, Double> probSimList = this.retrieveProbabilisticSimilarity(probExpertise, testAdeq);
		//<workerId, similarity value>
		HashMap<String, Double> cosineSimList = this.retrieveCosineSimilarity(probExpertise, testAdeq);
		HashMap<String, Double> eucSimList = this.retrieveEuclideanSimilarity(probExpertise, testAdeq);
		HashMap<String, Double> jacSimList = this.retrieveJaccardSimilarity(probExpertise, testAdeq, 0.0);
		HashMap<String, Double> jacSimList2 = this.retrieveJaccardSimilarity(probExpertise, testAdeq, 0.1);
		HashMap<String, Double> jacSimList3 = this.retrieveJaccardSimilarity(probExpertise, testAdeq, 0.2);
		HashMap<String, Double> jacSimList4 = this.retrieveJaccardSimilarity(probExpertise, testAdeq, 0.3);
		HashMap<String, Double> jacSimList5 = this.retrieveJaccardSimilarity(probExpertise, testAdeq, 0.4);
		
		HashMap<String, ArrayList<Double>> expertiseFeatureList = new HashMap<String, ArrayList<Double>>();
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
			
			expertiseFeatureList.put( workerId, featureList );
		}
		return expertiseFeatureList;
	}
	
	//probSim越大，说明expertise越大
	public HashMap<String, Double> retrieveProbabilisticSimilarity ( HashMap<String, HashMap<String, Double>> probExpertise, HashMap<String, Double> testAdeq ){
		HashMap<String, Double> probSimList = new HashMap<String, Double>();
		
		for ( String workerId : probExpertise.keySet() ){
			HashMap<String, Double> expertise = probExpertise.get( workerId );
			
			Double probSim = 0.0;
			for ( String term : testAdeq.keySet() ){
				Double inadeq = 1.0 - testAdeq.get( term );
				Double exp = 0.0;
				if ( expertise.containsKey( term ) && !expertise.get( term).equals( Double.NaN ) ){
					exp = expertise.get( term );
				}
				
				probSim += inadeq * exp;
			}
			
			probSimList.put( workerId, probSim );
		}
		return probSimList;
	}
	
	// 计算cosine similarity时，只考虑在testAdeq中出现的词，对于没有在testAdeq中出现，但是也属于worker expertise的，没有考虑（主要影响实在分母中没有考虑）。
	//cosineSim越大，说明expertise越大
	public HashMap<String, Double> retrieveCosineSimilarity ( HashMap<String, HashMap<String, Double>> probExpertise, HashMap<String, Double> testAdeq ){
		HashMap<String, Double> cosineSimList = new HashMap<String, Double>();
		
		for ( String workerId : probExpertise.keySet() ){
			HashMap<String, Double> expertiseList = probExpertise.get( workerId );
			
			Double adeqIndex = 0.0, expIndex = 0.0, combIndex = 0.0;
			for ( String term : testAdeq.keySet() ){
				Double inadeq = 1.0 - testAdeq.get( term );
				Double exp = 0.0;
				if ( expertiseList.containsKey( term ) && !expertiseList.get( term).equals( Double.NaN )  )
					exp = expertiseList.get( term );
				
				combIndex += inadeq * exp;
				adeqIndex += inadeq * inadeq ;
				expIndex += exp * exp;
			}
			
			if ( combIndex == 0.0 ){
				cosineSimList.put( workerId, 0.0 );
			}else{
				adeqIndex = Math.sqrt( adeqIndex );
				expIndex = Math.sqrt( expIndex );
				Double cosineSim = combIndex / ( adeqIndex * expIndex );
				
				cosineSimList.put( workerId, cosineSim );
			}			
		}
		
		return cosineSimList;
	}
	
	//euclidiean similarity = sqrt( sum (xi-yi)^2), if xi < yi, treat as 0
	//euclideanSim越大，说明expertise越大
	public HashMap<String, Double> retrieveEuclideanSimilarity ( HashMap<String, HashMap<String, Double>> probExpertise, HashMap<String, Double> testAdeq ){
		HashMap<String, Double> eucSimList = new HashMap<String, Double>();
		
		for ( String workerId : probExpertise.keySet() ){
			HashMap<String, Double> expertiseList = probExpertise.get( workerId );
			
			Double eucSim = 0.0;
			for ( String term : testAdeq.keySet() ){
				Double inadeq = 1.0 - testAdeq.get( term );
				Double exp = 0.0;
				if ( expertiseList.containsKey( term ) && !expertiseList.get( term).equals( Double.NaN ) ){
					exp = expertiseList.get( term );
				}
				
				if ( exp <= inadeq ){
					eucSim += 0.0;
				}else{
					eucSim += (exp-inadeq)*(exp-inadeq);
				}
			}
			if ( eucSim > 0 )
				eucSim = Math.sqrt( eucSim );
			eucSimList.put( workerId, eucSim );
		}
		return eucSimList;
	}
	
	//Jaccard similarity = (A and B) / (A or B), A or B 以testAde为主，只统计test context中的term
	//JaccardSim越大，说明expertise越大
	public HashMap<String, Double> retrieveJaccardSimilarity ( HashMap<String, HashMap<String, Double>> probExpertise, HashMap<String, Double> testAdeq, Double threshold ){
		HashMap<String, Double> jacSimList = new HashMap<String, Double>();
		
		HashSet<String> testTermList = new HashSet<String>();
		for ( String term : testAdeq.keySet() ){
			Double inadeq = 1.0 - testAdeq.get( term );
			if ( inadeq > threshold ){
				testTermList.add( term );
			}
		}
		
		for ( String workerId : probExpertise.keySet() ){
			HashSet<String> expTermList = new HashSet<String>();
			
			HashMap<String, Double> expertiseList = probExpertise.get( workerId );
			for ( String term : expertiseList.keySet() ){
				Double expertise = expertiseList.get( term );
				if ( expertise > threshold ){
					expTermList.add( term );
				}
			}
			
			int andTerms = 0;
			for ( String term : testTermList ){
				if ( expTermList.contains( term )){
					andTerms++;
				}
			}
			
			Double jacSim = 0.0;
			if ( testTermList.size() >0 )
				jacSim = andTerms*1.0 / ( testTermList.size() );
			
			jacSimList.put( workerId, jacSim );
		}
		return jacSimList;
	}
	
	//这里只是封装了一下，便于别的类调用
	public HashMap<String, HashMap<String, Double>> generateProbabiliticExpertise (TestProject project, TestTask task, HashMap<String, HashMap<Date, ArrayList<List<String>>>> curExpertiseList ){
		ProbabilisticExpertise probExpertiseTool = new ProbabilisticExpertise();
		HashMap<String, HashMap<String, Double>> probExpertise = probExpertiseTool.obtainWorkerExpertiseForTermWithAdjustfactor(project, task.getTaskDescription(), curExpertiseList );
		//<workerId, <term, P(wi|tj)>>，生成的就是限定在taskDescription的词；不是所有的词；后面计算相似度都将这个P(wi|tj)看作是人员expertise
		return probExpertise;
	}
	

	//暂时没有什么用处
	public HashMap<String, HashMap<String, Integer>> transformToRefinedExpertiseList ( TestProject project, int recTimePoint, HashMap<String, HashMap<Date, ArrayList<List<String>>>> curExpertiseList ){
		//<workerId, <term, bugNum>>
		HashMap<String, HashMap<String, Integer>> refinedExpertiseList = new HashMap<String, HashMap<String, Integer>>();
		for ( String workerId: curExpertiseList.keySet() ){
			HashMap<Date, ArrayList<List<String>>> history = curExpertiseList.get( workerId );
			
			HashMap<String, Integer> refinedExpertise = new HashMap<String, Integer>();
			if ( refinedExpertiseList.containsKey( workerId )){
				refinedExpertise = refinedExpertiseList.get( workerId );
			}
			
			for ( Date date : history.keySet() ){
				ArrayList<List<String>> reportsList = history.get( date );
				for ( int i =0; i <reportsList.size(); i++ ){
					List<String> termList = reportsList.get( i );
					for ( int j =0; j < termList.size(); j++ ){
						String term = termList.get( j );
						int bugNum = 1;
						if ( refinedExpertise.containsKey( term )){
							bugNum = refinedExpertise.get( term ) +1 ;
						}
						refinedExpertise.put( term, bugNum );
					}
				}
			}
			
			refinedExpertiseList.put( workerId, refinedExpertise );
		}
		return refinedExpertiseList;
	}
	
	public HashMap<String, HashMap<String, Double>> getProbExpertise() {
		return probExpertise;
	}
}
