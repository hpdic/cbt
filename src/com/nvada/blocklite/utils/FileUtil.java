package com.nvada.blocklite.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class FileUtil {
	
	 public static ArrayList<String> readSubFileList(String path) {
		 
		 if(path == null) {
			 return null;
		 } 
		 
		 ArrayList<String> pathList = new ArrayList<String>();
		 
		 File file = new File(path);
		 
		 if (file.isDirectory()) {  
	            
			 String filePath = "";
			 
			 File[] dirFile = file.listFiles();  

			 for (File item : dirFile) {
				 if(item.isDirectory()) {
					 filePath = item.getPath() + "/" + item.getName();;
				 } else {
					 filePath = item.getPath() + "/" + item.getName(); 
				 }
				 
				 pathList.add(filePath);
			 }
		 }
		 
		 return pathList;
	 }
	       

	public static void createFile(String filePath,boolean removeOld)
	{
		File file=new File(filePath);
		
		if(!file.exists()){
			
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.out.println(""+e.toString());
			}
		} else{
			
			if(removeOld)
			{
				file.delete();
				try {
					file.createNewFile();
				} catch (IOException e) {
					System.out.println(""+e.toString());
				}
			}
		}
	}
	

	public static void createDir(String filePath,boolean removeOld)
	{
		File file=new File(filePath);
		
		if(!file.exists()){			
			file.mkdir();
		} else{
			
			if(removeOld) {
				file.delete();
				file.mkdir();
			}
		}
	}

	public static String getCharset(String path)
	{
		byte[] b = new byte[3];
		
		File file = new File(path);
        InputStream in=null;
        
		try {
			in = new FileInputStream(file);
			in.read(b);
	        in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
       
        if (b[0] == -17 && b[1] == -69 && b[2] == -65){
        	 return "utf-8";
         } else {
        	 
        	 return "GBK";
         }
	}
	

	public static boolean verifyFile(String filePath,int minsize)
	{
		if(filePath==null || filePath.length()<1){
			return false;
		}
		
		if(minsize<1){
			minsize=1;
		}
		
		File file=new File(filePath);
		
		if(file.isFile() && file.exists()){
			
			long size=0;
			
			try{
				size=file.length();
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
			if(size>=minsize){
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean deletFile(String filePath)
	{
		if(filePath==null || filePath.length()<1){
			return false;
		}
		
		File file=new File(filePath);
		
		if(file.exists()){
			
			try{
				file.delete();
				return true;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return false;
	}
}


