package com.h.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.h.model.system.SysRole;
import com.h.vo.system.AssginRoleVo;

import java.util.Map;

/**
 * @author: Lin
 * @since: 2023-03-01
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 根据用户id查找角色信息
     * @param userId 用户id
     * @return 结果
     */
    Map<String, Object> findRoleByUserId(Long userId);

    /**
     * 分配角色
     * @param assginRoleVo 用户角色信息
     */
    void doAssign(AssginRoleVo assginRoleVo);

}
