package com.xiefayang.k_receiveTask;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;


public class ReceiveTask {
	
	
	ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
	
	@Test
	public void deploymentProcessDefinition(){
		
		// RepositoryService: 与流程定义和部署对象相关的Service
		Deployment deployment = processEngine.getRepositoryService() 
				.createDeployment() //创建一个部署对象
				.name("receiveTask") //添加部署的名称
				.addClasspathResource("diagrams/receiveTask.bpmn") //从classpath中加载资源, 一次只能加载一个
				.addClasspathResource("diagrams/receiveTask.png")
				.deploy(); //完成部署
		
		System.out.println("部署ID: " + deployment.getId());
		System.out.println("部署名称: " + deployment.getName());
	}
	
	
	/**
	 * 启动流程实例, 设置/获取流程变量, 向后执行一步
	 */
	@Test
	public void createProcessInstance(){
		
		String processDefinitionKey = "receiveTask";
		
		ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey(processDefinitionKey);
		
		System.out.println("流程实例ID: " + processInstance.getId());
		System.out.println("活动ID: " + processInstance.getActivityId());
		System.out.println("流程定义ID: " + processInstance.getProcessDefinitionId());
		
		/** 查询执行对象ID (注意不能用taskService查, 他不是普通的任务)*/
		Execution execution1 = this.processEngine.getRuntimeService().createExecutionQuery() //创建执行对象查询
					.processInstanceId(processInstance.getId()) //使用流程实例ID查询
					.activityId("receivetask1") //当前活动的ID, 对应receiveTask.bpmn中活动节点的属性值
					.singleResult();
		
		/** 使用流程变量传递业务参数值 */
		this.processEngine.getRuntimeService().setVariable(execution1.getId(), "当日销售额", 1000);
		
		/** 向后执行一步, 如果流程处于等待状态则向后执行 */
		this.processEngine.getRuntimeService().signal(execution1.getId());
		
		/** 查询执行对象ID */
		Execution execution2 = this.processEngine.getRuntimeService().createExecutionQuery() //创建执行对象查询
					.processInstanceId(processInstance.getId()) //使用流程实例ID查询
					.activityId("receivetask2") //当前活动的ID, 对应receiveTask.bpmn中活动节点的属性值
					.singleResult();
		
		/** 从流程变量中获取值 */
		Integer value = (Integer)this.processEngine.getRuntimeService().getVariable(execution2.getId(), "当日销售额");
		
		System.out.println("给老板发送短信, 当日销售额是: " + value);
		
		
	}
	
	
	
}
