package com.nvada.blocklite.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;



/*
 * 文件工具类
 * */

public class FileUtil {
	
	//读取目录下的文件
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
	       
	
	//按指定的绝对路径创建一个新文件, 
	//removeOld为true则删除已经存在的文件
	//java自身生成的文件是utf-8文件
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
	
	//按指定的绝对路径创建一个新文件目录, 
	//removeOld为true则删除已经存在的文件目录
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
	
	//检查文件的编码
	//不同编码的文本，是根据文本的前两个字节来定义其编码格式的。定义如下：
	//ANSI： 无格式定义；
	//Unicode： 前两个字节为FFFE；
	//Unicode big endian： 前两字节为FEFF；
	//UTF-8： 前两字节为EFBB；
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
        
        // 对于UTF-8编码格式的文本文件，其前3个字节的值就是-17、-69、-65
        if (b[0] == -17 && b[1] == -69 && b[2] == -65){
        	 return "utf-8";
         } else {
        	 //可能是GBK，也可能是其他编码
        	 return "GBK";
         }
	}
	
	//验证文件,如果不正常则删除
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


