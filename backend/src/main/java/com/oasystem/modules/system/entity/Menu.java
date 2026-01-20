package com.oasystem.modules.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单实体
 */
@Data
@TableName("sys_menu")
@Schema(description = "菜单")
public class Menu implements Serializable {

    @Schema(description = "ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "父菜单ID")
    private Long parentId;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "菜单类型：M-目录 C-菜单 F-按钮")
    private String menuType;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "权限标识")
    private String permission;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Schema(description = "是否可见：0-隐藏 1-显示")
    private Integer visible;

    @Schema(description = "是否缓存：0-不缓存 1-缓存")
    private Integer keepAlive;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "是否删除：0-否 1-是")
    @TableLogic
    private Integer deleted;

    @Schema(description = "子菜单")
    @TableField(exist = false)
    private List<Menu> children;
}
