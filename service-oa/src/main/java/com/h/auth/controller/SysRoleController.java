package com.h.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h.auth.service.SysRoleService;
import com.h.common.result.Result;
import com.h.model.system.SysRole;
import com.h.vo.system.AssginRoleVo;
import com.h.vo.system.SysRoleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author: Lin
 * @since: 2023-03-01
 */
@Api(tags = "角色管理接口")
@RestController
@RequestMapping("/admin/system/sysRole")
public class SysRoleController {


    @Resource
    private SysRoleService sysRoleService;

    /**
     * 查询所有角色
     * @return
     */
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")

    @ApiOperation("查询所有角色")
    @GetMapping("/findAll")
    public Result<List<SysRole>> findAll() {
        List<SysRole> roleList = sysRoleService.list();
        return Result.ok(roleList);
    }

    /**
     * 分页查询
     * @param page 当前页码数
     * @param limit 每页大小
     * @param sysRoleQueryVo 筛选条件
     * @return
     */
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("条件分页查询")
    @GetMapping("/{page}/{limit}")
    public Result pageQueryRole(@PathVariable Long page,
                                @PathVariable Long limit,
                                SysRoleQueryVo sysRoleQueryVo) {
        //1 创建page对象
        Page<SysRole> pageParam = new Page<>(page,limit);

        //2 封装条件，判断条件是否为空，不为空进行封装
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        String roleName = sysRoleQueryVo.getRoleName();
        if(!StringUtils.isEmpty(roleName)) {
            // 模糊查询
            wrapper.like(SysRole::getRoleName,roleName);
        }

        //3 调用方法实现
        IPage<SysRole> pageModel = sysRoleService.page(pageParam, wrapper);
        return Result.ok(pageModel);
    }

    /**
     * 根据角色id获取角色
     * @param id 角色id
     * @return 结果
     */
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation(value = "获取")
    @GetMapping("/get/{id}")
    public Result get(@PathVariable Long id) {
        SysRole role = sysRoleService.getById(id);
        return Result.ok(role);
    }

    /**
     * 新增用户
     * @param role 用户信息
     * @return 结果
     */
    @PreAuthorize("hasAuthority('bnt.sysRole.add')")
    @ApiOperation(value = "新增角色")
    @PostMapping("/save")
    public Result save(@RequestBody SysRole role) {
        sysRoleService.save(role);
        return Result.ok();
    }

    /**
     * 修改角色
     * @param role 角色
     * @return 结果
     */
    @PreAuthorize("hasAuthority('bnt.sysRole.update')")
    @ApiOperation(value = "修改角色")
    @PutMapping("/update")
    public Result updateById(@RequestBody SysRole role) {
        sysRoleService.updateById(role);
        return Result.ok();
    }

    /**
     * 删除角色
     * @param id 角色id
     * @return 结果
     */
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation(value = "删除角色")
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable Long id) {
        sysRoleService.removeById(id);
        return Result.ok();
    }

    /**
     * 根据id批量删除
     * @param idList id集合
     * @return 结果
     */
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation(value = "根据id列表删除")
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        sysRoleService.removeByIds(idList);
        return Result.ok();
    }


    /**
     * 根据用户获取角色数据
     * @param userId 用户id
     * @return 结果
     */
    @ApiOperation(value = "根据用户获取角色数据")
    @GetMapping("/toAssign/{userId}")
    public Result toAssign(@PathVariable Long userId) {
        Map<String, Object> roleMap = sysRoleService.findRoleByUserId(userId);
        return Result.ok(roleMap);
    }

    /**
     * 分配角色
     * @param assginRoleVo
     * @return
     */
    @ApiOperation(value = "用户分配角色")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestBody AssginRoleVo assginRoleVo) {
        sysRoleService.doAssign(assginRoleVo);
        return Result.ok();
    }


}
