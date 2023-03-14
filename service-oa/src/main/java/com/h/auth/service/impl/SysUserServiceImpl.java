package com.h.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.h.auth.mapper.SysUserMapper;
import com.h.auth.service.SysMenuService;
import com.h.auth.service.SysUserService;
import com.h.common.result.Result;
import com.h.common.utils.JwtHelper;
import com.h.model.system.SysUser;
import com.h.security.custom.LoginUserInfoHelper;
import com.h.vo.system.RouterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author Lin
 * @since 2023-03-04
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysMenuService sysMenuService;


    @Autowired
    private SysUserService sysUserService;
    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser sysUser = this.getById(id);
        if(status == 1) {
            sysUser.setStatus(status);
        } else {
            sysUser.setStatus(0);
        }
        this.updateById(sysUser);
    }

    @Override
    public Map<String, Object> getUserInfo(Long userId) {
        Map<String, Object> result = new HashMap<>(5);
        SysUser sysUser = this.getById(userId);

        // 根据用户id获取菜单权限值
        List<RouterVo> routerVoList = sysMenuService.findUserMenuListByUserId(sysUser.getId());
        // 根据用户id获取用户按钮权限
        List<String> permsList = sysMenuService.findUserPermsListByUserId(sysUser.getId());

        result.put("name", sysUser.getName());
        result.put("avatar", sysUser.getHeadUrl());
        //当前权限控制使用不到，我们暂时忽略
        result.put("roles",  new HashSet<>());
        result.put("buttons", permsList);
        result.put("routers", routerVoList);
        return result;
    }

    @Override
    public SysUser getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    @Override
    public Map<String, Object> getCurrentUser() {
        SysUser sysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        Map<String, Object> map = new HashMap<>(2);
        map.put("name", sysUser.getName());
        map.put("phone", sysUser.getPhone());
        return map;
    }
}
