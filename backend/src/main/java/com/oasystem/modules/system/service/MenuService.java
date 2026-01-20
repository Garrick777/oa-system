package com.oasystem.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oasystem.modules.system.entity.Menu;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface MenuService extends IService<Menu> {

    /**
     * 获取菜单树
     */
    List<Menu> getMenuTree();

    /**
     * 获取所有菜单列表
     */
    List<Menu> getAllMenus();

    /**
     * 根据用户ID获取菜单树
     */
    List<Menu> getMenuTreeByUserId(Long userId);

    /**
     * 创建菜单
     */
    Long createMenu(Menu menu);

    /**
     * 更新菜单
     */
    void updateMenu(Menu menu);

    /**
     * 删除菜单
     */
    void deleteMenu(Long id);
}
