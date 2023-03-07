package com.h.auth.utils;

import com.h.model.system.SysMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * 构建菜单工具类
 * @author: Lin
 * @since: 2023-03-07
 */
public class MenuHelper {

    /**
     * 使用递归方法建菜单
     * @param sysMenuList 所有菜单列表
     * @return 结果
     */
    public static List<SysMenu> buildTree(List<SysMenu> sysMenuList) {
        List<SysMenu> trees = new ArrayList<>();
        for (SysMenu sysMenu : sysMenuList) {
            if (sysMenu.getParentId() == 0) {
                trees.add(findChildren(sysMenu,sysMenuList));
            }
        }
        return trees;
    }

    /**
     * 递归查找子节点
     * @param sysMenu 当前菜单
     * @param sysMenuList 所有菜单列表
     * @return 结果
     */
    private static SysMenu findChildren(SysMenu sysMenu, List<SysMenu> sysMenuList) {
        // 构建空集合
        sysMenu.setChildren(new ArrayList<>());

        // 查找子节点
        for (SysMenu menu : sysMenuList) {
            if (menu.getParentId().equals(sysMenu.getId())) {
                // 防止空指针
                if (sysMenu.getChildren() == null) {
                    sysMenu.setChildren(new ArrayList<>());
                }
                // 该节点为sysMenu的子节点,继续递归查找该节点的子节点
                sysMenu.getChildren().add(findChildren(menu,sysMenuList));
            }
        }
        return sysMenu;
    }
}
