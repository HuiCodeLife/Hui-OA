package com.h.wechat.service.impl;

import com.h.model.wechat.Menu;
import com.h.vo.wechat.MenuVo;
import com.h.wechat.mapper.MenuMapper;
import com.h.wechat.service.MenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单 服务实现类
 * </p>
 *
 * @author Lin
 * @since 2023-03-14
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Override
    public List<MenuVo> findMenuInfo() {
        // 查询所有菜单
        List<Menu> menuList = baseMapper.selectList(null);

        // 过滤出所有一级菜单 parentId == 0
        List<Menu> oneMenuList = menuList
                .stream()
                .filter(menu -> menu.getParentId() == 0)
                .collect(Collectors.toList());

        // 遍历所有一级菜单设置二级菜单属性 要返回的列表
        List<MenuVo> list = new ArrayList<>();
        for (Menu oneMenu : oneMenuList) {
            // 转换为vo
            MenuVo oneMenuVo = new MenuVo();
            BeanUtils.copyProperties(oneMenu, oneMenuVo);

            // 过滤出二级菜单 menu.getParentId() ==  oneMenu.getId()
            List<Menu> twoMenuList = menuList.stream()
                    .filter(menu -> menu.getParentId().longValue() == oneMenu.getId())
                    .sorted(Comparator.comparing(Menu::getSort))
                    .collect(Collectors.toList());

            List<MenuVo> children = new ArrayList<>();
            for (Menu twoMenu : twoMenuList) {
                // 转换为vo
                MenuVo twoMenuVo = new MenuVo();
                BeanUtils.copyProperties(twoMenu, twoMenuVo);
                children.add(twoMenuVo);
            }
            // 设置子菜单
            oneMenuVo.setChildren(children);
            list.add(oneMenuVo);
        }
        return list;
    }
}
