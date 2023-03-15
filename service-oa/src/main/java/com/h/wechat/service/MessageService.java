package com.h.wechat.service;

/**
 * 消息推送
 * @author: Lin
 * @since: 2023-03-15
 */
public interface MessageService {

    /**
     * 推送待审批人员
     * @param processId 流程id
     * @param userId 推送者id
     * @param taskId 任务id
     */
    void pushPendingMessage(Long processId, Long userId, String taskId);

    /**
     * 审批后推送提交审批人员
     * @param processId 流程id
     * @param userId 推送者id
     * @param status
     */
    void pushProcessedMessage(Long processId, Long userId, Integer status);
}
