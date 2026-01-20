package com.oasystem.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oasystem.common.exception.BusinessException;
import com.oasystem.modules.system.entity.Menu;
import com.oasystem.modules.system.mapper.MenuMapper;
import com.oasystem.modules.system.mapper.RoleMenuMapper;
import com.oasystem.modules.system.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    private final RoleMenuMapper roleMenuMapper;

    @Override
    public List<Menu> getMenuTree() {
        List<Menu> allMenus = list(new LambdaQueryWrapper<Menu>()
                .eq(Menu::getStatus, 1)
                .orderByAsc(Menu::getSort));
        return buildTree(allMenus, 0L);
    }

    @Override
    public List<Menu> getAllMenus() {
        return list(new LambdaQueryWrapper<Menu>()
                .orderByAsc(Menu::getSort));
    }

    @Override
    public List<Menu> getMenuTreeByUserId(Long userId) {
        List<Menu> menus = baseMapper.selectMenusByUserId(userId);
        return buildTree(menus, 0L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMenu(Menu menu) {
        // 检查父菜单是否存在
        if (menu.getParentId() != null && menu.getParentId() > 0) {
            Menu parent = getById(menu.getParentId());
            if (parent == null) {
                throw new BusinessException("父菜单不存在");
            }
        } else {
            menu.setParentId(0L);
        }

        menu.setStatus(menu.getStatus() != null ? menu.getStatus() : 1);
        menu.setVisible(menu.getVisible() != null ? menu.getVisible() : 1);
        menu.setKeepAlive(menu.getKeepAlive() != null ? menu.getKeepAlive() : 0);
        
        save(menu);
        log.info("创建菜单成功: {}", menu.getMenuName());
        return menu.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(Menu menu) {
        Menu existMenu = getById(menu.getId());
        if (existMenu == null) {
            throw new BusinessException("菜单不存在");
        }

        // 检查父菜单
        if (menu.getParentId() != null && menu.getParentId() > 0) {
            if (menu.getParentId().equals(menu.getId())) {
                throw new BusinessException("父菜单不能是自己");
            }
            Menu parent = getById(menu.getParentId());
            if (parent == null) {
                throw new BusinessException("父菜单不存在");
            }
        }

        updateById(menu);
        log.info("更新菜单成功: {}", menu.getMenuName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        Menu menu = getById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }

        // 检查是否有子菜单
        Long childCount = count(new LambdaQueryWrapper<Menu>()
                .eq(Menu::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("存在子菜单，无法删除");
        }

        // 删除角色菜单关联
        roleMenuMapper.deleteByMenuId(id);

        // 删除菜单
        removeById(id);
        log.info("删除菜单成功: {}", menu.getMenuName());
    }

    /**
     * 构建菜单树
     */
    private List<Menu> buildTree(List<Menu> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> parentId.equals(menu.getParentId()))
                .map(menu -> {
                    menu.setChildren(buildTree(menus, menu.getId()));
                    return menu;
                })
                .collect(Collectors.toList());
    }
}
