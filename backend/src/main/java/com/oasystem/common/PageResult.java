package com.oasystem.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 */
@Data
@Schema(description = "分页响应结果")
public class PageResult<T> implements Serializable {

    @Schema(description = "状态码")
    private Integer code;

    @Schema(description = "消息")
    private String message;

    @Schema(description = "当前页")
    private Long current;

    @Schema(description = "每页大小")
    private Long size;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "总页数")
    private Long pages;

    @Schema(description = "数据列表")
    private List<T> records;

    @Schema(description = "时间戳")
    private Long timestamp;

    public PageResult() {
        this.code = 200;
        this.message = "操作成功";
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setRecords(page.getRecords());
        return result;
    }

    public static <T> PageResult<T> of(long current, long size, long total, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setCurrent(current);
        result.setSize(size);
        result.setTotal(total);
        result.setPages((total + size - 1) / size);
        result.setRecords(records);
        return result;
    }
}
