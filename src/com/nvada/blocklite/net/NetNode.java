package com.nvada.blocklite.net;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.nvada.blocklite.BlockChain;
import com.nvada.blocklite.data.DataCell;
import com.nvada.blocklite.data.MessageDataCell;
import com.nvada.blocklite.data.DataService.DataCellListener;
import com.nvada.blocklite.log.Logger;

public abstract class NetNode {
	
	protected boolean isWork = false;
	
	private BlockChain blockChain = null;
	private Lock lock = new ReentrantLock();
	protected boolean recoverCrash = true;
		
	protected String nodeName = "node";
	protected String logFileName = "node_log.txt";
	
	private int crashCnt = 0;
	private final static boolean RANDOM_CRASH = false;
	private Thread blockThread = null; 
	
	private OutPutMessageListener listener=null;
	
	protected static String readStringArg(String[] args, int index, String defaultValue) {
		String result = defaultValue;
		
		if(args != null && args.length > index) {
			result = args[index];
		} 
		
		return result;
	}
	
	
	protected static int readIntArg(String[] args, int index, int defaultValue) {
		int result = defaultValue;
		
		if(args != null && args.length > index) {
			try{
				result = Integer.parseInt(args[index]);
			} catch(Exception e) {
				e.printStackTrace();
				result = defaultValue;
			}
		} 
		
		return result;
	}
	
	protected static boolean readBooleanArg(String[] args, int index, boolean defaultValue) {
		boolean result = defaultValue;
		
		if(args != null && args.length > index) {
			try{
				result = Boolean.parseBoolean(args[index]);
			} catch(Exception e) {
				e.printStackTrace();
				result = defaultValue;
			}
		} 
		
		return result;
	}
	
	protected void outMessage(String message) {
		System.out.println(message);
		writeLog(message);
		
		if(listener!=null){
			listener.onOutMessage(message);
		}
	}
	
	// write to log file
	protected void writeLog(String log) {
		PrintWriter out = null;
		String logRoot = Logger.getInstance().getLogPath();
		try {
			out = new PrintWriter(new FileWriter(logRoot + "/" + logFileName, true));
		} catch (IOException e) {
			e.printStackTrace();
			out = null;
		} finally {
			if(out != null) {
				out.println(log); 
				out.flush();
				out.close();
			}
		}
	}
	
	protected boolean crashCheck() {
		
		boolean nodeCrash = false;
		
		if(RANDOM_CRASH) {
			Random random = new Random(System.nanoTime());
			nodeCrash = (random.nextInt(100) < 10);
		} else {
			crashCnt++;
			nodeCrash = (crashCnt % 3 == 0);
		}
		
		if(nodeCrash) {
			writeLog(nodeName + " crash");
        	lock.lock();
        	
        	if(blockChain != null) {
        		blockChain.getMainNode().crash();
        		try {
	            	if(this.recoverCrash) {
	            		int cnt = 0;
	            		// waitting 10 second for recover
	            		while(true == nodeCrash && cnt<200) {
	            			Thread.sleep(50);
	            			nodeCrash = blockChain.getMainNode().isCrash();
	            			cnt++;
	            		}
	            	}
	            	
	            	if(nodeCrash) {
	            		if(blockThread != null) {
	            			blockChain.stop();
	            			blockThread.join(30);
	            			blockThread = null;
	            		}
	            	}
        		} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
        	}
        	lock.unlock();
        }
		
		return nodeCrash;
	}
	
	protected MessageDataCell handleMessage(String message, boolean send) {
    	outMessage(message);
    	
    	MessageDataCell cell = new MessageDataCell(message);
    	
    	lock.lock();
    	
    	if(blockChain != null) {
    		if(send) {
        		blockChain.sendCell(cell);
        	} else {
        		blockChain.recvCell(cell);
        	}
    	}
    	lock.unlock();
    	
    	return cell;
    }
	
	protected void runBlockChain(final int numPeers) {
		
		if(blockThread != null) {
			try {
				blockThread.join(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			blockThread = null;
		}
		
		blockThread = new Thread() {
			public void run() {
				while(isWork) {
					lock.lock();
					blockChain = new BlockChain(numPeers, 60 * 1000, 10 *60 * 1000);
					blockChain.addDataCellListener(cellListener);
					blockChain.setCrashOption(recoverCrash);
					lock.unlock();
					
					blockChain.run();
				}
				
				Logger.getInstance().logRun("End of block chain");
			}
		};
		blockThread.start();
	}
	
	public void startWork(int numPeers) {
		this.isWork = true;
		
		runBlockChain(numPeers);
		workLoop();
		
		stopChain();
		
	}
	
	protected void stopChain() {
		this.isWork = false;
		
		lock.lock();
		
		if(blockChain != null) {
			blockChain.stop();
		}
		lock.unlock();
	}
	
	protected void recordAction(String actionName, String ack) {
		if(this.listener != null) {
			this.listener.onAction(actionName, ack);
		}
	}
	
	abstract public void stopLoop();
	abstract protected void workLoop();
	abstract protected void onAddCell(DataCell cell);
	
	
	private DataCellListener cellListener = new DataCellListener() {

		@Override
		public void onDataCellDidAdd(DataCell cell) {
			onAddCell(cell);
		}
	};
	
	public void setListener(OutPutMessageListener listener) {
		this.listener = listener;
	}

	public static interface OutPutMessageListener {
		public void onOutMessage(String info);
		public void onAction(String actionName, String ack);
	};
}
