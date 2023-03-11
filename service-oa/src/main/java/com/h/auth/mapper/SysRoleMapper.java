package com.h.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.h.model.system.SysRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author: Lin
 * @since: 2023-03-01
 */
@Mapper

public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 查询用户对应的角色列表
     * @param userId 用户id
     * @return 结果
     */
    List<SysRole> selectRolesByUserId(Long userId);

}
