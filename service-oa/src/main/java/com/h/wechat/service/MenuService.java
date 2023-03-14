package com.h.wechat.service;

import com.h.model.wechat.Menu;
import com.baomidou.mybatisplus.extension.service.IService;
import com.h.vo.wechat.MenuVo;

import java.util.List;

/**
 * <p>
 * 菜单 服务类
 * </p>
 *
 * @author Lin
 * @since 2023-03-14
 */
public interface MenuService extends IService<Menu> {

    /**
     * 查询所有菜单列表
     * @return 结果
     */
    List<MenuVo> findMenuInfo();
}
