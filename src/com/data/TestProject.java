package com.data;

import java.util.ArrayList;
import java.util.Date;

public class TestProject {
	String projectName;
	TestTask testTask;
	Date closeTime;
	ArrayList<TestReport> testReportsInProj;
	
	public TestProject( String projectName ) {
		// TODO Auto-generated constructor stub
		this.projectName = projectName;
		
		testTask = new TestTask();
		testReportsInProj = new ArrayList<TestReport>();
	}

	
	public TestProject( String projectName , Date closeTime) {
		// TODO Auto-generated constructor stub
		this.projectName = projectName;
		this.closeTime = closeTime;
		
		testTask = new TestTask();
		testReportsInProj = new ArrayList<TestReport>();
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}	

	public TestTask getTestTask() {
		return testTask;
	}

	public void setTestTask(TestTask testTask) {
		this.testTask = testTask;
	}

	public ArrayList<TestReport> getTestReportsInProj() {
		return testReportsInProj;
	}

	public void setTestReportsInProj(ArrayList<TestReport> testReportsInProj) {
		this.testReportsInProj = testReportsInProj;
	}

	public Date getCloseTime() {
		return closeTime;
	}

	public void setCloseTime(Date closeTime) {
		this.closeTime = closeTime;
	}
	
	
}
