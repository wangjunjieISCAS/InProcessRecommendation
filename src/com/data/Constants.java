package com.data;

import java.text.SimpleDateFormat;

public class Constants {
	public final static String INPUT_FILE_STOP_WORD = "data/input/stopWordListBrief.txt";
	//filter the 5% terms with the largest document frequency or smallest document frequency 
	public final static Double THRES_FILTER_TERMS_DF = 0.05;
	
	public final static String PROJECT_FOLDER = "data/input/project-orderByTime";
	public final static String TASK_DES_FOLDER = "data/input/taskDescription";
	
	public final static Integer TASK_DES_LENGTH = 5000;
	
	public final static SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public final static Integer TEST_SET_GROUP_SIZE = 28;
	
	public final static Integer REC_WORKER_NUM = 100;
	
	public final static Integer ACTIVE_THRES = 200; //prediction阶段，只考虑在前面75个小时active的worker，占所有情况的90%; 之前用的是200
	
	public final static String 	METHOD_NAME = "COCOME";
}
