package com.h.process.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h.model.process.Process;

import com.baomidou.mybatisplus.extension.service.IService;
import com.h.model.process.ProcessType;
import com.h.vo.process.ProcessFormVo;
import com.h.vo.process.ProcessQueryVo;
import com.h.vo.process.ProcessVo;

import java.util.List;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author Lin
 * @since 2023-03-12
 */
public interface ProcessService extends IService<Process> {

    /**
     * 根据指定条件分页查询
     * @param pageParam 分页信息
     * @param processQueryVo 条件
     * @return 结果
     */
    IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo);

    /**
     * 部署工作流
     * @param deployPath 文件路径
     */
    void deployByZip(String deployPath);

    /**
     * 启动一个流程实例
     * @param processFormVo 审核表单参数
     */
    void startUp(ProcessFormVo processFormVo);

    /**
     * 查找代表任务
     * @param pageParam 分页数据
     * @return 结果
     */
    IPage<ProcessVo>  findPending(Page<Process> pageParam);
}
