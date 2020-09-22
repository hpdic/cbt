
package com.nvada.blocklite.reader;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.nvada.blocklite.data.Transaction;

/*
Txhash, Blockno, UnixTimestamp, DateTime, From, To, Quantity
 */

public class TransactionReader {
	
	public List<Transaction>readTxfromCsv(String filepath) {
		List<Transaction> txList = new ArrayList<Transaction>();
		
		List<String> dataList = CsvReader.importCsv(new File(filepath));
		if (dataList != null && !dataList.isEmpty()) {
			Transaction tx = null;
			String line = null;
			String[] fields = null;
			Timestamp timestamp = null;
			// skip first line
			for (int i = 1; i < dataList.size(); i++) {
				line = dataList.get(i);
				
				if(line == null || line.isEmpty()) {
					continue;
				}
				
				line = line.replace("\"", ""); 
				
				fields = line.split(",");
				
				if(fields == null || fields.length != 7) {
					continue;
				}
				
				//fields: Txhash, Blockno, UnixTimestamp, DateTime, From, To, Quantity
				// Transaction: String uDtuId, String senderID, String receiverID, float amount, Timestamp dtuTime
				
				float amount = 0;
				try {
					amount = Float.parseFloat(fields[6]);
				} catch(Exception e) {
					e.printStackTrace(); break;
				}
				
				long unixTimestamp = 0;
				try {
					unixTimestamp = Long.parseLong(fields[2]);
				} catch(Exception e) {
					e.printStackTrace(); break;
				}
				
				timestamp = new Timestamp(unixTimestamp); 
				tx = new Transaction(fields[0], fields[4], fields[5], amount, timestamp);
				txList.add(tx);
				
				// System.out.println(tx.toString());
			}
		}
		
		return txList;
	}
	
	@Test
	public void testReader() {
		readTxfromCsv("data/export-token.csv");
	}
}