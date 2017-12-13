package com.xiefayang.c_processDefinition;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class ProcessDefinitionTest {

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
	 * 部署流程定义(从InputStream)
	 */
	@Test
	public void deploymentProcessDefinitionByInputStream(){
		InputStream inputStreambpmn = this.getClass().getResourceAsStream("/diagrams/process1.bpmn");
		InputStream inputStreampng = this.getClass().getResourceAsStream("/diagrams/process1.png");
		Deployment deployment = processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
						.createDeployment()//创建一个部署对象
						.name("流程定义")//添加部署的名称
						.addInputStream("process1.bpmn", inputStreambpmn)
						.addInputStream("process1.png", inputStreampng)
						.deploy();//完成部署
		System.out.println("部署ID："+deployment.getId());//
		System.out.println("部署名称："+deployment.getName());//
	}
	
	
	/**
	 * 部署流程定义(从zip文件)
	 */
	@Test
	public void deploymentProcessDefinitionByZip(){
		
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("diagrams/helloworld.zip");
		ZipInputStream zipInputStream = new ZipInputStream(in);
		Deployment deployment = processEngine.getRepositoryService()
				.createDeployment()
				.name("流程定义")
				.addZipInputStream(zipInputStream)
				.deploy();
		
		System.out.println("部署ID: " + deployment.getId());
		System.out.println("部署名称: " + deployment.getName());
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
}
