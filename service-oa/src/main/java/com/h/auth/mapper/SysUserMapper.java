package com.h.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.h.model.system.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author Lin
 * @since 2023-03-04
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

}
