package com.oasystem.modules.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oasystem.modules.system.entity.DictData;
import com.oasystem.modules.system.entity.DictType;

import java.util.List;

/**
 * 字典服务接口
 */
public interface DictService {

    // ============ 字典类型 ============

    /**
     * 分页查询字典类型
     */
    IPage<DictType> pageDictTypes(int current, int size, String dictName, String dictType, Integer status);

    /**
     * 获取所有字典类型
     */
    List<DictType> getAllDictTypes();

    /**
     * 获取字典类型详情
     */
    DictType getDictTypeById(Long id);

    /**
     * 创建字典类型
     */
    Long createDictType(DictType dictType);

    /**
     * 更新字典类型
     */
    void updateDictType(DictType dictType);

    /**
     * 删除字典类型
     */
    void deleteDictType(Long id);

    // ============ 字典数据 ============

    /**
     * 根据字典类型获取字典数据
     */
    List<DictData> getDictDataByType(String dictType);

    /**
     * 分页查询字典数据
     */
    IPage<DictData> pageDictData(int current, int size, String dictType, String dictLabel, Integer status);

    /**
     * 获取字典数据详情
     */
    DictData getDictDataById(Long id);

    /**
     * 创建字典数据
     */
    Long createDictData(DictData dictData);

    /**
     * 更新字典数据
     */
    void updateDictData(DictData dictData);

    /**
     * 删除字典数据
     */
    void deleteDictData(Long id);
}
