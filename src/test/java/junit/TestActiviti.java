package junit;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.junit.Test;

public class TestActiviti {
	
	@Test
	public void createTable1(){
		
		ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
		processEngineConfiguration.setJdbcDriver("com.mysql.jdbc.Driver");
		processEngineConfiguration.setJdbcUrl("jdbc:mysql://192.168.118.128:3306/activiti_test?useUnicode=true&characterEncoding=utf8");
		processEngineConfiguration.setJdbcUsername("root");
		processEngineConfiguration.setJdbcPassword("admin");
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
}
