package com.h.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.h.model.process.ProcessTemplate;
import com.h.model.process.ProcessType;
import com.h.process.mapper.ProcessTypeMapper;
import com.h.process.service.ProcessTemplateService;
import com.h.process.service.ProcessTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author Lin
 * @since 2023-03-12
 */
@Service
public class ProcessTypeServiceImpl extends ServiceImpl<ProcessTypeMapper, ProcessType> implements ProcessTypeService {


    @Autowired
    private ProcessTypeService processTypeService;

    @Autowired
    private ProcessTemplateService processTemplateService;


    @Override
    public List<ProcessType> findProcessTypeAndTemplate() {
        // 获取所有审批类型
        List<ProcessType> processTypeList = processTypeService.list();
        // 遍历封装所有审批类型的审批模板列表属性
        for (ProcessType processType : processTypeList) {
            // 获取审批类型的所有审批模板列表
            List<ProcessTemplate> processTemplateList = processTemplateService
                    .list(new LambdaQueryWrapper<ProcessTemplate>().eq(ProcessTemplate::getProcessTypeId,processType.getId()));
            // 封装数据
            processType.setProcessTemplateList(processTemplateList);
        }
        return processTypeList;
    }
}
