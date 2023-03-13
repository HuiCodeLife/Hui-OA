package com.h.process.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h.model.process.ProcessTemplate;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 审批模板 服务类
 * </p>
 *
 * @author Lin
 * @since 2023-03-12
 */
public interface ProcessTemplateService extends IService<ProcessTemplate> {

    /**
     * 查询审批流程模板
     * @param pageParam page
     * @return 结果
     */
    IPage<ProcessTemplate> selectPage(Page<ProcessTemplate> pageParam);

    /**
     * 发布审批模板
     * @param id 模板id
     */
    void publish(Long id);
}
