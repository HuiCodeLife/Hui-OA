package com.h.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h.model.process.ProcessTemplate;
import com.h.model.process.ProcessType;
import com.h.process.mapper.ProcessTemplateMapper;
import com.h.process.service.ProcessService;
import com.h.process.service.ProcessTemplateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.h.process.service.ProcessTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 审批模板 服务实现类
 * </p>
 *
 * @author Lin
 * @since 2023-03-12
 */
@Service
public class ProcessTemplateServiceImpl extends ServiceImpl<ProcessTemplateMapper, ProcessTemplate> implements ProcessTemplateService {


    @Autowired
    private ProcessTypeService processTypeService;


    @Autowired
    private ProcessService processService;

    @Override
    public IPage<ProcessTemplate> selectPage(Page<ProcessTemplate> pageParam) {
        // 查询分页数据 根据更新时间降序
        Page<ProcessTemplate> processTemplatePage =
                baseMapper.selectPage(pageParam, new LambdaQueryWrapper<ProcessTemplate>().orderByDesc(ProcessTemplate::getUpdateTime));
        // 获取processTemplate列表数据
        List<ProcessTemplate> records = processTemplatePage.getRecords();
        // 封装processTypeName
        records.forEach(processTemplate -> {
            // 获取processType的id
            Long processTypeId = processTemplate.getProcessTypeId();
            // 根据processType的id获取processTypeName
            ProcessType processType = processTypeService.getOne(new LambdaQueryWrapper<ProcessType>().eq(ProcessType::getId, processTypeId));
            if (processType != null) {
                String processTypeName = processType.getName();
                // 封装processTypeName属性
                processTemplate.setProcessTypeName(processTypeName);
            }
        });
        return processTemplatePage;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void publish(Long id) {
        ProcessTemplate processTemplate = this.getById(id);
        processTemplate.setStatus(1);
        baseMapper.updateById(processTemplate);

        //优先发布在线流程设计
        if(!StringUtils.isEmpty(processTemplate.getProcessDefinitionPath())) {
            processService.deployByZip(processTemplate.getProcessDefinitionPath());
        }
    }
}
