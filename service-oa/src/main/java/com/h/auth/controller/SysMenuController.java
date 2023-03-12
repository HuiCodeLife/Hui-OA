package com.h.auth.controller;


import com.h.auth.service.SysMenuService;
import com.h.common.result.Result;
import com.h.model.system.SysMenu;
import com.h.vo.system.AssginMenuVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 菜单表 前端控制器
 * </p>
 *
 * @author Lin
 * @since 2023-03-07
 */

@Api(tags = "菜单管理")
@RestController
@RequestMapping("/admin/system/sysMenu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 获取菜单节点
     * @return 结果
     */
    @ApiOperation(value = "获取菜单")
    @PreAuthorize("hasAuthority('bnt.sysMenu.list')")
    @GetMapping("findNodes")
    public Result findNodes() {
        List<SysMenu> list = sysMenuService.findNodes();
        return Result.ok(list);
    }

    /**
     * 新增菜单
     * @param sysMenu 菜单信息
     * @return 结果
     */
    @ApiOperation(value = "新增菜单")
    @PreAuthorize("hasAuthority('bnt.sysMenu.add')")
    @PostMapping("save")
    public Result save(@RequestBody SysMenu sysMenu) {
        sysMenuService.save(sysMenu);
        return Result.ok();
    }

    /**
     * 修改菜单
     * @param sysMenu 菜单信息
     * @return 结果
     */
    @ApiOperation(value = "修改菜单")
    @PreAuthorize("hasAuthority('bnt.sysMenu.update')")

    @PutMapping("update")
    public Result updateById(@RequestBody SysMenu sysMenu) {
        sysMenuService.updateById(sysMenu);
        return Result.ok();
    }

    @ApiOperation(value = "删除菜单")
    @PreAuthorize("hasAuthority('bnt.sysMenu.remove')")

    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        sysMenuService.removeById(id);
        return Result.ok();
    }

    /**
     * 根据角色获取菜单
     * @param roleId 角色id
     * @return 结果
     */
    @ApiOperation(value = "根据角色获取菜单")
    @PreAuthorize("hasAuthority('bnt.sysMenu.list')")
    @GetMapping("toAssign/{roleId}")
    public Result toAssign(@PathVariable Long roleId) {
        List<SysMenu> list = sysMenuService.findSysMenuByRoleId(roleId);
        return Result.ok(list);
    }

    /**
     * 给角色分配菜单
     * @param assignMenuVo 菜单参数
     * @return 结果
     */
    @ApiOperation(value = "给角色分配菜单")
    @PreAuthorize("hasAuthority('bnt.sysRole.update')")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestBody AssginMenuVo assignMenuVo) {
        sysMenuService.doAssign(assignMenuVo);
        return Result.ok();
    }
}

