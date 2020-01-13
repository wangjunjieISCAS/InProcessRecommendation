package com.dataProcess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.data.Constants;

public class ReadWriteTool {
	
	public void writerHashMapList ( List<Map.Entry<String,Double>> recList, String outFile ){
		try {
			BufferedWriter writer = new BufferedWriter ( new FileWriter ( new File ( outFile )));
			
			for ( int i =0; i < recList.size(); i++ ){
				Map.Entry<String, Double> recWorker = recList.get( i );
				writer.write( recWorker.getKey() + " " + recWorker.getValue() );
				writer.newLine();
			}
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writerHashMapHashMap ( HashMap<String, HashMap<String, Double>> workerContribList, String outFile ){
		try {
			BufferedWriter writer = new BufferedWriter ( new FileWriter ( new File ( outFile )));
			
			for ( String workerId : workerContribList.keySet() ){
				writer.write( workerId + ":");
				HashMap<String, Double> contrib = workerContribList.get( workerId );
				for ( String term : contrib.keySet() ){
					writer.write( term + "-" + contrib.get( term ) + " ");
				}
				writer.newLine();
			}			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writerHashMap ( HashMap<String, Double> testAdeqList, String outFile ){
		try {
			BufferedWriter writer = new BufferedWriter ( new FileWriter ( new File ( outFile )));
			
			for ( String workerId : testAdeqList.keySet() ){
				writer.write( workerId + ":" + testAdeqList.get( workerId ));
				writer.newLine();
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
