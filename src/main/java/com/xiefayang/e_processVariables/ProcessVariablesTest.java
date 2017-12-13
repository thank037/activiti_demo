package com.xiefayang.e_processVariables;

import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;

public class ProcessVariablesTest {
	
ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
	
	/**
	 * 部署流程定义(从classpath)
	 */
	@Test
	public void deploymentProcessDefinitionByClasspath(){
		
		Deployment deployment = processEngine.getRepositoryService() 
				.createDeployment() 
				.name("myProcess001")
				.addClasspathResource("diagrams/process1.bpmn") 
				.addClasspathResource("diagrams/process1.png")
				.deploy(); 
		
		System.out.println("部署ID: " + deployment.getId());
		System.out.println("部署名称: " + deployment.getName());
	}
	
	
	/**
	 * 启动流程实例
	 */
	@Test
	public void createProcessInstance(){
		
		String processDefinitionKey = "myProcess1";
		
		ProcessInstance processInstance = processEngine.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey);
		
		System.out.println("流程实例ID: " + processInstance.getId());
		System.out.println("活动ID: " + processInstance.getActivityId());
		System.out.println("流程定义ID: " + processInstance.getProcessDefinitionId());
	}
	
	
	/**设置流程变量*/
	@Test
	public void setVariables(){
		/**与任务（正在执行）*/
		TaskService taskService = processEngine.getTaskService();
		//任务ID
		String taskId = "2104";
		/**一：设置流程变量，使用基本数据类型*/
//		taskService.setVariableLocal(taskId, "请假天数", 5);//与任务ID绑定
//		taskService.setVariable(taskId, "请假日期", new Date());
//		taskService.setVariable(taskId, "请假原因", "回家探亲，一起吃个饭");
		/**二：设置流程变量，使用javabean类型*/
		/**
		 * 当一个javabean（实现序列号）放置到流程变量中，要求javabean的属性不能再发生变化
		 *    * 如果发生变化，再获取的时候，抛出异常
		 *  
		 * 解决方案：在Person对象中添加：
		 * 		private static final long serialVersionUID = 6757393795687480331L;
		 *      同时实现Serializable 
		 * */
		Person p = new Person();
		p.setId(20);
		p.setName("翠花");
		taskService.setVariable(taskId, "人员信息(添加固定版本)", p);
		
		System.out.println("设置流程变量成功！");
	}
	
	/**获取流程变量*/
	@Test
	public void getVariables(){
		/**与任务（正在执行）*/
		TaskService taskService = processEngine.getTaskService();
		//任务ID
		String taskId = "2104";
		/**一：获取流程变量，使用基本数据类型*/
//		Integer days = (Integer) taskService.getVariable(taskId, "请假天数");
//		Date date = (Date) taskService.getVariable(taskId, "请假日期");
//		String resean = (String) taskService.getVariable(taskId, "请假原因");
//		System.out.println("请假天数："+days);
//		System.out.println("请假日期："+date);
//		System.out.println("请假原因："+resean);
		/**二：获取流程变量，使用javabean类型*/
		Person p = (Person)taskService.getVariable(taskId, "人员信息(添加固定版本)");
		System.out.println(p.getId()+"        "+p.getName());
	}
	
	
	/**
	 * 设置和获取流程变量的一些场景和方法演示
	 */
	@Test
	public void setAndGetVariables(){
		
		//只有RuntimeService和TaskService
		RuntimeService runtimeService = processEngine.getRuntimeService();
		TaskService taskService = processEngine.getTaskService();
		
		/**************设置流程变量*****************/
		//使用执行对象ID, 流程变量名, 设置流程变量值
//		runtimeService.setVariable(executionId, variableName, value);
		
		//使用执行对象ID, Map集合设置流程变量值, Map(key=流程变量名, value=流程变量值), 一次设置多个值
//		runtimeService.setVariables(executionId, variables);
		
		//与runtimeService同
//		taskService.setVariable(taskId, variableName, value);
//		taskService.setVariables(taskId, variables);
		
		//启动流程实例的同时, 设置流程变量(Map集合)
//		runtimeService.startProcessInstanceById(processDefinitionId, variables);
		
		//完成任务的同时, 设置流程变量(Map集合)
//		taskService.complete(taskId, variables);
		
		/****************获取流程变量****************/
//		runtimeService.getVariable(executionId, variableName);
//		runtimeService.getVariables(executionId);
//		runtimeService.getVariables(executionId, variableNames);
		
//		taskService.getVariable(taskId, variableName);
//		taskService.getVariables(taskId);
//		taskService.getVariables(taskId, variableNames);
		
	}
	
	
	
	/**
	 * 完成当前任务
	 */
	@Test
	public void completePersonalTask(){
		
		String taskId = "25005";
		
		processEngine.getTaskService().complete(taskId);
		
		System.out.println("完成任务" + taskId);
	}
	
	
	@Test
	public void queryHistoryProcessVariables(){
		
		List<HistoricVariableInstance> historicVariableInstances = processEngine.getHistoryService().createHistoricVariableInstanceQuery()
		.variableName("请假天数").list();
		
		for(HistoricVariableInstance hvi : historicVariableInstances){
			System.out.println(hvi.getId() + ", " + hvi.getProcessInstanceId() + ", " + hvi.getValue());
		}
	}

}
