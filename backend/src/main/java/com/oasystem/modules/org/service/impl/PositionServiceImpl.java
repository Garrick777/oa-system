package com.oasystem.modules.org.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oasystem.common.exception.BusinessException;
import com.oasystem.modules.org.entity.Position;
import com.oasystem.modules.org.mapper.PositionMapper;
import com.oasystem.modules.org.service.PositionService;
import com.oasystem.modules.system.entity.User;
import com.oasystem.modules.system.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 岗位服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PositionServiceImpl extends ServiceImpl<PositionMapper, Position> implements PositionService {

    private final UserMapper userMapper;

    @Override
    public IPage<Position> pagePositions(int current, int size, String positionName, String positionCode, Integer status) {
        Page<Position> page = new Page<>(current, size);
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(positionName)) {
            wrapper.like(Position::getPositionName, positionName);
        }
        if (StringUtils.hasText(positionCode)) {
            wrapper.like(Position::getPositionCode, positionCode);
        }
        if (status != null) {
            wrapper.eq(Position::getStatus, status);
        }
        
        wrapper.orderByAsc(Position::getSort);
        return page(page, wrapper);
    }

    @Override
    public List<Position> getAllEnabled() {
        return list(new LambdaQueryWrapper<Position>()
                .eq(Position::getStatus, 1)
                .orderByAsc(Position::getSort));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPosition(Position position) {
        // 检查岗位编码是否存在
        if (position.getPositionCode() != null) {
            Position exist = getOne(new LambdaQueryWrapper<Position>()
                    .eq(Position::getPositionCode, position.getPositionCode()));
            if (exist != null) {
                throw new BusinessException("岗位编码已存在");
            }
        }

        position.setStatus(position.getStatus() != null ? position.getStatus() : 1);
        save(position);
        
        log.info("创建岗位成功: {}", position.getPositionName());
        return position.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePosition(Position position) {
        Position exist = getById(position.getId());
        if (exist == null) {
            throw new BusinessException("岗位不存在");
        }

        // 检查岗位编码
        if (position.getPositionCode() != null) {
            Position codeExist = getOne(new LambdaQueryWrapper<Position>()
                    .eq(Position::getPositionCode, position.getPositionCode())
                    .ne(Position::getId, position.getId()));
            if (codeExist != null) {
                throw new BusinessException("岗位编码已存在");
            }
        }

        updateById(position);
        log.info("更新岗位成功: {}", position.getPositionName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePosition(Long id) {
        Position position = getById(id);
        if (position == null) {
            throw new BusinessException("岗位不存在");
        }

        // 检查是否有员工使用该岗位
        Long userCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getPositionId, id));
        if (userCount > 0) {
            throw new BusinessException("该岗位下存在员工，无法删除");
        }

        removeById(id);
        log.info("删除岗位成功: {}", position.getPositionName());
    }
}
