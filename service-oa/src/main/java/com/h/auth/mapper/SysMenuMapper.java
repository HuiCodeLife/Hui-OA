package com.h.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.h.model.system.SysMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 菜单表 Mapper 接口
 * </p>
 *
 * @author Lin
 * @since 2023-03-07
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据用户id获取菜单权限
     * @param userId 用户id
     * @return 结果
     */
    List<SysMenu> findListByUserId(Long userId);
}
