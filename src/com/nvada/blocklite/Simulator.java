package com.nvada.blocklite;

import com.nvada.blocklite.log.Logger;

public class Simulator {
	
	
	private final static int NODE_NUM = 3;
	private final static int RUN_MILLS = 1*60*1000;

	
	public static void main(String[] args) {
		Logger.getInstance();
		
		int numPeers = readIntArg(args, 0, NODE_NUM);
		int expectIntervalMills = readIntArg(args, 1, 60 * 1000);
		
		BlockChain blockChain = new BlockChain(numPeers, expectIntervalMills, RUN_MILLS);
		blockChain.run();
	}
	
	private static int readIntArg(String[] args, int index, int defaultValue) {
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
}


