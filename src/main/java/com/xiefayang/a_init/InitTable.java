package com.xiefayang.a_init;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.junit.Test;

public class InitTable {
	
	@Test
	public void createTable1(){
		
		ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
		processEngineConfiguration.setJdbcDriver("com.mysql.jdbc.Driver");
		processEngineConfiguration.setJdbcUrl("jdbc:mysql://192.168.118.128:3306/activiti_test?useUnicode=true&characterEncoding=utf8");
		processEngineConfiguration.setJdbcUsername("root");
		processEngineConfiguration.setJdbcPassword("root");
		processEngineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
		
		ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();
		// 工作流核心对象 ProcessEngine
		System.out.println("processEngine1: " + processEngine);
	}
	
	
	@Test
	public void createTable2(){
		System.out.println("init table");
		ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.cfg.xml");
		ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();
		// 工作流核心对象 ProcessEngine
		System.out.println("processEngine2: " + processEngine);
	}
	
	
	/**
	 * getDefaultProcessEngine()会调用init方法, 
	 * 自动读取classpath下的activiti.cfg.xml文件来初始化数据库
	 */
	@Test
	public void createTable3(){
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		System.out.println("processEngine3: " + processEngine);
	}
	
}
