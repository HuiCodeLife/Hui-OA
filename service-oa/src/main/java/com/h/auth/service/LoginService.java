package com.h.auth.service;

import com.h.vo.system.LoginVo;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 登陆相关服务
 * @author: Lin
 * @since: 2023-03-08
 */
public interface LoginService {

    /**
     * 用户登陆
     * @param loginVo 用户信息
     * @return 结果
     */
    public String login(@RequestBody LoginVo loginVo);
}
