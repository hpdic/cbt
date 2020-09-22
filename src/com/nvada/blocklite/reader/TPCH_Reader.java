package com.nvada.blocklite.reader;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.nvada.blocklite.data.Transaction;

/*
	04/19/1995,203198.56
 */
public class TPCH_Reader {
	
	public List<Transaction>readTxfromCsv(String filepath) {
		List<Transaction> txList = new ArrayList<Transaction>();
		
		List<String> dataList = CsvReader.importCsv(new File(filepath));
		System.out.println("dataList.size(): " + dataList.size());
		if (dataList != null && !dataList.isEmpty()) {
			Transaction tx = null;
			String line = null;
			String[] fields = null;
			Timestamp timestamp = null;
			// data from the first line
			for (int i = 0; i < dataList.size(); i++) {
				line = dataList.get(i);
				
				if(line == null || line.isEmpty()) {
					continue;
				}
				
				line = line.replace("\"", ""); 
				
				fields = line.split(",");
				
				if(fields == null || fields.length != 2) {
					continue;
				}
				
				//fields: UnixTimestamp, Quantity
				// Transaction: String uDtuId, String senderID, String receiverID, float amount, Timestamp dtuTime
				
				float amount = 0;
				try {
					amount = Float.parseFloat(fields[1]);
				} catch(Exception e) {
					e.printStackTrace(); break;
				}
				
				String fmt = "dd/MM/yyyy";
				SimpleDateFormat dateFmt = new SimpleDateFormat(fmt);
				
				long unixTimestamp = 0;
				try {
					Date date = dateFmt.parse(fields[0]);
					unixTimestamp = date.getTime();
				} catch(Exception e) {
					e.printStackTrace(); break;
				}
				
				timestamp = new Timestamp(unixTimestamp); 
			    String uDtuId = Integer.toString(toHash(fields[0]));
				tx = new Transaction(uDtuId, null, null, amount, timestamp);
				txList.add(tx);
				
				//System.out.println(tx.toString());
			}
		}
		
		return txList;
	}
	
	public static int toHash(String key) {
		int arraySize = 11113; // 数组大小一般取质数
		int hashCode = 0;
		for (int i = 0; i < key.length(); i++) { // 从字符串的左边开始计算
			int letterValue = key.charAt(i) - 96;// 将获取到的字符串转换成数字，比如a的码值是97，则97-96=1
													// 就代表a的值，同理b=2；
			hashCode = ((hashCode << 5) + letterValue) % arraySize;// 防止编码溢出，对每步结果都进行取模运算
		}
		return hashCode;
	}

	public static void importCsv() {
		String filepath = "data/order.csv";
		List<String> dataList = CsvReader.importCsv(new File(filepath));
		if (dataList != null && !dataList.isEmpty()) {
			// skip first line
			for (int i = 0; i < 10; i++) {
				String s = dataList.get(i);
				String[] fields = s.split(",");
				StringBuffer sb = new StringBuffer(); 
				for(String field : fields) {
					sb.append(field);
					sb.append("  ");
				}
				System.out.println(sb.toString());
			}
		}
	}
	
	@Test
	public void testReader() {
		//importCsv();
		readTxfromCsv("data/orders.csv");
	}
}