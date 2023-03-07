package com.h.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.h.auth.service.SysRoleService;
import com.h.auth.mapper.SysRoleMapper;
import com.h.auth.service.SysUserRoleService;
import com.h.model.system.SysRole;
import com.h.model.system.SysUserRole;
import com.h.vo.system.AssginRoleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Lin
 * @since: 2023-03-01
 */

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Override
    public Map<String, Object> findRoleByUserId(Long userId) {
        // 查询所有角色
        List<SysRole> allRolesList = this.list();

        // 查询当前用户拥有的角色列表
        List<SysRole> existRoleList = baseMapper.selectRolesByUserId(userId);

        // 封装返回数据
        Map<String, Object> roleMap = new HashMap<>(2);
        roleMap.put("assginRoleList", existRoleList);
        roleMap.put("allRolesList", allRolesList);
        return roleMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void doAssign(AssginRoleVo assginRoleVo) {
        // 被分配的用户的id
        Long userId = assginRoleVo.getUserId();
        sysUserRoleService.remove(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId,userId));

        // 插入新分配的关联关系
        for(Long roleId : assginRoleVo.getRoleIdList()) {
            // 判断角色id是否为空
            if(StringUtils.isEmpty(roleId)) {
                continue;
            }
            // 保存关联关系到数据库
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(assginRoleVo.getUserId());
            userRole.setRoleId(roleId);
            sysUserRoleService.save(userRole);
        }

    }
}
