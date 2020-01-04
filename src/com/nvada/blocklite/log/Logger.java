package com.nvada.blocklite.log;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.nvada.blocklite.config.ConfigUtil;
import com.nvada.blocklite.utils.FileUtil;

/*
 * realize log storage
 * */


public class Logger {
	

	private String logPath;
	
	private static Logger logger=null;
	
	private static final String BLOCK_FILE="blockMinelog.txt";
	private static final String BLOCK_CREATE_FILE="blockCreateTime.txt";
	
	private static final String BLOCK_HISTORY_FILE="blockChainHistory.txt";
	private static final String TRANSACTION_CREATE_FILE="transactionCreate.txt";
	private static final String TRANSACTION_RECEIVE_FILE="transactionReceive.txt";
	
	private static final String NODE_FILE="node_log.txt";
	
	private static final String EVENT_FILE="event_log.txt";
	private static final String RUN_FILE="run_log.txt";
	private static final String ERROR_FILE="error_log.txt";
	
	private Logger()
	{
		logPath=ConfigUtil.getProjectPath();
		
		
		logPath=logPath+"/log";
		// System.out.println("logPath:"+logPath);
		
		
		FileUtil.createDir(logPath,false);
	}
	
	public static Logger getInstance()
	{
		if(logger==null){
			logger=new Logger();
		}
		
		return logger;
	}
	
	public String getLogPath() {
		return logPath;
	}
	
	//mine block
	public void logBlockMine(String info)
	{
		if(info==null || info.length()<1){
			return;
		}
		
		outMessage(info,prepareOutFile(BLOCK_FILE), false);
	}
	
	public void logBlockCreate(String info)
	{
		if(info==null || info.length()<1){
			return;
		}
		
		outMessage(info,prepareOutFile(BLOCK_CREATE_FILE), false);
	}
	
	public void logBlockHistory(String info)
	{
		if(info==null || info.length()<1){
			return;
		}
		
		outMessage(info,prepareOutFile(BLOCK_HISTORY_FILE), false);
	}
	
	public void logTxCreate(String info)
	{
		if(info==null || info.length()<1){
			return;
		}
		
		outMessage(info,prepareOutFile(TRANSACTION_CREATE_FILE), false);
	}
	
	public void logTxReceive(String info)
	{
		if(info==null || info.length()<1){
			return;
		}
		
		outMessage(info,prepareOutFile(TRANSACTION_RECEIVE_FILE), false);
	}
	

	public void logNode(String info)
	{
		if(info==null || info.length()<1){
			return;
		}
		
		outMessage(info,prepareOutFile(NODE_FILE), true);
	}
	

	public void logEvent(String info)
	{
		if(info==null || info.length()<1){
			return;
		}
		
		outMessage(info,prepareOutFile(EVENT_FILE), false);
	}
	

	public void logRun(String info)
	{
		if(info==null || info.length()<1){
			return;
		}
		
		outMessage(info,prepareOutFile(RUN_FILE), false);
	}
	

	public void logError(String info)
	{
		if(info==null || info.length()<1){
			return;
		}
		
		outMessage(info,prepareOutFile(ERROR_FILE), false);
	}
	

	private String prepareOutFile(String fileName)
	{
		String logFile=logPath+"/"+fileName;
		

		FileUtil.createFile(logFile,false);
		
		return logFile;
	}
	

	synchronized private void outMessage(String info, String outFile, boolean printMsg)
	{
		RandomAccessFile logFile=null;
		
		
		if(printMsg || ConfigUtil.OUT_DEBUG()){
			System.out.println(info);
		}
		
		try {
			logFile = new RandomAccessFile(outFile,"rw");
			
			logFile.seek(logFile.length());
			String outResult=info+"\r\n";
			logFile.write(outResult.getBytes("UTF-8"));
			
			logFile.close();
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}



