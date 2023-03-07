package com.h.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h.auth.mapper.SysMenuMapper;
import com.h.auth.service.SysMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.h.auth.service.SysRoleMenuService;
import com.h.auth.utils.MenuHelper;
import com.h.model.system.SysMenu;
import com.h.model.system.SysRoleMenu;
import com.h.vo.system.AssginMenuVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author Lin
 * @since 2023-03-07
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @Override
    public List<SysMenu> findNodes() {
        // 查询所有菜单信息
        List<SysMenu> sysMenuList = this.list();
        // 判断菜单集合是否为空
        CollectionUtils.isEmpty(sysMenuList);

        // 递归构建树形菜单节点
        return MenuHelper.buildTree(sysMenuList);
    }

    @Override
    public List<SysMenu> findSysMenuByRoleId(Long roleId) {
        // 获取全部权限列表
        List<SysMenu> allSysMenuList = this.list(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getStatus, 1));

        //根据角色id获取角色的菜单id集合
        List<Long> menuIdList = sysRoleMenuService
                .list(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());

        // 封装该菜单是否已经分配
        allSysMenuList.forEach(menu -> {
            if (menuIdList.contains(menu.getId())) {
                // 已分配
                menu.setSelect(true);
            } else {
                // 未分配
                menu.setSelect(false);
            }
        });

        // 返回结果
        return MenuHelper.buildTree(allSysMenuList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void doAssign(AssginMenuVo assignMenuVo) {
        // 删除角色原有的菜单
        sysRoleMenuService.remove(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, assignMenuVo.getRoleId()));

        // 从参数中重新添加新分配的菜单
        for (Long menuId : assignMenuVo.getMenuIdList()) {
            // 判断id是否为空
            if (StringUtils.isEmpty(menuId)) {
                continue;
            }
            // 保存关联关系
            SysRoleMenu menu = new SysRoleMenu();
            menu.setRoleId(assignMenuVo.getRoleId());
            menu.setMenuId(menuId);
            sysRoleMenuService.save(menu);
        }

    }
}
