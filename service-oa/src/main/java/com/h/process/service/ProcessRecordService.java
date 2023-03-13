package com.h.process.service;

import com.h.model.process.ProcessRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 审批记录 服务类
 * </p>
 *
 * @author Lin
 * @since 2023-03-13
 */
public interface ProcessRecordService extends IService<ProcessRecord> {

    /**
     * 记录流程
     * @param processId 审批记录id
     * @param status 状态
     * @param description 详情
     */
    void record(Long processId, int status, String description);
}
