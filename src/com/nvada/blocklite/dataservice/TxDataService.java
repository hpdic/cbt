package com.nvada.blocklite.dataservice;

import java.sql.Timestamp;
import java.util.List;
import com.nvada.blocklite.data.Transaction;

public abstract class TxDataService {
	
	private int index = 0;
	private int count = 0;
	private boolean isStart = false;
	
	private long startMills = 0;
	protected List<Transaction> txList;
	
	public abstract void readDataFromCsv();
	
	public int getCount() {
		return count;
	}
	
	public long getRunMills()
	{
		if(this.isStart) {
			return System.currentTimeMillis() - this.startMills;
		}
		return 0;
	}
	
	public void outputResult() {
		long runMills = this.getRunMills();
		System.out.println("transcation: " + this.count + ", " + runMills + " ms");
	}
	
	public void reset() {
		this.index = 0;
		this.count = 0;
		this.isStart = false;
		this.startMills = 0;
	}
	
	public Transaction nextTransaction(String senderID, String receiverID, Timestamp nextTxnTime) {
		
		if(this.txList != null && this.txList.size() > 0 ) {
			
			if(false == isStart) {
				isStart = true;
				startMills = System.currentTimeMillis(); 
			}
			
			Transaction tx = this.txList.get(index);
			tx.setSenderID(senderID);
			tx.setReceiverID(receiverID);
			tx.setDtuTime(nextTxnTime);
			
			//this.txList.remove(index);
			index = (index+1) % this.txList.size();
			count++;
			
			if(count % 100000 == 0) {
				System.out.println("count: " + count + ", " + (System.currentTimeMillis() - startMills) + " ms");
			}
			
			return tx;
		}
		
		return null;
	}
}