
package com.nvada.blocklite.config;

import java.io.File;


public class ConfigUtil {
	
	private String resourcePath;
	private String configFileName;							// 资源文件存放的目录
	
	private static String projectPath=null;

	private final static String WORKER_CONFIG_FILE="conf";
	
	public final static boolean IS_DEBUG=false;				// 是否调试
		
	private static ConfigUtil configUtil=null; 	
	
	public static ConfigUtil getInstance()
	{
		if(configUtil==null){
			configUtil=new ConfigUtil();
		}
		
		return configUtil;
	}
	
	//获取当前的运行路径
	public static String getProjectPath(){
		
		if(projectPath==null){
			//获取当前运行的目录的 url，例
			//file:/Users/aren/DeepSingularity/tencent/project/CrossChain
			projectPath=new File("").getAbsolutePath();
			
			if(projectPath!=null && projectPath.length()>0){
				//除去 file:/ 头部
				projectPath=projectPath.replace("file:/", "");
			}
		}
		
		return projectPath;
	}
	
	private ConfigUtil()
	{
		projectPath=getProjectPath();
		System.out.println("Project Path: "+projectPath);
		
		//加上 resources 目录
		resourcePath=resourcePath+"/resources";
		System.out.println("Resource Path: "+resourcePath);
		
		configFileName=resourcePath+"/"+WORKER_CONFIG_FILE;
	}
	
	//是否在终端输出调试信息
	public static boolean OUT_DEBUG() {
		return IS_DEBUG;
	}
	
	public String getResourcesPath() {
		return resourcePath;
	}
	
	public String getConfigFileName() {
		return configFileName;
	}
}
