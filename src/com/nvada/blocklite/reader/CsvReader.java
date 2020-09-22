
package com.nvada.blocklite.reader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {

	/**
	 * 读取
	 * 
	 * @param filepath csv file path，csv file would crease if not exist
	 * @param dataList data in List
	 * @return
	 */
	public static boolean exportCsv(File filepath, List<String> dataList) {
		boolean isSucess = false;
		FileOutputStream out = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		
		try {
			out = new FileOutputStream(filepath);
			osw = new OutputStreamWriter(out);
			bw = new BufferedWriter(osw);
			if (dataList != null && !dataList.isEmpty()) {
				for (String data : dataList) {
					bw.append(data).append("\r");
				}
			}
			isSucess = true;
		} catch (Exception e) {
			isSucess = false;
		} finally {
			if (bw != null) {
				try {
					bw.close();
					bw = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (osw != null) {
				try {
					osw.close();
					osw = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
					out = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return isSucess;
	}

	/**
	 * import csv as list
	 * @param filepath csv file path
	 * @return
	 */
	public static List<String> importCsv(File filepath) {
		List<String> dataList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filepath));
			String line = "";
			while ((line = br.readLine()) != null) {
				dataList.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
					br = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return dataList;
	}	

	/**
	 * Demo: import CSV to list
	 */
	public static void importCsv() {
		String filepath = "data/demo.csv";
		List<String> dataList = CsvReader.importCsv(new File(filepath));
		if (dataList != null && !dataList.isEmpty()) {
			// skip first line
			for (int i = 1; i < dataList.size(); i++) {
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

	/**
	 * Demo: export list to CSV
	 */
	public static void exportCsv() {
		List<String> dataList = new ArrayList<String>();
		dataList.add("number,name,sex");
		dataList.add("1,Jim,man");
		dataList.add("2,Smith,man");
		dataList.add("3,Lucy,female");
		String filepath = "data/demo.csv";
		boolean isSuccess = CsvReader.exportCsv(new File(filepath), dataList);
		if(isSuccess) {
			System.out.println("saved " + filepath + " success.");
		} else {
			System.out.println("saved " + filepath + " failed.");
		}
	}
	
	/**
	 * test export csv and import csv 
	 * @param args
	 */
	public static void main(String[] args) {
		 exportCsv();
		 importCsv();
	}
}