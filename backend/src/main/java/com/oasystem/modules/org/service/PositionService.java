package com.oasystem.modules.org.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oasystem.modules.org.entity.Position;

import java.util.List;

/**
 * 岗位服务接口
 */
public interface PositionService extends IService<Position> {

    /**
     * 分页查询岗位
     */
    IPage<Position> pagePositions(int current, int size, String positionName, String positionCode, Integer status);

    /**
     * 获取所有启用的岗位
     */
    List<Position> getAllEnabled();

    /**
     * 创建岗位
     */
    Long createPosition(Position position);

    /**
     * 更新岗位
     */
    void updatePosition(Position position);

    /**
     * 删除岗位
     */
    void deletePosition(Long id);
}
