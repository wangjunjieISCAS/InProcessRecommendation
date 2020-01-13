package com.data;

import java.util.ArrayList;

public class TestTask {
	ArrayList<String> taskDescription;
	
	public TestTask ( ) {
		taskDescription = new ArrayList<String>();
	}
	
	public TestTask ( ArrayList<String> taskDescription ) {
		this.taskDescription = taskDescription;
	}

	public ArrayList<String> getTaskDescription() {
		return taskDescription;
	}

	public void setTaskDescription(ArrayList<String> taskDescription) {
		this.taskDescription = taskDescription;
	}
}
