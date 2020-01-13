package com.dataProcess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.csvreader.CsvReader;
import com.data.Constants;
import com.data.TestProject;
import com.data.TestReport;
import com.data.TestTask;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/*
 * ���ļ��ж�ȡ���Ա��棬����Ϊһ��testProject�洢
 */
public class TestProjectReader {

	public TestProjectReader() {
		// TODO Auto-generated constructor stub
	}
	
	public String transferString ( String str ){
		String result = str;
		result = result.replaceAll( "\r\n", " " );
		result = result.replaceAll( "\r", " " );
		result = result.replaceAll( "\n", " " );
		
		return result;
	}
	
	public TestProject loadTestProject ( String fileName ){
		int begin = 0;
		if ( fileName.contains( "/"))
			begin = fileName.lastIndexOf("/") + 1;
		int end = fileName.length() - 4;
		String projectName = fileName.substring( begin, end ) ;
		System.out.println( "projectName is: " + projectName );
		TestProject testProject = new TestProject ( projectName );
		
		Date closeDate = null;
		SimpleDateFormat formatLine = new SimpleDateFormat ("yyyy/MM/dd HH:mm");
		SimpleDateFormat formatCon = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
		try {
			BufferedReader br = new BufferedReader(new FileReader( new File ( fileName )));
			
			CsvReader reader = new CsvReader( br, ',');
			
			// ������ͷ   �����Ҫ��ͷ�Ļ�����Ҫд��䡣  
			reader.readHeaders(); 
			//���ж������ͷ������      
			int index = 0;
	        while ( reader.readRecord() ){
	        	int id = index++;
	        	String userId = reader.get( "�û�id");
	        	String testCaseId =  reader.get( "case���");
	        	String testCaseName = reader.get( "case����");
	        	
	        	String location = reader.get( "����");
	        	String phoneType = reader.get( "����");
	        	String OS = reader.get( "����ϵͳ");
	        	String network = reader.get( "���绷��");
	        	String ISP = reader.get( "��Ӫ��");
	        	String ROM = reader.get( "ROM��Ϣ");
	        	
	        	String temp =  reader.get( "�ύʱ��");
	        	Date submitTime  = null;
	        	if ( temp.contains( "-")) {
	        		submitTime = formatCon.parse( temp );
	        	}
	        	else {
	        		submitTime = formatLine.parse( temp );
	        	}
	        	if ( closeDate == null || closeDate.compareTo( submitTime ) < 0 )
	        		closeDate = submitTime;
	        	
	        	//��ȡ���Ƿִ�֮���
	        	String bugDetail =  reader.get( "bug����");
	        	String reproSteps =  reader.get( "���ֲ���");
	        	
	        	bugDetail = bugDetail.replaceAll("\\s*", "");
	        	bugDetail = bugDetail.replaceAll( "[\\pP\\p{Punct}]","" );			
				
	        	reproSteps = reproSteps.replaceAll("\\s*", "");
	        	reproSteps = reproSteps.replaceAll( "[\\pP\\p{Punct}]","" );	
				
	        	String isKnown = reader.get( "�Ƿ�δ֪");
	        	String priority = reader.get( "���ȼ�");
	        	String tag =  reader.get( "���״̬");
	        	String duplicate = reader.get( "�ظ����");
	        	TestReport report = new TestReport ( id, userId, testCaseId, testCaseName, location, phoneType, OS, network, ISP, ROM, 
	        			submitTime, bugDetail, reproSteps, isKnown, priority, tag, duplicate);
	        	
	        	//System.out.println( report );
	        	
	        	testProject.getTestReportsInProj().add( report );
	        }
			testProject.setCloseTime( closeDate );
			
	        reader.close();
			System.out.println ( "testProject is: " + fileName + " size: " + testProject.getTestReportsInProj().size()  );
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		return testProject;
	}
	
	//the difference between loadTestProjectAndTaskDescription and loadTestProject is that this method also loas the test task descriptions from another file
	public TestProject loadTestProjectAndTask ( String fileName, String taskFileName ){
		TestProject project = this.loadTestProject( fileName );
		ArrayList<String> taskDes = new ArrayList<String>();
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader( new File ( taskFileName )));
			String line = "";
			while ( ( line = br.readLine() ) != null ) {
				String[] temp = line.split( " ");
				
				for ( int i =0; i < temp.length; i++ ) {
					taskDes.add( temp[i] );
				}
			}
			
			TestTask task = new TestTask ( taskDes );
			project.setTestTask( task );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return project;
	}
	
	public ArrayList<TestProject> loadTestProjectList ( String projectFolder ){
		ArrayList<TestProject> projectList = new ArrayList<TestProject>();
		
		File projectsFolder = new File ( projectFolder );
		if ( projectsFolder.isDirectory() ){
			String[] projectFileList = projectsFolder.list();
			for ( int i = 0; i< projectFileList.length; i++ ){
				String projectFileName = projectFolder + "/" + projectFileList[i];
				
				TestProject project = this.loadTestProject( projectFileName ); 
				projectList.add( project );
			}				
		}			
		return projectList;
	}
	
	public ArrayList<TestProject> loadTestProjectAndTaskList ( String projectFolder, String taskFolder ){
		ArrayList<TestProject> projectList = new ArrayList<TestProject>();
		
		File projectsFolder = new File ( projectFolder );
		if ( projectsFolder.isDirectory() ){
			String[] projectFileList = projectsFolder.list();
			for ( int i = 0; i< projectFileList.length; i++ ){
				String projectFileName = projectFolder + "/" + projectFileList[i];
				
				String projectName = projectFileList[i].substring( 0, projectFileList[i].length() - 4);
				String taskFileName = taskFolder + "/" + projectName + ".txt";
				
				TestProject project = this.loadTestProjectAndTask( projectFileName, taskFileName );
				projectList.add( project );
			}				
		}			
		
		return projectList;
	}
	
	public ArrayList<TestProject> loadTestProjectAndTaskListBasedId ( int beginTaskId, int endTaskId, String projectFolder, String taskFolder ){
		ArrayList<TestProject> projectList = new ArrayList<TestProject>();
		File projectsFolder = new File ( projectFolder );
		if ( projectsFolder.isDirectory() ){
			String[] projectFileList = projectsFolder.list();
			for ( int i = 0; i< projectFileList.length; i++ ){
				String[] temp = projectFileList[i].split( "-");
				Integer index = Integer.parseInt( temp[0] );
				if ( index < beginTaskId || index > endTaskId ) {
					continue;
				}
				
				String projectFileName = projectFolder + "/" + projectFileList[i];
				
				String projectName = projectFileList[i].substring( 0, projectFileList[i].length() - 4);
				String taskFileName = taskFolder + "/" + projectName + ".txt";
				
				TestProject project = this.loadTestProjectAndTask( projectFileName, taskFileName );
				projectList.add( project );
			}				
		}			
		
		return projectList;
	}
	
	public HashMap<Integer, TestProject> loadTestProjectAndTaskMapBasedId ( int beginTaskId, int endTaskId, String projectFolder, String taskFolder ){
		HashMap<Integer, TestProject> projectMap = new HashMap<Integer, TestProject>();
		
		File projectsFolder = new File ( projectFolder );
		if ( projectsFolder.isDirectory() ){
			String[] projectFileList = projectsFolder.list();
			for ( int i = 0; i< projectFileList.length; i++ ){
				String[] temp = projectFileList[i].split( "-");
				Integer index = Integer.parseInt( temp[0] );
				if ( index < beginTaskId || index > endTaskId ) {
					continue;
				}
				
				String projectFileName = projectFolder + "/" + projectFileList[i];
				
				String projectName = projectFileList[i].substring( 0, projectFileList[i].length() - 4);
				String taskFileName = taskFolder + "/" + projectName + ".txt";
				
				TestProject project = this.loadTestProjectAndTask( projectFileName, taskFileName );
				projectMap.put( index, project );
			}				
		}			
		
		return projectMap;
	}
	
	public TestProject loadTestProjectAndTaskBasedId ( int taskId, String projectFolder, String taskFolder  ) {
		ArrayList<TestProject> projectList = this.loadTestProjectAndTaskListBasedId( taskId, taskId, projectFolder, taskFolder);
		if ( projectList.size() ==0 || projectList.size() > 1) {
			System.out.println( "Wrong in loadTestProjectAndTaskBasedId! " + projectList.size() );
		}
		return projectList.get( 0 );
	}
	
	public static void main ( String args[] ){
		TestProjectReader projReader = new TestProjectReader();
		TestProject project = projReader.loadTestProject( "data/input/project/368-166-bug����V4.1.6����_1470654360.csv");
		
	}
}
