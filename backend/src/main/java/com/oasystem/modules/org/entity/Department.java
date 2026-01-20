package com.oasystem.modules.org.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门实体
 */
@Data
@TableName("org_department")
@Schema(description = "部门")
public class Department implements Serializable {

    @Schema(description = "ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "父部门ID")
    private Long parentId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "部门编码")
    private String deptCode;

    @Schema(description = "部门负责人ID")
    private Long leaderId;

    @Schema(description = "部门负责人姓名")
    @TableField(exist = false)
    private String leaderName;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "是否删除：0-否 1-是")
    @TableLogic
    private Integer deleted;

    @Schema(description = "子部门")
    @TableField(exist = false)
    private List<Department> children;
}
