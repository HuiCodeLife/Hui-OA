package com.h.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h.auth.service.LoginService;
import com.h.auth.service.SysUserService;
import com.h.common.config.exception.ServiceException;
import com.h.common.result.ResultCodeEnum;
import com.h.common.utils.JwtHelper;
import com.h.common.utils.Md5Utils;
import com.h.model.system.SysUser;
import com.h.vo.system.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: Lin
 * @since: 2023-03-08
 */
@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private SysUserService sysUserService;

    @Override
    public String login(LoginVo loginVo) {
        // 根据用户名查找用户
        SysUser sysUser = sysUserService
                .getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, loginVo.getUsername()));
        // 判断查询结果是否为空
        if (null == sysUser) {
            throw new ServiceException(ResultCodeEnum.FAIL.getCode(), "用户不存在");
        }
        // 判断密码是否匹配
        if (!Md5Utils.encrypt(loginVo.getPassword()).equals(sysUser.getPassword())) {
            throw new ServiceException(ResultCodeEnum.FAIL.getCode(), "密码错误");
        }
        // 判断用户是否被禁用
        if (sysUser.getStatus() == 0) {
            throw new ServiceException(ResultCodeEnum.FAIL.getCode(), "用户被禁用");
        }
        // 以用户id和用户名作为负载生成token
        return JwtHelper.createToken(sysUser.getId(), sysUser.getUsername());
    }
}
