package com.recommendBasic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import com.data.TestProject;
import com.data.TestReport;
import com.dataProcess.TestProjectReader;

public class RecTimePoint {

	public RecTimePoint() {
		// TODO Auto-generated constructor stub
	}
	
	//确定需要推荐的时间点，以第i个报告衡量；当连续stablePara个报告没有新的缺陷时，则认为是合适的推荐时间点;recTimePint假设该index对应的缺陷报告已经提交了，检查的是该位置+1
	//这个暂时不用了，只用另外一个
	public ArrayList<Integer> decideRecTimePoint ( TestProject project, int stablePara ){
		ArrayList<Integer> recTimeList = new ArrayList<Integer>();   //因为会有多个符合条件的
		
		//找到最后一个发现的缺陷，因为如果后面没有缺陷了，那就没有必要预测了
		ArrayList<TestReport> reportList = project.getTestReportsInProj();
		int lastBugIndex = reportList.size();
		HashSet<String> noDupBugSet = new HashSet<String>();
		for ( int i =0; i < reportList.size(); i++ ){
			TestReport report = reportList.get( i );
			String bugTag = report.getTag();
			String dupTag = report.getDuplicate();
			if ( bugTag.equals( "审核通过") && !noDupBugSet.contains( dupTag)){
				lastBugIndex = i;
			}
		}
		
		int stableNum = 0;
		Boolean isMarked = false;
		noDupBugSet.clear();
		for ( int i =0; i < lastBugIndex; i++ ){    //因为越往后推荐
			TestReport report = reportList.get( i );
			String bugTag = report.getTag();
			String dupTag = report.getDuplicate();
			//System.out.println( bugTag + " " + dupTag );
			
			if ( bugTag.equals("审核通过") && !noDupBugSet.contains( dupTag )){   //is a new bug
				stableNum = 0;
				noDupBugSet.add( dupTag );
				isMarked = false;
				
				continue;
			}
			stableNum++;
			//System.out.println ( i + ": " + stableNum );
			if ( !isMarked && stableNum >= stablePara ){
				recTimeList.add( i );
				isMarked = true;
				//System.out.println( "***************************** recTime: " + i);
			}
		}
		
		return recTimeList;
	}
	
	/* stablePara 控制的是有连续多少个报告没有新的缺陷
	 * lengthPara 控制的是一共有连续多长串的non-contributing reports
	 * 这个方法主要用于在建立模型时使用；在要进行预测时，相对于用到了feature information，是不严谨的
	 */
	public ArrayList<Integer> decideRecTimePoint ( TestProject project, int stablePara, int lengthPara ){
		//先统计出来，以i开头的报告，有连续多长的non-contributing stage
		HashSet<String> noDupBugSet = new HashSet<String>();
		ArrayList<Boolean> reportStatusList = new ArrayList<Boolean>();   //如果是new bug，为true；否则为false
		ArrayList<TestReport> reportList = project.getTestReportsInProj();
		for ( int i =0; i < reportList.size(); i++ ){
			TestReport report = reportList.get( i );
			String bugTag = report.getTag();
			String dupTag = report.getDuplicate();
			
			if ( bugTag.equals("审核通过") && !noDupBugSet.contains( dupTag )) { // is a new bug
				reportStatusList.add( true );
				noDupBugSet.add( dupTag );
			}
			else{
				reportStatusList.add( false );
			}
		}
		
		TreeMap<Integer, Integer> stableInfoList = new TreeMap<Integer, Integer>();   //<beginPoint, length of the stable stage>
		int noBugBeginIndex = 0;
		Boolean isFormerBug = true;
		for ( int i = 0; i< reportStatusList.size(); i++ ){
			if ( reportStatusList.get(i) == true && isFormerBug == false ){   //从noBugBeinIndex - i-1 均为non contributing stage
				int length = i - noBugBeginIndex ;
				if ( length > 1 )
					stableInfoList.put( noBugBeginIndex, length );
				isFormerBug = true;
			}
			if ( reportStatusList.get(i) == false && isFormerBug == true){
				isFormerBug = false;
				noBugBeginIndex = i;
			}
		}
		
		//找到最后一个发现的缺陷，因为如果后面没有缺陷了，那就没有必要预测了
		int lastBugIndex = reportList.size();
		noDupBugSet.clear();
		for ( int i =0; i < reportList.size(); i++ ){
			TestReport report = reportList.get( i );
			String bugTag = report.getTag();
			String dupTag = report.getDuplicate();
			if ( bugTag.equals( "审核通过") && !noDupBugSet.contains( dupTag)){
				noDupBugSet.add( dupTag );
				lastBugIndex = i;
			}
		}		
				
		ArrayList<Integer> recTimeList = new ArrayList<Integer>();
		for ( Integer beginIndex: stableInfoList.keySet() ){
			Integer length = stableInfoList.get( beginIndex );
			if ( length < lengthPara || length < stablePara )
				continue;
			
			Integer recTime = beginIndex + stablePara-1;
			
			if ( recTime < lastBugIndex )	
				recTimeList.add( recTime );
		}
		
		for ( int i =0; i < recTimeList.size(); i++ )
			System.out.println( recTimeList.get(i) );
			
		return recTimeList;
	}
	
	public static void main ( String[] args ){
		RecTimePoint pointTool = new RecTimePoint();
		
		TestProjectReader reader = new TestProjectReader();
		TestProject project = reader.loadTestProject( "data/input/project-orderByTime/366-206-bug通辽碧桂园V2.07.160526测试_1470654364.csv");
		pointTool.decideRecTimePoint(project, 5, 7);
		
		
	}
}
