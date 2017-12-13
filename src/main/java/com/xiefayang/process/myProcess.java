package com.xiefayang.process;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class myProcess {
	
	ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
	
	/*************************流程定义, 部署********************************/
	
	/**
	 * 1.部署流程定义
	 */
	@Test
	public void deploymentProcessDefinition(){
		
		// RepositoryService: 与流程定义和部署对象相关的Service
		Deployment deployment = processEngine.getRepositoryService() 
				.createDeployment() //创建一个部署对象
				.name("myProcess001") //添加部署的名称
				.addClasspathResource("diagrams/process1.bpmn") //从classpath中加载资源, 一次只能加载一个
				.addClasspathResource("diagrams/process1.png")
				.deploy(); //完成部署
		
		System.out.println("部署ID: " + deployment.getId());
		System.out.println("部署名称: " + deployment.getName());
	}
	
	
	/**
	 * 删除流程定义
	 */
	@Test
	public void deleteProcessDefinition(){
		
		RepositoryService repositoryService = this.processEngine.getRepositoryService();
		String deploymentId = "17501";
		
		// 普通删除，如果当前规则下有正在执行的流程，则抛异常 (比如已经启动的流程) 
		repositoryService.deleteDeployment(deploymentId);
		
		// 级联删除，会删除和当前规则相关的所有信息，正在执行的信息，也包括历史信息  
		//repositoryService.deleteDeployment(deploymentId, true);
	}
	
	
	/**
	 * 删除该key下所有版本的流程定义
	 * 执行成功后 会清除:  
	 * 		act_re_deployment：部署对象表
  	 * 		act_re_procdef：流程定义表
  	 * 		act_ge_bytearray：资源文件表
	 */
	@Test
	public void deleteProcessDefinitionByKey(){
		
		String processDefinitionKey = "myProcess1";  
		
		RepositoryService repositoryService = this.processEngine.getRepositoryService();
		List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).list();
		
		// 遍历获得每个流程的部署ID, 再根据部署ID级联删除
		for(ProcessDefinition processDefinition : processDefinitionList){
			String deploymentId = processDefinition.getDeploymentId();
			repositoryService.deleteDeployment(deploymentId, true);
		}
	}
	
	
	
	/**
	 * 查询流程定义
	 * 可以看到:
	 * 		id = [key]:[version]:[generatedId]
	 * 		name: 对应流程文件process节点的name属性值
	 * 		key: 对应流程文件process节点的id属性值
	 * 		version: 发布时自动生成, 从1开始, 如果当前流程引擎中存在相同的流程, 则找到当前key对应的最高版本+1
	 */
	@Test
	public void queryProcessDefinition(){
		
		List<ProcessDefinition>  processDefList = processEngine.getRepositoryService().createProcessDefinitionQuery()
									//可以在ProcessDefinitionQuery上设置查询的相关参数
									//.processDefinitionId(processDefinitionId)
									//.processDefinitionName(processDefinitionName)
									//.processDefinitionKey(processDefinitionKey)
									.orderByProcessDefinitionVersion().asc()
									//.count()
									//.listPage(firstResult, maxResults)
									//.singleResult()
									.list();
		
		
		//这段handler处理的作用是利用map的相同key替换原理, 取出同一个key下的最新流程定义对象(也就是version最高的)
		// =======Begin handler=========
//		Map<String, ProcessDefinition> map = new HashMap<String, ProcessDefinition>();
//		for(ProcessDefinition processDefinition: processDefList){
//			map.put(processDefinition.getKey(), processDefinition);
//		}
//		processDefList = new ArrayList<ProcessDefinition>(map.values());
		// =======End handler========= 
		
		if(null==processDefList || processDefList.size()==0){
			System.out.println("无任何流程定义");
			return ;
		}
		for(ProcessDefinition processDef: processDefList){
			System.out.println("---------------------------------------------------------");
			System.out.println("id: " + processDef.getId());
			System.out.println("name: " + processDef.getName());
			System.out.println("key: " + processDef.getKey());
			System.out.println("version: " + processDef.getVersion());
			System.out.println("resourceBpmnName: " + processDef.getResourceName());
			System.out.println("resourcePngName: " + processDef.getDiagramResourceName());
			System.out.println("---------------------------------------------------------");
		}
	}
	
	
	/**
	 * 查看流程附件(图片)
	 */
	@Test
	public void viewProcessImage() throws Exception{
		
		String deploymentId = "17501";
		List<String> imgNameList = processEngine.getRepositoryService().getDeploymentResourceNames(deploymentId);
		String imgName = null;
		for(String name: imgNameList){
			if(name.indexOf(".png") > 0){
				imgName = name;
				break;
			}
		}
		System.out.println("imgName: " + imgName);
		
		File file = new File("e:/"+imgName);  
        //通过部署ID和文件名称得到文件的输入流  
        InputStream in = processEngine.getRepositoryService().getResourceAsStream(deploymentId, imgName);  
        FileUtils.copyInputStreamToFile(in, file);
	}
	
	
	
	/***************************流程实例********************************/
	
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
		String processInstanceKey = "myProcess1";
		
		// RuntimeService: 与正在执行的流程实例和执行对象相关的Service
		ProcessInstance processInstance = processEngine.getRuntimeService()
				// 按照流程定义的id启动, 并不一定是最新版本, 因为该流程可能有多个版本
//				.startProcessInstanceById(processDefinitionId)  
				// 使用流程定义的key启动流程实例, key对应bpmn文件id的属性值(使用key值启动默认是按照最新版本的流程定义启动)
				.startProcessInstanceByKey(processInstanceKey); 
		
		
		
		System.out.println("流程实例ID: " + processInstance.getId());
		System.out.println("活动ID: " + processInstance.getActivityId());
		System.out.println("流程定义ID: " + processInstance.getProcessDefinitionId());
	}
	
	
	
	
	
	/*****************************任务(Task)******************************/
	
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
		
		String assignee = "张三";
		
		// TaskService: 与正在执行的任务相关的Service
		List<Task> taskList = processEngine.getTaskService()
				.createTaskQuery() //创建任务查询对象
				
				//支持各种查询条件
				.taskAssignee(assignee) //指定个人任务查询, 指定办理人
				
				//支持排序
				.orderByTaskCreateTime()
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
		
		String taskId = "25005";
		
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
