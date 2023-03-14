package com.h.process.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.h.auth.service.SysUserService;
import com.h.model.process.Process;
import com.h.model.process.ProcessRecord;
import com.h.model.process.ProcessTemplate;
import com.h.model.system.SysUser;
import com.h.process.mapper.ProcessMapper;
import com.h.process.service.ProcessRecordService;
import com.h.process.service.ProcessService;
import com.h.process.service.ProcessTemplateService;
import com.h.security.custom.LoginUserInfoHelper;
import com.h.vo.process.ApprovalVo;
import com.h.vo.process.ProcessFormVo;
import com.h.vo.process.ProcessQueryVo;
import com.h.vo.process.ProcessVo;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author Lin
 * @since 2023-03-12
 */
@Service
public class ProcessServiceImpl extends ServiceImpl<ProcessMapper, Process> implements ProcessService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ProcessTemplateService processTemplateService;

    @Autowired
    private ProcessRecordService processRecordService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;


    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo) {
        return baseMapper.selectPage(pageParam, processQueryVo);
    }

    @Override
    public void deployByZip(String deployPath) {
        // 定义zip输入流
        InputStream inputStream = this
                .getClass()
                .getClassLoader()
                .getResourceAsStream(deployPath);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        // 流程部署
        Deployment deployment = repositoryService.createDeployment()
                .addZipInputStream(zipInputStream)
                .deploy();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void startUp(ProcessFormVo processFormVo) {
        // 获取当前用户
        SysUser sysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        // 获取审核模板信息
        ProcessTemplate processTemplate
                = processTemplateService.getById(processFormVo.getProcessTemplateId());

        // 封装process
        Process process = new Process();
        BeanUtils.copyProperties(processFormVo, process);
        // 当前时间戳作为workNo
        String workNo = String.valueOf(System.currentTimeMillis());
        process.setProcessCode(workNo);
        process.setUserId(LoginUserInfoHelper.getUserId());
        process.setFormValues(processFormVo.getFormValues());
        process.setTitle(sysUser.getName() + "发起" + processTemplate.getName() + "申请");
        process.setStatus(1);
        // 保存到数据库
        baseMapper.insert(process);

        //绑定业务id
        String businessKey = String.valueOf(process.getId());
        //流程参数
        Map<String, Object> variables = new HashMap<>();
        //将表单数据放入流程实例中
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formData = jsonObject.getJSONObject("formData");
        //循环转换
        Map<String, Object> map = new HashMap<>(formData);
        variables.put("data", map);
        // 启动流程实例
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey(processTemplate.getProcessDefinitionKey(), businessKey, variables);

        //设置业务表关联当前流程实例id
        String processInstanceId = processInstance.getId();
        process.setProcessInstanceId(processInstanceId);

        // 设置一个流程负责人，可能有多个（并行审批）
        setNextAssignee(process);
        // 更新审核表记录
        baseMapper.updateById(process);

        //记录操作行为
        processRecordService.record(process.getId(), 1, "发起申请");
    }

    /**
     * 设置一个流程负责人
     *
     * @param process 流程信息
     * @return CollectionUtils.isEmpty(taskList)  任务列表是否为空
     */
    private boolean setNextAssignee(Process process) {
        String processInstanceId = process.getProcessInstanceId();
        List<Task> taskList = this.getCurrentTaskList(processInstanceId);
        if (!CollectionUtils.isEmpty(taskList)) {
            // 下一个审批人列表
            List<String> assigneeList = new ArrayList<>();
            for (Task task : taskList) {
                // 获取任务负责人信息
                SysUser user = sysUserService.getByUsername(task.getAssignee());
                // 添加到下一个审批人列表
                assigneeList.add(user.getName());
                //TODO 推送消息给下一个审批人
            }
            // 设置详细信息
            process.setDescription("等待" + StringUtils.join(assigneeList.toArray(), ",") + "审批");
            process.setStatus(1);
        }
        return CollectionUtils.isEmpty(taskList);
    }

    @Override
    public IPage<ProcessVo> findPending(Page<Process> pageParam) {
        // 根据当前用户查询
        TaskQuery query = taskService.createTaskQuery().taskAssignee(LoginUserInfoHelper.getUsername()).orderByTaskCreateTime().desc();
        // 任务列表
        List<Task> list = query.listPage((int) ((pageParam.getCurrent() - 1) * pageParam.getSize()), (int) pageParam.getSize());
        // 任务总数
        long totalCount = query.count();
        List<ProcessVo> processList = new ArrayList<>();
        // 遍历->将Task转为ProcessVo
        for (Task item : list) {
            // 获取任务的实例id
            String processInstanceId = item.getProcessInstanceId();
            // 根据任务的实例id获取任务实例
            ProcessInstance processInstance = runtimeService
                    .createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            if (processInstance == null) {
                continue;
            }
            // 获取业务key
            String businessKey = processInstance.getBusinessKey();
            if (businessKey == null) {
                continue;
            }
            // 根据业务key获取流程信息
            Process process = this.getById(Long.parseLong(businessKey));
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process, processVo);
            processVo.setTaskId(item.getId());
            // 添加到任务列表
            processList.add(processVo);
        }
        // 封装page分页对象
        IPage<ProcessVo> page = new Page<>(pageParam.getCurrent(), pageParam.getSize(), totalCount);
        page.setRecords(processList);
        return page;
    }

    @Override
    public Map<String, Object> show(Long id) {
        // 根据id获取审批信息
        Process process = this.getById(id);

        // 查询该流程的流程记录
        List<ProcessRecord> processRecordList = processRecordService.list(new LambdaQueryWrapper<ProcessRecord>().eq(ProcessRecord::getProcessId, id));

        // 查询该流程模板
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());

        // 判断当前用户是否有可操作权限
        boolean isApprove = false;
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if (!CollectionUtils.isEmpty(taskList)) {
            for (Task task : taskList) {
                if (task.getAssignee().equals(LoginUserInfoHelper.getUsername())) {
                    isApprove = true;
                }
            }
        }
        Map<String, Object> map = new HashMap<>(4);
        map.put("process", process);
        map.put("processRecordList", processRecordList);
        map.put("processTemplate", processTemplate);
        map.put("isApprove", isApprove);
        return map;
    }

    @Override
    public void approve(ApprovalVo approvalVo) {
        // 获取任务id
        String taskId = approvalVo.getTaskId();

        Map<String, Object> variables1 = taskService.getVariables(approvalVo.getTaskId());
        for (Map.Entry<String, Object> entry : variables1.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }
        // 根据任务状态设置
        if (approvalVo.getStatus() == 1) {
            //已通过
            taskService.complete(taskId);
        } else {
            //驳回
            this.endTask(taskId);
        }

        String description = approvalVo.getStatus() == 1 ? "已通过" : "驳回";
        // 流程记录
        processRecordService.record(approvalVo.getProcessId(), approvalVo.getStatus(), description);

        Process process = this.getById(approvalVo.getProcessId());
        // 设置一个流程负责人
        boolean taskIsEmpty = setNextAssignee(process);
        // 如果没有下一个任务
        if (taskIsEmpty) {
            // 审批完毕
            if (approvalVo.getStatus() == 1) {
                // 审批同意
                process.setDescription("审批完成（同意）");
                process.setStatus(2);
            } else {
                // 审批拒绝
                process.setDescription("审批完成（拒绝）");
                process.setStatus(-1);
            }
        }
        // TODO 推送消息给申请人

        // 修改流程状态
        this.updateById(process);
    }

    @Override
    public IPage<ProcessVo> findProcessed(Page<Process> pageParam) {
        // 根据当前人的用户名查询
        HistoricTaskInstanceQuery query = historyService
                .createHistoricTaskInstanceQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername())
                .finished().orderByTaskCreateTime()
                .desc();
        // 分页查询历史任务列表
        List<HistoricTaskInstance> list = query
                .listPage((int) ((pageParam.getCurrent() - 1) * pageParam.getSize()), (int) pageParam.getSize());
        // 查询总记录数
        long totalCount = query.count();
        // 封装为vo对象
        List<ProcessVo> processList = new ArrayList<>();
        for (HistoricTaskInstance item : list) {
            String processInstanceId = item.getProcessInstanceId();
            Process process = this.getOne(new LambdaQueryWrapper<Process>().eq(Process::getProcessInstanceId, processInstanceId));
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process, processVo);
            processVo.setTaskId("0");
            processList.add(processVo);
        }
        IPage<ProcessVo> page = new Page<>(pageParam.getCurrent(), pageParam.getSize(), totalCount);
        page.setRecords(processList);
        return page;
    }

    @Override
    public IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam) {
        ProcessQueryVo processQueryVo = new ProcessQueryVo();
        processQueryVo.setUserId(LoginUserInfoHelper.getUserId());
        IPage<ProcessVo> page = baseMapper.selectPage(pageParam, processQueryVo);
        for (ProcessVo item : page.getRecords()) {
            item.setTaskId("0");
        }
        return page;
    }



    /**
     * 结束任务
     * @param taskId 任务id
     */
    private void endTask(String taskId) {
        // 基本固定写法
        //  当前任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        List endEventList = bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
        // 并行任务可能为null
        if (CollectionUtils.isEmpty(endEventList)) {
            return;
        }
        FlowNode endFlowNode = (FlowNode) endEventList.get(0);
        FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());
        //  临时保存当前活动的原始方向
        List originalSequenceFlowList = new ArrayList<>();
        originalSequenceFlowList.addAll(currentFlowNode.getOutgoingFlows());
        //  清理活动方向
        currentFlowNode.getOutgoingFlows().clear();
        //  建立新方向
        SequenceFlow newSequenceFlow = new SequenceFlow();
        newSequenceFlow.setId("newSequenceFlowId");
        newSequenceFlow.setSourceFlowElement(currentFlowNode);
        newSequenceFlow.setTargetFlowElement(endFlowNode);
        List newSequenceFlowList = new ArrayList<>();
        newSequenceFlowList.add(newSequenceFlow);
        //  当前节点指向新的方向
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);
        //  完成当前任务
        taskService.complete(task.getId());
    }

    /**
     * 通过流程实例id查询任务相关列表
     *
     * @param processInstanceId 流程实例id
     * @return 结果
     */
    private List<Task> getCurrentTaskList(String processInstanceId) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    }


}
