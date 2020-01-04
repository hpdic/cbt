package com.nvada.blocklite.net;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.nvada.blocklite.config.Constants;
import com.nvada.blocklite.data.DataCell;
import com.nvada.blocklite.log.Logger;

public class Master extends NetNode {
	
	private int hubPort = Constants.DEFAULT_HUB;
	private boolean usbHub = false;
	
	private LinkedList<Integer> workList;
    
    private LinkedList<Action> proposalACKList;
    private LinkedList<Action> commitACKList;
    private LinkedList<Action> rollbackACKList;
   
	private String actionId = null;
    private int workerNum = 0;
    private int taskCount = 5;
    
    private Lock listLock = new ReentrantLock();
    
    public Master(String name, boolean recoverCrash) {
    	this.nodeName = name;
    	this.logFileName = this.nodeName + "_log.txt";
    	this.recoverCrash = recoverCrash;
        workList = new LinkedList<Integer>();
        proposalACKList = new LinkedList<Action>();
        commitACKList = new LinkedList<Action>();
        rollbackACKList = new LinkedList<Action>();
    }
  
	private void readWorker() throws FileNotFoundException {
		workerNum = 0;
        Scanner scanner = new Scanner(new File("resources/work"));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            try {
            	int port = Integer.parseInt(line);
            	workList.add(port);
            	workerNum++;
            } catch(NumberFormatException e) {
            	continue;
            }
        }
        scanner.close();
    }
	
	public void connect2Hub(boolean usehub) {
		this.usbHub = usehub;
	}
	
	public void setTaskCount(int taskCnt) {
		if(taskCnt >= 1) {
			this.taskCount = taskCnt;
		}
	}
	
	private void readHub() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File("resources/hub"));
        if(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            try {
            	hubPort = Integer.parseInt(line);
            } catch(NumberFormatException e) {
            	;
            }
        }
        scanner.close();
    }
    
    
	private void executAction(final String actionName) {
		
		if(false == this.isWork) {
			return;
		}
		
    	boolean isCrash = crashCheck();
    	
    	if(isCrash) {
    		outMessage(nodeName + " crashed");
    		this.stopChain();
    		return;
    	}
    	
        for (final int workerPort : workList) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                	
                	Action action = new Action(actionId, actionName, workerPort);
                	String cmd = action.asCommand();
                	
                	String resp = null;
                    handleMessage("send: " + cmd, true);
                    
                    try {
                    	Socket socket = null;
                    	if(usbHub) {
                    		socket = new Socket(Constants.HOST_NAME, hubPort);
                    	} else {
                    		socket = new Socket(Constants.HOST_NAME, workerPort);
                    	}
                        
                        socket.setSoTimeout(Constants.TIMEOUT_MS);
                        PrintStream out = new PrintStream(socket.getOutputStream());
                        out.println(cmd);
                        out.flush();
                        
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        resp = in.readLine();
                        
                        in.close();
                        out.close();
                        socket.close();
                    } catch (ConnectException e) {
                    	outMessage("Worker " + workerPort + " time out when " + actionName);
                    } catch (SocketTimeoutException e) {
                    	outMessage("Worker " + workerPort + " time out when " + actionName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
                    LinkedList<Action> list = rollbackACKList;
                    
                    if(actionName.equals(Action.PROPOSAL_ACTION)) {
                    	list = proposalACKList;
                    } else if (actionName.equals(Action.COMMIT_ACTION)) {
                    	list = commitACKList;
                    } 
                    
                    if (resp != null) {
                    	if(resp.equals(Action.ACK_STATUS)) {
                    		action.setStatus(Action.ACK_STATUS);
                    	} else {
                    		action.setStatus(Action.FAIL_STATUS);
                    	}
                    } else {
                    	action.setStatus(Action.TIMEOUT_STATUS);
                    }
                    
                    listLock.lock();
                    list.add(action);
                    listLock.unlock();
                    
                    recordAction(action.getName(), action.getStatus());
                    
                    if(resp != null) {
                    	handleMessage("receive: " + action.toString(), false);
                    }
                }
            });
            thread.start();
        }
    }
	
	private boolean waitForAckList(LinkedList<Action> list) {
		boolean received = false;
        for (int i = 0; i < 3; i++) {
        	listLock.lock();
        	received = (list.size() == workerNum);
        	listLock.unlock();
        	
            if (received) {
            	break;
            } 
            
            try {
            	//System.out.println("list.size: " + list.size() +" , workerNum: " + workerNum +", sleep: " + delay_ms/50 + "ms");
                Thread.sleep(Constants.TIMEOUT_MS/50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        return received;
    }
    
	private boolean checkACKList(LinkedList<Action> list) {
		boolean ack = false;
        listLock.lock();
		if (list.size() == workerNum) {
			ack = true;
        	for (Action workerStatus : list) {
                if (!workerStatus.getStatus().equals(Action.ACK_STATUS)) {
                	ack = false;
                	break;
                }
            }
        }
		listLock.unlock();
        return ack;
    }
    
	private boolean checkTimeoutList(LinkedList<Action> list) {
		boolean timeout = false;
		listLock.lock();
        for (Action workerStatus : list) {
            if (workerStatus.getStatus().equals(Action.TIMEOUT_STATUS)) {
            	timeout = true;
            	break;
            }
        }
        listLock.unlock();
        return timeout;
    }

	private void sendProposal() {
        actionId = UUID.randomUUID().toString();
        System.out.println("current taskId : " + actionId);
        
        executAction(Action.PROPOSAL_ACTION);
    }

	private boolean checkProposalACK() {
    	return checkACKList(proposalACKList);
    }

	private void sendCommit() {
    	executAction(Action.COMMIT_ACTION);
    }

	private boolean checkCommitACK() {
    	return checkACKList(commitACKList);
    }
    
	private void sendRollback() {
    	executAction(Action.ROLLBACK_ACTION);
    }

	private boolean checkRollbackACK() {
    	return checkACKList(rollbackACKList);
    }

	private void waitForSeconds(){
        try{
            Thread.sleep(Constants.TIMEOUT_MS/50);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
 
	@Override
	protected void workLoop() {
    	int cnt = 0;
    	try {
    		
    		while(cnt<taskCount && this.isWork) {
	    		this.workList.clear();    
	            this.proposalACKList.clear();
	        	this.commitACKList.clear();
	        	this.rollbackACKList.clear();
	        	
	        	this.readHub();
	        	this.readWorker();
	        	this.sendProposal();
	        	
	        	if(false == this.isWork) {
	        		break;
	        	}
	            
	            while(!this.waitForAckList(this.proposalACKList)){
                	this.waitForSeconds();
                }
	            
	            boolean isTimeOut = this.checkTimeoutList(this.proposalACKList);
	            
	            outMessage("isTimeOut = " + isTimeOut);
	            
	            if (false == isTimeOut && this.checkProposalACK() == true) {
	            	outMessage("***** proposal/ack = true *******");
	            	this.sendCommit();
	            	if(false == this.isWork) {
		        		break;
		        	}
	            	
	                while(!this.waitForAckList(this.commitACKList)){
	                	this.waitForSeconds();
	                }
	                
	                //isTimeOut = this.checkTimeoutList(this.commitACKList);
	                if(checkCommitACK()) {
	                	outMessage(actionId + "/commit");
	                }
	                
	            } else {
	            	if (isTimeOut == true) {
	            		outMessage("***** wait ack/proposal TimeOut *******");
	            	} else {
	            		outMessage("***** proposal/ack = false *******");
	            	}
	                
	            	this.sendRollback();
	            	if(false == this.isWork) {
		        		break;
		        	}
	            	
	                while(!this.waitForAckList(this.rollbackACKList)){
	                	this.waitForSeconds();
	                } 
	                //isTimeOut = this.checkTimeoutList(this.rollbackACKList);
	                if(checkRollbackACK()) {
	                	outMessage(actionId + "/rollback");;
	                }
	            }
	            
	            outMessage("*****************************************************\n");

	            cnt++;
    		}
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
	}
	
	@Override
	public void stopLoop() {
		stopChain();
	}
	
	@Override
	protected void onAddCell(DataCell cell) {
		// add to do
	}

    public static void main(String[] args) {
    	Logger.getInstance();
        Master master = new Master(readStringArg(args, 0, "Node.A"), readBooleanArg(args, 1, true));
        
        // conection to hub or conect to worker directly;
        boolean useHub  = readBooleanArg(args, 2, false);
        int taskCount = readIntArg(args, 3, 5);
        master.connect2Hub(useHub);
        master.setTaskCount(taskCount);
        master.startWork(2);
    }
}
