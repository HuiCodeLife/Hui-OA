package com.h.process.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h.model.process.Process;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.h.vo.process.ProcessQueryVo;
import com.h.vo.process.ProcessVo;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 审批列表 Mapper 接口
 * </p>
 *
 * @author Lin
 * @since 2023-03-12
 */
public interface ProcessMapper extends BaseMapper<Process> {
    /**
     * 查询审批列表
     * @param page page对象
     * @param processQueryVo 查询条件
     * @return 结果
     */
    IPage<ProcessVo> selectPage(Page<ProcessVo> page, @Param("vo") ProcessQueryVo processQueryVo);

}
