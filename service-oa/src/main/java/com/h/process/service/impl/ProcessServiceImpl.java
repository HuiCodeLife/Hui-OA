package com.h.process.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.h.auth.service.SysUserService;
import com.h.model.process.Process;
import com.h.model.process.ProcessTemplate;
import com.h.model.system.SysUser;
import com.h.process.mapper.ProcessMapper;
import com.h.process.service.ProcessRecordService;
import com.h.process.service.ProcessService;
import com.h.process.service.ProcessTemplateService;
import com.h.security.custom.LoginUserInfoHelper;
import com.h.vo.process.ProcessFormVo;
import com.h.vo.process.ProcessQueryVo;
import com.h.vo.process.ProcessVo;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
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
        BeanUtils.copyProperties(processFormVo,process);
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

        //计算下一个审批人，可能有多个（并行审批）
        List<Task> taskList = this.getCurrentTaskList(processInstanceId);
        if (!CollectionUtils.isEmpty(taskList)) {
            // 下一个审批人列表
            List<String> assigneeList = new ArrayList<>();
            for(Task task : taskList) {
                // 获取任务负责人信息
                SysUser user = sysUserService.getByUsername(task.getAssignee());
                // 添加到下一个审批人列表
                assigneeList.add(user.getName());
                //TODO 推送消息给下一个审批人
            }
            // 设置详细信息
            process.setDescription("等待" + StringUtils.join(assigneeList.toArray(), ",") + "审批");
        }
        // 更新审核表记录
        baseMapper.updateById(process);

        //记录操作行为
        processRecordService.record(process.getId(), 1, "发起申请");
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

    private List<Task> getCurrentTaskList(String processInstanceId) {
        // 通过流程实例id查询任务相关列表
        return taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    }


}
