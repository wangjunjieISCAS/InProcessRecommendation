package com.dataProcess;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.fnlp.nlp.cn.CNFactory;
import org.fnlp.util.exception.LoadModelException;

import com.data.Constants;
import com.data.TestReport;



public class WordSegment {
	
	private static CNFactory factory;
	private static Set<String> stopWordList;
	
	//静态初始化，读取停用词表
	static {
		try {
			factory = CNFactory.getInstance("models");
			
			stopWordList = new HashSet<String>( );	
			BufferedReader br = new BufferedReader ( new FileReader ( Constants.INPUT_FILE_STOP_WORD ));
			String str = "";
			while ( (str = br.readLine() ) != null ){
				stopWordList.add( str.trim() );
			}
			br.close();
			
		} catch (LoadModelException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String[] segmentWord ( String strInput ){	
		if ( strInput == null || strInput.length() == 0 ){
			String[] words = {};
			return words;
		}
		
		String[] words = factory.seg( strInput );
		String[] remainWords = removeStopWords( words );
		return remainWords;
	}
	
	public static String[] removeStopWords( String words[]){
		ArrayList<String> remainWords = new ArrayList<String>();
		for ( int i = 0; i< words.length; i++ ){
			if ( words[i] == null || words[i].trim().equals( ""))
				continue;
			else if ( stopWordList.contains( words[i]))
				continue;
			else{
				String str = words[i].trim().replaceAll("\\d", "" );
				remainWords.add( words[i].trim() );	
			}				
		}
		return (String[])remainWords.toArray( new String[remainWords.size()]);
	}
	
	public static String[] segmentWordWithNV ( String strInput ){
		if ( strInput == null || strInput.length() == 0 ){
			String[] words = {};
			return words;
		}

		String results = factory.tag2String ( strInput );
		String[] words = results.split( " ");
		
		ArrayList<String> remainWords = new ArrayList<String>();
		for ( int i = 0; i< words.length; i++ ){
			if ( words[i] == null || words[i].trim().equals( ""))
				continue;
			String[] temp = words[i].trim().split( "/");
			if ( temp.length != 2 )
				continue;
			if ( temp[1].trim().contains("名词")  || temp[1].trim().contains( "动词") )
				remainWords.add( temp[0].trim() );				
		}
		
		String[] result =  (String[])remainWords.toArray( new String[remainWords.size()]);	
		result = removeStopWords( result );
		
		return result;		
	}
	
	
	public static String[] segmentWordRestricted ( String strInput ){
		if ( strInput == null || strInput.trim().length() == 0 ){
			String[] words = {};
			return words;
		}
		
		String[] wordTag = { "名词", "动词", "形容词", "副词", "形谓词", "态词", "叹词", "趋向词", "介词", "方位词", "限定词", "语气词"};
		
		String results = factory.tag2String ( strInput );
		String[] words = results.split( " ");
		
		ArrayList<String> remainWords = new ArrayList<String>();
		for ( int i = 0; i< words.length; i++ ){
			if ( words[i] == null || words[i].trim().equals( ""))
				continue;
			String[] temp = words[i].trim().split( "/");
			if ( temp.length != 2 )
				continue;
			
			boolean flag = true;
			for ( int j = 0; j < wordTag.length && flag ; j++){
				if ( temp[1].trim().contains( wordTag[j]))
					flag = false;
			}
			if ( !flag )
				remainWords.add( temp[0].trim() );		
		}
		
		String[] result = (String[])remainWords.toArray( new String[remainWords.size()]);
		/*
		for ( int i =0; i < result.length; i++ ){
			System.out.print ( result[i] + "-" );
		}
		System.out.println ();
		*/
		result = removeStopWords( result );
		
		return result;	
	} 
	
}
