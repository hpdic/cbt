
package com.nvada.blocklite.config;

import java.io.File;


public class ConfigUtil {
	
	private String resourcePath;
	private String configFileName;
	
	private static String projectPath=null;

	private final static String WORKER_CONFIG_FILE="conf";
	
	public final static boolean IS_DEBUG=false;
		
	private static ConfigUtil configUtil=null; 	
	
	public static ConfigUtil getInstance()
	{
		if(configUtil==null){
			configUtil=new ConfigUtil();
		}
		
		return configUtil;
	}
	
	
	public static String getProjectPath(){
		
		if(projectPath==null){
			
			//file:/Users/aren/DeepSingularity/tencent/project/CrossChain
			projectPath=new File("").getAbsolutePath();
			
			if(projectPath!=null && projectPath.length()>0){
				
				projectPath=projectPath.replace("file:/", "");
			}
		}
		
		return projectPath;
	}
	
	private ConfigUtil()
	{
		projectPath=getProjectPath();
		System.out.println("Project Path: "+projectPath);
		
		
		resourcePath=resourcePath+"/resources";
		System.out.println("Resource Path: "+resourcePath);
		
		configFileName=resourcePath+"/"+WORKER_CONFIG_FILE;
	}
	
	
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
