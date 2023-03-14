package com.h.auth.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h.auth.service.SysUserService;
import com.h.common.result.Result;
import com.h.model.system.SysUser;
import com.h.vo.system.SysUserQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author Lin
 * @since 2023-03-04
 */
@Api(tags = "用户管理接口")
@RestController
@RequestMapping("/admin/system/sysUser")
@CrossOrigin

public class SysUserController {

    @Autowired
    private SysUserService sysUserService;


    @ApiOperation(value = "获取当前用户基本信息")
    @GetMapping("getCurrentUser")
    public Result getCurrentUser() {
        return Result.ok(sysUserService.getCurrentUser());
    }

    /**
     * 用户条件分页查询
     * @param page 当前页码
     * @param limit 每页大小
     * @param sysUserQueryVo 筛选条件
     * @return 结果
     */
    @ApiOperation("用户条件分页查询")
    @PreAuthorize("hasAuthority('bnt.sysUser.list')")
    @GetMapping("{page}/{limit}")
    public Result index(@PathVariable Long page,
                        @PathVariable Long limit,
                        SysUserQueryVo sysUserQueryVo) {
        //创建page对象
        Page<SysUser> pageParam = new Page<>(page,limit);

        //封装条件，判断条件值不为空
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        //获取条件值
        String username = sysUserQueryVo.getKeyword();
        String createTimeBegin = sysUserQueryVo.getCreateTimeBegin();
        String createTimeEnd = sysUserQueryVo.getCreateTimeEnd();
        //判断条件值不为空
        //like 模糊查询
        if(!StringUtils.isEmpty(username)) {
            wrapper.like(SysUser::getUsername,username);
        }
        //ge 大于等于
        if(!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge(SysUser::getCreateTime,createTimeBegin);
        }
        //le 小于等于
        if(!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le(SysUser::getCreateTime,createTimeEnd);
        }

        //调用mp的方法实现条件分页查询
        IPage<SysUser> pageModel = sysUserService.page(pageParam, wrapper);
        return Result.ok(pageModel);
    }

    /**
     * 通过id查询用户
     * @param id 用户id
     * @return 结果
     */
    @ApiOperation(value = "获取用户")
    @PreAuthorize("hasAuthority('bnt.sysUser.list')")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        return Result.ok(user);
    }

    /**
     * 保存用户
     * @param user 用户数据
     * @return 结果
     */
    @ApiOperation(value = "保存用户")
    @PreAuthorize("hasAuthority('bnt.sysUser.add')")
    @PostMapping("save")
    public Result save(@RequestBody SysUser user) {
        sysUserService.save(user);
        return Result.ok();
    }

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 结果
     */
    @PreAuthorize("hasAuthority('bnt.sysUser.update')")
    @ApiOperation(value = "更新用户")
    @PutMapping("update")
    public Result updateById(@RequestBody SysUser user) {
        sysUserService.updateById(user);
        return Result.ok();
    }

    /**
     * 根据id删除用户
     * @param id 用户id
     * @return 结果
     */
    @ApiOperation(value = "删除用户")
    @PreAuthorize("hasAuthority('bnt.sysUser.remove')")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.ok();
    }

    /**
     * 更改用户状态
     * @param id 用户id
     * @param status 状态
     * @return 结果
     */
    @PreAuthorize("hasAuthority('bnt.sysUser.update')")
    @ApiOperation(value = "更新状态")
    @GetMapping("updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        sysUserService.updateStatus(id, status);
        return Result.ok();
    }
}

