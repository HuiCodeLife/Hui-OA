package com.h.process;

import com.h.ServiceAuthApplication;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Lin
 * @since: 2023-03-12
 */
@SpringBootTest(classes = ServiceAuthApplication.class)
public class ProcessTest2 {



    @Autowired
    private RepositoryService repositoryService;


    @Autowired
    private RuntimeService runtimeService;


    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;



    /**
     * 启动流程实例
     */
    @Test
    public void startUpProcess01() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("assignee1","zhangsan");
        variables.put("assignee2","lisi");
        //创建流程实例,我们需要知道流程定义的key
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("qingjia2", variables);
        //输出实例的相关信息
        System.out.println("流程定义id：" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例id：" + processInstance.getId());
    }
    @Test
    public void deployProcess01() {
        // 流程部署
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("process/qingjia2.bpmn20.xml")
                .name("请假2申请流程")
                .deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());
    }


}
