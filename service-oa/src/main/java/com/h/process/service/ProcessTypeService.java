package com.h.process.service;

import com.h.model.process.ProcessType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author Lin
 * @since 2023-03-12
 */
public interface ProcessTypeService extends IService<ProcessType> {

    /**
     * 获取审批类型以及审批模板
     * @return 结果
     */
    List<ProcessType> findProcessTypeAndTemplate();

}
