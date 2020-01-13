package com.data;


public class CrowdWorker {
	String workerId;
	Activeness activeness;
	Expertise expertise;
	
	public CrowdWorker ( String workerId) {
		this.workerId = workerId;
	}
	
	public CrowdWorker ( String workerId, Activeness activeness, Expertise expertise ) {
		this.workerId = workerId;
		this.activeness = activeness;
		this.expertise = expertise;
	}

	public String getWorkerId() {
		return workerId;
	}

	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}

	public Activeness getActiveness() {
		return activeness;
	}

	public void setActiveness(Activeness activeness) {
		this.activeness = activeness;
	}

	public Expertise getExpertise() {
		return expertise;
	}

	public void setExpertise(Expertise expertise) {
		this.expertise = expertise;
	}	
}
