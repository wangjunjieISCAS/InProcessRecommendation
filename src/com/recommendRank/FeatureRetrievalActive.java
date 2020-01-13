package com.recommendRank;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.data.Constants;

public class FeatureRetrievalActive {
	//抽取和activeness相关的features 
	public HashMap<String, ArrayList<Double>> retrieveActiveFeatures ( HashMap<String, HashMap<Date, ArrayList<String>>> curActiveList, Date curTime ){
		/* featureList: 
		 * 1 duration (in hours) with last bug, 
		 * 2 duration (in hours) with last report, 
		 * 3,4,5,6,7 bug number in last 8 hours, last 24 hours, last 1 week, last 2 week, in the past
		 * 8,9,10,11,12 report number in last 8 hours, last 24 hours, last 1 week, last 2 week, in the past
		 */
		HashMap<String, ArrayList<Double>> activeFeatureList = new HashMap<String, ArrayList<Double>>();
		for ( String workerId : curActiveList.keySet() ){
			ArrayList<Double> featureList = new ArrayList<Double>();
			HashMap<Date, ArrayList<String>> curActive = curActiveList.get( workerId );    //<Date, projectId:dupTag(-1 if nobug)>
			
			ArrayList<Double> featuresLastActive = this.retrieveDurationWithLastActivity(curActive, curTime);
			featureList.addAll( featuresLastActive );
			
			ArrayList<Double> featuresBugNum = this.retrieveBugNumberInPast(curActive, curTime, true );
			featureList.addAll( featuresBugNum );
			
			ArrayList<Double> featuresReportNum = this.retrieveBugNumberInPast(curActive, curTime, false );
			featureList.addAll( featuresReportNum );
			
			activeFeatureList.put( workerId, featureList );
		}
		
		return activeFeatureList;
	}
	
	public ArrayList<Double> retrieveDurationWithLastActivity ( HashMap<Date, ArrayList<String>> curActive, Date curTime ){
		Date earliestTime = null;
		try {
			earliestTime = Constants.dateFormat.parse( "2015-01-01 00:00:00" );
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Double durationLastBug = (curTime.getTime() - earliestTime.getTime() ) / (1000*60*60) + 1.0;
		Double durationLastReport = (curTime.getTime() - earliestTime.getTime() ) / (1000*60*60) + 1.0;
		for ( Date date : curActive.keySet() ){
			ArrayList<String> infoList = curActive.get( date );
			Boolean isBug = false;
			for ( int i =0; i < infoList.size() && !isBug; i++ ){
				String info = infoList.get( i );
				String[] temp = info.split("-");
				String tag = temp[1].trim();
				if ( !tag.equals( "-1")){
					isBug = true;
				}
			}
			
			Double duration = (curTime.getTime() - date.getTime() )/ (1000*60*60) + 1.0;
			if ( durationLastReport > duration ){
				durationLastReport = duration;
			}
			if ( isBug == true && durationLastBug > duration ){
				durationLastBug = duration;
			}				
		}
		
		ArrayList<Double> features = new ArrayList<Double>();
		features.add( durationLastBug);
		features.add( durationLastReport );
		
		return features;
	}
	
	//3,4,5,6,7 bug number in last 8 hours, last 24 hours, last 1 week, last 2 week, in the past
	//isForBug = true 表示只统计bug，isForBug = false表示统计所有的report
	public ArrayList<Double> retrieveBugNumberInPast ( HashMap<Date, ArrayList<String>> curActive, Date curTime , boolean isForBug){
		Double bug8Hours = 0.0, bug24Hours = 0.0, bug1Week = 0.0, bug2Week = 0.0, bugPast = 0.0;
		for ( Date date: curActive.keySet() ){
			Double duration = ( curTime.getTime() - date.getTime() )/ (1000*60*60)*1.0;
			ArrayList<String> infoList = curActive.get( date );
			for ( int i =0; i < infoList.size(); i++ ){
				String info = infoList.get( i );
				String[] temp = info.split( "-");
				String tag = temp[1].trim();
				if ( (isForBug == true && !tag.equals( "-1")) || (isForBug == false)){
					bugPast += 1.0;
					if ( duration <= 14*24 ){
						bug2Week += 1.0;
					}
					if ( duration <= 7*24){
						bug1Week += 1.0;
					}
					if ( duration <= 24 ){
						bug24Hours += 1.0;
					}
					if ( duration <= 8 ){
						bug8Hours += 1.0;
					}
				}
			}
		}
		
		ArrayList<Double> features = new ArrayList<Double>();
		features.add( bug8Hours);
		features.add( bug24Hours );
		features.add( bug1Week );
		features.add( bug2Week );
		features.add( bugPast );
		
		return features;
	}
}
