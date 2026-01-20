package com.oasystem.modules.org.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oasystem.common.PageResult;
import com.oasystem.common.Result;
import com.oasystem.modules.org.entity.Position;
import com.oasystem.modules.org.service.PositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 岗位管理控制器
 */
@Tag(name = "岗位管理")
@RestController
@RequestMapping("/org/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @Operation(summary = "分页查询岗位列表")
    @GetMapping
    public PageResult<Position> list(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "岗位名称") @RequestParam(required = false) String positionName,
            @Parameter(description = "岗位编码") @RequestParam(required = false) String positionCode,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        IPage<Position> page = positionService.pagePositions(current, size, positionName, positionCode, status);
        return PageResult.of(page);
    }

    @Operation(summary = "获取所有启用的岗位")
    @GetMapping("/all")
    public Result<List<Position>> getAllEnabled() {
        List<Position> positions = positionService.getAllEnabled();
        return Result.success(positions);
    }

    @Operation(summary = "获取岗位详情")
    @GetMapping("/{id}")
    public Result<Position> getById(@PathVariable Long id) {
        Position position = positionService.getById(id);
        return Result.success(position);
    }

    @Operation(summary = "创建岗位")
    @PostMapping
    @PreAuthorize("hasAnyRole('super_admin', 'hr_admin')")
    public Result<Long> create(@Valid @RequestBody Position position) {
        Long positionId = positionService.createPosition(position);
        return Result.success(positionId);
    }

    @Operation(summary = "更新岗位")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('super_admin', 'hr_admin')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Position position) {
        position.setId(id);
        positionService.updatePosition(position);
        return Result.success();
    }

    @Operation(summary = "删除岗位")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('super_admin', 'hr_admin')")
    public Result<Void> delete(@PathVariable Long id) {
        positionService.deletePosition(id);
        return Result.success();
    }
}
