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
	
	//ȷ����Ҫ�Ƽ���ʱ��㣬�Ե�i�����������������stablePara������û���µ�ȱ��ʱ������Ϊ�Ǻ��ʵ��Ƽ�ʱ���;recTimePint�����index��Ӧ��ȱ�ݱ����Ѿ��ύ�ˣ������Ǹ�λ��+1
	//�����ʱ�����ˣ�ֻ������һ��
	public ArrayList<Integer> decideRecTimePoint ( TestProject project, int stablePara ){
		ArrayList<Integer> recTimeList = new ArrayList<Integer>();   //��Ϊ���ж������������
		
		//�ҵ����һ�����ֵ�ȱ�ݣ���Ϊ�������û��ȱ���ˣ��Ǿ�û�б�ҪԤ����
		ArrayList<TestReport> reportList = project.getTestReportsInProj();
		int lastBugIndex = reportList.size();
		HashSet<String> noDupBugSet = new HashSet<String>();
		for ( int i =0; i < reportList.size(); i++ ){
			TestReport report = reportList.get( i );
			String bugTag = report.getTag();
			String dupTag = report.getDuplicate();
			if ( bugTag.equals( "���ͨ��") && !noDupBugSet.contains( dupTag)){
				lastBugIndex = i;
			}
		}
		
		int stableNum = 0;
		Boolean isMarked = false;
		noDupBugSet.clear();
		for ( int i =0; i < lastBugIndex; i++ ){    //��ΪԽ�����Ƽ�
			TestReport report = reportList.get( i );
			String bugTag = report.getTag();
			String dupTag = report.getDuplicate();
			//System.out.println( bugTag + " " + dupTag );
			
			if ( bugTag.equals("���ͨ��") && !noDupBugSet.contains( dupTag )){   //is a new bug
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
	
	/* stablePara ���Ƶ������������ٸ�����û���µ�ȱ��
	 * lengthPara ���Ƶ���һ���������೤����non-contributing reports
	 * ���������Ҫ�����ڽ���ģ��ʱʹ�ã���Ҫ����Ԥ��ʱ��������õ���feature information���ǲ��Ͻ���
	 */
	public ArrayList<Integer> decideRecTimePoint ( TestProject project, int stablePara, int lengthPara ){
		//��ͳ�Ƴ�������i��ͷ�ı��棬�������೤��non-contributing stage
		HashSet<String> noDupBugSet = new HashSet<String>();
		ArrayList<Boolean> reportStatusList = new ArrayList<Boolean>();   //�����new bug��Ϊtrue������Ϊfalse
		ArrayList<TestReport> reportList = project.getTestReportsInProj();
		for ( int i =0; i < reportList.size(); i++ ){
			TestReport report = reportList.get( i );
			String bugTag = report.getTag();
			String dupTag = report.getDuplicate();
			
			if ( bugTag.equals("���ͨ��") && !noDupBugSet.contains( dupTag )) { // is a new bug
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
			if ( reportStatusList.get(i) == true && isFormerBug == false ){   //��noBugBeinIndex - i-1 ��Ϊnon contributing stage
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
		
		//�ҵ����һ�����ֵ�ȱ�ݣ���Ϊ�������û��ȱ���ˣ��Ǿ�û�б�ҪԤ����
		int lastBugIndex = reportList.size();
		noDupBugSet.clear();
		for ( int i =0; i < reportList.size(); i++ ){
			TestReport report = reportList.get( i );
			String bugTag = report.getTag();
			String dupTag = report.getDuplicate();
			if ( bugTag.equals( "���ͨ��") && !noDupBugSet.contains( dupTag)){
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
		TestProject project = reader.loadTestProject( "data/input/project-orderByTime/366-206-bugͨ�ɱ̹�԰V2.07.160526����_1470654364.csv");
		pointTool.decideRecTimePoint(project, 5, 7);
		
		
	}
}
