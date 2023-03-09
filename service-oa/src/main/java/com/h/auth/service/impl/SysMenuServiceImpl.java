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
import com.h.vo.system.MetaVo;
import com.h.vo.system.RouterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
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

    @Override
    public List<RouterVo> findUserMenuListByUserId(Long userId) {
        // 超级管理员admin账号id为：1
        List<SysMenu> sysMenuList = null;
        if (userId == 1) {
            // 获取所有菜单权限
            sysMenuList = this.list(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getStatus, 1).orderByAsc(SysMenu::getSortValue));
        } else {
            // 获取用户相关菜单权限
            sysMenuList = baseMapper.findListByUserId(userId);
        }
        // 构建树形数据
        List<SysMenu> sysMenuTreeList = MenuHelper.buildTree(sysMenuList);

        // 构建前端所需数据结构
        List<RouterVo> routerVoList = this.buildMenus(sysMenuTreeList);
        return routerVoList;
    }

    /**
     * 构建前端所需结构
     * @param menus 菜单树形结构
     * @return 结果
     */
    private List<RouterVo> buildMenus(List<SysMenu> menus) {
        List<RouterVo> routers = new LinkedList<>();
        for (SysMenu menu : menus) {
            RouterVo router = new RouterVo();
            // 不隐藏菜单
            router.setHidden(false);
            router.setAlwaysShow(false);
            router.setPath(getRouterPath(menu));
            router.setComponent(menu.getComponent());
            router.setMeta(new MetaVo(menu.getName(), menu.getIcon()));
            List<SysMenu> children = menu.getChildren();
            // 如果当前是菜单，需将按钮对应的路由加载出来，如：“角色授权”按钮对应的路由在“系统管理”下面
            if(menu.getType() == 1) {
                // 获取二级菜单下的所有隐藏菜单 判断component下是否为空
                List<SysMenu> hiddenMenuList = children
                        .stream()
                        .filter(item -> !StringUtils.isEmpty(item.getComponent())).collect(Collectors.toList());
                for (SysMenu hiddenMenu : hiddenMenuList) {
                    RouterVo hiddenRouter = new RouterVo();
                    // 隐藏菜单
                    hiddenRouter.setHidden(true);
                    hiddenRouter.setAlwaysShow(false);
                    hiddenRouter.setPath(getRouterPath(hiddenMenu));
                    hiddenRouter.setComponent(hiddenMenu.getComponent());
                    hiddenRouter.setMeta(new MetaVo(hiddenMenu.getName(), hiddenMenu.getIcon()));
                    routers.add(hiddenRouter);
                }
            } else {
                if (!CollectionUtils.isEmpty(children)) {
                    if(children.size() > 0) {
                        router.setAlwaysShow(true);
                    }
                    router.setChildren(buildMenus(children));
                }
            }
            routers.add(router);
        }
        return routers;
    }

    /**
     * 获取路由地址
     * @param menu 菜单
     * @return 结果
     */
    private String getRouterPath(SysMenu menu) {
        String routerPath = "/" + menu.getPath();
        if(menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }

    @Override
    public List<String> findUserPermsListByUserId(Long userId) {
        // 超级管理员admin账号id为：1
        List<SysMenu> sysMenuList = null;
        if (userId == 1) {
            // 获取所有按钮权限
            sysMenuList = this.list(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getStatus, 1));
        } else {
            // 根据用户id获取按钮权限
            sysMenuList = baseMapper.findListByUserId(userId);
        }
        return sysMenuList
                .stream()
                .filter(item -> item.getType() == 2)
                .map(SysMenu::getPerms)
                .collect(Collectors.toList());
    }
}
