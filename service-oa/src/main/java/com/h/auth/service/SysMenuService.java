package com.h.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.h.model.system.SysMenu;
import com.h.vo.system.AssginMenuVo;
import com.h.vo.system.RouterVo;

import java.util.List;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author Lin
 * @since 2023-03-07
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 获取菜单节点列表
     * @return 结果
     */
    List<SysMenu> findNodes();


    /**
     * 根据角色id获取菜单
     * @param roleId 角色id
     * @return 结果
     */
    List<SysMenu> findSysMenuByRoleId(Long roleId);

    /**
     * 分配菜单
     * @param assignMenuVo 需要分配的角色菜单信息
     */
    void doAssign(AssginMenuVo assignMenuVo);


    /**
     * 根据用户id获取用户菜单
     * @param userId 用户id
     * @return 结果
     */
    List<RouterVo> findUserMenuListByUserId(Long userId);

    /**
     * 根据用户id获取用户按钮权限
     * @param userId 用户id
     * @return 结果
     */
    List<String> findUserPermsListByUserId(Long userId);
}
