package com.h.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.h.model.system.SysUser;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author Lin
 * @since 2023-03-04
 */
public interface SysUserService extends IService<SysUser> {
    /**
     * 更改用户状态
     * @param id 用户id
     * @param status 状态
     */
    void updateStatus(Long id, Integer status);
}
