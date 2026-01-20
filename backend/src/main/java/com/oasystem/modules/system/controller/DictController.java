package com.oasystem.modules.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oasystem.common.PageResult;
import com.oasystem.common.Result;
import com.oasystem.modules.system.entity.DictData;
import com.oasystem.modules.system.entity.DictType;
import com.oasystem.modules.system.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理控制器
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("/system/dicts")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    // ============ 字典类型 ============

    @Operation(summary = "分页查询字典类型")
    @GetMapping("/types")
    @PreAuthorize("hasRole('super_admin')")
    public PageResult<DictType> pageDictTypes(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "字典名称") @RequestParam(required = false) String dictName,
            @Parameter(description = "字典类型") @RequestParam(required = false) String dictType,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        IPage<DictType> page = dictService.pageDictTypes(current, size, dictName, dictType, status);
        return PageResult.of(page);
    }

    @Operation(summary = "获取所有字典类型")
    @GetMapping("/types/all")
    public Result<List<DictType>> getAllDictTypes() {
        List<DictType> types = dictService.getAllDictTypes();
        return Result.success(types);
    }

    @Operation(summary = "获取字典类型详情")
    @GetMapping("/types/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<DictType> getDictTypeById(@PathVariable Long id) {
        DictType dictType = dictService.getDictTypeById(id);
        return Result.success(dictType);
    }

    @Operation(summary = "创建字典类型")
    @PostMapping("/types")
    @PreAuthorize("hasRole('super_admin')")
    public Result<Long> createDictType(@Valid @RequestBody DictType dictType) {
        Long id = dictService.createDictType(dictType);
        return Result.success(id);
    }

    @Operation(summary = "更新字典类型")
    @PutMapping("/types/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<Void> updateDictType(@PathVariable Long id, @Valid @RequestBody DictType dictType) {
        dictType.setId(id);
        dictService.updateDictType(dictType);
        return Result.success();
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/types/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<Void> deleteDictType(@PathVariable Long id) {
        dictService.deleteDictType(id);
        return Result.success();
    }

    // ============ 字典数据 ============

    @Operation(summary = "根据字典类型获取字典数据")
    @GetMapping("/data/{dictType}")
    public Result<List<DictData>> getDictDataByType(@PathVariable String dictType) {
        List<DictData> dataList = dictService.getDictDataByType(dictType);
        return Result.success(dataList);
    }

    @Operation(summary = "分页查询字典数据")
    @GetMapping("/data")
    @PreAuthorize("hasRole('super_admin')")
    public PageResult<DictData> pageDictData(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "字典类型") @RequestParam(required = false) String dictType,
            @Parameter(description = "字典标签") @RequestParam(required = false) String dictLabel,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        IPage<DictData> page = dictService.pageDictData(current, size, dictType, dictLabel, status);
        return PageResult.of(page);
    }

    @Operation(summary = "获取字典数据详情")
    @GetMapping("/data/detail/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<DictData> getDictDataById(@PathVariable Long id) {
        DictData dictData = dictService.getDictDataById(id);
        return Result.success(dictData);
    }

    @Operation(summary = "创建字典数据")
    @PostMapping("/data")
    @PreAuthorize("hasRole('super_admin')")
    public Result<Long> createDictData(@Valid @RequestBody DictData dictData) {
        Long id = dictService.createDictData(dictData);
        return Result.success(id);
    }

    @Operation(summary = "更新字典数据")
    @PutMapping("/data/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<Void> updateDictData(@PathVariable Long id, @Valid @RequestBody DictData dictData) {
        dictData.setId(id);
        dictService.updateDictData(dictData);
        return Result.success();
    }

    @Operation(summary = "删除字典数据")
    @DeleteMapping("/data/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<Void> deleteDictData(@PathVariable Long id) {
        dictService.deleteDictData(id);
        return Result.success();
    }
}
