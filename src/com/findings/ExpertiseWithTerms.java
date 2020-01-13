package com.findings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import com.data.Constants;
import com.data.TestProject;
import com.dataProcess.TestProjectReader;
import com.recommendBasic.WorkerExpertiseHistory;

public class ExpertiseWithTerms {
	public void expertiseWithTermsCounter ( String projectFolder  ){
		TestProjectReader projReader = new TestProjectReader();
		ArrayList<TestProject> projList = projReader.loadTestProjectList( projectFolder );
		
		WorkerExpertiseHistory expHistory = new WorkerExpertiseHistory();
		HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerExpertiseHistory = expHistory.retrieveWorkerExpertiseHistory( projList );   //<worker, <Date, >
		
		ArrayList<String> workerList = this.obtainWorkers();
		//Random rand = new Random( 12345);
		int pickNum = 20;
		ArrayList<String> pickWorkerList = new ArrayList<String>();
		for ( int i =0; i < pickNum; i++ ){
			int pick = workerList.size()-i-1;
			pickWorkerList.add( workerList.get( pick) );			
		}	
		
		HashMap<String, HashMap<String, Integer>> workerExpertiseList = new HashMap<String, HashMap<String, Integer>>();  //<worker, <term, number>>
		for ( String workerId : workerExpertiseHistory.keySet() ){
			if ( !pickWorkerList.contains( workerId ))
				continue;
			HashMap<Date, ArrayList<List<String>>> expertise = workerExpertiseHistory.get( workerId );
			HashMap<String, Integer> expertiseTerms = new HashMap<String, Integer>();
			for ( Date date: expertise.keySet() ){
				ArrayList<List<String>> termsList = expertise.get( date);
				for ( int i =0; i < termsList.size(); i++ ){
					List<String> terms = termsList.get( i );
					for ( int j =0; j < terms.size(); j++ ){
						String term = terms.get(j);
						int count = 1;
						if ( expertiseTerms.containsKey( term ) ) 
							count += expertiseTerms.get( term );
						expertiseTerms.put( term, count );
					}
				}
			}
			workerExpertiseList.put( workerId, expertiseTerms );
		}
		
		HashMap<String, Integer> termOccurrence = new HashMap<String, Integer>();      //统计的是被多少人提到过
		for ( String workerId: workerExpertiseList.keySet() ){
			HashMap<String, Integer> expertise = workerExpertiseList.get( workerId );
			for ( String term: expertise.keySet() ){
				int occr = 1;
				if ( termOccurrence.containsKey( term ))
					occr += termOccurrence.get( term );
				termOccurrence.put( term , occr );
			}
		}
		
		System.out.println( "termOccurrence size is : " + termOccurrence.size() );
		List<Map.Entry<String,Integer>> termOccurrenceList = new ArrayList<Map.Entry<String,Integer>>( termOccurrence.entrySet());
        Collections.sort( termOccurrenceList,new Comparator<Map.Entry<String,Integer>>() {
            //升序排序
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }            
        });
        
        BufferedWriter writer;
		try {
			writer = new BufferedWriter( new FileWriter ( "data/output/findings/termsList.csv" ));
			
			for ( int i =0; i < termOccurrenceList.size(); i++ ) {
				Map.Entry<String, Integer> entry = termOccurrenceList.get( i );
				writer.write( entry.getKey() +"," + entry.getValue() );
				writer.newLine();
			}
			writer.flush();
			writer.close();
			
			writer.close();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
		
        
        ArrayList<String> pickTermList = new ArrayList<String>();
        int pickTermNum = 50;
        int begin = (int) (termOccurrenceList.size() * 0.05);
        int end = (int) ( termOccurrenceList.size() * 0.15);
        Random rand = new Random();
        for ( int i =0; i < pickTermNum; i++ ){
        	int index = rand.nextInt( end ) + begin;
        	
        	Map.Entry<String, Integer> entry = termOccurrenceList.get( index );
        	pickTermList.add( entry.getKey() );
        	System.out.println( "item and its occurrence " +  entry.getKey() + " "+ entry.getValue() );
        }
        
		HashMap<String, Integer[]> workerTermsDistList = new HashMap<String, Integer[]>();
		for ( String workerId : workerExpertiseList.keySet() ){
			HashMap<String, Integer> expertise = workerExpertiseList.get( workerId );
			Integer[] termOccur = new Integer[pickTermNum];
			for ( int i =0; i < termOccur.length; i++ )
				termOccur[i] = 0;
			for ( int i =0; i < pickTermList.size(); i++ ){
				String term = pickTermList.get( i );
				Integer occur = 0;
				if ( expertise.containsKey( term )){
					occur = expertise.get( term );
				}				
				termOccur[i] = occur;
			}
			workerTermsDistList.put( workerId, termOccur );
		}
		
		
		//output to file
		//BufferedWriter writer;
		try {
			writer = new BufferedWriter( new FileWriter ( "data/output/findings/expertise.csv" ));
			
			writer.write( "worker" + ","  );
			for ( int i =0; i < pickTermList.size(); i++ ){
				writer.write( pickTermList.get(i) + "," );
			}
			writer.newLine();
			for ( String worker : workerTermsDistList.keySet() ) {
				Integer[] termOccur = workerTermsDistList.get( worker );
				writer.write( worker + ",");
				for ( int i = 0; i < termOccur.length; i++ ){   
					writer.write( termOccur[i] +",");
				}
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
		ExpertiseWithTerms expertiseTool = new ExpertiseWithTerms();
		expertiseTool.expertiseWithTermsCounter( Constants.PROJECT_FOLDER );
	}
}

