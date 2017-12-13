package com.xiefayang.j_start;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;


public class StartTest {
	
	
	ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
	
	@Test
	public void deploymentProcessDefinition(){
		
		// RepositoryService: 与流程定义和部署对象相关的Service
		Deployment deployment = processEngine.getRepositoryService() 
				.createDeployment() //创建一个部署对象
				.name("开始活动") //添加部署的名称
				.addClasspathResource("diagrams/start.bpmn") //从classpath中加载资源, 一次只能加载一个
				.addClasspathResource("diagrams/start.png")
				.deploy(); //完成部署
		
		System.out.println("部署ID: " + deployment.getId());
		System.out.println("部署名称: " + deployment.getName());
	}
	
	
	/**
	 * 启动流程实例+判断流程是否结束+查询历史
	 */
	@Test
	public void createProcessInstance(){
		
		//String processDefinitionId = "myProcess1:1:22504";
		String processDefinitionKey = "start";
		
		// RuntimeService: 与正在执行的流程实例和执行对象相关的Service
		ProcessInstance processInstance = processEngine.getRuntimeService()
				// 按照流程定义的id启动, 并不一定是最新版本, 因为该流程可能有多个版本
//				.startProcessInstanceById(processDefinitionId)  
				// 使用流程定义的key启动流程实例, key对应bpmn文件id的属性值(使用key值启动默认是按照最新版本的流程定义启动)
				.startProcessInstanceByKey(processDefinitionKey);
		
		System.out.println("流程实例ID: " + processInstance.getId());
		System.out.println("活动ID: " + processInstance.getActivityId());
		System.out.println("流程定义ID: " + processInstance.getProcessDefinitionId());
		
		//判断流程是否结束, 查询正在执行的执行对象表
		ProcessInstance rpi = this.processEngine.getRuntimeService().createProcessInstanceQuery()
				.processInstanceId(processInstance.getId()).singleResult();
		
		//如果流程结束, 查询历史流程相关信息
		if(null == rpi) {
			HistoricProcessInstance instance = this.processEngine.getHistoryService().createHistoricProcessInstanceQuery()
			.processInstanceId(processInstance.getId()).singleResult();
			
			System.out.println("流程结束了:" + instance.getId() + ", " + instance.getStartTime() + ", " + instance.getEndTime());
		}
		
	}
	
	
	
}
