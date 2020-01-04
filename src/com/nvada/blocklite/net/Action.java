package com.nvada.blocklite.net;

import com.nvada.blocklite.config.Constants;

public class Action {
	protected int workerPort;
	
	protected String uid;
	protected String name;		// proposal \ commit \ rollback
	protected String status;		// send \ receive \ ack \ fail  
	
    public final static String ACK_STATUS = "ack";
	public final static String FAIL_STATUS = "fail";
	public final static String TIMEOUT_STATUS = "timeout";
	
	public final static String PROPOSAL_ACTION = "proposal";
	public final static String COMMIT_ACTION = "commit";
	public final static String ROLLBACK_ACTION = "rollback";
	
	public final static int ALL_WOKER_ID = -1;
	
	public static Action parse(String line) {
		Action action = null;
		
		if(line != null) {
			String fields[] = line.split("/");
			if(fields != null && fields.length>=3) {
				int workerId = Constants.DEFAULT_WORK;
				try {
					workerId = Integer.parseInt(fields[2]);
	            } catch(NumberFormatException e) {
	            	;
	            }
				if(fields.length == 3) {
					action = new Action(fields[0], fields[1], workerId);
				} else if(fields.length == 4) {
					action = new Action(fields[0], fields[1], fields[3], workerId);
				}
    		}
		}
		
		
		return action;
	}
	
	public Action(String uid, String actionName, String actionStatus) {
		this(uid, actionName, actionStatus, ALL_WOKER_ID);
    }
	
	public Action(String uid, String actionName, int workerId) {
		this(uid, actionName, null, workerId);
	}
	
    public Action(String uid, String actionName, String actionStatus, int workerPort) {
    	this.uid = uid;
    	this.name = actionName;
        this.status = actionStatus;
        this.workerPort = workerPort;
    }
    
    public int getWorkerPort() {
		return workerPort;
	}

	public void setWorkerPort(int port) {
		this.workerPort = port;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String actionName) {
		this.name = actionName;
	}

	public String getStatus() {
        return status;
    }

    public void setStatus(String actionStatus) {
        this.status = actionStatus;
    }
    
    public String asCommand() {
    	return uid + "/"+ name + "/" + workerPort;
    }
    
    public String toString() {
    	return uid + "/" + name + "/" + workerPort + "/" + status;
    }
}
