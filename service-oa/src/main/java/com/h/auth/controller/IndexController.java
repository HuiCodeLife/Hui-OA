package com.h.auth.controller;

import com.h.auth.service.LoginService;
import com.h.auth.service.SysMenuService;
import com.h.auth.service.SysUserService;
import com.h.common.result.Result;
import com.h.common.utils.JwtHelper;
import com.h.model.system.SysUser;
import com.h.vo.system.LoginVo;
import com.h.vo.system.RouterVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 后台登录管理
 *
 * @author: Lin
 * @since: 2023-03-02
 */
@Api(tags = "后台登录管理")
@RestController
@RequestMapping("/admin/system/index")
public class IndexController {


    @Autowired
    private LoginService loginService;

    @Autowired
    private SysUserService sysUserService;


    /**
     * 登录
     *
     * @return
     */
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo) {
        String token = loginService.login(loginVo);
        Map<String, Object> map = new HashMap<>(1);
        map.put("token", token);
        return Result.ok(map);
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    @GetMapping("info")
    public Result info(HttpServletRequest request) {
        // 解析请求头中的token获取userId
        Long userId = JwtHelper.getUserId(request.getHeader("token"));
        Map<String, Object> map = sysUserService.getUserInfo(userId);
        return Result.ok(map);

    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("logout")
    public Result logout() {
        return Result.ok();
    }
}
