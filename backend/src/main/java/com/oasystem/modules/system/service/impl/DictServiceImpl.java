package com.oasystem.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oasystem.common.exception.BusinessException;
import com.oasystem.modules.system.entity.DictData;
import com.oasystem.modules.system.entity.DictType;
import com.oasystem.modules.system.mapper.DictDataMapper;
import com.oasystem.modules.system.mapper.DictTypeMapper;
import com.oasystem.modules.system.service.DictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 字典服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final DictTypeMapper dictTypeMapper;
    private final DictDataMapper dictDataMapper;

    // ============ 字典类型 ============

    @Override
    public IPage<DictType> pageDictTypes(int current, int size, String dictName, String dictType, Integer status) {
        Page<DictType> page = new Page<>(current, size);
        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(dictName)) {
            wrapper.like(DictType::getDictName, dictName);
        }
        if (StringUtils.hasText(dictType)) {
            wrapper.like(DictType::getDictType, dictType);
        }
        if (status != null) {
            wrapper.eq(DictType::getStatus, status);
        }
        
        wrapper.orderByDesc(DictType::getCreateTime);
        return dictTypeMapper.selectPage(page, wrapper);
    }

    @Override
    public List<DictType> getAllDictTypes() {
        return dictTypeMapper.selectList(new LambdaQueryWrapper<DictType>()
                .eq(DictType::getStatus, 1)
                .orderByAsc(DictType::getDictType));
    }

    @Override
    public DictType getDictTypeById(Long id) {
        return dictTypeMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDictType(DictType dictType) {
        // 检查类型编码是否存在
        DictType exist = dictTypeMapper.selectOne(new LambdaQueryWrapper<DictType>()
                .eq(DictType::getDictType, dictType.getDictType()));
        if (exist != null) {
            throw new BusinessException("字典类型编码已存在");
        }

        dictType.setStatus(dictType.getStatus() != null ? dictType.getStatus() : 1);
        dictTypeMapper.insert(dictType);
        
        log.info("创建字典类型成功: {}", dictType.getDictName());
        return dictType.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDictType(DictType dictType) {
        DictType exist = dictTypeMapper.selectById(dictType.getId());
        if (exist == null) {
            throw new BusinessException("字典类型不存在");
        }

        // 检查类型编码是否被其他使用
        DictType codeExist = dictTypeMapper.selectOne(new LambdaQueryWrapper<DictType>()
                .eq(DictType::getDictType, dictType.getDictType())
                .ne(DictType::getId, dictType.getId()));
        if (codeExist != null) {
            throw new BusinessException("字典类型编码已存在");
        }

        // 如果修改了类型编码，同步更新字典数据
        if (!exist.getDictType().equals(dictType.getDictType())) {
            DictData updateData = new DictData();
            updateData.setDictType(dictType.getDictType());
            dictDataMapper.update(updateData, new LambdaQueryWrapper<DictData>()
                    .eq(DictData::getDictType, exist.getDictType()));
        }

        dictTypeMapper.updateById(dictType);
        log.info("更新字典类型成功: {}", dictType.getDictName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictType(Long id) {
        DictType dictType = dictTypeMapper.selectById(id);
        if (dictType == null) {
            throw new BusinessException("字典类型不存在");
        }

        // 删除关联的字典数据
        dictDataMapper.delete(new LambdaQueryWrapper<DictData>()
                .eq(DictData::getDictType, dictType.getDictType()));

        // 删除字典类型
        dictTypeMapper.deleteById(id);
        
        log.info("删除字典类型成功: {}", dictType.getDictName());
    }

    // ============ 字典数据 ============

    @Override
    public List<DictData> getDictDataByType(String dictType) {
        return dictDataMapper.selectList(new LambdaQueryWrapper<DictData>()
                .eq(DictData::getDictType, dictType)
                .eq(DictData::getStatus, 1)
                .orderByAsc(DictData::getSort));
    }

    @Override
    public IPage<DictData> pageDictData(int current, int size, String dictType, String dictLabel, Integer status) {
        Page<DictData> page = new Page<>(current, size);
        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(dictType)) {
            wrapper.eq(DictData::getDictType, dictType);
        }
        if (StringUtils.hasText(dictLabel)) {
            wrapper.like(DictData::getDictLabel, dictLabel);
        }
        if (status != null) {
            wrapper.eq(DictData::getStatus, status);
        }
        
        wrapper.orderByAsc(DictData::getSort);
        return dictDataMapper.selectPage(page, wrapper);
    }

    @Override
    public DictData getDictDataById(Long id) {
        return dictDataMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDictData(DictData dictData) {
        // 检查字典类型是否存在
        DictType dictType = dictTypeMapper.selectOne(new LambdaQueryWrapper<DictType>()
                .eq(DictType::getDictType, dictData.getDictType()));
        if (dictType == null) {
            throw new BusinessException("字典类型不存在");
        }

        dictData.setStatus(dictData.getStatus() != null ? dictData.getStatus() : 1);
        dictData.setIsDefault(dictData.getIsDefault() != null ? dictData.getIsDefault() : 0);
        dictDataMapper.insert(dictData);
        
        log.info("创建字典数据成功: {} - {}", dictData.getDictType(), dictData.getDictLabel());
        return dictData.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDictData(DictData dictData) {
        DictData exist = dictDataMapper.selectById(dictData.getId());
        if (exist == null) {
            throw new BusinessException("字典数据不存在");
        }

        dictDataMapper.updateById(dictData);
        log.info("更新字典数据成功: {} - {}", dictData.getDictType(), dictData.getDictLabel());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictData(Long id) {
        DictData dictData = dictDataMapper.selectById(id);
        if (dictData == null) {
            throw new BusinessException("字典数据不存在");
        }

        dictDataMapper.deleteById(id);
        log.info("删除字典数据成功: {} - {}", dictData.getDictType(), dictData.getDictLabel());
    }
}
