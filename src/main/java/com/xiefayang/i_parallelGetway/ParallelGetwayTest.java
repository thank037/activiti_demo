package com.xiefayang.i_parallelGetway;

import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;


/**
 * 说明: 
 * 	1. 一个流程中流程实例只有1个, 执行对象有多个
 * 	2. 并行网关的功能是基于进入和外出的顺序流的:
 * 		分支(fork): 并行后的所有外出顺序流, 为每个顺序流都创建一个并发分支.
 * 		汇聚(join): 所有到达并行网关, 再次等待的进入分支, 直到所有进入顺序流的分支都到达后, 流程就会通过汇聚网关
 * 	3. 并行网关的进入和外出都是使用相同的节点标识.
 *  4. 如果同一个并行网关有多个进入和多个外出的顺序流, 他就同时具有分支和汇聚功能.
 *  5. 并行网关不会解析条件, 即使顺序流中定义了条件, 也会被忽略. 
 */
public class ParallelGetwayTest {
	
	
	ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
	
	@Test
	public void deploymentProcessDefinition(){
		
		// RepositoryService: 与流程定义和部署对象相关的Service
		Deployment deployment = processEngine.getRepositoryService() 
				.createDeployment() //创建一个部署对象
				.name("并行网关") //添加部署的名称
				.addClasspathResource("diagrams/ParallelGetway.bpmn") //从classpath中加载资源, 一次只能加载一个
				.addClasspathResource("diagrams/ParallelGetway.png")
				.deploy(); //完成部署
		
		System.out.println("部署ID: " + deployment.getId());
		System.out.println("部署名称: " + deployment.getName());
	}
	
	
	/**
	 * 2.启动流程实例
	 *     act_ru_execution: 正在执行的执行对象表
	 *	       其中ID_是执行对象ID，PROC_INST_ID是流程实例ID
	 *
	 *	   act_hi_procinst 可以看到流程执行的历史(有正在运行的实例, 就一定有历史)
	 *	   一个流程, 流程实例只有一个, 执行对象: 一个(单例流程), 多个(存在分支和聚合的流程)	
	 *
	 *	我这里有个问题, 启动一个流程实例后会act_ru_execution出现两条数据. 而且流程实例ID不等于执行对象ID(单例流程)	
	 */
	@Test
	public void createProcessInstance(){
		
		//String processDefinitionId = "myProcess1:1:22504";
		String processDefinitionKey = "parallelGetway";
		
		// RuntimeService: 与正在执行的流程实例和执行对象相关的Service
		ProcessInstance processInstance = processEngine.getRuntimeService()
				// 按照流程定义的id启动, 并不一定是最新版本, 因为该流程可能有多个版本
//				.startProcessInstanceById(processDefinitionId)  
				// 使用流程定义的key启动流程实例, key对应bpmn文件id的属性值(使用key值启动默认是按照最新版本的流程定义启动)
				.startProcessInstanceByKey(processDefinitionKey);
		
		System.out.println("流程实例ID: " + processInstance.getId());
		System.out.println("活动ID: " + processInstance.getActivityId());
		System.out.println("流程定义ID: " + processInstance.getProcessDefinitionId());
	}
	
	
	
	/**
	 * 3.查询当前任务(当前人的待办事项)
	 *   实际就是查询act_ru_task表
	 *   
	 *   ACT_HI_TASKINST: 任务节点的历史表
	 *   
	 *   ACT_HI_ACTINST: 所有活动节点的历史表
	 */
	@Test
	public void queryPersonalTask(){
		
		String assignee = "买家";
		
		// TaskService: 与正在执行的任务相关的Service
		List<Task> taskList = processEngine.getTaskService()
				.createTaskQuery() //创建任务查询对象
				
				//支持各种查询条件
				.taskAssignee(assignee) //指定个人任务查询, 指定办理人
				
				//支持排序
				.orderByTaskCreateTime().asc()
				//支持返回结果
				.list(); 
		if(null==taskList || taskList.size()==0){
			System.out.println("当前用户无代办工作");
		} else {
			for(Task task: taskList){
				System.out.println("任务ID: " + task.getId());
				System.out.println("任务名称: " + task.getName());
				System.out.println("任务创办时间: " + task.getCreateTime());
				System.out.println("任务的办理人: " + task.getAssignee());
				System.out.println("流程实例ID: " + task.getProcessInstanceId());
				System.out.println("执行对象ID: " + task.getExecutionId());
				System.out.println("流程定义ID: " + task.getProcessDefinitionId());
			}
		}
	}
	
	
	
	/**
	 * 4.完成当前任务
	 */
	@Test
	public void completePersonalTask(){
		
		String taskId = "57502";
		
		processEngine.getTaskService().complete(taskId);
		
		System.out.println("完成任务" + taskId);
	}
	
	
	
	/**
	 * 查询流程实例状态(是否结束)
	 */
	@Test
	public void queryProcessStatus(){
		
		String processInstanceId = "25001";
		
		ProcessInstance processInstance = processEngine.getRuntimeService()
				.createProcessInstanceQuery().processInstanceId(processInstanceId)
				.singleResult();
		
		System.out.println(processInstance==null ? "流程实例已结束":"流程实例未结束");
	}
	
	
	
	/**
	 * 查询历史任务
	 */
	@Test
	public void queryHistoryTask(){
		
		String assignee = "张三";
		
		List<HistoricTaskInstance> list = processEngine.getHistoryService()
							.createHistoricTaskInstanceQuery()
							.taskAssignee(assignee).list();
		
		for(HistoricTaskInstance historicTaskInstance : list){
			System.out.println(historicTaskInstance.getId() + ", " + historicTaskInstance.getName() + ", " + historicTaskInstance.getProcessInstanceId()
			+ ", " + historicTaskInstance.getStartTime());
		}
	}
	
	
	
	/**
	 * 查询历史流程实例
	 */
	@Test
	public void queryHistoryProcessInstance(){
		
		String processInstanceId = "25001";
		
		HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService()
							.createHistoricProcessInstanceQuery()
							.processInstanceId(processInstanceId)
							.singleResult();
		
		System.out.println(historicProcessInstance.getProcessDefinitionId() 
				+ ", " + historicProcessInstance.getStartTime()
				+ ", " + historicProcessInstance.getEndTime());
	}
}
